# 🐛 BUG — Online Examination System

A web-based platform for conducting objective-type (MCQ) examinations with **automated CI/CD deployment** using Jenkins and Docker.

---

## 📋 Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Technology Stack](#technology-stack)
- [Prerequisites](#prerequisites)
- [Quick Start — Local Development](#quick-start--local-development)
- [Docker Deployment](#docker-deployment)
- [Jenkins CI/CD Pipeline](#jenkins-cicd-pipeline)
- [Pipeline Stages](#pipeline-stages)
- [Jenkins Setup Guide](#jenkins-setup-guide)
- [Access URLs](#access-urls)
- [Project Structure](#project-structure)
- [User Roles](#user-roles)
- [Exam Workflow](#exam-workflow)

---

## Overview

The **BUG Online Examination System** provides:

- **Admin/Examiner** — Create & manage exams, add questions, view student results, monitor live sessions, leaderboard
- **Student** — Register/login, select exam topics, attempt MCQ exams with a timer, auto-evaluation, view results
- **DevOps Pipeline** — Automated build, test, containerize, deploy, and verify using Jenkins + Docker

### Problem Solved

| Traditional System | BUG System |
|---|---|
| Paper-based exams | Digital MCQ platform |
| Manual evaluation | Automatic answer evaluation |
| Delayed results | Instant result generation |
| Manual deployment | Automated CI/CD pipeline |
| Environment issues | Docker containerization |

---

## Architecture

```
┌─────────┐     ┌──────────┐     ┌─────────────────┐     ┌──────────────┐     ┌──────────┐
│Developer │────▶│  GitHub   │────▶│    Jenkins       │────▶│ Docker Image │────▶│ Docker   │
│          │push │Repository │     │ Build/Test/Deploy│     │ Build        │     │Container │
└─────────┘     └──────────┘     └─────────────────┘     └──────────────┘     └──────────┘
                                                                                     │
                                                                                     ▼
                                                                              ┌──────────────┐
                                                                              │   Nginx       │
                                                                              │ Reverse Proxy │
                                                                              └──────┬───────┘
                                                                                     │
                                                            ┌────────────────────────┼────────────────────────┐
                                                            ▼                        ▼                        ▼
                                                    ┌──────────────┐      ┌──────────────┐        ┌──────────────┐
                                                    │   Frontend   │      │   Backend    │        │    MySQL     │
                                                    │ React + Vite │      │ Spring Boot  │        │   Database   │
                                                    │   (Port 80)  │      │  (Port 8080) │        │  (Port 3306) │
                                                    └──────────────┘      └──────────────┘        └──────────────┘
```

**CI/CD Pipeline Flow:**
```
Developer → GitHub → Jenkins → Build/Test → Docker Image → Docker Container → Nginx → Browser
```

---

## Technology Stack

| Layer | Technology |
|---|---|
| **Frontend** | React 18, Vite 5, Axios |
| **Backend** | Java 17, Spring Boot 3.2, Spring Data JPA, WebSocket/STOMP |
| **Database** | MySQL 8.0 (Docker) / H2 In-Memory (Local) |
| **Cache** | Redis 7 |
| **Message Broker** | RabbitMQ 3 |
| **Web Server** | Nginx (reverse proxy, rate limiting, gzip) |
| **Containerization** | Docker, Docker Compose |
| **CI/CD** | Jenkins (Declarative Pipeline) |
| **Version Control** | Git, GitHub |

---

## Prerequisites

| Tool | Version | Purpose |
|---|---|---|
| **Docker** | 20.10+ | Container runtime |
| **Docker Compose** | 2.0+ | Multi-container orchestration |
| **Git** | 2.30+ | Version control |
| **Jenkins** | 2.400+ | CI/CD automation |
| **Java** | 17 | Backend build (if running locally) |
| **Node.js** | 18 | Frontend build (if running locally) |
| **Maven** | 3.9+ | Backend dependency management (if running locally) |

---

## Quick Start — Local Development

```bash
# 1. Clone the repository
git clone https://github.com/Arman3612/Deveops.git
cd Deveops

# 2. Start the backend (H2 in-memory database, no Docker needed)
cd backend
mvn clean package -DskipTests
java -jar target/proctor-0.0.1-SNAPSHOT.jar

# 3. Start the frontend (in a new terminal)
cd frontend
npm install
npm run dev
```

**Or use the Windows batch scripts:**
```batch
start-local.bat   :: Starts both frontend and backend
stop-local.bat    :: Stops both services
```

| Service | URL |
|---|---|
| Frontend | http://localhost:3000 |
| Backend API | http://localhost:8080 |
| H2 Console | http://localhost:8080/h2-console |

---

## Docker Deployment

### Build and Deploy (one command)

```bash
# Build images and start all 6 services
docker-compose up -d --build

# Check container health status
docker-compose ps

# View logs
docker-compose logs -f

# Stop all services
docker-compose down
```

### Services Started by Docker Compose

| Container | Image | Port | Purpose |
|---|---|---|---|
| `proctor_db` | mysql:8.0 | 3306 | Persistent database |
| `proctor_redis` | redis:7-alpine | 6379 | Session/cache store |
| `proctor_rabbitmq` | rabbitmq:3-management | 5672, 15672 | Message broker |
| `proctor_backend` | proctor-backend | 8080 | Spring Boot API |
| `proctor_frontend` | proctor-frontend | 3000 | React application |
| `proctor_nginx` | nginx:alpine | 80, 443 | Reverse proxy |

### Health Checks

All services include Docker health checks. The backend waits for MySQL, Redis, and RabbitMQ to be healthy before starting. The frontend waits for the backend.

```bash
# Verify health
docker inspect --format='{{.State.Health.Status}}' proctor_backend
# Expected output: healthy

# Test health endpoint directly
curl http://localhost:8080/api/health
# {"status":"UP","application":"proctor","timestamp":"..."}
```

---

## Jenkins CI/CD Pipeline

### Pipeline Stages

```
┌──────────┐   ┌────────────────┐   ┌───────────────┐   ┌──────────────┐
│ Checkout │──▶│ Build Frontend │──▶│ Build Backend │──▶│ Test Backend │
│ (Git)    │   │ (npm ci/build) │   │ (mvn package) │   │ (mvn test)   │
└──────────┘   └────────────────┘   └───────────────┘   └──────────────┘
                                                               │
                                                               ▼
┌──────────────────┐   ┌───────────────────┐   ┌────────────────────────┐
│ Verify           │◀──│ Deploy Containers │◀──│ Build Docker Images    │
│ (Health Check)   │   │ (docker-compose)  │   │ (docker build)         │
└──────────────────┘   └───────────────────┘   └────────────────────────┘
        │
        ▼
   ✅ SUCCESS
```

| # | Stage | Description |
|---|---|---|
| 1 | **Checkout** | Clones the latest source code from GitHub |
| 2 | **Build Frontend** | Installs npm dependencies and creates production build |
| 3 | **Build Backend** | Compiles Java source and packages Spring Boot JAR |
| 4 | **Test Backend** | Runs unit tests with Maven |
| 5 | **Build Docker Images** | Creates Docker images for frontend and backend |
| 6 | **Stop Existing** | Stops and removes previous deployment containers |
| 7 | **Deploy Containers** | Launches all services via `docker-compose up -d` |
| 8 | **Verify Deployment** | Polls `/api/health` endpoint until application is healthy |

### Post-Build Actions

- **On Success**: Prints deployment URLs and success message
- **On Failure**: Tears down broken containers and reports errors
- **Always**: Cleans the Jenkins workspace

---

## Jenkins Setup Guide

### Step 1: Install Jenkins

```bash
# Using Docker (recommended)
docker run -d \
  --name jenkins \
  -p 8081:8080 \
  -p 50000:50000 \
  -v jenkins_home:/var/jenkins_home \
  -v /var/run/docker.sock:/var/run/docker.sock \
  jenkins/jenkins:lts
```

### Step 2: Install Required Plugins

In Jenkins → Manage Jenkins → Plugins, install:
- **Git Plugin**
- **Pipeline Plugin**
- **Docker Pipeline Plugin**

### Step 3: Create Pipeline Job

1. **New Item** → Enter name `online-exam-pipeline` → Select **Pipeline** → OK
2. Under **Pipeline** section:
   - **Definition**: `Pipeline script from SCM`
   - **SCM**: Git
   - **Repository URL**: `https://github.com/Arman3612/Deveops.git`
   - **Branch**: `*/main`
   - **Script Path**: `Jenkinsfile`
3. **Save** and **Build Now**

### Step 4: Configure Credentials (if private repo)

1. Jenkins → Manage Jenkins → Credentials
2. Add credential: **Username with password** or **SSH key**
3. Set ID to `github-credentials`
4. Update the `Jenkinsfile` to uncomment the `credentialsId` line

---

## Access URLs

| Service | URL | Credentials |
|---|---|---|
| **Application** (via Nginx) | http://localhost | — |
| **Frontend** (direct) | http://localhost:3000 | — |
| **Backend API** | http://localhost:8080 | — |
| **Health Check** | http://localhost:8080/api/health | — |
| **H2 Console** (local only) | http://localhost:8080/h2-console | sa / (empty) |
| **RabbitMQ Management** | http://localhost:15672 | guest / guest |
| **Jenkins** (if Dockerized) | http://localhost:8081 | (initial setup) |

### Default Login Credentials

| Role | Email | Password |
|---|---|---|
| **Admin** | `vijayapandian112007@gmail.com` | `1234567890` |
| **Student** | `user@gmail.com` | any 4+ chars |
| **New Student** | Register via the UI | — |

---

## Project Structure

```
Deveops/
├── Jenkinsfile                     # CI/CD Pipeline definition
├── docker-compose.yml              # Multi-container orchestration
├── nginx.conf                      # Reverse proxy configuration
├── start-local.bat                 # Windows: start services locally
├── stop-local.bat                  # Windows: stop local services
│
├── frontend/                       # React + Vite application
│   ├── Dockerfile                  # Multi-stage build → Nginx
│   ├── .dockerignore               # Excludes node_modules from build
│   ├── package.json
│   ├── vite.config.js
│   ├── index.html
│   └── src/
│       ├── App.jsx                 # Root component + routing
│       ├── main.jsx                # Entry point
│       ├── components/
│       │   ├── LoginPage.jsx       # Auth (login/register)
│       │   ├── StudentDashboard.jsx# Student home
│       │   ├── AdminDashboard.jsx  # Admin panel
│       │   ├── ExamInterface.jsx   # Exam taking UI
│       │   ├── ResultScreen.jsx    # Results display
│       │   └── ...
│       └── data/
│           ├── config.js           # API base URL config
│           ├── store.js            # Topic/question store
│           └── questionBank.js     # Static question data
│
├── backend/                        # Spring Boot application
│   ├── Dockerfile                  # Multi-stage build → JRE
│   ├── .dockerignore               # Excludes target/ from build
│   ├── pom.xml                     # Maven dependencies
│   └── src/main/java/com/exam/proctor/
│       ├── ProctorApplication.java # Spring Boot entry point
│       ├── controller/
│       │   ├── AuthController.java # Login/Register API
│       │   ├── ExamController.java # Exam management API
│       │   ├── AdminController.java# Admin operations API
│       │   ├── HealthController.java# Health check endpoint
│       │   └── ...
│       ├── entity/                 # JPA entities
│       ├── repository/             # Spring Data repositories
│       ├── service/                # Business logic
│       ├── config/                 # App configuration
│       └── dto/                    # Data transfer objects
│
└── certs/                          # SSL certificates (production)
```

---

## Exam Workflow

```
Admin creates exam
       │
       ▼
Admin adds MCQ questions
       │
       ▼
Student registers/logs in
       │
       ▼
Student selects exam topic
       │
       ▼
Student attempts questions (timed)
       │
       ▼
Student submits answers
       │
       ▼
System auto-evaluates answers
       │
       ▼
Result generated & stored
       │
       ▼
Leaderboard updated
```

---

## License

This project is licensed under the MIT License. See `LICENSE` for details.
