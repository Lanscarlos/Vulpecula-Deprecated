plugins {
    java
    id("io.izzel.taboolib") version "1.34"
    id("org.jetbrains.kotlin.jvm") version "1.5.10"
}

taboolib {
    install("common")
    install("common-5")
    install("module-lang")
    install("module-kether")
    install("module-database")
    install("module-configuration")
    install("module-chat")
    install("platform-bukkit")
    install("expansion-javascript")
    classifier = null
    version = "6.0.7-26"
}

repositories {
    maven { url = uri("https://repo.tabooproject.org/repository/releases/") }
    mavenCentral()
}

dependencies {
    implementation("org.tabooproject.reflex:analyser:1.0.6")
    implementation("org.tabooproject.reflex:reflex:1.0.6") // 需要 analyser 模块

    compileOnly("ink.ptms:nms-all:1.0.0")
    compileOnly("ink.ptms.core:v11701:11701:mapped")
    compileOnly("ink.ptms.core:v11701:11701:universal")
    compileOnly(kotlin("stdlib"))
    compileOnly(fileTree("libs"))
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}