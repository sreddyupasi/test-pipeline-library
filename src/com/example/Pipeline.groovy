#!/usr/bin/env groovy
package com.example

class Pipeline {
    def script
    def configurationFile
    def buildKind
    def databaseKind
    def deployKind
    def testList
    def notifyKind

    Pipeline(script, configurationFile) {
        this.script = script
        this.configurationFile = configurationFile
    }

    def sendEmail(String recipients){
        emailext subject: "STARTED: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]'",
                 body: """<p>STARTED: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]':</p>
                         <p>Check console output at &QUOT;<a href='${env.BUILD_URL}'>${env.JOB_NAME} [${env.BUILD_NUMBER}]</a>&QUOT;</p>""",
                 to: recipients
    }

    def notifyBuild(){
      if (notifyKind[on_start] == "always" || notifyKind[on_failure] == "always" || notifyKind[on_success] == "always"){
            sendEmail(notifyKind[recipients])
        }
    }

    def codeBuild(){
        try{
          sh 'cd ${buildKind[projectFolder]} && ${buildKind[buildCommand]}'
        } catch (err){
            sh "echo 'Build step error:$err'"
            currentBuild.result = "FAILED"
        }
    }

    def codeDBConfig(){
        try{
            sh 'cd ${databaseKind[databaseFolder]} && ${databaseKind[databaseCommand]}'
        } catch (err){
            sh "echo 'Database step error:$err'"
            currentBuild.result = "FAILED"
        }
    }

    def codeDeploy(){
        try{
            sh '${deployKind[deployCommand]}'
        } catch (err){
            sh "echo 'Deploy step error:$err'"
            currentBuild.result = "FAILED"
        }
    }

    def codeTest(){
        try{
            def parallelTasks = [:]
            for(int i=0; i<testList.size; i++){
                def task = testList.size[i]
                parallelTasks["Execute_${task[name]}"] = {
                  sh 'cd ${task[testFolder]} && ${task[testCommand]}'
                }
            }
            parallel parallelTasks
        } catch (err){
            sh "echo 'Test step parallel exception error:$err'"
            currentBuild.result = "FAILED"
        }
    }

    def execute() {
      echo "Execute method called"
//    ===================== Your Code Starts Here =====================
//    Note : use "script" to access objects from jenkins pipeline run (WorkflowScript passed from Jenkinsfile)
//           for example: script.node(), script.stage() etc

//    ===================== Parse configuration file ==================
//         def yamlTask = readYaml text: """
// notifications:
//   email:
//     recipients: "my@box.com"
//     on_start: "never"
//     on_failure: "always"
//     on_success: "always"
// #Build configuration
// build:
//   projectFolder: 'project'
//   buildCommand: "mvn clean test"
// #Database configuration
// database:
//   databaseFolder: 'database'
//   databaseCommand: "mvn clean test -Dscope=FlywayMigration"
// #Deploy configuration
// deploy:
//   deployCommand: "mvn clean install"
// #Test configuration (should be run in parallel)
// test:
// - testFolder: "test"
//   name: "performance"
//   testCommand: "mvn clean test -Dscope=performance"
// - testFolder: "test"
//   name: "regression"
//   testCommand: "mvn clean test -Dscope=regression; exit 1"
// - testFolder: "test"
//   name: "integration"
//   testCommand: "mvn clean test -Dscope=integration"
// """
        // node (){
          try{
            def yamlTask = readYaml file: configurationFile
            sh "echo 'yaml task parsed object: $yamlTask'"
            buildKind = yamlTask.build
            databaseKind = yamlTask.database
            deployKind  = yamlTask.deploy
            testList  = yamlTask.test
            notifyKind = yamlTask.notifications.email

            // codeBuild(buildKind)
            // codeDBConfig(databaseKind)
            // codeDeploy(deployKind)
            // codeTest(testList)
          } catch (err){
            echo "Pipeline Error"
            currentBuild.result = "FAILED"
          }
        // }

//    ===================== Run pipeline stages =======================
        // node{
        //       stage("build"){
        //           try{
        //             sh 'cd ${buildKind.projectFolder} && ${buildKind.buildCommand}'
        //           } catch (err){
        //               echo "Build step error:$err.message()"
        //               currentBuild.result = "FAILED"
        //           }
        //       }
        //       stage("database"){
        //           try{
        //               sh 'cd ${databaseKind.databaseFolder} && ${databaseKind.databaseCommand}'
        //           } catch (err){
        //               echo "Database step error:$err.message()"
        //               currentBuild.result = "FAILED"
        //           }
        //       }
        //       stage("deploy"){
        //           try{
        //               sh '${deployKind.deployCommand}'
        //           } catch (err){
        //               echo "Deploy step error:$err.message()"
        //               currentBuild.result = "FAILED"
        //           }
        //       }
        //       stage("test"){
        //           try{
        //               def parallelTasks = [:]
        //               for(int i=0; i<testList.size; i++){
        //                   def task = testList.size[i]
        //                   parallelTasks["Execute_${task.name}"] = {
        //                     sh 'cd ${task.testFolder} && ${task.testCommand}'
        //                   }
        //               }
        //               parallel parallelTasks
        //           } catch (err){
        //               echo "Test step parallel exception error:$err.message()"
        //               currentBuild.result = "FAILED"
        //           }
        //       }
        //   } catch (err){
        //       echo "Pipeline Error"
        //       currentBuild.result = "FAILED"
        //   } finally {
        //       if (notifyKind.on_start == "always" || notifyKind.on_failure == "always" || notifyKind.on_success == "always"){
        //           notifyBuild(notifyKind.recipients)
        //       }
        //   }
        // }
//    ===================== End pipeline ==============================
    }
}