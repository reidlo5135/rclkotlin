plugins {
    kotlin("jvm") version "1.9.0"
}

group = "net.wavem.rclkotlin"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("io.github.lambdaprime:jros2client:1.0")
    implementation("io.github.pinorobotics:rtpstalk:4.0")
    implementation(kotlin("reflect"))
    implementation("io.reactivex:rxjava:1.3.8")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}