dependencies {
    implementation(project(":projectlibre_contrib"))
    testImplementation(platform("org.junit:junit-bom:5.10.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

sourceSets {
    named("test") {
        java.setSrcDirs(listOf("src/test/java"))
        resources.setSrcDirs(listOf("src/test/resources"))
    }
}

tasks.test {
    enabled = true
    useJUnitPlatform()
}

sourceSets {
    main {
        resources.srcDir(rootProject.file("projectlibre_build/src"))
    }
}

tasks.processResources {
    filesMatching("com/projectlibre1/version/version.properties") {
        expand("version" to rootProject.version.toString())
    }
}
