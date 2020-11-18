import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.10"
    application
}
group = "me.eliastorenstam"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}
dependencies {
    implementation("com.google.zxing:core:3.4.1")
    implementation("com.google.zxing:javase:3.4.1")
    implementation("com.itextpdf:itextpdf:5.5.13.2")
    implementation("org.apache.pdfbox:pdfbox:2.0.21")
    testImplementation(kotlin("test-junit"))
}
tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}
application {
    mainClassName = "MainKt"
}