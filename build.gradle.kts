/*
 * Copyright (C) 2021 PatrickKR
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import groovy.lang.MissingPropertyException
import org.gradle.jvm.tasks.Jar
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `maven-publish`
    signing
    kotlin("jvm") version "1.4.31"
    id("com.github.johnrengelman.shadow") version "6.1.0"
    id("org.jetbrains.dokka") version "1.4.30"
}

group = "com.github.patrick-mc"
version = "1.1.0"

repositories {
    maven("https://repo.maven.apache.org/maven2/")
    maven("https://jcenter.bintray.com/")
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
}

dependencies {
    compileOnly(kotlin("stdlib-jdk8"))

    compileOnly("org.spigotmc:spigot-api:1.8-R0.1-SNAPSHOT")

    implementation("com.neovisionaries:nv-websocket-client:2.14")
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "11"
    }

    withType<DokkaTask> {
        dokkaSourceSets {
            named("main") {
                displayName.set(rootProject.name)
                sourceLink {
                    localDirectory.set(file("src/main/kotlin"))
                    remoteUrl.set(uri("https://github.com/patrick-mc/${rootProject.name}/tree/master/src/main/kotlin").toURL())
                    remoteLineSuffix.set("#L")
                }
            }
        }
    }

    withType<ProcessResources> {
        exclude("*.html")
    }

    withType<ShadowJar> {
        archiveClassifier.set("")
    }

    create<Jar>("sourcesJar") {
        archiveClassifier.set("sources")
        from(sourceSets["main"].allSource)
    }

    create<Jar>("dokkaJar") {
        archiveClassifier.set("javadoc")
        dependsOn("dokkaHtml")

        from("$buildDir/dokka/html/") {
            include("**")
        }

        from("$rootDir/src/main/resources/") {
            include("*.html")
        }
    }

    create<Copy>("distJar") {
        if (System.getProperty("os.name").startsWith("Windows")) { // due to ci error
            from(shadowJar)

            val fileName = "${project.name.split("-").joinToString("") { it.capitalize() }}.jar"

            rename {
                fileName
            }

            var dest = file("W:/Servers/1.16.4/plugins")
            if (File(dest, fileName).exists()) dest = File(dest, "update")
            into(dest)
        }
    }
}

try {
    publishing {
        publications {
            create<MavenPublication>(rootProject.name) {
                from(components["java"])
                artifact(tasks["sourcesJar"])
                artifact(tasks["dokkaJar"])

                repositories {
                    mavenLocal()

                    maven {
                        name = "central"

                        credentials {
                            username = project.property("centralUsername").toString()
                            password = project.property("centralPassword").toString()
                        }

                        url = uri(if (version.endsWith("SNAPSHOT")) {
                            "https://oss.sonatype.org/content/repositories/snapshots/"
                        } else {
                            "https://oss.sonatype.org/service/local/staging/deploy/maven2/"
                        })
                    }
                }

                pom {
                    name.set(rootProject.name)
                    description.set("A twip library for Bukkit")
                    url.set("https://github.com/patrick-mc/${rootProject.name}")

                    licenses {
                        license {
                            name.set("Apache License, Version 2.0")
                            url.set("http://www.apache.org/licenses/LICENSE-2.0")
                        }
                    }

                    developers {
                        developer {
                            id.set("patrick-mc")
                            name.set("PatrickKR")
                            email.set("mailpatrickkorea@gmail.com")
                            url.set("https://github.com/patrick-mc")
                            roles.addAll("developer")
                            timezone.set("Asia/Seoul")
                        }
                    }

                    scm {
                        connection.set("scm:git:git://github.com/patrick-mc/${rootProject.name}.git")
                        developerConnection.set("scm:git:ssh://github.com:patrick-mc/${rootProject.name}.git")
                        url.set("https://github.com/patrick-mc/${rootProject.name}")
                    }
                }
            }
        }
    }

    signing {
        isRequired = true
        sign(tasks["sourcesJar"], tasks["dokkaJar"], tasks["shadowJar"])
        sign(publishing.publications[rootProject.name])
    }
} catch (ignored: MissingPropertyException) {}