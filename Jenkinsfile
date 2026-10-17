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
    }

    post {
        success { echo 'Pipeline concluída com sucesso.' }
        failure { echo 'Pipeline falhou — verifique o stage em vermelho.' }
    }
}