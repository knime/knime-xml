#!groovy
def BN = (BRANCH_NAME == 'master' || BRANCH_NAME.startsWith('releases/')) ? BRANCH_NAME : 'releases/2021-06'

library "knime-pipeline@$BN"

properties([
    pipelineTriggers([
        upstream('knime-base/' + env.BRANCH_NAME.replaceAll('/', '%2F'))
    ]),
    parameters(workflowTests.getConfigurationsAsParameters()),
    buildDiscarder(logRotator(numToKeepStr: '5')),
    disableConcurrentBuilds()
])

SSHD_IMAGE = "${dockerTools.ECR}/knime/sshd:alpine3.11"

try {
    knimetools.defaultTychoBuild('org.knime.update.xml')

    configs = [
        "Workflowtests" : {
            workflowTests.runTests(
                dependencies: [
            repositories: [ "knime-xml", "knime-streaming", "knime-filehandling", "knime-exttool", "knime-chemistry", "knime-distance", 'knime-kerberos' ]
        ],
         sidecarContainers: [
            [ image: SSHD_IMAGE, namePrefix: "SSHD", port: 22 ]
        ]
            )
        },
        "Filehandlingtests" : {
            workflowTests.runFilehandlingTests (
                dependencies: [
                    repositories: [
                        'knime-xml'
                    ]
                ],
            )
        }
    ]

    parallel configs

    stage('Sonarqube analysis') {
        env.lastStage = env.STAGE_NAME
        workflowTests.runSonar()
    }
} catch (ex) {
    currentBuild.result = 'FAILURE'
    throw ex
} finally {
    notifications.notifyBuild(currentBuild.result)
}
/* vim: set shiftwidth=4 expandtab smarttab: */
