/*
 * Copyright (C) 2020 PatrickKR
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * Contact me on <mailpatrickkr@gmail.com>
 */

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import groovy.lang.MissingPropertyException
import org.gradle.jvm.tasks.Jar
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.net.URL

plugins {
    `maven-publish`
    signing
    kotlin("jvm") version "1.4.10"
    id("com.github.johnrengelman.shadow") version "6.1.0"
    id("org.jetbrains.dokka") version "1.4.10"
}

group = "com.github.patrick-mc"
version = "1.0"

repositories {
    maven("https://repo.maven.apache.org/maven2/")
    maven("https://jcenter.bintray.com/")
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
}

dependencies {
    compileOnly(kotlin("stdlib-jdk8"))
    compileOnly("org.spigotmc:spigot-api:1.8-R0.1-SNAPSHOT")
    compileOnly("com.google.code.gson:gson:2.8.6")
    implementation("com.neovisionaries:nv-websocket-client:2.10")
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }

    withType<DokkaTask> {
        dokkaSourceSets {
            named("main") {
                displayName.set("Twipe")
                sourceLink {
                    localDirectory.set(file("src/main/kotlin"))
                    remoteUrl.set(URL("https://github.com/patrick-mc/twipe/tree/master/src/main/kotlin"))
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
        from(shadowJar)
        into("W:\\Servers\\1.16.3\\plugins\\update")
    }
}

try {
    publishing {
        publications {
            create<MavenPublication>("twipe") {
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
                    name.set("twipe")
                    description.set("A twip library for Bukkit")
                    url.set("https://github.com/patrick-mc/twipe")

                    licenses {
                        license {
                            name.set("GNU General Public License v2.0")
                            url.set("https://opensource.org/licenses/gpl-2.0.php")
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
                        connection.set("scm:git:git://github.com/patrick-mc/twipe.git")
                        developerConnection.set("scm:git:ssh://github.com:patrick-mc/twipe.git")
                        url.set("https://github.com/patrick-mc/twipe")
                    }
                }
            }
        }
    }

    signing {
        isRequired = true
        sign(tasks["sourcesJar"], tasks["dokkaJar"], tasks["shadowJar"])
        sign(publishing.publications["twipe"])
    }
} catch (ignored: MissingPropertyException) {}