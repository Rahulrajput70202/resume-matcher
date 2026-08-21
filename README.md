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
