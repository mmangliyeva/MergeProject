plugins {
    kotlin("jvm") version "2.0.21"
    kotlin("plugin.serialization") version "1.9.10"
    application
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}
application {
    mainClass.set("org.example.MainKt")
}
dependencies {
    testImplementation(kotlin("test"))
    implementation ("com.google.code.gson:gson:2.10.1")

    implementation("org.jetbrains.kotlin:kotlin-stdlib:2.1.0")
}

tasks.test {
    useJUnitPlatform()
}