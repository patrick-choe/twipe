# Twipe

###### (ko)

Bukkit 서버용 Twip (후원 플랫폼) 라이브러리

## 사용 방법

### 플러그인 개발자

1. 개발 환경에서 의존성으로 이 플러그인으로 추가해주세요.
2. Bukkit 리스너에서 Twipe 관련 이벤트를 받아주세요 (com.github.patrick.twipe.event 패키지).

### 서버 운영자

1. 이 플러그인을 포함한 상태로 1회 이상 서버를 열어주세요. [noonmaru](https://github.com/noonmaru) 님의 [Kotlin Plugin](https://github.com/noonmaru/kotlin-plugin) 을 의존성으로 갖습니다.
2. 플러그인의 [`config.yml` 파일을 열고 형식에 맞게 수정](https://github.com/patrick-mc/twipe/blob/master/src/main/resources/config.yml) 해주세요.
3. 재미있게 플레이해주세요!

###### (en)

A Twip (Donation Platform) library for Bukkit server

## How to use

### For developers

1. Add this plugin as a dependency.
2. Listen to Twipe Events (in com.github.patrick.twipe.event package, Using bukkit listeners).

### For server owners

1. Run bukkit server with this plugin at least once. Requires [noonmaru](https://github.com/noonmaru)'s [Kotlin Plugin](https://github.com/noonmaru/kotlin-plugin).
2. [Modify config.yml](https://github.com/patrick-mc/twipe/blob/master/src/main/resources/config.yml) in plugin data folder.
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
    implementation 'com.github.patrick-mc:twipe:1.1.0'
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
    implementation("com.github.patrick-mc:twipe:1.1.0")
}
```