pipeline {
  agent any
  triggers{
    bitbucketPush()
  }
  tools {
    jdk "jdk-8"
  }
  stages {
    stage('Build') {
      steps {
        sh 'mvn -B clean install'
      }
    }
    stage('Deploy') {
      steps {
        withMaven() {
          sh 'mvn -B deploy'
        }
      }
    }
  }
}