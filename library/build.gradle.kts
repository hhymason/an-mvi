import com.mason.logic.config.Apps

plugins {
    id("mason.lib")
    id("maven-publish")
}
android {
    namespace = Apps.LIB_ID

}
dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.navigation.ui)
    implementation(libs.androidx.navigation.fragment)

    implementation(libs.kotlin.coroutine)
    implementation(libs.kotlin.stdlib)
    implementation(libs.mason.util)
}
tasks.register<Jar>("androidSourcesJar") {
    from(android.sourceSets["main"].java.srcDirs)
    archiveClassifier.set("sources")
}
publishing {
    publications {
        create<MavenPublication>("releaseAar") {
            version = Apps.VERSION_NAME
            groupId = Apps.GROUP_ID
            artifactId = Apps.ARTIFACT_ID

            artifact(tasks["androidSourcesJar"])
        }
        // Creates a Maven publication called “snapshot”.
        create<MavenPublication>("snapshotAar") {
            // Applies the component for the snapshot build variant.
            groupId = Apps.GROUP_ID
            artifactId = Apps.ARTIFACT_ID
            version = "${Apps.VERSION_NAME}-SNAPSHOT"

            artifact(tasks["androidSourcesJar"])
        }
    }
}
