# STAGE 05 REPORT: ANDROID EMULATOR READINESS & PREVIEW VERIFICATION
**Document Identifier:** DOC-MPN-025  
**Project:** Mechanical Pro Notes (`com.researchbangla.mechanicalpronotes`)  
**Scope:** Stage 5 — Read-Only Environment Audit for JDK 17, Android SDK 35, Gradle Wrapper / AGP Execution, Android Emulator / AVD Availability & Live Emulator UI Inspection  
**Classification Standard:** Strict 6-Tier Evidence Classification (`PASS` / `FAIL` / `BLOCKED` / `PENDING` / `UNVERIFIED`)  
**Overall Stage 05 Verdict:** **BLOCKED** (Missing JDK 17, Android SDK 35, Gradle/Maven Caches, Android Emulator/AVD & `/dev/kvm` inside Sandboxed Web Container)  
**Date:** 2026-11-15  

---

## 1. Executive Summary
A strictly **read-only** environment and project readiness inspection was performed across `/app/applet` and `/app/applet/android`. No Kotlin, XML, Gradle, resource, or SQLite database files were modified.

- **Environment Capability Verdict:** This Debian GNU/Linux 12 (`bookworm`, `4.19.0-gvisor`) container is configured solely for web/Node.js/Python execution and **cannot** compile Kotlin/Android binaries, execute Gradle/AGP, launch an Android Emulator/AVD, or perform genuine Compose UI runtime inspection.
- **Per Strict Stage 05 Rules:** Execution **STOPPED** at the exact JDK/SDK/Emulator blocker. Zero emulator UI checks (`navigation`, `17` core modules, bottom bar, calculators, equipment details, search, settings, soft keyboard, or `360dp` compact layouts) are claimed as verified runtime passes.

---

## 2. Classification Summary Matrix (`PASS` / `FAIL` / `BLOCKED` / `PENDING` / `UNVERIFIED`)

| Readiness Area / Check | Status | Evidence & Exact Finding |
| :--- | :--- | :--- |
| **Static Android Project & Gradle Wrapper Artifacts** | **PASS** | `android/build.gradle.kts`, `android/app/build.gradle.kts` (`compileSdk = 35`, `minSdk = 26`, `targetSdk = 35`, `JavaVersion.VERSION_17`), `android/settings.gradle.kts`, `android/gradle/libs.versions.toml` (`agp = 8.7.2`, `kotlin = 2.0.21`, `ksp = 2.0.21-1.0.27`, `composeBom = 2024.10.01`, `room = 2.6.1`), executable `android/gradlew` (`-rwxr-xr-x`, `6123` bytes), `gradle-wrapper.jar` (`43504` bytes), and `gradle-wrapper.properties` (`gradle-8.9-bin.zip`) are intact on disk. |
| **JDK 17 Availability** | **BLOCKED** | `java`, `javac`, and `kotlinc` are `NOT FOUND` in `PATH`; `JAVA_HOME` is unset; `/usr/lib/jvm` does not exist. |
| **Android SDK 35 Availability** | **BLOCKED** | `ANDROID_HOME` and `ANDROID_SDK_ROOT` are unset; `adb`, `sdkmanager`, and `avdmanager` are `NOT FOUND`; `/opt/android*`, `/root/Android`, `/root/.android`, and `android/local.properties` do not exist. |
| **Gradle Wrapper & AGP Execution (`./android/gradlew`)** | **BLOCKED** | Executing `./android/gradlew --version` exits with code `1` (`ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH`). `/root/.gradle` does not exist (no pre-cached Gradle distribution or AndroidX/AGP artifacts). Note also `android/gradle.properties` is absent (no JVM heap/AndroidX flags override present; relies on Gradle/AGP defaults when run locally). |
| **Android Emulator / AVD & Hardware Acceleration** | **BLOCKED** | `emulator` and `avdmanager` are `NOT FOUND`; `/root/.android` (AVD store) does not exist; `/dev/kvm` does not exist under `4.19.0-gvisor`. |
| **Emulator APK Install & Live UI Inspection** | **BLOCKED** | Cannot execute `./gradlew assembleDebug`, `adb install`, or `adb shell am start` without JDK 17, Android SDK 35, and an Emulator/AVD. |
| **Automated Kotlin JVM JUnit Suites (`Phase 06–07`, `Stage 1–4`)** | **UNVERIFIED** | Source files exist cleanly on disk (including `Stage04UiStateAndAccessibilityTest.kt`), but `./gradlew testDebugUnitTest` cannot execute inside this JDK-less container. |
| **Interactive Emulator UI & Physical Device Walkthroughs** | **PENDING** | Live inspection of all `17` core modules, `4` Bottom Navigation destinations, `20` calculators, equipment reader tabs, unified search, settings/SAF backup, soft keyboard (`imePadding`), `>= 48.dp` touch ergonomics, TalkBack, and `320–420dp` layouts remains **PENDING** on an Owner PC with Android Studio + Emulator or physical phone. |
| **Stage 05 Runtime Failures** | **NONE (`0` FAIL)** | Zero build logic bugs were triggered because execution stopped cleanly prior to toolchain invocation at the confirmed missing-dependency blockers. |

---

## 3. Exact Commands Executed & Actual Outputs

### Command 1 — OS, Environment Variables, Binaries, Directories & Gradle Wrapper Probe
Executed in workspace root `/app/applet`:
```bash
uname -a; cat /etc/os-release 2>/dev/null | head -n 5
env | grep -iE 'java|jdk|android|gradle|kotlin|sdk' || true
for cmd in java javac kotlinc gradle adb emulator sdkmanager avdmanager; do echo -n "$cmd: "; command -v "$cmd" || echo "NOT FOUND"; done
ls -ld /usr/lib/jvm /opt/android* ~/Android ~/.android ~/.gradle /dev/kvm 2>&1 || true
ls -l android/gradlew android/gradle/wrapper/gradle-wrapper.jar
./android/gradlew --version; echo "EXIT=$?"
ls -la android/local.properties android/gradle.properties 2>&1 || true
```

**Observed Raw Outputs:**
```text
Linux localhost 4.19.0-gvisor #1 SMP Sun Jan 10 15:06:54 PST 2016 x86_64 GNU/Linux
PRETTY_NAME="Debian GNU/Linux 12 (bookworm)"
NAME="Debian GNU/Linux"
VERSION_ID="12"
VERSION="12 (bookworm)"
VERSION_CODENAME=bookworm

=== ENV ===
(empty)

=== WHICH ===
java: NOT FOUND
javac: NOT FOUND
kotlinc: NOT FOUND
gradle: NOT FOUND
adb: NOT FOUND
emulator: NOT FOUND
sdkmanager: NOT FOUND
avdmanager: NOT FOUND

=== DIRECTORIES ===
ls: cannot access '/usr/lib/jvm': No such file or directory
ls: cannot access '/opt/android*': No such file or directory
ls: cannot access '/root/Android': No such file or directory
ls: cannot access '/root/.android': No such file or directory
ls: cannot access '/root/.gradle': No such file or directory
ls: cannot access '/dev/kvm': No such file or directory

=== GRADLEW ===
-rwxr-xr-x 1 root root  6123 Nov 15 12:51 android/gradlew
-rw-r--r-- 1 root root 43504 Nov 15 12:51 android/gradle/wrapper/gradle-wrapper.jar

./android/gradlew --version ->
ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
Please set the JAVA_HOME variable in your environment to match the
location of your Java installation.
EXIT=1

ls -la android/local.properties android/gradle.properties ->
ls: cannot access 'android/local.properties': No such file or directory
ls: cannot access 'android/gradle.properties': No such file or directory
```

### Command 2 — Non-Standard Runtime Paths (`/opt`, `/usr/local`)
```text
/opt:
yarn-v1.22.22/

/usr/local:
bin  CHANGELOG.md  etc  games  include  lib  LICENSE  man -> share/man  nodejs  README.md  sbin  share  src  versions
```

---

## 4. Inspected Gradle & SDK Configuration Requirements (Read-Only)
From `android/app/build.gradle.kts`, `android/gradle/libs.versions.toml`, `android/gradle/wrapper/gradle-wrapper.properties`, and `android/settings.gradle.kts`:

| Project Requirement | Configured Version / Coordinate |
| :--- | :--- |
| **JDK Compatibility** | Java 17 (`sourceCompatibility = JavaVersion.VERSION_17`, `targetCompatibility = JavaVersion.VERSION_17`, `jvmTarget = "17"`) |
| **Android SDK Targets** | `compileSdk = 35`, `minSdk = 26`, `targetSdk = 35` |
| **Gradle Wrapper** | Gradle `8.9` (`gradle-8.9-bin.zip`) |
| **Android Gradle Plugin (AGP)** | `8.7.2` |
| **Kotlin / KSP / Compose** | Kotlin `2.0.21`, KSP `2.0.21-1.0.27`, Compose BOM `2024.10.01`, Material3 `1.3.1` |
| **Plugin & Dependency Repositories** | `google()`, `mavenCentral()`, `gradlePluginPortal()` (first-run sync requires internet access on the build host to populate Gradle cache) |

---

## 5. Minimum External Requirements Needed to Unblock Stage 05
To perform genuine native builds and interactive Emulator UI verification, run Stage 05 on an **Owner Workstation / CI Runner** providing these minimum dependencies:

1. **JDK 17:** OpenJDK 17 or Android Studio JBR 17 with `JAVA_HOME` configured.
2. **Android SDK 35:** `platforms;android-35`, `build-tools;35.0.0`, and `platform-tools` (`adb`), plus network access on first Gradle Sync to download `gradle-8.9-bin.zip`, AGP `8.7.2`, Kotlin `2.0.21`, and Maven dependencies (Android Studio sets `sdk.dir` automatically in gitignored `android/local.properties`).
3. **Android Emulator & System Image:** Android Emulator binary + AVD targeting **API 34 or API 35** (recommended phone profile `360dp`–`412dp` width, hardware KVM/Hypervisor enabled).
4. **Optional Recommended Configuration Check (Not Modified in Stage 5):** If Gradle daemon hits heap limits or fails `android.useAndroidX` checks locally, standard `android/gradle.properties` settings (`org.gradle.jvmargs=-Xmx2048m`, `android.useAndroidX=true`) can be added during local build troubleshooting once an Android SDK/JDK host is active.

---

## 6. Next Safe Action (Do Not Proceed to Stage 06 Here)
1. **In Android Studio (Recommended):** Open `<repo>/android`, let Gradle Sync complete, open Split/Design view to inspect the `40` `@Preview` composables, and launch the **app** configuration on an API 34/35 Android Emulator via **Device Manager**.
2. **Via CLI on Host with JDK 17 + Android SDK 35 + Running Emulator:**
   ```bash
   cd android
   ./gradlew testDebugUnitTest assembleDebug
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   adb shell am start -n com.researchbangla.mechanicalpronotes.debug/com.researchbangla.mechanicalpronotes.presentation.MainActivity
   ```
3. Capture live test/Emulator results (including compact-width screenshots, IME keyboard behavior, TalkBack focus order, dark/high-legibility contrast, and `CALC-01..20` runs) before authorizing Stage 06.
