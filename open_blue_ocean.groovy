/* /shared/tools */

environment {
    ENVIRONMENT = ''
    PROJECT_PATH = ''
    SERVER_TO_RESTORE = ''
}

def getFiles(String env) {
  def path = "/shared/tools" + env + "/product_name"
 
  final foundFiles = sh(script: "ls -1 ${path}", returnStdout: true).split()
  return foundFiles.join('\n')
}

node('master') {
    script {
        def ENVIRONMENT = input(id: 'ENVIRONMENT', message: 'Product Environment', ok: 'Next',
            parameters: [
            choice(name: 'ENVIRONMENT', choices: ['PRD', 'PPD', 'INT','QAL'].join('\n'), description: 'Please select the source environment to restore')])
    
        def PROJECT_PATH = input(id: 'PROJECT_PATH',message: 'backup directories', ok: 'Next',  
            parameters: [
            choice(name: 'PROJECT_BACKUP', choices: getFiles("${ENVIRONMENT}"), description: 'Please select the ProjectName backup directory to restore on ppd environment')])
            
        def SERVER_TO_RESTORE = input(id: 'SERVER_TO_RESTORE', message: 'Please provide PPD ProjectName Server to restore', ok: 'Submit',
            parameters: [
            choice(name: 'SERVER_TO_RESTORE', choices: ['ALL','server_1','server_2'].join('\n'), description: 'Please provide server to restore')])    
        
        env.PROJECT_PATH = PROJECT_PATH
        env.ENVIRONMENT = ENVIRONMENT
        if(SERVER_TO_RESTORE == 'ALL') {
            SERVER_TO_RESTORE = 'server_1,server_2'
        }
        env.SERVER_TO_RESTORE = SERVER_TO_RESTORE
    }   
    
    echo "${PROJECT_PATH}"
    echo "${ENVIRONMENT}"
        
}    
node("server_node_name") {
    
    echo "Environment: ${env.ENVIRONMENT}"
    echo "PROJECT_PATH : ${env.PROJECT_PATH}"
    echo "SERVER_TO_RESTORE : ${env.SERVER_TO_RESTORE}"
    
    def APP_ENV_NAME = "CLI" + "${env.ENVIRONMENT}"
    def backup_to_restore = "${env.PROJECT_PATH}"
    
    def pillar='\\{\\"upstream\\"\\:\\"' + "${APP_ENV_NAME}" + '\\"\\,\\"backup_to_restore\\"\\:\\"' + "${env.PROJECT_PATH}" + '\\"\\}'
    sh """ salt -L """ + '"' + """ $env.SERVER_TO_RESTORE """ + '"' + """ state.sls project.restore pillar=$pillar -l debug saltenv=ppd pillarenv=ppd -t 360 test=True """
}
