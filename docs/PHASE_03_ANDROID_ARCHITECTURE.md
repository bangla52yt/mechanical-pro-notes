# PHASE 03: ANDROID ARCHITECTURE SPECIFICATION
**Document Identifier:** DOC-MPN-013  
**Project:** Mechanical Pro Notes (Personal Reference App)  
**Classification Standard:** Strict 6-Tier Evidence Classification  
**Status:** PROPOSED ARCHITECTURAL SPECIFICATION - PENDING OWNER APPROVAL  
**Author:** Senior Android Architect, Offline-First Application Engineer  
**Date:** 2026-11-12  

---

## 1. Executive Architectural Overview

Mechanical Pro Notes is engineered as a **100% Offline-First Personal Mechanical Knowledge and Plant Maintenance Reference Application** running natively on Android (smartphone optimized).

The architectural design is governed by three foundational constraints:
1. **Zero External Server Dependency:** The entire corpus of 64 equipment records, industrial boiler blueprints, engineering formulas, diagrams, and inspection checklists resides locally on the device inside an embedded SQLite database.
2. **Shop-Floor Usability:** Cold start latency under 1.5 seconds, instant (<50ms) search response, zero network latency, high-contrast readable typography for industrial shop-floor environments, and zero battery drain from background services.
3. **Pristine Separation of Concerns:** Rigid Clean Architecture with Unidirectional Data Flow (UDF), ensuring business logic (such as safety-critical boiler formulas) is isolated from UI rendering and database storage.

```
+-------------------------------------------------------------------------------+
|                             UI / PRESENTATION LAYER                           |
|  +-------------------------------------------------------------------------+  |
|  |  Jetpack Compose Screen Composables (Material 3 Dark/Light)            |  |
|  |  - Home Dashboard          - Dedicated Boiler Module                   |  |
|  |  - Wash Plant Explorer     - Interactive Calculation Engines           |  |
|  |  - Full-Text Search UI     - Maintenance Checklist & Offline Notes     |  |
|  +-------------------------------------------------------------------------+  |
|                                     ^                                         |
|                             StateFlow<UiState>                                |
|                                     v                                         |
|  +-------------------------------------------------------------------------+  |
|  |  Android Architecture Components ViewModels (Lifecycle-Aware)           |  |
|  |  - EquipmentViewModel      - BoilerViewModel     - SearchViewModel     |  |
|  |  - CalculationViewModel    - ChecklistViewModel  - BackupViewModel     |  |
|  +-------------------------------------------------------------------------+  |
+-------------------------------------------------------------------------------+
                                      |
                           Executes Domain Use Cases
                                      v
+-------------------------------------------------------------------------------+
|                                DOMAIN LAYER                                   |
|  +-------------------------------------------------------------------------+  |
|  |  Pure Kotlin Business Logic & Use Cases (Zero Android Framework Deps)   |  |
|  |  - GetEquipmentDetailsUseCase     - CalculateBoilerEfficiencyUseCase   |  |
|  |  - SearchKnowledgeBaseUseCase     - CalculateSafetyValveCapacityUseCase|  |
|  |  - ExportUserNotesBackupUseCase   - ValidateCalculationInputsUseCase   |  |
|  +-------------------------------------------------------------------------+  |
|  |  Domain Models (Clean POKOs):                                           |  |
|  |  - Equipment, BoilerSpec, CalculationResult, ChecklistItem, UserNote    |  |
|  +-------------------------------------------------------------------------+  |
|  |  Repository Interfaces:                                                 |  |
|  |  - EquipmentRepository, BoilerRepository, SearchRepository, BackupRepo  |  |
|  +-------------------------------------------------------------------------+  |
+-------------------------------------------------------------------------------+
                                      |
                            Implements Repositories
                                      v
+-------------------------------------------------------------------------------+
|                                  DATA LAYER                                   |
|  +-------------------------------------------------------------------------+  |
|  |  Repository Implementations:                                            |  |
|  |  - EquipmentRepositoryImpl, BoilerRepositoryImpl, SearchRepositoryImpl  |  |
|  +-------------------------------------------------------------------------+  |
|  |  Local Persistence (Room SQLite + FTS5 Full-Text Search Engine):        |  |
|  |  - MechanicalProDatabase (Pre-populated from assets, versioned schema) |  |
|  |  - DAOs: EquipmentDao, BoilerDao, SearchFtsDao, NoteDao, ChecklistDao  |  |
|  |  - Entities: SystemFamilyEntity, EquipmentEntity, FtsEntity, NoteEntity |  |
|  +-------------------------------------------------------------------------+  |
|  |  Local Key-Value Preferences (Jetpack DataStore Preferences):          |  |
|  |  - AppPreferences: Theme (Dark/Light), Default Units (SI/Metric), etc.  |  |
|  +-------------------------------------------------------------------------+  |
|  |  File-Based Technical Asset Storage:                                    |  |
|  |  - Local Vector Drawables, Compressed WebP P&IDs, SAF JSON Export Files |  |
|  +-------------------------------------------------------------------------+  |
+-------------------------------------------------------------------------------+
```

---

## 2. Layered Clean Architecture Breakdown

### 2.1 Presentation Layer (`/ui`, `/presentation`)
- **Technology:** Android Jetpack Compose with Material Design 3 (`androidx.compose.material3`).
- **Pattern:** Model-View-ViewModel (MVVM) with Unidirectional Data Flow (UDF).
- **Responsibility:**
  - Emits UI Events (`UiEvent`) triggered by user interactions (tap, text input, slider change).
  - Collects immutable `UiState` as `StateFlow` from the ViewModel.
  - Zero business logic in Composables; all validation and computations are delegated to ViewModels.
  - Accommodates both English technical terminology and Bengali summaries seamlessly.

### 2.2 Domain Layer (`/domain`)
- **Technology:** Pure Kotlin (zero Android framework classes, zero `android.*` imports).
- **Responsibility:**
  - Encapsulates the application's core engineering rules, safety limits, unit conversions, and validation logic.
  - **Use Cases (Interactors):** Fine-grained, single-responsibility classes (e.g., `CalculateBlowdownRateUseCase`).
  - **Domain Entities:** Clean Kotlin data classes independent of Room table annotations.
  - **Repository Interfaces:** Defined here to allow total inversion of control (Dependency Inversion Principle).

### 2.3 Data Layer (`/data`)
- **Technology:** Room Persistence Library (`androidx.room`), SQLite FTS5, Jetpack DataStore, Android Storage Access Framework (SAF).
- **Responsibility:**
  - Implements Repository interfaces defined by the Domain Layer.
  - Translates Room Entities to Domain Models via mapper extensions (`toDomain()`, `toEntity()`).
  - Manages SQLite transactions, pre-populated database bundling (`createFromAsset`), and schema migrations.
  - Executes FTS5 search queries across indexed tables.

---

## 3. Recommended Project Directory and Package Structure

The package namespace is aligned with the authorized future GitHub organization: `com.researchbangla.mechanicalpronotes`.

```
app/src/main/java/com/researchbangla/mechanicalpronotes/
|-- MechanicalProApp.kt                  // Application entry point & DI graph initialization
|
|-- di/                                  // Dependency Injection Container
|   |-- AppModule.kt                    // Database, Dispatchers, DataStore bindings
|   `-- ViewModelFactory.kt             // Lifecycle-aware ViewModel provider
|
|-- core/                                // Core Shared Utilities
|   |-- common/                         // Result wrappers, Constants, Extensions
|   |   |-- Resource.kt                 // Success, Error, Loading state wrapper
|   |   `-- DispatcherProvider.kt       // Coroutine dispatcher abstraction for testability
|   |-- math/                           // Physical constants, SI unit conversion engines
|   |   |-- UnitConverter.kt            // bar to psi, kW to BHP, m3/h to GPM
|   |   `-- SteamTables.kt              // Embedded saturated steam thermodynamic lookup table
|   `-- designsystem/                   // Material 3 theme, Typography, Colors, Components
|       |-- Theme.kt                    // Dynamic / Dark / Light theme definitions
|       |-- Color.kt                    // Industrial palette (Safety Orange, Slate, Steel Blue)
|       |-- Type.kt                     // Monospace code styles, crisp body typography
|       `-- components/                 // Shared composables (TopBar, NavDrawer, Card, MathText)
|
|-- domain/                              // Core Domain Layer (Pure Kotlin)
|   |-- model/                          // Domain POKO models
|   |   |-- Equipment.kt
|   |   |-- BoilerSpec.kt
|   |   |-- ContentDepth.kt             // FULL, PARTIAL, INVENTORY_ONLY, RESEARCH_PENDING
|   |   |-- CalculationModel.kt
|   |   |-- ChecklistItem.kt
|   |   `-- UserNote.kt
|   |-- repository/                     // Repository Interfaces
|   |   |-- EquipmentRepository.kt
|   |   |-- BoilerRepository.kt
|   |   |-- CalculationRepository.kt
|   |   |-- SearchRepository.kt
|   |   `-- UserDataRepository.kt
|   `-- usecase/                        // Pure Business Use Cases
|       |-- equipment/
|       |   |-- GetEquipmentListUseCase.kt
|       |   `-- GetEquipmentDetailUseCase.kt
|       |-- boiler/
|       |   |-- GetBoilerSpecsUseCase.kt
|       |   `-- CalculateBoilerEfficiencyUseCase.kt
|       |-- search/
|       |   `-- SearchKnowledgeBaseUseCase.kt
|       `-- backup/
|           |-- ExportUserDataUseCase.kt
|           `-- ImportUserDataUseCase.kt
|
|-- data/                                // Data Persistence & Repositories
|   |-- local/
|   |   |-- database/
|   |   |   |-- MechanicalProDatabase.kt // Room Database class (versioned, type converters)
|   |   |   |-- entity/                 // Room Entity definitions (Tables & FTS)
|   |   |   |   |-- SystemFamilyEntity.kt
|   |   |   |   |-- EquipmentEntity.kt
|   |   |   |   |-- ComponentEntity.kt
|   |   |   |   |-- KnowledgeSectionEntity.kt
|   |   |   |   |-- CalculationEntity.kt
|   |   |   |   |-- UserNoteEntity.kt
|   |   |   |   `-- KnowledgeFtsEntity.kt // SQLite FTS5 Virtual Table Entity
|   |   |   |-- dao/                    // Room Data Access Objects
|   |   |   |   |-- EquipmentDao.kt
|   |   |   |   |-- BoilerDao.kt
|   |   |   |   |-- SearchDao.kt
|   |   |   |   `-- UserNoteDao.kt
|   |   |   `-- migration/              // Room Database Migration definitions
|   |   |       `-- DatabaseMigrations.kt
|   |   |-- datastore/
|   |   |   `-- UserPreferencesDataStore.kt // Theme, units, font scale
|   |   `-- mapper/                     // Entity <-> Domain Mappers
|   |       |-- EquipmentMapper.kt
|   |       `-- BoilerMapper.kt
|   `-- repository/                     // Repository Implementations
|       |-- EquipmentRepositoryImpl.kt
|       |-- BoilerRepositoryImpl.kt
|       |-- SearchRepositoryImpl.kt
|       `-- UserDataRepositoryImpl.kt
|
`-- presentation/                        // Presentation / UI Layer
    |-- navigation/                     // Navigation Graph & Destination Routes
    |   |-- Screen.kt                   // Sealed class defining all 17 top destinations
    |   `-- NavGraph.kt                 // Jetpack Navigation Compose graph builder
    |-- home/                           // 1. Home Dashboard
    |   |-- HomeScreen.kt
    |   `-- HomeViewModel.kt
    |-- fundamentals/                   // 2. Mechanical Fundamentals
    |-- washplant/                      // 3. Wash Plant Equipment Explorer
    |   |-- WashPlantListScreen.kt
    |   |-- EquipmentDetailScreen.kt    // 21-section tabbed reader
    |   `-- WashPlantViewModel.kt
    |-- boiler/                         // 4. Dedicated Industrial Boiler Module
    |   |-- BoilerDashboardScreen.kt
    |   |-- GasBoilerDetailScreen.kt
    |   |-- JhutBoilerDetailScreen.kt
    |   |-- BoilerStatutoryScreen.kt    // The Boilers Act, 2022 & Rules 2025
    |   `-- BoilerViewModel.kt
    |-- pumps/                          // 5. Pumps and Fluid Systems
    |-- transmission/                   // 6. Motors, Drives & Transmission
    |-- steam/                          // 7. Steam & Condensate Systems
    |-- water/                          // 8. Water Treatment & Chemical Systems
    |-- air/                            // 9. Compressed Air & Pneumatics
    |-- thermal/                        // 10. Cooling & Heating Systems
    |-- etp/                            // 11. ETP & Wastewater Systems
    |-- maintenance/                    // 12. Maintenance Management
    |-- checklists/                     // 13. Inspection & Checklists
    |-- calculations/                   // 14. Engineering Calculations Suite
    |   |-- CalculationListScreen.kt
    |   |-- CalculationRunnerScreen.kt  // Interactive input/validation/output UI
    |   `-- CalculationViewModel.kt
    |-- library/                        // 15. Technical Reference Library (Codes/Standards)
    |-- search/                         // 16. Full-Text Search & Bookmarks
    |   |-- SearchScreen.kt
    |   `-- SearchViewModel.kt
    `-- settings/                       // 17. Settings & Local Data Management (Backup/Restore)
        |-- SettingsScreen.kt
        `-- SettingsViewModel.kt
```

---

## 4. Unidirectional Data Flow (UDF) & State Management

To guarantee predictability and avoid race conditions on mobile devices, every screen implements the standard Android UDF pattern:

```
          +------------------------------------------------+
          |                                                |
          |                  UI Component                  |
          |               (Jetpack Compose)                |
          |                                                |
          +------------------------------------------------+
                  |                                ^
                  | User Action                    | State Emission
                  | (UiEvent)                      | (StateFlow<UiState>)
                  v                                |
          +------------------------------------------------+
          |                                                |
          |                   ViewModel                    |
          |           (androidx.lifecycle.ViewModel)       |
          |                                                |
          +------------------------------------------------+
                  |                                ^
                  | Invoke Use Case                | Return Domain
                  | with Coroutines (Dispatchers)  | Result Models
                  v                                |
          +------------------------------------------------+
          |                                                |
          |                 Domain Layer                   |
          |             (Use Cases & Models)               |
          |                                                |
          +------------------------------------------------+
```

### State Structure Guidelines:
1. Every screen has a dedicated sealed interface or data class representing state:
   ```kotlin
   data class EquipmentDetailUiState(
       val isLoading: Boolean = false,
       val equipment: Equipment? = null,
       val contentDepth: ContentDepth = ContentDepth.INVENTORY_ONLY,
       val selectedTab: Int = 0,
       val userNote: String = "",
       val isBookmarked: Boolean = false,
       val errorMessage: String? = null
   )
   ```
2. User actions are encapsulated in a sealed interface:
   ```kotlin
   sealed interface EquipmentDetailUiEvent {
       data class TabSelected(val tabIndex: Int) : EquipmentDetailUiEvent
       data class SaveNote(val noteText: String) : EquipmentDetailUiEvent
       object ToggleBookmark : EquipmentDetailUiEvent
   }
   ```

---

## 5. Dependency Injection Architecture: Evaluation & Recommendation

### Comparison of Options:
| DI Approach | Setup Complexity | Build Time Impact | APK Size Impact | Runtime Performance | Recommendation |
|---|---|---|---|---|---|
| **Vanilla Manual DI / AppContainer** | Very Low (0 external libraries) | Zero overhead | 0 KB added | Instant, 100% deterministic | **Primary Choice for Phase 03/04 Baseline** |
| **Kotlin Koin** | Low (Kotlin DSL) | Low | ~250 KB | Service locator reflection / DSL | Viable alternative if manual DI grows large |
| **Google Hilt / Dagger** | High (KSP/KAPT code-gen) | Significant build overhead | ~1.5 MB + annotation processor | Compile-time verified | Overkill for a personal offline app |

### Recommended Decision:
- **Decision:** Use **Vanilla Clean Manual Dependency Injection (`AppContainer`)** for the initial baseline.
- **Rationale:** A personal-use offline reference app has a fixed, stable dependency graph: 1 Room database, 1 DataStore, 5 Repositories, ~15 Use Cases, and ~10 ViewModels. Manual DI requires zero external code generation, zero KSP plugins, zero build-time delays, and guarantees 100% debuggability without complex annotation processing errors.

---

## 6. Error Handling & Industrial Resilience Strategy

1. **Safety-Critical Calculations:** Calculations never fail silently or return `NaN`. If inputs are outside physical boundary conditions (e.g., boiler steam pressure $<0$ or feed water temperature $>100^\circ\text{C}$ in an atmospheric deaerator), the domain layer returns a strongly-typed `ValidationResult.Error(field, message)` preventing false outputs.
2. **Corrupted Database Recovery:** If SQLite encounters disk corruption or unreadable data, the database helper catches `SQLiteDatabaseCorruptException`, logs an internal error, and gracefully offers the user the option to restore from the bundled read-only asset baseline without crashing the app.
3. **Zero Crash Policy:** Top-level Compose error boundaries wrap complex content tabs; if an unexpected layout exception occurs in a specific diagram, the container renders an informative fallback card ("Diagram rendering temporarily unavailable") while keeping the rest of the 21-section technical text intact.
