pipeline {
    agent any
    
    tools {
        jdk 'JDK17'
    }
    
    environment {
        GRADLE_OPTS = '-Dorg.gradle.daemon=false'
        DOCKER_COMPOSE_FILE = 'docker-compose.yml'
    }
    
    stages {
        stage('Checkout') {
            steps {
                echo 'Checking out source code...'
                checkout scm
            }
        }
        
        stage('Build') {
            steps {
                echo 'Building application...'
                sh './gradlew clean build -x test'
            }
        }
        
        stage('Unit Tests') {
            steps {
                echo 'Running unit tests...'
                sh './gradlew test'
            }
            post {
                always {
                    junit '**/build/test-results/test/*.xml'
                    publishHTML([
                        reportDir: 'build/reports/tests/test',
                        reportFiles: 'index.html',
                        reportName: 'Test Report',
                        keepAll: true
                    ])
                }
            }
        }
        
        stage('Architecture Tests') {
            steps {
                echo 'Running architecture tests...'
                sh './gradlew test --tests "*ArchitectureTest"'
            }
        }
        
        stage('Code Quality Analysis') {
            steps {
                echo 'Running code quality checks...'
                // Adicione aqui ferramentas como SonarQube quando necessário
                sh './gradlew check'
            }
        }
        
        stage('Package') {
            steps {
                echo 'Packaging application...'
                sh './gradlew bootJar'
            }
        }
        
        stage('Build Docker Image') {
            when {
                branch 'main'
            }
            steps {
                echo 'Building Docker image...'
                script {
                    def appVersion = sh(script: "grep '^version' build.gradle.kts | cut -d'\"' -f2", returnStdout: true).trim()
                    sh "docker build -t bridal-cover-crm:${appVersion} ."
                    sh "docker tag bridal-cover-crm:${appVersion} bridal-cover-crm:latest"
                }
            }
        }
        
        stage('Deploy to Development') {
            when {
                branch 'develop'
            }
            steps {
                echo 'Deploying to development environment...'
                // Adicione comandos de deploy aqui
            }
        }
        
        stage('Deploy to Production') {
            when {
                branch 'main'
            }
            steps {
                input message: 'Deploy to production?', ok: 'Deploy'
                echo 'Deploying to production environment...'
                // Adicione comandos de deploy aqui
            }
        }
    }
    
    post {
        always {
            echo 'Cleaning up workspace...'
            cleanWs()
        }
        success {
            echo 'Pipeline completed successfully!'
        }
        failure {
            echo 'Pipeline failed!'
            // Adicione notificações (email, Slack, etc.) aqui
        }
    }
}

