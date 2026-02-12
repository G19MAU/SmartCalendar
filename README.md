# SmartCalendar

SmartCalendar är en kalenderapplikation utformad för att hjälpa användare att hantera sina händelser och scheman på ett effektivt sätt.

See english description below.

## 🚀 Live Deployment

- **Frontend App**: [https://www.smartcalendar.se](https://www.smartcalendar.se)
- **Backend API**: [https://www.api.smartcalendar.se](https://www.api.smartcalendar.se)

---

# Swedish

## Ladda ner och köra projektet

Versionen av koden som har lämnats för granskning är taggad som `v3.0-review`. För att ladda ner och använda denna version:

1. Gå till projektets GitHub-sida: https://github.com/G19MAU/SmartCalendar.
2. Klicka på fliken "Releases" eller "Tags" och hitta taggen `v3.0-review`.
3. Klicka på "Download ZIP" under `v3.0-review` för att ladda ner källkoden som en ZIP-fil.
4. Extrahera ZIP-filen till en mapp på din dator.
5. Öppna projektet i din IDE:
   - **IntelliJ IDEA**: Välj "Open" och navigera till den extraherade mappen.
   - **VS Code**: Välj "Open Folder" och välj den extraherade mappen.

## Starta applikationen

### Backend

Backend är en Spring Boot-applikation byggd med Maven och startas via klassen `AppApplication`. För att köra den krävs:

- Java 21 (eller den version som anges i `pom.xml`).
- Maven (installerat på din dator eller via din IDE:s inbyggda stöd).

#### Steg i IntelliJ IDEA

1. Öppna projektet i IntelliJ IDEA genom att välja den extraherade mappen.
2. Vänta tills IntelliJ har indexerat projektet och laddat ner Maven-beroenden (detta sker automatiskt om Maven är aktiverat).
3. Hitta filen `AppApplication.java` i `backend/backend/app/src/main/java/com.smartcalender.app/AppApplication.java`.
4. Högerklicka på `AppApplication.java` och välj "Run 'AppApplication.main()'".
5. Backend startar och körs på port 8080 ifall man vill testa detta på webbläsaren eller i exempelvis programmet Postman.

#### Steg i VS Code

1. Öppna projektet i VS Code genom att välja den extraherade mappen.

2. Installera rekommenderade tillägg som "Java Extension Pack" och "Spring Boot Extension Pack" om du inte redan har dem.

3. Öppna en terminal i VS Code (Terminal &gt; New Terminal).

4. Navigera till rotmappen (om du inte redan är där) och kör:

   ```
   mvn clean install
   mvn spring-boot:run
   ```

5. Backend startar och körs på port 8080.

### Frontend

Frontend är byggd med Create React App, en verktygslåda för React-applikationer som använder Node.js och npm. För att köra den krävs:

- Node.js (rekommenderas version 16 eller senare) och npm (ingår med Node.js). Ladda ner från nodejs.org om det inte är installerat.

#### Steg i IntelliJ IDEA

1. Öppna en terminal i IntelliJ (View &gt; Tool Windows &gt; Terminal).

2. Navigera till frontend-mappen:

   ```
   cd frontend
   ```

3. Installera beroenden:

   ```
   npm install
   ```

4. Starta frontend:

   ```
   npm start
   ```

5. Frontend startar på port 3000 och öppnas automatiskt i din webbläsare på `http://localhost:3000`.

#### Steg i VS Code

1. Öppna projektet i VS Code och navigera till frontend-mappen i filutforskaren.

2. Öppna en terminal (Terminal &gt; New Terminal).

3. Kontrollera att du är i frontend-mappen (annars: `cd frontend`).

4. Installera beroenden:

   ```
   npm install
   ```

5. Starta frontend:

   ```
   npm start
   ```

6. Frontend startar på port 3000 och öppnas automatiskt i din webbläsare på `http://localhost:3000`.

## Miljövariabler och Konfiguration

### Backend - .env File Configuration

Backend använder en `.env` fil för att hantera miljövariabler. Filen ligger i `backend/backend/app/.env` och laddas automatiskt vid start.

**Skapa `.env` filen:**

```bash
# Navigera till backend-mappen
cd backend/backend/app

# Skapa .env från exempel-filen (om du inte redan har den)
cp .env.example .env

# Redigera .env med dina faktiska värden
nano .env  # eller använd din favorit editor
```

**Nödvändiga variabler i `.env`:**

```bash
# Databas Konfigurering (PostgreSQL)
DB_HOST=localhost                    # Eller din databasserver
DB_PORT=din databas_port (vanligtvis 5432)
DB_NAME=smartcalendar
DB_USER=din_databas_användare
DB_PASSWORD=ditt_databas_lösenord

# Email Service (Brevo API för e-postverifiering)
EMAIL_API_KEY=din_brevo_api_nyckel

# JWT Konfigurering (Krav för autentisering)
JWT_SECRET=din_jwt_secret_nyckel     # Generera med: openssl rand -base64 32
JWT_EXPIRATION=86400000              # 24 timmar i millisekunder
```

**Generera en säker JWT-nyckel:**

```bash
openssl rand -base64 32
```

**Viktigt:**
- ✅ `.env` filen är i `.gitignore` och commitas **ALDRIG** till Git
- ✅ Använd `.env.example` som mall
- ✅ JWT_SECRET måste vara minst 256 bitar (32+ tecken i base64)
- ✅ Alla variabler måste sättas för att backend ska fungera

### Frontend - React Environment Variables

Frontend använder env-filer för att veta vilken backend som ska anropas:

- **`.env.production`** finns i repot och innehåller `REACT_APP_BACKEND_URL` för produktionsservern
- **`.env.local`** (gitignored) behöver du skapa själv för lokal utveckling:

  ```bash
  # frontend/.env.local
  REACT_APP_BACKEND_URL=http://localhost:8080/api/
  ```

**Skapa frontend .env.local:**

```bash
cd frontend
echo "REACT_APP_BACKEND_URL=http://localhost:8080/api/" > .env.local
```


## Viktig information för testare och granskare

### Förberedelser innan start:

1. **Java 21**: Projektet kräver Java 21
   ```bash
   # Kontrollera Java-version
   java -version
   
   # Installera Java 21:
   # Via SDKMAN: sdk install java 21.0.2-open
   # Via Homebrew: brew install openjdk@21
   ```

2. **Backend .env fil**: Skapa och konfigurera `backend/backend/app/.env` med alla nödvändiga variabler (se avsnittet *Miljövariabler och Konfiguration* ovan)

3. **Frontend .env.local**: Skapa `frontend/.env.local` för att peka på lokal backend

4. **PostgreSQL databas**: Säkerställ att du har tillgång till en PostgreSQL-databas med rätt credentials

### Snabbstart:

```bash
# 1. Skapa backend .env
cd backend/backend/app
cp .env.example .env
# Redigera .env med dina värden

# 2. Skapa frontend .env.local
cd ../../../frontend
echo "REACT_APP_BACKEND_URL=http://localhost:8080/api/" > .env.local

# 3. Starta backend (i en terminal)
cd ../backend/backend/app
./mvnw spring-boot:run

# 4. Starta frontend (i en annan terminal)
cd ../../../frontend
npm install
npm start
```

## Ytterligare information

- **Java 21 Required**: Projektet kräver Java 21. Om du har en tidigare version installerat kommer backend inte att starta.
- **Backend**: Applikationen använder en extern PostgreSQL-databas, vilket kan orsaka problem för externa aktörer som försöker använda funktioner kopplade till datan i databasen.
- **Frontend**: I dagsläget är funktionaliteten begränsad, men applikationen startar utan problem och visar en grundläggande vy.
- **Portar**: Se till att portarna 8080 (backend) och 3000 (frontend) är lediga på din dator.
- **IDE-inställningar**: Om du stöter på problem, kontrollera att din IDE har Java 21 och rätt Node.js-version konfigurerad i inställningarna.
- **.env Säkerhet**: Committa aldrig `.env` filen till Git. Den innehåller känslig information som databas-lösenord och API-nycklar.

## 📋 CI/CD Pipeline

Projektet använder automatiserad CI/CD med GitHub Actions:
- ✅ Automatisk testning på varje commit
- ✅ Automatisk deployment till Render på main-branch
- ✅ Snabb deployment (~5-7 minuter från commit till live)

---
# ENGLISH

# SmartCalendar

SmartCalendar is a calendar application designed to help users manage their events and schedules efficiently.

## Downloading and running the project

The version of the code submitted for review is tagged `v3.0-review`. To download and use this version:

1. Visit the project's GitHub page: https://github.com/slidecart/G19SmartCalender.
2. Click the "Releases" or "Tags" tab and locate the tag `v3.0-review`.
3. Click "Download ZIP" under `v3.0-review` to download the source code as a ZIP file.
4. Extract the ZIP file to a folder on your computer.
5. Open the project in your IDE:
   - **IntelliJ IDEA**: Choose "Open" and navigate to the extracted folder.
   - **VS Code**: Choose "Open Folder" and select the extracted folder.

## Starting the application

### Backend

The backend is a Spring Boot application built with Maven and started via the class `AppApplication`. To run it you need:

- **Java 21** 
- Maven (installed on your computer or via your IDE's built-in support)

#### Steps in IntelliJ IDEA

1. Open the project in IntelliJ IDEA by selecting the extracted folder.
2. Wait for IntelliJ to index the project and download Maven dependencies (this happens automatically if Maven is enabled).
3. Locate the file `AppApplication.java` in `backend/backend/app/src/main/java/com.smartcalender.app/AppApplication.java`.
4. Right-click `AppApplication.java` and choose "Run 'AppApplication.main()'".
5. The backend starts and runs on port 8080 if you want to test it in your browser or for example in Postman.

#### Steps in VS Code

1. Open the project in VS Code by selecting the extracted folder.
2. Install recommended extensions such as "Java Extension Pack" and "Spring Boot Extension Pack" if you don't already have them.
3. Open a terminal in VS Code (Terminal > New Terminal).
4. Navigate to the root folder (if you are not already there) and run:

   ```
   mvn clean install
   mvn spring-boot:run
   ```

5. The backend starts and runs on port 8080.

### Frontend

The frontend is built with Create React App, a toolkit for React applications that uses Node.js and npm. To run it you need:

- Node.js (version 16 or later is recommended) and npm (included with Node.js). Download from nodejs.org if it is not installed.

#### Steps in IntelliJ IDEA

1. Open a terminal in IntelliJ (View > Tool Windows > Terminal).
2. Navigate to the frontend folder:

   ```
   cd frontend
   ```

3. Install dependencies:

   ```
   npm install
   ```

4. Start the frontend:

   ```
   npm start
   ```

5. The frontend starts on port 3000 and automatically opens in your browser at `http://localhost:3000`.

#### Steps in VS Code

1. Open the project in VS Code and navigate to the frontend folder in the file explorer.
2. Open a terminal (Terminal > New Terminal).
3. Ensure you are in the frontend folder (otherwise: `cd frontend`).
4. Install dependencies:

   ```
   npm install
   ```

5. Start the frontend:

   ```
   npm start
   ```

6. The frontend starts on port 3000 and opens automatically in your browser at `http://localhost:3000`.

## Environment Variables and Configuration

### Backend - .env File Configuration

The backend uses a `.env` file to manage environment variables. The file is located in `backend/backend/app/.env` and is loaded automatically at startup.

**Create the `.env` file:**

```bash
# Navigate to backend folder
cd backend/backend/app

# Create .env from example file (if you don't have it already)
cp .env.example .env

# Edit .env with your actual values
nano .env  # or use your favorite editor
```

**Required variables in `.env`:**

```bash
# Database Configuration (PostgreSQL)
DB_HOST=localhost                    # Or your database server
DB_PORT=your_database_port (usually 5432)
DB_NAME=smartcalendar
DB_USER=your_database_username
DB_PASSWORD=your_database_password

# Email Service (Brevo API for email verification)
EMAIL_API_KEY=your_brevo_api_key

# JWT Configuration (REQUIRED for authentication)
JWT_SECRET=your_jwt_secret_key       # Generate with: openssl rand -base64 32
JWT_EXPIRATION=86400000              # 24 hours in milliseconds
```

**Generate a secure JWT key:**

```bash
openssl rand -base64 32
```

**Important:**
- ✅ The `.env` file is in `.gitignore` and should **NEVER** be committed to Git
- ✅ Use `.env.example` as a template for team members
- ✅ JWT_SECRET must be at least 256 bits (32+ characters in base64)
- ✅ All variables must be set for the backend to function

### Frontend - React Environment Variables

The frontend uses env files to know which backend to call:

- **`.env.production`** is included in the repo and contains `REACT_APP_BACKEND_URL` for the production server
- **`.env.local`** (gitignored) must be created by you for local development:

  ```bash
  # frontend/.env.local
  REACT_APP_BACKEND_URL=http://localhost:8080/api/
  ```

**Create frontend .env.local:**

```bash
cd frontend
echo "REACT_APP_BACKEND_URL=http://localhost:8080/api/" > .env.local
```

## Important information for testers and reviewers

### Prerequisites before starting:

1. **Java 21**: The project requires Java 21
   ```bash
   # Check Java version
   java -version
   
   # Install Java 21:
   # Via SDKMAN: sdk install java 21.0.2-open
   # Via Homebrew: brew install openjdk@21
   ```

2. **Backend .env file**: Create and configure `backend/backend/app/.env` with all required variables (see *Environment Variables and Configuration* section above)

3. **Frontend .env.local**: Create `frontend/.env.local` to point to local backend

4. **PostgreSQL database**: Ensure you have access to a PostgreSQL database with the correct credentials

### Quick start:

```bash
# 1. Create backend .env
cd backend/backend/app
cp .env.example .env
# Edit .env with your values

# 2. Create frontend .env.local
cd ../../../frontend
echo "REACT_APP_BACKEND_URL=http://localhost:8080/api/" > .env.local

# 3. Start backend (in one terminal)
cd ../backend/backend/app
./mvnw spring-boot:run

# 4. Start frontend (in another terminal)
cd ../../../frontend
npm install
npm start
```

## Additional information

- **Java 21 Required**: The project requires Java 21. If you have an older version installed, the backend will not start.
- **Backend**: The application uses an external PostgreSQL database, which may cause issues for external parties trying to use features tied to the data in the database.
- **Frontend**: The functionality is currently limited, but the application starts without problems and shows a basic view.
- **Ports**: Ensure that ports 8080 (backend) and 3000 (frontend) are free on your computer.
- **IDE settings**: If you encounter problems, verify that your IDE is configured with Java 21 and the correct Node.js version.
- **.env Security**: Never commit the `.env` file to Git. It contains sensitive information such as database passwords and API keys.

## 📋 CI/CD Pipeline

This project uses automated CI/CD with GitHub Actions:
- ✅ Automated testing on every commit
- ✅ Automatic deployment to Render on main branch
- ✅ Fast deployment (~5-7 minutes from commit to live)
