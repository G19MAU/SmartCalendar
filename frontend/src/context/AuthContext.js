import {createContext, useContext, useState} from "react";
import {Navigate, Outlet, useNavigate} from "react-router-dom";

const AuthContext = createContext();

const AuthProvider = ({ children }) => {
    const [accessToken, setAccToken] = useState(localStorage.getItem("accessToken") || "");
    const [refreshToken, setRefToken] = useState(localStorage.getItem("refreshToken") || "");
    const navigate = useNavigate();

    const loginAction = async (data) => {
        try {
            const response = await fetch(`${process.env.REACT_APP_BACKEND_URL}auth/login`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                },
                body: JSON.stringify(data),
            });

            if (!response.ok) {
                const errorData = await response.json();
                const error = new Error(errorData.message || "Ett fel inträffade.");
                error.responseData = errorData;
                throw error;
            }

            const res = await response.json();
            setAccToken(res.accessToken);
            setRefToken(res.refreshToken);
            localStorage.setItem("accessToken", res.accessToken);
            localStorage.setItem("refreshToken", res.refreshToken);
            navigate("/today");
        } catch (error) {
            throw error; // Re-throw the error to be handled by the caller
        }
    };

    const logoutAction = () => {
        setAccToken("");
        localStorage.removeItem("accessToken");
        localStorage.removeItem("refreshToken");
        navigate("/login");
    };

    return (
        <AuthContext.Provider value={{ accessToken, refreshToken, loginAction, logoutAction }}>
            {children}
        </AuthContext.Provider>
    );
};

export default AuthProvider;

export const useAuth = () => {
    return useContext(AuthContext);
};

export const PrivateRoute = () => {
    const { accessToken } = useAuth();
    return accessToken ? <Outlet /> : <Navigate to="/login" replace />;
};