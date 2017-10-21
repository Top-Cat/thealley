properties([parameters([string(defaultValue: 'master', description: '', name: 'BRANCH')]), pipelineTriggers([githubPush()])])

node {
    def saneBranch = params.BRANCH.replaceAll("/","_")
    def IMAGE_TAG = "${saneBranch}-b${currentBuild.number}"
    def buildVer = "${IMAGE_TAG}-SNAPSHOT"

    dir("./checkout") {
        stage("Checkout code") {
            checkout([$class: 'GitSCM', branches: [[name: "${params.BRANCH}"]], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'CleanBeforeCheckout']], submoduleCfg: [], userRemoteConfigs: [[credentialsId: '70f45b6a-2fec-4264-955d-4f98e2927ff4', url: 'git@github.com:Top-Cat/thealley.git']]])
        }

        stage("Grade Build") {
            def workspace = pwd()
            sh("DOCKER_HOST=unix:///var/hostrun/docker.sock docker run --rm -i -u `id -u` --volumes-from \$(basename \"\$(cat /proc/1/cpuset)\") java:8-jdk-alpine sh -c 'cd ${workspace} &amp;&amp; ./gradlew -Pversion=${buildVer} --info clean build test'")
            step([$class: "JUnitResultArchiver", testResults: "build/**/TEST-*.xml"])
        }

        stage("Docker Build") {
            def fullImageName = "iamtopcat/thealley:${buildVer}"
            sh("DOCKER_HOST=unix:///var/hostrun/docker.sock docker build -f Dockerfile --build-arg BUILDVER=${buildVer} -t ${fullImageName} .")
            sh("docker push ${fullImageName}")
        }
    }
}
