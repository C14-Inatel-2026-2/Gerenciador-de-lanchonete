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

        // Roda os testes e publica o relatório JUnit e a cobertura JaCoCo
        stage('Testes Backend') {
            steps {
                dir('backend') {
                    sh './mvnw -B -ntp test'
                }
            }
            post {
                always {
                    junit 'backend/target/surefire-reports/*.xml'
                    recordCoverage(tools: [[parser: 'JACOCO', pattern: 'backend/target/site/jacoco/jacoco.xml']])
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

        stage('Frontend Tests and Coverage') {
            steps {
                dir('frontend') {
                    sh 'npm run test:coverage' // Executa os testes e gera o relatório de cobertura
                }
                
                // Lê o LCOV e gera o relatório de cobertura no Jenkins
                recordCoverage(tools: [[
                    parser: 'LCOV',
                    pattern: 'frontend/coverage/lcov.info'
                ]],
                // Faz o Jenkins resolver os caminhos do LCOV a partir da pasta do frontend.
                sourceDirectories: [[path: 'frontend']])
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