# PHASE 03: TECHNOLOGY STACK EVALUATION & ARCHITECTURAL DECISIONS
**Document Identifier:** DOC-MPN-018  
**Project:** Mechanical Pro Notes (Personal Reference App)  
**Classification Standard:** Strict 6-Tier Evidence Classification  
**Status:** PROPOSED TECHNOLOGY EVALUATION - PENDING OWNER APPROVAL  
**Author:** Senior Android Architect, Technology Strategist  
**Date:** 2026-11-12  

---

## 1. Executive Summary: Technology Stack Recommendation

The previously proposed technology stack (from Phase 01/02) has been rigorously evaluated against the specific, non-negotiable requirements of Mechanical Pro Notes:
- **Personal smartphone use exclusively** (no commercial multi-platform needs).
- **100% offline-first reliability** in noisy, low-connectivity factory environments.
- **Fast cold boot (<1.5s)** and instant search response.
- **Zero-cost, zero-licensing, open-source Google/Android standard libraries.**

```
+-------------------------------------------------------------------------------+
|                      RECOMMENDED ANDROID TECHNOLOGY STACK                     |
+-------------------------------------------------------------------------------+
|  Language:               Kotlin 2.0+ (Modern, expressive, null-safe)          |
|  UI Framework:           Jetpack Compose (Declarative, reactive, modern)      |
|  Design System:          Material Design 3 (High-contrast industrial theme)   |
|  Local Database:         Android Room SQLite 2.6+ with FTS5 Full-Text Search  |
|  Preferences:            Jetpack DataStore Preferences (Async key-value store)|
|  Architecture Pattern:   Clean Architecture + MVVM + Unidirectional Data Flow |
|  Concurrency:            Kotlin Coroutines + StateFlow / SharedFlow           |
|  Dependency Injection:   Vanilla Clean Manual DI (AppContainer pattern)       |
|  OS Compatibility:       Min SDK 26 (Android 8.0) | Target SDK 35 (Android 15) |
+-------------------------------------------------------------------------------+
```

---

## 2. In-Depth Evaluation Against Technical Alternatives

### 2.1 Language & Framework: Native Kotlin + Compose vs Flutter vs React Native
| Dimension | Native Kotlin + Compose | Flutter (Dart) | React Native (JS/TS) |
|---|---|---|---|
| **APK Base Size** | Smallest (~8–12 MB baseline) | Moderate (~18–25 MB due to Dart engine) | Large (~25–35 MB due to Hermes engine) |
| **Startup Latency** | Ultra-Fast (<1.2s cold start) | Moderate (~1.8–2.5s) | Slower (~2.2–3.2s) |
| **Offline SQLite Support**| First-class native Room with direct FTS5 integration | Good (sqflite plugin), but non-native abstraction | Fragile (native bridge dependencies, JSI overhead) |
| **Long-Term Android Health**| Direct 1st-party Google native platform | 3rd-party framework (engine update risks) | 3rd-party framework (Node/Hermes updates) |
| **Suitability for Single-Phone Owner**| **100% OPTIMAL** | Unnecessary cross-platform baggage | Unnecessary web abstraction baggage |

**Verdict:** **Native Kotlin + Jetpack Compose** is unequivocally the best choice. Since the owner operates exclusively from an Android phone and does not need an iOS version, introducing cross-platform frameworks (Flutter/React Native) adds massive APK overhead and fragile native bridge plugins without any benefit.

---

### 2.2 Local Storage: Room SQLite vs Raw SQLite vs Realm vs ObjectBox
| Dimension | Room Persistence Library | Raw SQLiteOpenHelper | Realm / ObjectBox |
|---|---|---|---|
| **Compile-Time Safety** | High (SQL queries checked at compile time) | Zero (Syntax errors crash at runtime) | High (Object-oriented) |
| **Full-Text Search (FTS5)**| Fully supported via `@Fts4`/`@Fts5` entities | Manual virtual table queries | Proprietary search indexing |
| **Licensing & Cost** | 100% Free Apache 2.0 (Google standard) | 100% Free Public Domain | Commercial / Proprietary tiers |
| **Asset Pre-Packaging** | Built-in `.createFromAsset()` | Manual raw file copying code | Complex binary file conversions |
| **Suitability for MPN** | **100% OPTIMAL** | Error-prone boilerplate | Risky proprietary lock-in |

**Verdict:** **Android Room SQLite** is the industry standard. It eliminates runtime SQL typos, provides seamless reactive `Flow` emissions, and supports `.createFromAsset()` out of the box for shipping the pre-populated 64-equipment database.

---

### 2.3 Dependency Injection: Vanilla Manual DI vs Hilt vs Koin
| Dimension | Vanilla Manual DI (`AppContainer`) | Google Hilt / Dagger | Koin |
|---|---|---|---|
| **Build-Time Overhead** | Zero seconds | Significant (KAPT/KSP annotation processing) | Low |
| **Setup Boilerplate** | Low (single clean class) | High (annotations on every class, gradle plugins) | Low |
| **Failure Modes** | Deterministic compile errors | Obscure generated code build failures | Runtime injection exceptions |
| **Suitability for Personal App**| **RECOMMENDED FOR BASELINE** | Over-engineered for a single-developer app | Good fallback if manual DI grows |

**Verdict:** **Vanilla Manual DI (`AppContainer`)** is recommended for the initial project baseline. It keeps the Gradle build fast and 100% transparent. If the dependency graph expands significantly later, migrating to Koin is trivial.

---

## 3. Platform Compatibility & Target SDK Strategy

- **Minimum SDK (minSdk): 26 (Android 8.0 Oreo)**
  - *Rationale:* Covers **98.2%+** of active Android smartphones globally. Guarantees hardware-accelerated rendering, modern java.time APIs, notification channels, and high-performance SQLite 3.18+ (which includes native FTS5 support).
- **Target SDK (targetSdk): 35 (Android 15)**
  - *Rationale:* Complies with the latest modern Android platform behaviors, edge-to-edge system bar rendering, and optimal background battery restrictions.
- **Compile SDK (compileSdk): 35**

---

## 4. Master Decision Ledger Status (Phase 03)

In accordance with strict project governance, all technical choices remain labeled:

| Decision ID | Area | Proposed Choice | Governance Status | Next Step |
|---|---|---|---|---|
| **DEC-011** | UI Framework | Kotlin + Jetpack Compose (Material 3) | **PROPOSED** | Pending Owner Explicit Approval |
| **DEC-012** | Database | Android Room SQLite with FTS5 | **PROPOSED** | Pending Owner Explicit Approval |
| **DEC-013** | Architecture | Clean Architecture + MVVM + UDF | **PROPOSED** | Pending Owner Explicit Approval |
| **DEC-014** | Compatibility | Min SDK 26, Target SDK 35 | **PROPOSED** | Pending Owner Explicit Approval |
| **DEC-015** | DI Engine | Vanilla Manual AppContainer | **PROPOSED** | Pending Owner Explicit Approval |
| **DEC-016** | Permissions | Zero Internet Permission in Manifest | **PROPOSED** | Pending Owner Explicit Approval |
| **DEC-017** | Backup Format | Local JSON via Storage Access Framework | **PROPOSED** | Pending Owner Explicit Approval |

---

## 5. Risk Assessment and Mitigation Matrix

| Risk ID | Identified Risk Description | Severity | Probability | Concrete Technical Mitigation |
|---|---|---|---|---|
| **RSK-01** | Low-memory phone encountering Out-Of-Memory (OOM) during large diagram viewing | Medium | Low | All images compressed via WebP; Compose memory handles large bitmaps with Subsampling Scale Image; zero bitmap caching in RAM. |
| **RSK-02** | User enters invalid inputs in boiler safety valve calculator leading to false output | High | Medium | Pure domain validator enforces strict physical bounds ($P > 0$, $T > 0$); invalid inputs block calculation and trigger inline warnings. |
| **RSK-03** | Future database migration corrupting existing user notes | High | Very Low | Migrations only alter static reference tables; user tables (`user_note`) are un-dropped; automated SQLite rollback on failure. |
| **RSK-04** | Accidental data loss during smartphone change | Medium | Medium | One-tap local JSON backup to external storage / SD card via Android Storage Access Framework. |
| **RSK-05** | Workspace development environment lacking Android SDK CLI tools | Low | High | Source code, schemas, and assets cleanly architected in Workspace; compilation/APK packaging cleanly mapped to standard Gradle workflow. |
