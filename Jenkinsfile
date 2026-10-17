pipeline {
    agent any

    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }

    stages {
        // Compila o backend antes de qualquer outra etapa (fail fast)
        stage('Build Backend') {
            steps {
                dir('backend') {
                    sh './mvnw -B -ntp clean compile'
                }
            }
        }

        stage('Frontend Lint') {
            steps {
                dir('frontend') {
                    sh 'node --version'
                    sh 'npm --version'
                    sh 'npm ci'
                    sh 'npm run lint'
                }
            }
        }

        stage('Frontend Tests') {
            steps {
                dir('frontend') {
                    sh 'npm run test:run'
                }
            }
        }

        stage('Frontend Build') {
            steps {
                dir('frontend') {
                    sh 'npm run build'
                }
            }
        }
    }

    post {
        success { echo 'Pipeline concluída com sucesso.' }
        failure { echo 'Pipeline falhou — verifique o stage em vermelho.' }
    }
}