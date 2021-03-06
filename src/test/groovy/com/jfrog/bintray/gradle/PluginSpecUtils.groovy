package com.jfrog.bintray.gradle

import com.jfrog.bintray.client.api.handle.Bintray
import com.jfrog.bintray.client.impl.BintrayClient

/**
 * Created by user on 20/11/2014.
 */
class PluginSpecUtils {
    private static Bintray bintrayClient
    private static def config = TestsConfig.getInstance().config
    public static final OS_NAME = System.getProperty("os.name")

    def static getGradleCommandPath() {
        def windows = OS_NAME.contains("Windows")
        if (System.getenv("GRADLE_HOME")) {
            def ext = windows ? ".bat" : ""
            return System.getenv("GRADLE_HOME") + File.separator + "bin" + File.separator + "gradle" + ext
        }
        return windows ? "gradlew.bat" : "./gradlew"
    }

    private static def getGradleProjectDir() {
        def resource = getClass().getResource("/gradle")
        new File(resource.toURI())
    }

    private static def getGradleProjectFile() {
        def resource = getClass().getResource("/gradle/build.gradle")
        new File(resource.toURI())
    }

    def static GradleLauncher createGradleLauncher() {
        File projectFile = getGradleProjectFile()
        GradleLauncher launcher = new GradleLauncher(
                getGradleCommandPath(), projectFile.getCanonicalPath())
                .addTask("clean")
                .addTask("bintrayUpload")
                .addEnvVar("bintrayUser", config.bintrayUser)
                .addEnvVar("bintrayKey", config.bintrayKey)
                .addEnvVar("repoName", config.repo)
                .addEnvVar("pkgName", config.pkgName)
                .addEnvVar("pkgDesc", config.pkgDesc)
                .addEnvVar("versionName", config.versionName)
                .addSwitch("stacktrace")

        config.pkgLabels.eachWithIndex { label, index ->
            launcher.addEnvVar("label${index+1}", label)
        }
        launcher
    }

    def static launchGradle(String testMethodName) {
        def testFileName = testMethodName.replaceAll(" ", "_")
        createGradleLauncher().addEnvVar("testName", testFileName).launch()
    }

    def static getBintrayClient() {
        if (bintrayClient == null) {
            bintrayClient = BintrayClient.create(config.url as String,
                    config.bintrayUser as String, config.bintrayKey as String)
        }
        bintrayClient
    }
}
