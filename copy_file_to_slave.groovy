node('master') {
    echo "${AKAMAI_CDN_PURGE_URI_FILE}"
    def cdn_purge_file = "${WORKSPACE}/${AKAMAI_CDN_PURGE_URI_FILE}"
    sh 'mv ${WORKSPACE}/${AKAMAI_CDN_PURGE_URI_FILE} ${WORKSPACE}/tmp/akamai_purge_uris.txt'
    dir("${WORKSPACE}/tmp") {
          stash 'master-file'
    }
}
node('slave_node_lable'){
    dir('/data/jenkins/tmp') {
          unstash 'master-file'
    }
    sh 'salt-call state.sls projects/projct_name/akamai_purge -t 1200 -l info saltenv=prd pillarenv=prd'
}
node('master') {
    sh 'mv ${WORKSPACE}/tmp/akamai_purge_uris.txt ${WORKSPACE}/done/$(date +%Y-%m-%d-%H-%M-%S).txt'
    sh 'rm /home/data/wars/${JOB_NAME}/${AKAMAI_CDN_PURGE_URI_FILE}'
}
