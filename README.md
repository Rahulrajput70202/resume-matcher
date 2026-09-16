# 🚀 Resume Matcher — RAG-Powered AI Resume Screening

A full-stack **Java Spring Boot Resume Matcher** that analyzes resumes against job descriptions using traditional keyword matching combined with **semantic search, vector embeddings, PostgreSQL + pgvector, and local AI through Ollama**.

The project evolved from a basic keyword-based resume matcher into a **RAG-enabled resume analysis system** capable of retrieving relevant sections of a resume based on semantic meaning.

---

## ✨ Features

### 🔐 Authentication & Security

- User registration and login
- JWT-based authentication
- Spring Security
- Protected REST APIs
- BCrypt password hashing

### 📄 Resume Processing

- Upload PDF resumes
- Extract resume text using Apache PDFBox
- Validate uploaded PDF files
- Split resume content into smaller chunks

### 🧠 RAG & Semantic Search

- Generate embeddings using Ollama
- Uses `nomic-embed-text`
- 768-dimensional embeddings
- Store embeddings in PostgreSQL using pgvector
- Perform semantic similarity search
- Retrieve the most relevant resume sections for a query

### 🎯 Resume Matching

- Compare resume against job descriptions
- Identify matched skills
- Identify missing skills
- Calculate resume/job compatibility score
- Combine traditional matching with semantic retrieval

### 🤖 AI Assistance

- Local AI using Ollama
- AI-assisted resume analysis
- Context-aware suggestions
- No external AI API key required for the local Ollama pipeline

### 🌐 Web Application

- Responsive frontend
- Resume PDF upload
- Job title and job description input
- Match score visualization
- Analysis history
- View previous analyses
- Delete analysis history

---

# 🏗️ System Architecture

```text
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
                    │   nomic-embed-text   │
                    └──────────┬───────────┘
                               │
                         768-D Embedding
                               │
                               ▼
                    ┌──────────────────────┐
                    │ PostgreSQL + pgvector│
                    │    Vector Database   │
                    └──────────┬───────────┘
                               │
                         Semantic Search
                               │
                               ▼
                    ┌──────────────────────┐
                    │ Relevant Resume      │
                    │ Chunks Retrieved     │
                    └──────────┬───────────┘
                               │
                               ▼
                    ┌──────────────────────┐
                    │     AI Analysis      │
                    │       Ollama         │
                    └──────────┬───────────┘
                               │
                               ▼
                    ┌──────────────────────┐
                    │ Match Score + Skills │
                    │ Suggestions + Results│
                    └──────────────────────┘

🧠 How the RAG Pipeline Works

The application uses Retrieval-Augmented Generation concepts to provide relevant resume context to the AI.

1. Resume Upload

The user uploads a PDF resume.

Resume.pdf
2. Text Extraction

Apache PDFBox extracts the text from the PDF.

3. Text Chunking

The extracted resume text is divided into smaller chunks.

Example:

Chunk 1 → Professional Summary + Skills


Chunk 2 → Education + Experience


Chunk 3 → Projects


Chunk 4 → Additional Experience
4. Embedding Generation

Each chunk is converted into a numerical vector using:

Ollama
nomic-embed-text

The embeddings used by this project contain:

768 dimensions
5. Vector Storage

The embeddings are stored in:

PostgreSQL
+
pgvector

Database structure:

document_chunks


id
document_name
chunk_text
embedding
6. Semantic Retrieval

When a query such as:

Java Spring Boot developer

is received, it is converted into an embedding.

The system searches PostgreSQL for the most semantically similar resume chunks.

This allows the application to retrieve relevant information based on semantic similarity rather than only exact keyword matches.

7. AI Analysis

The retrieved resume context can be used as contextual information for AI-assisted analysis and recommendations.

🔍 Traditional Matching vs RAG
Previous Version
Resume
   ↓
Keyword Matching
   ↓
Matched Keywords
   ↓
Score

The traditional matcher relies heavily on keyword overlap.

RAG-Enabled Version
Resume
   ↓
Text Chunks
   ↓
Embeddings
   ↓
Vector Database
   ↓
Semantic Search
   ↓
Relevant Resume Context
   ↓
AI Analysis

This allows the system to search based on semantic similarity, not only exact keyword matches.

🛠️ Technology Stack
Backend
Java 17
Spring Boot
Spring Security
Spring Data JPA
Spring JDBC
REST APIs
JWT
AI / RAG
Spring AI
Ollama
nomic-embed-text
Embeddings
Semantic Search
Retrieval-Augmented Generation
Database
PostgreSQL 18
pgvector
H2 for development/testing
Resume Processing
Apache PDFBox
Frontend
HTML5
CSS3
JavaScript
Development Tools
IntelliJ IDEA
Maven
Git
GitHub
Postman
📁 Project Structure
resume-matcher/
│
├── src/
│   └── main/
│       ├── java/
│       │   └── com/rahul/resumematcher/
│       │       │
│       │       ├── controller/
│       │       │   ├── AuthController.java
│       │       │   ├── EmbeddingController.java
│       │       │   ├── HomeController.java
│       │       │   └── ResumeController.java
│       │       │
│       │       ├── service/
│       │       │   ├── EmbeddingService.java
│       │       │   ├── TextChunkingService.java
│       │       │   ├── PdfParserService.java
│       │       │   ├── MatchingService.java
│       │       │   └── AiMatchingService.java
│       │       │
│       │       ├── repository/
│       │       │   ├── DocumentChunkRepository.java
│       │       │   ├── ResumeAnalysisRepository.java
│       │       │   └── UserRepository.java
│       │       │
│       │       ├── security/
│       │       │   ├── JwtAuthFilter.java
│       │       │   ├── JwtUtil.java
│       │       │   └── SecurityConfig.java
│       │       │
│       │       ├── entity/
│       │       ├── dto/
│       │       └── ResumeMatcherApplication.java
│       │
│       └── resources/
│           ├── static/
│           │   ├── index.html
│           │   ├── app.js
│           │   └── styles.css
│           │
│           └── application.properties
│
├── pom.xml
└── README.md
⚙️ Requirements

Install the following before running the application:

Java 17+
Maven
PostgreSQL 18
pgvector
Ollama
Git
🐘 PostgreSQL + pgvector Setup

Create the database:

CREATE DATABASE resume_matcher;

Connect to it:

psql -U postgres -d resume_matcher

Enable pgvector:

CREATE EXTENSION vector;

Create the vector table:

CREATE TABLE document_chunks (
    id BIGSERIAL PRIMARY KEY,
    document_name TEXT NOT NULL,
    chunk_text TEXT NOT NULL,
    embedding VECTOR(768)
);

Verify pgvector:

SELECT extversion
FROM pg_extension
WHERE extname = 'vector';
🤖 Ollama Setup

Install Ollama and download the required models.

Embedding model
ollama pull nomic-embed-text
Chat model
ollama pull llama3.2

Verify installed models:

ollama list

Make sure Ollama is running before starting the Spring Boot application.

🔧 Application Configuration

Update:

src/main/resources/application.properties

Example PostgreSQL configuration:

spring.datasource.url=jdbc:postgresql://localhost:5432/resume_matcher
spring.datasource.username=postgres
spring.datasource.password=YOUR_PASSWORD


spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false


spring.ai.ollama.base-url=http://localhost:11434
spring.ai.ollama.chat.options.model=llama3.2
spring.ai.ollama.chat.options.temperature=0.2
spring.ai.ollama.embedding.options.model=nomic-embed-text

Never commit real passwords, API keys, or production secrets to GitHub.

▶️ Running the Application

Clone the repository:

git clone https://github.com/Rahulrajput70202/resume-matcher.git

Enter the project directory:

cd resume-matcher

Build the project:

mvn clean package

Run the application:

mvn spring-boot:run

The application will run on:

http://localhost:8080
🧪 API Testing
Test Embedding
GET /api/embedding/test

Example:

http://localhost:8080/api/embedding/test?text=Java%20Spring%20Boot%20developer

Example response:

{
  "text": "Java Spring Boot developer",
  "dimensions": 768,
  "embedding": [...]
}
Store Test Embedding
POST /api/embedding/store-test
Semantic Search
GET /api/embedding/search

Example:

http://localhost:8080/api/embedding/search?text=Java%20Spring%20Boot%20developer&limit=5

Example response:

[
  {
    "id": 3,
    "document_name": "resume.pdf",
    "chunk_text": "Technical Skills: Java, Spring Boot...",
    "distance": 0.32
  }
]

A lower distance indicates greater semantic similarity.

📄 Resume Analysis

Endpoint:

POST /api/resume/analyze

Multipart form data:

resume          → PDF file
jobTitle        → Java Developer
jobDescription  → Job description

The application extracts the resume text, performs matching, and returns the analysis result.

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
Protected API

Example:

Authorization: Bearer <JWT_TOKEN>
🎯 Example Use Case

A recruiter enters:

Job Title:
Java Developer

Example job requirements:

Java
Spring Boot
REST APIs
PostgreSQL
Hibernate
Docker
Microservices

The system can identify relevant skills from the resume and provide:

Matched Skills:
✓ Java
✓ Spring Boot
✓ REST APIs
✓ Hibernate
✓ PostgreSQL


Missing / Weak Skills:
✗ Docker
✗ Microservices

The semantic retrieval layer can also retrieve resume sections relevant to the job description.

🚀 Future Improvements
Advanced RAG prompt engineering
Hybrid keyword + vector search
Reranking retrieved chunks
Multiple resume comparison
Job recommendation system
Recruiter dashboard
Resume skill-gap analysis
Docker deployment
Cloud deployment
RAG retrieval evaluation
DOCX resume support
AI-generated interview questions
📊 Project Highlights
Full-stack Java application
Spring Boot REST API
JWT authentication
PDF resume processing
Traditional keyword matching
Semantic vector search
768-dimensional embeddings
PostgreSQL + pgvector
Local AI with Ollama
RAG-based retrieval architecture
AI-assisted resume analysis
👨‍💻 Author

Rahul Tekchand Bainade

B.Sc. Computer Science

GitHub

https://github.com/Rahulrajput70202

LinkedIn

https://www.linkedin.com/in/rahul-bainade-919985327/

Portfolio

https://rahulbainade.netlify.app/

⭐ Support

If you find this project useful, consider giving the repository a ⭐ on GitHub!
............
