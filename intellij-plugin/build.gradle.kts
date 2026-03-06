plugins {
    id("java")
    id("org.jetbrains.intellij.platform") version "2.2.1"
}

group = "com.puzzlang"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

dependencies {
    intellijPlatform {
        intellijIdeaCommunity("2024.1")
    }
}

intellijPlatform {
    pluginConfiguration {
        id = "com.puzzlang.intellij"
        name = "PuzzLang"
        version = project.version.toString()
        description = """
            Language support for PuzzLang (.puzz files).
            
            PuzzLang is a domain-specific language for solving coding puzzles 
            and challenges like Advent of Code.
            
            Features:
            - File type recognition for .puzz files
            - Custom file icon
            - Syntax highlighting (coming soon)
        """.trimIndent()
        
        ideaVersion {
            sinceBuild = "241"
            untilBuild = "253.*"
        }
        
        vendor {
            name = "PuzzLang"
            url = "https://github.com/puzzlang/puzz-lang"
        }
    }
}

tasks {
    buildSearchableOptions {
        enabled = false
    }
}
