#!groovy

node {
  stage('Prepare') {
    echo 'Get Bonita BPM repository from Subversion'
    git (
      url: "https://github.com/mehdi-bonita/CI"
    )
  
    echo 'Delete previous build output directory (\'target\' directory)'
    dir('target') {
      deleteDir()
    }
 
    echo 'Create new output directory (\'target\' directory) for current build'
    bat "mkdir target"
 
    // Remove Tomcat work folder to avoid issue when we build the UI Designers artifacts (Studio embedded Tomcat is used to run the UI Designer)
    echo 'Delete Tomcat work directory'
    dir("$STUDIO_INSTALL_DIR/workspace/tomcat/work") {
      deleteDir()
    }
  }

  // Run BonitaStudioBuilder to generate all Studio artifacts
  // -repoPath set to the path where we checkout the repository content
  // -outputFolder set to our temporary path that will be used to store the build files (*.bar...)
  // -buildAll to build all Studio artifacts (including: process definition, REST API extension...)
  // -migrate to be able to build a repository created with an older version of the Studio
  // -environment include the Developement configuration of the processes
  // Write the output of the build script to a log file named build.log
  
  stage('Build') {
    bat (
      script: '''
        echo Begin building of Bonita BPM artifacts

        %STUDIO_INSTALL_DIR%\\workspace_api_scripts\\BonitaStudioBuilder.bat -repoPath=.\\ -outputFolder=target -buildAll -migrate -environment=Production -link 2>&1 > build.log

        if exist build.log (
          findstr "Build completed successfully" build.log

          if errorlevel 0 (
              echo Bonita BPM artifacts build successfull!
          ) else (
              echo Bonita BPM artifacts build failed! Return code: $retcode
              echo Deleting generated processes
              rmdir /s target
              exit /B 1;
          )
        ) else (
          echo File build.log does not exist. Should have been created by BonitaStudioBuilder.
        )'''
    )
  }
   
  stage('Deploy') {
      withMaven(
        maven: 'maven-3.3.9',
        mavenSettingsFilePath: 'D:\\dev\\apache-maven-3.2.1\\conf\\settings.xml',
        mavenLocalRepo: 'C:\\Users\\Mehdi\\.m2\\repository') {
        
          // Run the Maven Bonita BPM deploy plugin configured in the deploy-test/pom.xml file. Target server declared in the pom.xml
          // is overriden by the target server we get from the Jenkins build parameter.
          bat "mvn -f deploy-test\\pom.xml -Dserver.url=${TEST_SERVER_URL} bonita-deployer:bonita-deploy"
          
      }
  }
  
  stage('Test') {    
    withMaven(
      maven: 'maven-3.3.9',
      mavenSettingsFilePath: 'D:\\dev\\apache-maven-3.2.1\\conf\\settings.xml',
      mavenLocalRepo: 'C:\\Users\\Mehdi\\.m2\\repository') {
      
        try {
          bat "mvn -f test\\pom.xml -Dserver.url=${TEST_SERVER_URL} clean test"
          junit 'test/target/surefire-reports/*.xml'
        } catch(err) {
          step([$class: 'JUnitResultArchiver', testResults: 'test/target/surefire-reports/*.xml'])
          if (currentBuild.result == 'UNSTABLE')
            currentBuild.result = 'FAILURE'
          throw err
        }
    }
  }
  
  stage('Archive Bonita BPM artifacts') {
    archiveArtifacts artifacts: 'target/*'
  }
}