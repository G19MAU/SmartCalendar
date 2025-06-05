# SmartCalendar

SmartCalendar är en kalenderapplikation utformad för att hjälpa användare att hantera sina händelser och scheman på ett effektivt sätt.

## Ladda ner och köra projektet

Versionen av koden som har lämnats för granskning är taggad som `v3.0-review`. För att ladda ner och använda denna version:

1. Gå till projektets GitHub-sida: https://github.com/slidecart/G19SmartCalender.
2. Klicka på fliken "Releases" eller "Tags" och hitta taggen `v3.0-review`.
3. Klicka på "Download ZIP" under `v3.0-review` för att ladda ner källkoden som en ZIP-fil.
4. Extrahera ZIP-filen till en mapp på din dator.
5. Öppna projektet i din IDE:
   - **IntelliJ IDEA**: Välj "Open" och navigera till den extraherade mappen.
   - **VS Code**: Välj "Open Folder" och välj den extraherade mappen.

## Starta applikationen

### Backend

Backend är en Spring Boot-applikation byggd med Maven och startas via klassen `AppApplication`. För att köra den krävs:

- Java 17 (eller den version som anges i `pom.xml`).
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

## Miljövariabler

Backend läser in följande variabler från miljön innan start (se `backend/backend/app/src/main/resources/application.properties`):

- `DB_HOST`, `DB_PORT`, `DB_NAME`
- `DB_USER`, `DB_PASSWORD`
- `EMAIL_API_KEY`

Dessa måste finnas i din miljö eller sättas via ett startskript för att backend ska fungera.

Frontend använder env-filer för att veta vilken backend som ska anropas:

- `.env.production` finns i repot och innehåller `REACT_APP_BACKEND_URL` för produktionsservern.
- `.env.local` (gitignored) behöver du skapa själv och ge innehållet
  
  ```
  REACT_APP_BACKEND_URL=http://localhost:8080/api/
  ```


## Viktig information för testare och granskare

Se till att miljövariablerna enligt ovan är satta innan du startar backend. Frontend använder env-filerna `.env.production` respektive `.env.local` enligt beskrivningen i avsnittet *Miljövariabler*.

## Ytterligare information

- **Backend**: Applikationen använder en extern PostgreSQL-databas, vilket kan orsaka problem för externa aktörer som försöker använda funktioner kopplade till datan i databasen.
- **Frontend**: I dagsläget är funktionaliteten begränsad, men applikationen startar utan problem och visar en grundläggande vy.
- **Portar**: Se till att portarna 8080 (backend) och 3000 (frontend) är lediga på din dator.
- **IDE-inställningar**: Om du stöter på problem, kontrollera att din IDE har rätt Java- och Node.js-versioner konfigurerade i inställningarna.

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

- Java 17 (or the version specified in `pom.xml`).
- Maven (installed on your computer or via your IDE's built-in support).

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

## Environment variables

The backend reads the following variables from the environment before startup (see `backend/backend/app/src/main/resources/application.properties`):

- `DB_HOST`, `DB_PORT`, `DB_NAME`
- `DB_USER`, `DB_PASSWORD`
- `EMAIL_API_KEY`

These must be present in your environment or provided through a startup script for the backend to function.

The frontend uses env files to know which backend to call:

- `.env.production` is included in the repo and contains `REACT_APP_BACKEND_URL` for the production server.
- `.env.local` (gitignored) must be created by you with the content

  ```
  REACT_APP_BACKEND_URL=http://localhost:8080/api/
  ```

## Important information for testers and reviewers

Make sure the environment variables described above are set before you start the backend. The frontend uses the env files `.env.production` and `.env.local` as explained in the *Environment variables* section.

## Additional information

- **Backend**: The application uses an external PostgreSQL database, which may cause issues for external parties trying to use features tied to the data in the database.
- **Frontend**: The functionality is currently limited, but the application starts without problems and shows a basic view.
- **Ports**: Ensure that ports 8080 (backend) and 3000 (frontend) are free on your computer.
- **IDE settings**: If you encounter problems, verify that your IDE is configured with the correct versions of Java and Node.js.
