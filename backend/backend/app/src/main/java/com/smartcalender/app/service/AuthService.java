package com.smartcalender.app.service;

import com.smartcalender.app.dto.LoginRequest;
import com.smartcalender.app.dto.LoginResponseDTO;
import com.smartcalender.app.dto.RegisterRequest;
import com.smartcalender.app.dto.UserDTO;
import com.smartcalender.app.entity.PasswordResetToken;
import com.smartcalender.app.entity.RefreshToken;
import com.smartcalender.app.entity.User;
import com.smartcalender.app.exception.*;
import com.smartcalender.app.repository.PasswordResetTokenRepository;
import com.smartcalender.app.repository.RefreshTokenRepository;
import com.smartcalender.app.repository.UserRepository;
import com.smartcalender.app.config.JwtUtil;
import jakarta.transaction.Transactional;
import jakarta.validation.ValidationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.UUID;

@Service
public class AuthService {

    @Value("${email-verification.required}")
    private boolean emailVerificationRequired;

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final OtpService otpService;
    private final EmailService emailService;


    public AuthService(UserRepository userRepository, RefreshTokenRepository refreshTokenRepository, PasswordResetTokenRepository passwordResetTokenRepository,
                       AuthenticationManager authenticationManager, JwtUtil jwtUtil, OtpService otpService, EmailService emailService) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.otpService = otpService;
        this.emailService = emailService;
    }

    @Transactional
    public LoginResponseDTO authenticateUser(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new NotFoundException("Användaren kunde inte hittas."));

        if (emailVerificationRequired && !user.isEmailVerified()) {
            throw new EmailNotVerifiedException("E-postadressen är inte verifierad.");
        }

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities("USER")
                .build();

        String accessToken = jwtUtil.generateToken(userDetails);

        RefreshToken refreshToken = refreshTokenRepository.findByUserAndExpiresAtAfter(user, Instant.now())
                .orElseGet(() -> {
                    RefreshToken newToken = new RefreshToken();
                    newToken.setUser(user);
                    newToken.setExpiresAt(Instant.now().plusSeconds(60 * 60 * 24 * 7)); // 7 days expiration
                    refreshTokenRepository.save(newToken);
                    return newToken;
                });

        return new LoginResponseDTO(accessToken, refreshToken.getId());
    }

    @Transactional
    public void forgotPassword(String email) {
        User user = userRepository.findByEmailAddress(email)
                .orElseThrow(() -> new NotFoundException("Användaren kunde inte hittas."));

        // Generate a unique token
        String token = UUID.randomUUID().toString();

        // Create and save the password reset token
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setExpiryDate(Instant.now().plusSeconds(3600)); // 1 hour expiration
        passwordResetTokenRepository.save(resetToken);

        // Create the reset URL
        String resetUrl = "https://smartcalendar.se/reset-password?token=" + token;

        // Send the email
        emailService.sendPasswordResetEmail(user.getEmailAddress(), "Återställ ditt lösenord | SmartCalendar", resetUrl);
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        // Find the reset token
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new ValidationException("Ogiltig eller utgånge återställningstoken"));

        // Check if the token has expired
        if (resetToken.getExpiryDate().isBefore(Instant.now())) {
            passwordResetTokenRepository.delete(resetToken);
            throw new ValidationException("Token för lösenordsåterstlääning har gått ut");
        }

        // Get the user and update the password
        User user = resetToken.getUser();
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Delete the reset token
        passwordResetTokenRepository.delete(resetToken);
    }

    @Transactional
    public void changePassword(String currentPassword, String newPassword, UserDetails currentUser) {
        User user = userRepository.findByUsername(currentUser.getUsername())
                .orElseThrow(() -> new NotFoundException("Användaren kunde inte hittas."));

        if (currentPassword == null || currentPassword.trim().isEmpty()) {
            throw new InvalidInputException("Nuvarande lösenord kan inte vara tomt");
        }
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new InvalidInputException("Nytt lösenord kan inte vara tomt");
        }

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new PermissionDeniedException("Felaktigt nuvarande lösenord");
        }

        if (newPassword.length() < 8 || !newPassword.matches(".*[A-Z].*") || !newPassword.matches(".*[0-9].*")) {
            throw new InvalidInputException("Nytt lösenord måste vara minst 8 tecken långt, innehålla minst en versal och en siffra.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public UserDTO registerUser(RegisterRequest request) {
        userRepository.findByUsername(request.getUsername())
                .ifPresent(user -> {throw new AlreadyExistsException("Användarnamnet är upptaget.");});

        userRepository.findByEmailAddress(request.getEmailAddress())
                .ifPresent(user -> {throw new AlreadyExistsException("E-postadressen är redan registrerad.");
                });

        if (request.getPassword() == null || request.getPassword().length() < 8 || !request.getPassword().matches(".*[A-Z].*") || !request.getPassword().matches(".*[0-9].*")) {
            throw new InvalidInputException("Lösenordet måste vara minst 8 tecken långt, innehålla minst en versal och en siffra.");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmailAddress());
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        User savedUser = userRepository.save(user);

        if (emailVerificationRequired) {
            String otp = otpService.generateAndStoreOtp(savedUser.getId());
            String verificationUrl = "https://smartcalendar.se/verify-email?uid=" + savedUser.getId() + "&otp=" + otp;
            emailService.sendVerificationEmail(savedUser.getEmailAddress(), "Verifiera din e-postadress | SmartCalendar", verificationUrl, otp);
        }

        return new UserDTO(savedUser.getId(), savedUser.getUsername(), savedUser.getEmailAddress());
    }

    @Transactional
    public String verifyEmail(Long userId, String otp) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Användaren kunde inte hittas."));

        if (user.isEmailVerified()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "E-postadressen är redan verifierad.");
        }

        if (!otpService.isOtpValid(userId, otp)) {
            throw new ValidationException("Ogiltigt otp token.");
        }

        user.setEmailVerified(true);
        userRepository.save(user);
        otpService.deleteOtp(userId);
        return user.getEmailAddress();
    }

    public void changeEmail(String newEmail, String password, UserDetails currentUser) {
        User user = userRepository.findByUsername(currentUser.getUsername())
                .orElseThrow(() -> new NotFoundException("Användaren kunde inte hittas."));

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new PermissionDeniedException("Felaktigt lösenord");
        }

        if (userRepository.existsByEmailAddress(newEmail)) {
            throw new AlreadyExistsException("E-postadressen är redan registrerad.");
        }

        user.setEmail(newEmail);
        user.setEmailVerified(false);
        userRepository.save(user);

        String otp = otpService.generateAndStoreOtp(user.getId());
        String verificationUrl = "https://smartcalendar.se/verify-email?uid=" + user.getId() + "&otp=" + otp;
        emailService.sendVerificationEmail(newEmail, "Verifiera din e-postadress | SmartCalendar", verificationUrl, otp);
    }

    public void deleteAccount(String password, UserDetails currentUser) {
        User user = userRepository.findByUsername(currentUser.getUsername())
                .orElseThrow(() -> new NotFoundException("Användaren kunde inte hittas."));

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new PermissionDeniedException("Felaktigt lösenord");
        }

        userRepository.delete(user);
    }

    @Transactional
    public void resendVerification(String emailAddress) {
        User user = userRepository.findByEmailAddress(emailAddress)
                .orElseThrow(() -> new NotFoundException("Användaren kunde inte hittas."));

        if (!user.isEmailVerified()) {
            String otp = otpService.generateAndStoreOtp(user.getId());
            String verificationUrl = "https://smartcalendar.se/verify-email?uid=" + user.getId() + "&otp=" + otp;
            emailService.sendVerificationEmail(user.getEmailAddress(), "Verifiera din e-postadress | SmartCalendar", verificationUrl, otp);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "E-postadressen är redan verifierad.");
        }
    }

    public LoginResponseDTO refreshToken(UUID refreshToken) {
        final var refreshTokenEntity = refreshTokenRepository
                .findByIdAndExpiresAtAfter(refreshToken, Instant.now())
                .orElseThrow(() -> new ValidationException("Ogiltig eller utgången uppdateringstoken"));

        User user = refreshTokenEntity.getUser();

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities("USER")
                .build();

        final var newAccessToken = jwtUtil.generateToken(userDetails);

        return new LoginResponseDTO(newAccessToken, refreshToken);
    }

    public void revokeRefreshToken(UUID refreshToken) {
        refreshTokenRepository.deleteById(refreshToken);
    }

    @Scheduled(fixedRate = 86400000) // Every day
    public void deleteExpiredRefreshToken() {
        Instant now = Instant.now();
        refreshTokenRepository.deleteByExpirationBefore(now);
    }
}
