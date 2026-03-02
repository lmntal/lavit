plugins {
    id("java")
    id("idea")
    id("application")
}

fun extractVersion(s: String): String {
    // version format: versionX.Y.Z
    val m = Regex("version(\\d+\\.\\d+\\.\\d+)").findAll(s).toList()
    // get first match
    if (m.isEmpty()) return "0.0.0"
    return m[0].groups[1]!!.value
}

allprojects {

    group = "jp.ac.waseda.info.ueda"
    version = extractVersion(projectDir.resolve("version.txt").readText())

    repositories {
        mavenCentral()
    }
}

java.sourceSets {
    getByName("main").java.srcDirs("src", "gen")
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

tasks.register<Copy>("copyResource") {
    from("gen/lavit/localizedtext/res")
    into("build/classes/java/main/lavit/localizedtext/res")
}

// for running from IDE directly
tasks.named("compileJava").configure {
    dependsOn("copyResource")
}

// Distribution
// TODO: archive structure

tasks.distTar {
    archiveBaseName.set("LaViT")
    archiveVersion.set(version.toString())

    from(".") {
        include("demo/**")
        include("run.*")
        include("version.txt")
    }
}

tasks.distZip {
    archiveBaseName.set("LaViT")
    archiveVersion.set(version.toString())

    from(".") {
        include("demo/**")
        include("run.*")
        include("version.txt")
    }
}

// Configuration for generated jar

tasks.jar {
    archiveBaseName.set("LaViT")
    version = "" // remove version from jar name

    from(".") {
        include("img/**")
        include("lang/**")
    }

    from("src") {
        include("**/*.txt")
    }

    into("extgui") {
        from("src/extgui") {
            include("**/*.png")
        }
    }

    manifest {
        attributes(
            "Main-Class" to "lavit.runner.LaViT",
        )
    }
}