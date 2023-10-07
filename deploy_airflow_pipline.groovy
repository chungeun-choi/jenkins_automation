pipeline{
    agent any 
    environment {
        PROJECT_NAME = "T-Cloud-Bigdata-Airflow"
        DOCKER_IMAGE = "10.0.2.39:5000/airflow_build_image:latest"
        DOCKER_PATH = "./envfile/docker"

    }

    stages {
        stage("Github Pull") {
            steps {
                git branch: 'main', credentialsId: 'github-airflow', url: 'git@github.com:Gyeongun/T-Cloud-Bigdata-Airflow.git'    
            }
        }
        
        stage("Check files related to docker") {
            steps {
                script {
                    dockerfile_change = sh(script: "git diff HEAD HEAD^ ${DOCKER_PATH}/dockerfile",returnStdout: true)
                    dockercompose_change = sh(script: "git diff HEAD HEAD^ ${DOCKER_PATH}/prod/airflow*.yml",returnStdout: true)
                    requierments_change = sh(script: "git diff HEAD HEAD^ ${DOCKER_PATH}/requirements.txt",returnStdout: true)
                }
            }
        }
        
        stage("Git pull Airflw server") {
            
            steps("git pull Airflow componnet server"){
               sshagent (credentials: ['airflow-ssh']) {
                    script {
                        try{
                            sh "ssh ubuntu@54.180.134.94 -o StrictHostKeyChecking=no \'cd ${PROJECT_NAME} && git pull origin main'"
        
                        }catch (err) {
                            println(err.getMessage())
                            def CHANGE = sh(script: "ssh ubuntu@54.180.134.94 -o StrictHostKeyChecking=no \'git clone git@github.com:Gyeongun/T-Cloud-Bigdata-Airflow.git'", returnStdout: true)
                            }
                    }
               }
            }
            
        }
        stage("Git pull Airflow worker server"){
            steps("git pull Airflow worker server"){
                sshagent (credentials: ['airflow-ssh']) {
                    script {
                        try{
                            sh "ssh ubuntu@13.125.69.10 -o StrictHostKeyChecking=no \'cd ${PROJECT_NAME} && git pull origin main'"
        
                        }catch (err) {
                            println(err.getMessage())
                            def CHANGE = sh(script: "ssh ubuntu@13.125.69.10 -o StrictHostKeyChecking=no \'git clone git@github.com:Gyeongun/T-Cloud-Bigdata-Airflow.git'", returnStdout: true)
                            }
                    }
               }
            }
        }        
        stage('Docker image build') {
            when {   expression {     requierments_change.length() > 0 || dockerfile_change.length() > 0 || dockercompose_change.length() > 0} }
            steps {
                println("Docker file changes exist")
                sh "docker build -t ${DOCKER_IMAGE} ./envfile/docker/"
                sh "docker push ${DOCKER_IMAGE}"  
            }
              
        }

        stage("Docker compose up"){
            when {   expression {     requierments_change.length() > 0 || dockerfile_change.length() > 0 || dockercompose_change.length() > 0} }
            steps {
                sshagent (credentials: ['airflow-ssh']) {
                  sh "ssh ubuntu@54.180.134.94 -o StrictHostKeyChecking=no \'docker image pull ${DOCKER_IMAGE}'"
                  sh "ssh ubuntu@54.180.134.94 -o StrictHostKeyChecking=no \'cd ${PROJECT_NAME} && docker-compose -f ${DOCKER_PATH}/prod/airflow_component.yml stop && docker-compose -f ${DOCKER_PATH}/prod/airflow_component.yml up -d'"
                  sh "ssh ubuntu@13.125.69.10 -o StrictHostKeyChecking=no \'docker image pull ${DOCKER_IMAGE}'"
                  sh "ssh ubuntu@13.125.69.10 -o StrictHostKeyChecking=no \'cd ${PROJECT_NAME} && docker-compose -f ${DOCKER_PATH}/prod/airflow_worker.yml stop && docker-compose -f ${DOCKER_PATH}/prod/airflow_worker.yml up -d'"
                }
            }
        }

}
}