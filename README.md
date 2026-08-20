# Resume Matcher — AI-Assisted Resume / Job Description Matcher

A Spring Boot REST API that takes a resume (PDF) and a job description, scores how well they
match, lists matched/missing keywords, and stores the analysis history per user. Includes JWT
authentication and an optional LLM-powered suggestions layer.

## Tech Stack

- Java 17, Spring Boot 3.2 (Web, Data JPA, Security, Validation)
- H2 in-memory DB by default (zero setup) — MySQL profile included for production
- Apache PDFBox for resume text extraction
- JWT (jjwt) for stateless authentication
- JUnit 5 for tests

## How the matching works

Two layers, both implemented in `MatchingService`:

1. **Curated technical-skill dictionary** — phrase-aware matching (so "spring boot" and "rest api"
   are recognized as single terms, not broken apart by tokenization).
2. **Frequency-based keyword extraction** — pulls the most-repeated meaningful words out of the
   job description (after stopword removal) to catch role-specific terms not in the dictionary.

The score is `(matched keywords / total required keywords) * 100`. This runs 100% locally with
zero external dependencies, so the core feature works immediately without any API key.

`AiMatchingService` is an **optional** add-on: if you set `app.ai.api-key` in
`application.properties`, each analysis will also get a short LLM-generated improvement
suggestion. Leave it blank and that field just comes back `null` — nothing breaks.

## Project Structure

```
src/main/java/com/rahul/resumematcher/
├── ResumeMatcherApplication.java
├── entity/          User, ResumeAnalysis
├── repository/      UserRepository, ResumeAnalysisRepository
├── security/         JwtUtil, JwtAuthFilter, SecurityConfig
├── dto/              RegisterRequest, LoginRequest, AuthResponse, AnalysisResponse
├── service/           PdfParserService, MatchingService, AiMatchingService, UserDetailsServiceImpl
└── controller/        AuthController, ResumeController
```

## Setup & Run

Requires JDK 17+ and Maven.

```bash
cd resume-matcher
mvn clean install
mvn spring-boot:run
```

The app starts on `http://localhost:8080` using an in-memory H2 database (data resets on
restart — fine for demos, switch to MySQL for anything persistent, see below).

### Run against MySQL instead

1. Create a database: `CREATE DATABASE resumematcher;`
2. Edit `src/main/resources/application-mysql.properties` with your MySQL username/password.
3. Run: `mvn spring-boot:run -Dspring-boot.run.profiles=mysql`

### Enable AI suggestions (optional)

Set in `application.properties` (or override via env var / command line):

```
app.ai.api-key=YOUR_API_KEY
```

## Frontend

A single-page frontend lives in `src/main/resources/static/` (`index.html`, `styles.css`,
`app.js` — no build step, no npm install). Spring Boot serves it automatically as a static
resource, so once the backend is running:

```
http://localhost:8080/
```

...opens the app directly. Log in or register, paste a job description, drop in a resume PDF,
and hit "Run scan" — the score dial, matched/gap keyword chips, and scan history all render
live from the real API. No separate frontend server or CORS config needed since it's served
from the same origin as the API.

## API Usage

### 1. Register

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"rahul","password":"password123","email":"rahul@example.com"}'
```

Response includes a JWT `token` — use it in the `Authorization: Bearer <token>` header for
everything below.

### 2. Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"rahul","password":"password123"}'
```

### 3. Analyze a resume against a job description

```bash
curl -X POST http://localhost:8080/api/resume/analyze \
  -H "Authorization: Bearer <token>" \
  -F "resume=@/path/to/resume.pdf" \
  -F "jobTitle=Java Developer" \
  -F "jobDescription=We need a Java developer with Spring Boot, REST API, SQL and Git experience."
```

Returns match score, matched keywords, missing keywords, and (if enabled) AI suggestions.

### 4. View analysis history

```bash
curl http://localhost:8080/api/resume/history -H "Authorization: Bearer <token>"
```

### 5. Delete an analysis

```bash
curl -X DELETE http://localhost:8080/api/resume/1 -H "Authorization: Bearer <token>"
```

## Testing

```bash
mvn test
```

Covers `MatchingService` — full overlap, no overlap, and empty-input edge cases.

## Notes

- This project was built and reviewed for correctness by hand; since this environment has no
  internet access to download Maven dependencies, run `mvn clean install` locally first to catch
  any environment-specific issues before you demo it.
- H2 console (dev only): `http://localhost:8080/h2-console` — JDBC URL `jdbc:h2:mem:resumematcher`.
- Before deploying anywhere real, replace `app.jwt.secret` in `application.properties` with your
  own generated secret (`openssl rand -base64 32`) — the shipped one is for local dev only.
