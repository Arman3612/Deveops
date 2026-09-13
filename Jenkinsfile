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
                // If repo is public, remove 'credentialsId'. If private, configure
                // 'github-credentials' in Jenkins → Manage Credentials first.
                git branch: 'main',
                    url: 'https://github.com/Arman3612/Deveops.git'
                    // credentialsId: 'github-credentials'
                echo "✅ Source code cloned successfully."
            }
        }

        // ── Stage 2: Build Frontend (Vite + React) ──────────────────────────
        stage('Build Frontend') {
            steps {
                echo '🔨 Installing frontend dependencies and building...'
                dir('frontend') {
                    sh 'npm ci'
                    sh 'npm run build'
                }
                echo '✅ Frontend build completed.'
            }
        }

        // ── Stage 3: Build Backend (Spring Boot + Maven) ────────────────────
        stage('Build Backend') {
            steps {
                echo '🔨 Building Spring Boot backend...'
                dir('backend') {
                    sh 'mvn clean package -DskipTests'
                }
                echo '✅ Backend build completed.'
            }
        }

        // ── Stage 4: Run Backend Tests ──────────────────────────────────────
        stage('Test Backend') {
            steps {
                echo '🧪 Running backend unit tests...'
                dir('backend') {
                    sh 'mvn test'
                }
                echo '✅ All tests passed.'
            }
        }

        // ── Stage 5: Build Docker Images ────────────────────────────────────
        stage('Build Docker Images') {
            steps {
                echo '🐳 Building Docker images for frontend and backend...'
                sh "docker build -t ${DOCKER_FRONTEND}:latest ./frontend"
                sh "docker build -t ${DOCKER_BACKEND}:latest ./backend"
                echo '✅ Docker images built successfully.'
            }
        }

        // ── Stage 6: Stop & Remove Existing Containers ──────────────────────
        stage('Stop Existing Containers') {
            steps {
                echo '🛑 Stopping and removing existing containers...'
                sh "docker-compose -f ${COMPOSE_FILE} down --remove-orphans || true"
                echo '✅ Old containers removed.'
            }
        }

        // ── Stage 7: Deploy New Containers ──────────────────────────────────
        stage('Deploy Containers') {
            steps {
                echo '🚀 Deploying application with Docker Compose...'
                sh "docker-compose -f ${COMPOSE_FILE} up -d"
                echo '⏳ Waiting for services to initialize...'
                sh 'sleep 30'
                echo '✅ Containers deployed.'
            }
        }

        // ── Stage 8: Verify Deployment ──────────────────────────────────────
        stage('Verify Deployment') {
            steps {
                echo '🔍 Verifying application health...'
                script {
                    def maxRetries = 10
                    def retryInterval = 10 // seconds
                    def healthy = false

                    for (int i = 1; i <= maxRetries; i++) {
                        try {
                            def response = sh(
                                script: "curl -sf ${HEALTH_URL}",
                                returnStdout: true
                            ).trim()
                            echo "Health check response: ${response}"
                            healthy = true
                            break
                        } catch (Exception e) {
                            echo "⏳ Attempt ${i}/${maxRetries} - Service not ready yet. Retrying in ${retryInterval}s..."
                            sleep retryInterval
                        }
                    }

                    if (!healthy) {
                        error '❌ Deployment verification failed! Application did not become healthy in time.'
                    }
                }
                echo '✅ Application is healthy and responding!'
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
            ║   🐰  RabbitMQ Mgmt:      http://localhost:15672      ║
            ║                                                        ║
            ║   Pipeline: Build → Test → Docker → Deploy → Verify   ║
            ║                                                        ║
            ╚══════════════════════════════════════════════════════════╝
            '''
        }
        failure {
            echo '❌ Pipeline failed! Check the logs above for details.'
            // Clean up on failure to avoid leaving broken containers
            sh "docker-compose -f ${COMPOSE_FILE} down || true"
        }
        always {
            echo "🧹 Pipeline finished. Cleaning workspace..."
            cleanWs()
        }
    }
}
