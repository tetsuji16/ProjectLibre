dependencies {
    implementation(project(":projectlibre_contrib"))
    implementation(project(":projectlibre_core"))
    implementation(libs.mpxj)
    testImplementation("junit:junit:4.13.2")
}

tasks.test {
    enabled = true
    systemProperty("java.awt.headless", "true")
    // POD restore walks task and dependency graphs recursively. Do not depend
    // on the platform-specific default stack size of the test worker.
    jvmArgs("-Xss2m")
}
