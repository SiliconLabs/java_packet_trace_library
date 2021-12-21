def emailBody=""

pipeline
{
    agent { label 'Build-Farm' }
    
    stages
    {
        stage('Git Setup')
        {
            steps
            {
                checkout scm
            }
        }
        stage('gradle test')
        {
            steps
            {
                sh './gradlew test'
            }
        }
        stage('gradle all')
        {
            steps
            {
                sh './gradlew all'
            }
        }
        stage('archive artifacts')
        {
            steps
            {
                archiveArtifacts artifacts: 'silabs-pti/build/libs/*.jar', fingerprint: true
            }
        }
    }
}
