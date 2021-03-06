pipeline {
    agent any
    environment {
        app = null
        registery = 'https://keremyaldiz.com:5000'
    }
    stages {
        stage('Test') { 
            agent {
	        docker {
	            image 'maven:3-jdk-8-alpine' 
	        }
            }
            steps {
	        sh 'mvn clean surefire-report:report'
	    }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }
        stage('Package') {
            agent {
	        docker {
	            image 'maven:3-jdk-8-alpine'
	        }
            }
            steps {
	        sh 'mvn package -Dmaven.test.skip=true'
	    }
            post {
                success {
                    stash(name: "jar", includes: 'target/*.jar')
		    archiveArtifacts 'target/*.jar'
                }
            }
        }
        stage("Static Code Analysis") {
            agent any
            environment {
                scannerHome = tool 'SonarQube Scanner'
            }
            steps {
                withSonarQubeEnv('sonarqube.keremyaldiz.com') {
                    sh '${scannerHome}/bin/sonar-scanner -Dsonar.projectKey=computer-security -Dsonar.sources=. -Dsonar.java.binaries=target -Dsonar.projectName=computer-security -Dsonar.projectVersion=${BUILD_NUMBER}'
                }
            }
        }
	stage('Build Docker Image') {
	    steps {
		script {
                    unstash("jar")
		    app = docker.build 'computer-security'
		}
	    }
	}
	stage('Push Docker Image') {
	    steps {
		script {
		    docker.withRegistry(registery) {
			app.push '${BUILD_NUMBER}'
			app.push 'latest'
		    }
		}
	    }
	}
        stage('Deployment') {
            agent any
            steps {
	        sh 'docker rm -f computer-security || true; docker run -d --name=computer-security -p 8000:8080 keremyaldiz.com:5000/computer-security'
	    }
        }
        stage('Dynamic Code Analysis') {
            agent any
	    options {
      	    	timeout(time: 2, unit: 'MINUTES') 
            }
            steps {
	        sh 'docker run --rm -u root -v /var/lib/docker/volumes/jenkins/_data/$(pwd | sed \'s-/var/jenkins_home--\'):/zap/wrk/ -t owasp/zap2docker-stable zap-baseline.py -t http://keremyaldiz.com:8000 -r dynamic.html || true'
	    }
            post {
                success {
                    archiveArtifacts 'dynamic.html'
                }
		cleanup {
		    deleteDir()
                }
            }
        }
    }
    post {
	success {
	    emailext (
		      subject: "SUCCESSFUL: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]'",
		      to: "contact@keremyaldiz.com",
		      body: """<p>SUCCESSFUL: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]':</p>
                      <p>Check console output at &QUOT;<a href='${env.BUILD_URL}'>${env.JOB_NAME} [${env.BUILD_NUMBER}]</a>&QUOT;</p>"""
		      )
	}
	failure {
	    emailext (
		      subject: "FAILED: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]'",
		      to: "contact@keremyaldiz.com",
		      body: """<p>FAILED: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]':</p>
                      <p>Check console output at &QUOT;<a href='${env.BUILD_URL}'>${env.JOB_NAME} [${env.BUILD_NUMBER}]</a>&QUOT;</p>"""
		      )
	    sh 'curl -H "Content-Type: application/json" -X POST --data \'{"issue":{"project_id": 2,"subject":' + "\"${env.JOB_NAME} [${env.BUILD_NUMBER}]\"" + '}}\' -H "X-Redmine-API-Key: 691aea146ccfbdd24420aa7a1e981c1a864886fa" https://management.keremyaldiz.com/issues.json'
	}
	cleanup {
	    deleteDir()
        }
    }
}
