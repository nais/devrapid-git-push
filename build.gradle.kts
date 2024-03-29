import org.cyclonedx.gradle.CycloneDxTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.google.protobuf.gradle.*

plugins {
    kotlin("jvm") version ("1.9.10")
    kotlin("plugin.serialization") version "1.9.10"
    id("com.google.protobuf") version "0.9.4"
    id("org.cyclonedx.bom") version "1.7.4"
    application
}

group = "io.nais"
version = "generatedlater"

apply(plugin = "com.google.protobuf")


repositories {
    maven("https://jitpack.io")
    maven("https://packages.confluent.io/maven/")
    mavenCentral()
}

configurations {
    this.all {
        exclude(group = "ch.qos.logback")
    }
}

val junitVersion = "5.10.0"
val ktorVersion = "2.3.4"
val log4jVersion = "2.20.0"
val assertJVersion = "3.24.2"
val prometheusVersion = "0.16.0"
val micrometerVersion = "1.11.4"
val serializerVersion = "0.20.0"
val protobufVersion = "3.24.3"



dependencies {
    implementation(kotlin("stdlib-jdk8:1.9.0"))
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.9.10")
    implementation("com.natpryce:konfig:1.6.10.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.7.3")
    implementation("org.apache.logging.log4j:log4j-api:$log4jVersion")
    implementation("org.apache.logging.log4j:log4j-core:$log4jVersion")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:$log4jVersion")
    implementation("com.vlkan.log4j2:log4j2-logstash-layout-fatjar:1.0.5")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serializerVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:$serializerVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime-common:$serializerVersion")
    implementation("io.prometheus:simpleclient:$prometheusVersion")
    implementation("io.micrometer:micrometer-registry-prometheus:$micrometerVersion")
    implementation("commons-codec:commons-codec:1.16.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.2")
    implementation("org.apache.kafka:kafka-clients:7.5.0-ce")
    implementation("io.confluent:kafka-protobuf-serializer:7.5.0")
    api("com.google.protobuf:protobuf-java:$protobufVersion")
    implementation("io.ktor:ktor-server-default-headers:$ktorVersion")
    implementation("io.ktor:ktor-server-call-logging:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-netty-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-metrics-micrometer-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-auth-jvm:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    protobuf(files("src/main/protobuf/"))
    testImplementation("org.assertj:assertj-core:$assertJVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    testImplementation("org.assertj:assertj-core:$assertJVersion")

}
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks {
    withType<CycloneDxTask> {
        setOutputFormat("json")
        setIncludeLicenseText(false)
    }

    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    withType<Test> {
        useJUnitPlatform()
        testLogging {
            showExceptions = true
            showStackTraces = true
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
            events("passed", "skipped", "failed")
        }
    }

    named<Jar>("jar") {
        archiveFileName.set("app.jar")

        manifest {
            attributes["Main-Class"] = "io.nais.devrapid.AppKt"
            attributes["Class-Path"] = configurations.runtimeClasspath.get().joinToString(separator = " ") {
                it.name
            }
        }

        doLast {
            configurations.runtimeClasspath.get().forEach {
                val file = File("$buildDir/libs/${it.name}")
                if (!file.exists())
                    it.copyTo(file)
            }
        }
    }
}

java {
    val mainJavaSourceSet: SourceDirectorySet = sourceSets.getByName("main").java
    val protoSrcDir = "$buildDir/generated/source/proto/main"
    mainJavaSourceSet.srcDirs("$protoSrcDir/java", "$protoSrcDir/grpc", "$protoSrcDir/grpckotlin")
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:$protobufVersion"
    }
}

sourceSets{
    create("proto"){
        proto {
            srcDir("src/main/protobuf/")
        }
    }
}

application {
    mainClass.set("io.nais.devrapid.App")
}
