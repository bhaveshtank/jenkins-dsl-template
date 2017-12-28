/*
This is the dummy execution script
*/
def executeScript='''
#!/bin/bash
echo "Hello-World"
'''
//Your Project URL
def gitUrl='{GIT_REPO_NAME}'

//Git Private credentails
def gitPrivateToken=''
def gitCredentials=''

//Define your jobname here
def jobName = 'my-first-dsl-template'

/*
###################################################
########  Create Build Pipeline ###################
###################################################
*/

buildPipelineView("${jobName}" + '-commit-pipeline') {
  selectedJob(jobName)
  title("${jobName}" + '-commit-pipeline')
  showPipelineDefinitionHeader(true)
  showPipelineParameters(true)
  showPipelineParametersInHeaders(true)
  displayedBuilds(10)
  consoleOutputLinkStyle(OutputStyle.NewWindow)
}

/*
###################################################
########  Pipeline  Monitor     ###################
###################################################
*/

buildMonitorView("${jobName}"+ '-pipeline') {
 description('All jobs for the Django Template CD pipeline')
 jobs {
     name("${jobName}" + '-units-test')
     name("${jobName}"+ '-create-infra')
     name("${jobName}"+ '-integration-test')
     }
}


/*
###################################################
########  Parent Job for All ######################
###################################################
*/
freeStyleJob(jobName) {
	scm{
		git{
			remote{
				url(gitUrl)
				credentials(gitCredentials)
			}
		}	
	}
	steps{
		shell(executeScript)
	}
	publishers{
    downstreamParameterized{
    	trigger("${jobName}"+ '-units-test'){
      	condition('UNSTABLE_OR_BETTER')
      		parameters{
      				sameNode()	
      				currentBuild()
      				}
        		}

      		}
    	}
    }
/*
###################################################
########  Executes the Unit Test Cases ############
###################################################
*/
freeStyleJob("${jobName}" + '-units-test') {
	steps{
		shell('echo this is new job')
		}
	publishers{
    downstreamParameterized{
    	trigger("${jobName}"+ '-create-infra'){
      	condition('UNSTABLE_OR_BETTER')
      		parameters{
      				sameNode()	
      				currentBuild()
      				}
        		}

      		}
    	}
	}
/*
###################################################
########  Executes Ansible Playbook ###############
###################################################
*/
freeStyleJob("${jobName}"+ '-create-infra') {
	scm{
		git{
			remote{
				url(gitUrl)
				credentials(gitCredentials)
			}
		}	
	}
	steps{
		shell(executeScript)
	}
	publishers{
    downstreamParameterized{
    	trigger("${jobName}"+ '-integration-test'){
      	condition('UNSTABLE_OR_BETTER')
      		parameters{
      				sameNode()	
      				currentBuild()
    			}
       		}

      	}
    }
}
/*
###################################################
########  Executes  Integration Test Cases ########
###################################################
*/
freeStyleJob("${jobName}"+ '-integration-test') {
	scm{
		git{
			remote{
				url(gitUrl)
				credentials(gitCredentials)
			}
		}	
	}
	steps{
		shell(executeScript)
	}
}
