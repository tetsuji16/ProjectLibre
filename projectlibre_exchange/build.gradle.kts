dependencies {
    implementation(project(":projectlibre_contrib"))
    implementation(project(":projectlibre_core"))
    testImplementation("junit:junit:4.13.2")
}

sourceSets {
    named("test") {
        java.setSrcDirs(listOf("src/test"))
        resources.setSrcDirs(emptyList<String>())
    }
}

tasks.test {
    enabled = true
    systemProperty("java.awt.headless", "true")
}
