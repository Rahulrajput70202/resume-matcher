🚀 Resume Matcher — Hybrid AI Resume Screening System

A full-stack Java Spring Boot Resume Matcher that analyzes resumes against job descriptions using keyword matching + semantic vector search.

The system combines traditional rule-based matching with Ollama embeddings, PostgreSQL + pgvector, and configurable hybrid scoring to identify both exact and semantically related skills.

---

✨ Key Features

🔐 Authentication & Security

- User registration and login
- JWT-based authentication
- Spring Security
- Protected REST APIs
- BCrypt password hashing

📄 Resume Processing

- PDF resume upload
- Resume text extraction using Apache PDFBox
- PDF validation
- Text chunking for semantic processing
- Resume-specific identifiers for isolated vector retrieval

🎯 Hybrid Resume Matching

The matching engine combines two complementary approaches:

1. Keyword Matching

Identifies explicit skills, technologies, and job-related terms present in the resume.

2. Semantic Matching

Uses vector embeddings to identify resume content that is semantically related to the job description, even when exact keywords are different.

3. Hybrid Score

The final compatibility score combines both signals:

Final Score =
    Keyword Score × Keyword Weight
  + Semantic Score × Semantic Weight

The weights and semantic retrieval limit are configurable through application properties.

This reduces dependence on exact keyword overlap and allows the system to recognize related concepts.

🧠 Semantic Search & Embeddings

- Ollama for local AI processing
- "nomic-embed-text" embedding model
- 768-dimensional embeddings
- PostgreSQL with pgvector
- Cosine-distance based similarity search
- Top-K semantic retrieval
- Resume-scoped vector search
- Graceful fallback to keyword matching when semantic infrastructure is unavailable

🤖 AI-Assisted Analysis

- Local LLM support through Ollama
- Context-aware resume analysis
- AI-generated suggestions
- Optional AI assistance
- No external AI API key required for the local Ollama pipeline

🌐 Web Application

- Responsive web interface
- Resume PDF upload
- Job title and job description input
- Match score visualization
- Analysis history
- Previous analysis viewing
- Analysis deletion

---

🏗️ System Architecture

                    ┌──────────────────────┐
                    │      Resume PDF      │
                    └──────────┬───────────┘
                               │
                               ▼
                    ┌──────────────────────┐
                    │     Apache PDFBox    │
                    │    Text Extraction   │
                    └──────────┬───────────┘
                               │
                               ▼
                    ┌──────────────────────┐
                    │    Text Chunking     │
                    └──────────┬───────────┘
                               │
                               ▼
                    ┌──────────────────────┐
                    │       Ollama         │
                    │  nomic-embed-text    │
                    └──────────┬───────────┘
                               │
                         768-D Vector
                               │
                               ▼
                    ┌──────────────────────┐
                    │ PostgreSQL + pgvector│
                    │   Vector Storage     │
                    └──────────┬───────────┘
                               │
                         Semantic Search
                               │
                               ▼
                    ┌──────────────────────┐
                    │ Resume-Specific      │
                    │ Relevant Chunks      │
                    └──────────┬───────────┘
                               │
              ┌────────────────┴────────────────┐
              │                                 │
              ▼                                 ▼
    ┌────────────────────┐          ┌────────────────────┐
    │ Keyword Matching   │          │ Semantic Matching  │
    │                    │          │                    │
    │ Explicit Terms     │          │ Vector Similarity  │
    └──────────┬─────────┘          └──────────┬─────────┘
               │                               │
               └───────────────┬───────────────┘
                               │
                               ▼
                    ┌──────────────────────┐
                    │   Hybrid Scoring     │
                    │                      │
                    │ Keyword + Semantic   │
                    └──────────┬───────────┘
                               │
                               ▼
                    ┌──────────────────────┐
                    │ Match Score + Skills │
                    │ Suggestions + Result │
                    └──────────────────────┘

---

🧠 How the Matching Pipeline Works

1. Resume Upload

The user uploads a PDF resume and provides a target job description.

Resume PDF
    +
Job Description

---

2. Text Extraction

Apache PDFBox extracts machine-readable text from the uploaded resume.

PDF
 ↓
Apache PDFBox
 ↓
Resume Text

---

3. Text Chunking

The resume is divided into smaller sections before generating embeddings.

Example:

Chunk 1 → Summary + Skills
Chunk 2 → Education + Experience
Chunk 3 → Projects
Chunk 4 → Certifications / Additional Information

---

4. Embedding Generation

Each resume chunk is converted into a vector using:

Ollama
    ↓
nomic-embed-text
    ↓
768-dimensional embedding

---

5. Vector Storage

Embeddings are stored in PostgreSQL using pgvector.

Each resume is associated with a unique "resume_id" so that semantic searches are scoped to the current resume.

Conceptually:

document_chunks

id
resume_id
document_name
chunk_text
embedding

This prevents semantic retrieval from accidentally mixing chunks belonging to different resumes.

---

6. Semantic Retrieval

The job description is converted into an embedding.

The system then searches the current resume's vector chunks using cosine distance.

Job Description
       ↓
Embedding
       ↓
pgvector similarity search
       ↓
Top-K relevant resume chunks

The semantic score is calculated from the retrieved similarities.

---

🎯 Hybrid Matching

The project does not rely only on keyword overlap.

The matcher produces:

Keyword Score
      +
Semantic Score
      ↓
Hybrid Score

The current implementation supports configurable weights:

app.matching.keyword-weight=0.50
app.matching.semantic-weight=0.50
app.matching.semantic-top-k=5

For example:

Keyword Score  = 80
Semantic Score = 70

Keyword Weight  = 0.50
Semantic Weight = 0.50

Final Score =
(80 × 0.50) + (70 × 0.50)

= 75

The semantic component can also fall back safely to the keyword score when the vector infrastructure is unavailable.

---

🔍 Traditional Matching vs Hybrid Matching

Previous Keyword-Based Approach

Resume
   ↓
Keyword Extraction
   ↓
Exact Term Matching
   ↓
Keyword Score

This works well for explicit skills but can miss semantically equivalent terminology.

For example:

REST
RESTful APIs

PostgreSQL
Postgres

Object-Oriented Programming
OOP

Current Hybrid Approach

Resume
   ↓
Text Chunking
   ↓
 ┌───────────────────────┐
 │                       │
 ▼                       ▼
Keyword Matching    Embedding Generation
 │                       │
 │                       ▼
 │                  pgvector Search
 │                       │
 └───────────┬───────────┘
             ▼
       Hybrid Scoring
             │
             ▼
       Match Analysis

---

🛠️ Technology Stack

Backend

- Java 17
- Spring Boot 3.5.6
- Spring Security
- Spring Data JPA
- Spring JDBC
- REST APIs
- JWT
- BCrypt

AI / Semantic Search

- Spring AI
- Ollama
- "nomic-embed-text"
- Vector Embeddings
- Semantic Similarity Search
- Retrieval-Augmented Generation concepts
- Configurable Hybrid Matching

Database

- PostgreSQL
- pgvector
- H2 for development/testing

Resume Processing

- Apache PDFBox

Frontend

- HTML5
- CSS3
- JavaScript

Development Tools

- IntelliJ IDEA
- Maven
- Git
- GitHub
- Postman
- Docker

---

📁 Project Structure

resume-matcher/
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/rahul/resumematcher/
│   │   │       │
│   │   │       ├── controller/
│   │   │       │   ├── AuthController.java
│   │   │       │   ├── EmbeddingController.java
│   │   │       │   ├── HomeController.java
│   │   │       │   └── ResumeController.java
│   │   │       │
│   │   │       ├── service/
│   │   │       │   ├── EmbeddingService.java
│   │   │       │   ├── TextChunkingService.java
│   │   │       │   ├── PdfParserService.java
│   │   │       │   ├── MatchingService.java
│   │   │       │   ├── SemanticMatchingService.java
│   │   │       │   ├── HybridMatchingService.java
│   │   │       │   └── AiMatchingService.java
│   │   │       │
│   │   │       ├── repository/
│   │   │       │   ├── DocumentChunkRepository.java
│   │   │       │   ├── ResumeAnalysisRepository.java
│   │   │       │   └── UserRepository.java
│   │   │       │
│   │   │       ├── security/
│   │   │       │   ├── JwtAuthFilter.java
│   │   │       │   ├── JwtUtil.java
│   │   │       │   └── SecurityConfig.java
│   │   │       │
│   │   │       ├── entity/
│   │   │       ├── dto/
│   │   │       └── ResumeMatcherApplication.java
│   │   │
│   │   └── resources/
│   │       ├── static/
│   │       │   ├── index.html
│   │       │   ├── app.js
│   │       │   └── styles.css
│   │       │
│   │       └── application.properties
│   │
│   └── test/
│       └── java/
│
├── pom.xml
└── README.md

---

⚙️ Requirements

Install the following:

- Java 17+
- Maven
- PostgreSQL
- pgvector
- Ollama
- Git
- Docker (optional)

---

🐘 PostgreSQL + pgvector Setup

Create the database:

CREATE DATABASE resume_matcher;

Connect to the database:

psql -U postgres -d resume_matcher

Enable pgvector:

CREATE EXTENSION IF NOT EXISTS vector;

Create the document chunk table:

CREATE TABLE IF NOT EXISTS document_chunks (
    id BIGSERIAL PRIMARY KEY,
    resume_id VARCHAR(36),
    document_name TEXT NOT NULL,
    chunk_text TEXT NOT NULL,
    embedding VECTOR(768)
);

Create the resume lookup index:

CREATE INDEX IF NOT EXISTS idx_document_chunks_resume_id
ON document_chunks(resume_id);

Verify pgvector:

SELECT extversion
FROM pg_extension
WHERE extname = 'vector';

---

🤖 Ollama Setup

Install Ollama and pull the required models.

Embedding Model

ollama pull nomic-embed-text

Chat Model

ollama pull llama3.2

Verify:

ollama list

Make sure Ollama is running before starting Spring Boot.

---

🔧 Application Configuration

Configure:

src/main/resources/application.properties

Example:

spring.datasource.url=jdbc:postgresql://localhost:5432/resume_matcher
spring.datasource.username=postgres
spring.datasource.password=YOUR_PASSWORD

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false

spring.ai.ollama.base-url=http://localhost:11434
spring.ai.ollama.chat.options.model=llama3.2
spring.ai.ollama.chat.options.temperature=0.2
spring.ai.ollama.embedding.options.model=nomic-embed-text

app.matching.keyword-weight=0.50
app.matching.semantic-weight=0.50
app.matching.semantic-top-k=5

«Security: Never commit real database passwords, API keys, JWT secrets, or production credentials to GitHub.»

---

▶️ Running the Application

Clone the repository:

git clone https://github.com/Rahulrajput70202/resume-matcher.git

Enter the project:

cd resume-matcher

Build:

mvn clean package

Run:

mvn spring-boot:run

The application starts at:

http://localhost:8080

---

🧪 API Endpoints

Test Embedding

GET /api/embedding/test

Example:

http://localhost:8080/api/embedding/test?text=Java%20Spring%20Boot%20developer

Returns the embedding dimension and generated vector.

---

Store Test Embedding

POST /api/embedding/store-test

---

Semantic Search

GET /api/embedding/search

Example:

http://localhost:8080/api/embedding/search?text=Java%20Spring%20Boot%20developer&limit=5

The result contains semantically similar resume chunks and their vector distance.

---

📄 Resume Analysis API

POST /api/resume/analyze

Multipart form data:

resume          → PDF file
jobTitle        → Target job title
jobDescription  → Job description

The analysis pipeline:

PDF Resume
    ↓
Text Extraction
    ↓
Text Chunking
    ↓
Embedding Generation
    ↓
Vector Storage
    ↓
Keyword Matching
    ↓
Semantic Matching
    ↓
Hybrid Score
    ↓
Matched / Missing Skills
    ↓
AI Suggestions

---

🔐 Authentication

The application uses JWT authentication.

Authentication flow:

Register
   ↓
Login
   ↓
JWT Token
   ↓
Authorization Header
   ↓
Protected REST API

Example:

Authorization: Bearer <JWT_TOKEN>

---

🎯 Example Use Case

A recruiter wants to evaluate a candidate for:

Java Developer

Example job requirements:

Java
Spring Boot
REST APIs
PostgreSQL
Hibernate
Docker
Microservices

The system analyzes the resume and can identify:

Matched Skills

✓ Java
✓ Spring Boot
✓ REST APIs
✓ Hibernate
✓ PostgreSQL

Missing / Weak Skills

✗ Docker
✗ Microservices

The semantic layer can additionally retrieve resume sections that are conceptually relevant to the job description.

---

🧪 Testing

The project includes unit tests covering:

- Keyword matching
- Semantic matching
- Hybrid matching
- Semantic failure fallback
- Vector repository behavior
- AI matching service

Run all tests:

mvn clean test

Expected result:

Tests run: 11
Failures: 0
Errors: 0
Skipped: 0

---

📊 Current Architecture Highlights

- Full-stack Java application
- Spring Boot REST APIs
- JWT authentication
- PDF resume processing
- Rule-based keyword matching
- Semantic vector search
- 768-dimensional embeddings
- PostgreSQL + pgvector
- Resume-scoped vector retrieval
- Configurable hybrid scoring
- Local AI using Ollama
- AI-assisted resume analysis
- Graceful semantic fallback
- Automated unit testing

---

🚀 Future Improvements

Planned enhancements include:

- Reranking retrieved resume chunks
- Improved semantic scoring calibration
- Human-labeled evaluation dataset
- Precision / Recall evaluation
- NDCG-based ranking evaluation
- Multiple-resume comparison
- Recruiter dashboard
- Resume skill-gap analysis
- Job recommendation system
- DOCX resume support
- AI-generated interview questions
- Dockerized production deployment
- Cloud deployment
- Improved observability and scoring analytics

---

👨‍💻 Author

Rahul Tekchand Bainade

B.Sc. Computer Science

GitHub

https://github.com/Rahulrajput70202

LinkedIn

https://www.linkedin.com/in/rahul-bainade-919985327/

Portfolio

https://rahulbainade.netlify.app/

---

⭐ Project Goal

Resume Matcher was built to explore how traditional ATS keyword matching can be combined with semantic vector retrieval to create a more context-aware resume screening system.

The project demonstrates practical experience with:

Java
Spring Boot
Spring Security
REST APIs
JWT
PostgreSQL
pgvector
Spring AI
Ollama
Vector Embeddings
Semantic Search
RAG Concepts
PDF Processing
Maven
Docker
Git
