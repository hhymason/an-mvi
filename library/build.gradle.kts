plugins {
    id("mason.lib")
}
android {
    namespace = "com.mason.mvi"
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