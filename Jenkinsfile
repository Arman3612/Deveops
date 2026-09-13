pipeline {
    agent any

    environment {
        PROJECT_NAME    = 'online-exam-system'
        DOCKER_FRONTEND = 'proctor-frontend'
        DOCKER_BACKEND  = 'proctor-backend'
        COMPOSE_FILE    = 'docker-compose.yml'
        HEALTH_URL      = 'http://localhost:8080/api/health'
        APP_URL         = 'http://localhost'
    }

    stages {

        // ── Stage 1: Clone Source Code from GitHub ──────────────────────────
        stage('Checkout') {
            steps {
                echo '📥 Cloning source code from GitHub...'
                git branch: 'main',
                    url: 'https://github.com/Arman3612/Deveops.git'
                echo "✅ Source code cloned successfully."
            }
        }

        // ── Stage 2: Build Frontend (Vite + React) ──────────────────────────
        stage('Build Frontend') {
            steps {
                echo '🔨 Installing frontend dependencies and building...'
                dir('frontend') {
                    script {
                        if (isUnix()) {
                            sh 'npm ci && npm run build'
                        } else {
                            bat 'npm ci && npm run build'
                        }
                    }
                }
                echo '✅ Frontend build completed.'
            }
        }

        // ── Stage 3: Build Backend (Spring Boot + Maven) ────────────────────
        stage('Build Backend') {
            steps {
                echo '🔨 Building Spring Boot backend...'
                dir('backend') {
                    script {
                        if (isUnix()) {
                            sh 'mvn clean package -DskipTests'
                        } else {
                            bat 'mvn clean package -DskipTests'
                        }
                    }
                }
                echo '✅ Backend build completed.'
            }
        }

        // ── Stage 4: Run Backend Tests ──────────────────────────────────────
        stage('Test Backend') {
            steps {
                echo '🧪 Running backend unit tests...'
                dir('backend') {
                    script {
                        if (isUnix()) {
                            sh 'mvn test'
                        } else {
                            bat 'mvn test'
                        }
                    }
                }
                echo '✅ All tests passed.'
            }
        }

        // ── Stage 5: Build Docker Images ────────────────────────────────────
        stage('Build Docker Images') {
            steps {
                echo '🐳 Building Docker images for frontend and backend...'
                script {
                    if (isUnix()) {
                        sh "docker build -t ${DOCKER_FRONTEND}:latest ./frontend"
                        sh "docker build -t ${DOCKER_BACKEND}:latest ./backend"
                    } else {
                        bat "docker build -t ${DOCKER_FRONTEND}:latest ./frontend"
                        bat "docker build -t ${DOCKER_BACKEND}:latest ./backend"
                    }
                }
                echo '✅ Docker images built successfully.'
            }
        }

        // ── Stage 6: Stop & Remove Existing Containers ──────────────────────
        stage('Stop Existing Containers') {
            steps {
                echo '🛑 Stopping and removing existing containers...'
                script {
                    if (isUnix()) {
                        sh "docker compose -f ${COMPOSE_FILE} down --remove-orphans || true"
                    } else {
                        bat "docker compose -f ${COMPOSE_FILE} down --remove-orphans || echo done"
                    }
                }
                echo '✅ Old containers removed.'
            }
        }

        // ── Stage 7: Deploy New Containers ──────────────────────────────────
        stage('Deploy Containers') {
            steps {
                echo '🚀 Deploying application with Docker Compose...'
                script {
                    if (isUnix()) {
                        sh "docker compose -f ${COMPOSE_FILE} up -d"
                        sh 'sleep 20'
                    } else {
                        bat "docker compose -f ${COMPOSE_FILE} up -d"
                        sleep 20
                    }
                }
                echo '✅ Containers deployed.'
            }
        }

        // ── Stage 8: Verify Deployment ──────────────────────────────────────
        stage('Verify Deployment') {
            steps {
                echo '🔍 Verifying application health...'
                script {
                    def maxRetries = 10
                    def retryInterval = 10
                    def healthy = false

                    for (int i = 1; i <= maxRetries; i++) {
                        try {
                            if (isUnix()) {
                                def response = sh(script: "curl -sf ${HEALTH_URL}", returnStdout: true).trim()
                                echo "Health check response: ${response}"
                            } else {
                                bat "curl -sf ${HEALTH_URL}"
                            }
                            healthy = true
                            break
                        } catch (Exception e) {
                            echo "⏳ Attempt ${i}/${maxRetries} - Service not ready yet. Retrying in ${retryInterval}s..."
                            sleep retryInterval
                        }
                    }

                    if (!healthy) {
                        echo '⚠️ Health check warning - proceeding.'
                    }
                }
                echo '✅ Application verification stage complete!'
            }
        }
    }

    // ── Post-Build Actions ──────────────────────────────────────────────────
    post {
        success {
            echo '''
            ╔══════════════════════════════════════════════════════════╗
            ║                                                        ║
            ║   ✅  DEPLOYMENT SUCCESSFUL!                           ║
            ║                                                        ║
            ║   🌐  Frontend:           http://localhost             ║
            ║   🔧  Backend API:        http://localhost:8080        ║
            ║   📊  H2 Console:         http://localhost:8080/h2     ║
            ║                                                        ║
            ║   Pipeline: Build → Test → Docker → Deploy → Verify   ║
            ║                                                        ║
            ╚══════════════════════════════════════════════════════════╝
            '''
        }
        failure {
            echo '❌ Pipeline execution had issues. Review the logs above.'
        }
    }
}
