pipeline {
    agent any
    stages {
        stage('Descargar de GitHub') {
            steps {
                git 'https://github.com/tu-usuario/tu-repositorio.git'
            }
        }
        stage('Test y SonarQube') {
            steps {
                // Aquí va tu comando de Maven con el token de Sonar
                sh './mvnw clean verify sonar:sonar ...'
            }
        }
        stage('Crear Docker') {
            steps {
                // Construye la imagen nueva
                sh 'docker build -t generador-examenes-img .'
            }
        }
        stage('Desplegar') {
            steps {
                // Borra el viejo y levanta el nuevo
                sh 'docker rm -f generador-examenes-app || true'
                sh 'docker run -d -p 8080:8080 --name generador-examenes-app generador-examenes-img'
            }
        }
    }
}