plugins {
    `java-library`
}

group = "com.parthiyer"
version = "0.0.1"

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation(files("libs/kyberJCE-3.0.0.jar"));
    implementation("com.github.aelstad:keccakj:1.1.0");
}

tasks.withType<Jar> {
    archiveBaseName.set("netXchange")
    archiveVersion.set(project.version.toString())
    archiveClassifier.set("")

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    manifest {
        from("src/main/java/META-INF/MANIFEST.MF")
    }

    // If you have dependencies that need to be included in the JAR
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) }) {
        exclude("META-INF/*.SF")
        exclude("META-INF/*.RSA")
    }
}

tasks.register<Jar>("testJar") {
    archiveClassifier.set("test")
    from(sourceSets.test.get().output)

    manifest {
        attributes["Main-Class"] = "main.Main"
    }

    // Include test runtime dependencies in the test jar
    from(configurations.testRuntimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) }) {
        exclude("META-INF/*.SF")
        exclude("META-INF/*.RSA")
    }
}

tasks.build {
    dependsOn("testJar")
}
