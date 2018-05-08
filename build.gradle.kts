import com.github.jengelman.gradle.plugins.shadow.ShadowExtension
import com.github.robfletcher.compass.CompassExtension
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern.compile

configurations.all {
    it.resolutionStrategy.cacheChangingModulesFor(0, TimeUnit.SECONDS)
}

buildscript {
    repositories {
        mavenLocal()
        jcenter()
        maven { setUrl("http://dl.bintray.com/robfletcher/gradle-plugins") }
        maven { setUrl("https://repo.spring.io/release") }
        maven { setUrl("https://repo.spring.io/snapshot") }
        mavenCentral()
    }
    dependencies {
        classpath("com.github.jengelman.gradle.plugins:shadow:1.2.4")
        classpath("com.github.robfletcher:compass-gradle-plugin:2.0.6")
        classpath("io.projectreactor.ipc:reactor-netty:0.7.4.RELEASE")
    }
}

apply {
    plugin("java")
    plugin("com.github.robfletcher.compass")
    plugin("application")
    plugin("com.github.johnrengelman.shadow")
}

group = "io.lettuce"
version = "1.0.0.BUILD-SNAPSHOT"

configure<JavaPluginConvention> {
    setSourceCompatibility(1.8)
    setTargetCompatibility(1.8)
}

configure<ApplicationPluginConvention> {
    mainClassName = "io.lettuce.Application"
}

configure<ShadowExtension> {
    version = null
}

configure<CompassExtension> {
    sassDir = file("$projectDir/src/main/sass")
    cssDir = file("$buildDir/resources/main/static/assets/css")
}

repositories {
    mavenLocal()
    mavenCentral()
    maven { setUrl("http://repo.spring.io/release") }
    maven { setUrl("http://repo.spring.io/milestone") }
    maven { setUrl("https://repo.spring.io/snapshot") }
}

dependencies {
    compile("org.springframework:spring-core:5.0.4.RELEASE")
    compile("io.projectreactor.ipc:reactor-netty:0.7.7.RELEASE") {
        exclude(group = "io.netty", module = "netty-transport-native-epoll")
    }
    compile("io.projectreactor:reactor-core:3.1.7.RELEASE")
    compile("io.lettuce:lettuce-core:5.0.4.RELEASE") {
        exclude(group = "io.netty")
    }
    compile("org.yaml:snakeyaml:1.17")
    compile("org.slf4j:slf4j-api:1.7.25")
    runtime("commons-logging:commons-logging:1.2")
    runtime("ch.qos.logback:logback-classic:1.1.7")
    compileOnly("org.projectlombok:lombok:1.16.20")
}

val processResources = tasks.getByName("processResources")
val compassCompile = tasks.getByName("compassCompile")
processResources.dependsOn(compassCompile)
