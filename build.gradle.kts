plugins {
    java
    antlr
    application
}

group = "com.puzzlang"
version = "1.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

application {
    mainClass.set("com.puzzlang.Main")
}

repositories {
    mavenCentral()
}

dependencies {
    antlr("org.antlr:antlr4:4.13.1")
    implementation("org.antlr:antlr4-runtime:4.13.1")
    implementation("org.ow2.asm:asm:9.7")
    implementation("org.ow2.asm:asm-util:9.7")

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.generateGrammarSource {
    maxHeapSize = "64m"
    arguments  = arguments + listOf("-visitor", "-listener", "-long-messages")
    outputDirectory = file("src/main/generated-antlr/com/puzzlang/parser")
}

sourceSets {
    main {
        java {
            srcDir("src/main/generated-antlr")
        }
    }
}

tasks.compileJava {
    dependsOn(tasks.generateGrammarSource)
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "failed", "skipped")
    }
}