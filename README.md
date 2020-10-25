# Twipe

A twip library for Bukkit

[Javadoc.io](https://www.javadoc.io/doc/com.github.patrick-mc/twipe/latest/index.html) to see Javadoc online!

## How to use

### For developers

1. Add this plugin as a dependency.
2. Listen to AsyncTwipDonateEvent (Using bukkit listeners).

### For server owners

1. Run bukkit server with this plugin at least once.
2. Modify config.yml in plugin data folder.
3. Enjoy!

## Repositories

### Gradle (Groovy DSL)

```groovy
allprojects {
    repositories {
        mavenCentral() // or maven { url 'https://repo.maven.apache.org/maven2/' }
    }
}
```

```groovy
dependencies {
    implementation 'com.github.patrick-mc:twipe:1.0'
}
```

### Gradle (Kotlin DSL)

```kotlin
allprojects {
    repositories {
        mavenCentral() // or maven("https://repo.maven.apache.org/maven2/")
    }
}
```

```kotlin
dependencies {
    implementation("com.github.patrick-mc:twipe:1.0")
}
```