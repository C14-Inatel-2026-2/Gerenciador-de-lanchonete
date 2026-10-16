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
    }

    post {
        success { echo 'Pipeline concluída com sucesso.' }
        failure { echo 'Pipeline falhou — verifique o stage em vermelho.' }
    }
}