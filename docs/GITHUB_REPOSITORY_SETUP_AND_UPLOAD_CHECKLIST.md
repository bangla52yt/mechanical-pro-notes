# GITHUB REPOSITORY SETUP & FIRST UPLOAD CHECKLIST

**Document Identifier:** DOC-MPN-045  
**Project:** Mechanical Pro Notes (`com.researchbangla.mechanicalpronotes`)  
**Intended Repository:** `researchbangla/mechanical-pro-notes`  
**Recommended Visibility:** **Private** (`DEC-002`, `DEC-004` — personal engineering reference app)  
**Prepared:** 2026-11-18  

---

## ⚠️ Important Engineering Warning Before You Upload

> **Batch 15A Native Build Verification Is Still `PARTIAL / BLOCKED`:**
> - This project was authored and checked in a cloud workspace that lacks **JDK 17**, **Android SDK 35**, and an **Android Emulator**.
> - Although `android/gradlew` now has local executable permission (`0755`), **`./gradlew testDebugUnitTest`, `./gradlew assembleDebug`, `./gradlew lintDebug`, Room KSP schema generation, and Emulator runtime testing have NOT executed yet**.
> - Upload this repository as a **private backup and Android Studio handoff baseline** — do **not** treat it as a compiled or release-ready APK yet.

---

## 1. Repository Creation Steps (Using GitHub Mobile Web / App)

You do **not** need a computer terminal to create the repository on GitHub:

1. Open your mobile browser (Chrome) and sign in to **`https://github.com`** under the **`researchbangla`** account.
2. Tap the **`+`** icon (top-right corner) -> tap **New repository**.
3. Enter the exact details:
   - **Owner:** `researchbangla`
   - **Repository name:** `mechanical-pro-notes`
   - **Description (optional):** `Personal offline-first mechanical engineering & garments wash plant maintenance Android reference app`
   - **Visibility:** Select **Private** *(recommended so personal engineering notes and internal workflows stay private)*.
   - **Initialize this repository:** **Leave all checkboxes OFF** (`Add a README` = OFF, `.gitignore` = None, `License` = None), because `.gitignore` and `README.md` already exist in your project.
4. Tap **Create repository**.

---

## 2. Important First-Upload Precautions

- **Never upload generated/local build or dependency folders:** Do **not** upload `node_modules/`, `.gradle/`, `android/.gradle/`, `**/build/`, `local.properties`, `.idea/`, or `__pycache__/` if uploading manually or archiving files.
- **If uploading via Git (recommended when transferring workspace to a PC/Android Studio) vs GitHub Web UI:**
  - GitHub Web's **"uploading an existing file"** drag-and-drop has a `100`-file-per-upload limit and drops empty folders or file permissions like executable bits. Because this project contains `android/` source trees, drawables, `62+` docs, and `gradle-wrapper.jar`, **the cleanest method is downloading the workspace archive to a PC (or Git client), connecting `origin`, and pushing cleanly via Git so `.gitignore` runs automatically and directory hierarchy + file permissions stay intact**.
- **Privacy Guarantee Confirmed Before Upload:**
  - `android/app/src/main/assets/database/mechanical_pro_notes.db` contains **`0` personal user rows** (`user_note = 0`, `user_bookmark = 0`, `calculation_history = 0`, `checklist_progress = 0`).
  - Zero API keys, passwords, keystores, SSH keys, or personal emails exist in the codebase (`/.env.example` contains placeholder strings only).

---

## 3. Required Files That MUST Be Included in GitHub

Confirm these essential paths exist in `researchbangla/mechanical-pro-notes` after upload:

| Essential Item | Exact Path in Repository | Purpose |
|---|---|---|
| **Root Project Files** | `.gitignore`, `.env.example`, `README.md` | Ignore rules & project overview |
| **Android Root & Gradle** | `android/build.gradle.kts`, `android/settings.gradle.kts`, `android/gradle/libs.versions.toml`, `android/.gitignore` | Android Studio & dependency catalog |
| **Gradle Wrapper (All 4 Files)** | `android/gradlew`, `android/gradlew.bat`, `android/gradle/wrapper/gradle-wrapper.jar`, `android/gradle/wrapper/gradle-wrapper.properties` | Locks Gradle `8.9` without requiring manual Gradle install |
| **App Module & Manifest** | `android/app/build.gradle.kts`, `android/app/proguard-rules.pro`, `android/app/src/main/AndroidManifest.xml` | Build config (`minSdk 26`, `targetSdk 35`) & zero-`INTERNET` guarantee |
| **Kotlin Source (`64` files)** | `android/app/src/main/java/com/researchbangla/mechanicalpronotes/...` | All screens, ViewModels, DB, calculations & steam engine |
| **Vector Schematics & Res (`24` XMLs)** | `android/app/src/main/res/drawable/diag_*.xml` (`18` diagrams) + values/mipmap | Offline vector diagrams & theme resources |
| **SQLite Database Asset** | `android/app/src/main/assets/database/mechanical_pro_notes.db` (**MUST NOT BE IGNORED**) | `856,064`-byte pre-populated `64`-asset & `20`-calc offline database |
| **JVM Unit Tests (`12` files / `125` tests)** | `android/app/src/test/java/com/researchbangla/mechanicalpronotes/...` | Offline engineering, DB, search & UI state tests |
| **DB Builders & Reference Docs** | `build_database.py`, `seed_*.py`, `docs/*.md` | Reproducible DB builders & full audit/design history |

---

## 4. Files That MUST Remain Excluded (`.gitignore`)

Verify that **NONE** of the following appear in GitHub after your upload:
- `node_modules/` and `__pycache__/` / `*.pyc`
- `.gradle/`, `android/.gradle/`, `.kotlin/`, `**/build/`, `android/app/build/`
- `local.properties` or `android/local.properties`
- `.idea/`, `*.iml`, `.vscode/`, `.DS_Store`, `Thumbs.db`
- `*.apk`, `*.aab`, `*.dex`, `*.class`, `*.log`, `*.hprof`
- `.env` (real env files), `*.jks`, `*.keystore`, `*.p12`, `*.pem`, `*.key`, `keystore.properties`, `google-services.json`, `*credentials*.json`

*(Note: `.env.example` IS safe and included as a template; `mechanical_pro_notes.db` and `gradle-wrapper.jar` ARE intentionally kept.)*

---

## 5. Post-Upload Verification Checklists (On Your Phone in GitHub UI)

Once files are uploaded to `https://github.com/researchbangla/mechanical-pro-notes`, check these right in your mobile browser:

### Check 1 — Complete Android Project Present
1. Open `android/` -> confirm you see `app/`, `gradle/`, `build.gradle.kts`, `settings.gradle.kts`, `gradlew`, `gradlew.bat`, `.gitignore`.
2. Open `android/gradle/wrapper/` -> confirm both `gradle-wrapper.jar` (`~43.5 KB`) and `gradle-wrapper.properties` (`gradle-8.9-bin.zip`) are present.
3. Open `android/app/src/main/` -> confirm `AndroidManifest.xml`, `java/`, `res/`, and `assets/` are all present.

### Check 2 — SQLite Database Asset Included
1. Tap `android/app/src/main/assets/database/`.
2. Confirm **`mechanical_pro_notes.db`** is visible and shows a file size around **`836 KB` (`856,064` bytes)**.
3. If `mechanical_pro_notes.db` is missing or `0 bytes`, the offline app will crash on startup — re-upload that file cleanly before compiling.

### Check 3 — Confirm `android/gradlew` Executable Permission (`100755`)
- **Why this check is mandatory:** This cloud workspace has no `.git` history yet, so while the local filesystem file mode on `android/gradlew` is fixed to **`0755`** (`-rwxr-xr-x`), GitHub/Git cannot verify index mode **`100755`** until the first Git commit is made.
- **How to check on GitHub UI:**
  1. Tap `android/gradlew` in GitHub.
  2. Look just above the code viewer (or tap raw/file details): if uploaded via a Git client on Linux/macOS/Android Termux, Git preserves executable mode (`Executable File`). If uploaded from Windows or via GitHub Web UI, it may store as `100644` (non-executable on Linux/macOS CI).
  3. **If you run on Android Studio (Windows or Mac/Linux) later:** Android Studio's Gradle button works either way, and on Linux/macOS you can fix Git mode anytime in one step with `git update-index --chmod=+x android/gradlew`.

### Check 4 — Zero Credentials or Personal Data Uploaded
1. Tap the root folder and confirm there is **no `.env` file** (only `.env.example`), **no `.jks` / `.keystore`**, and **no `local.properties`**.
2. Tap `android/app/src/main/AndroidManifest.xml` and confirm there is **no `<uses-permission android:name="android.permission.INTERNET" />`** tag.
3. Confirm the repository badge at the top still reads **`Private`**.

---

## 6. Recommended Next Step After Confirming First Upload

1. Open the repository on a workstation with **JDK 17** and **Android Studio Ladybug (2024.2+) + Android SDK Platform 35** (or clone `https://github.com/researchbangla/mechanical-pro-notes.git` into Android Studio).
2. Allow Android Studio to generate local-only `android/local.properties` (`sdk.dir=...`).
3. If Gradle prompts for `android.useAndroidX=true` or daemon memory during first sync, add `android/gradle.properties` (`org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8` and `android.useAndroidX=true`).
4. Complete the remaining **Batch 15A** native verification tasks:
   - Execute `./gradlew testDebugUnitTest lintDebug assembleDebug` (run all `125` JUnit tests, generate KSP `android/app/schemas/.../1.json`, check Lint, build debug APK).
   - Run **Stage 05** API 34/35 Emulator & physical phone QA with network disabled.
   - Commit the generated `android/app/schemas/.../1.json` (now safely un-ignored in `android/.gitignore`) and verified `android/gradle.properties` (if needed).
