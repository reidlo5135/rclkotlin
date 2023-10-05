plugins {
    kotlin("jvm") version "1.9.0"
}

group = "net.wavem.rclkotlin"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation ("us.ihmc:ros2-msg-to-pubsub-generator:0.23.1")
    implementation ("us.ihmc:ros2-common-interfaces:0.23.1")
    implementation ("us.ihmc:ihmc-ros2-library:0.23.1")
    implementation ("io.github.lambdaprime:jros2client:1.0")
    implementation ("io.github.pinorobotics:rtpstalk:4.0")
    implementation ("io.reactivex:rxjava:1.3.8")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}