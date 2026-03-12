pipeline {
    agent any
    stages {
        stage('Preparar Entorno') {
            steps {

                sh 'chmod +x mvnw'
            }
        }
        stage('Test y SonarQube') {
            steps {

                sh './mvnw clean verify sonar:sonar -Dsonar.projectKey=generador-examenes-back -Dsonar.projectName=generador-examenes-back -Dsonar.host.url=http://sprint3-sonarqube:9000 -Dsonar.token=squ_1f0ffbe31c6276884d2a7db4c097ec8116026f86'
            }
        }
        stage('Construir Imagen Docker') {
            steps {

                sh 'docker build -t generador-examenes-img .'
            }
        }
        stage('Desplegar Aplicación') {
            steps {

                sh 'docker rm -f app-backend || true'
                sh 'docker run -d -p 8080:8080 --name app-backend generador-examenes-img'
            }
        }
    }
}