# PHASE 05 VIEWMODEL INTEGRATION & REACTIVE DATA FLOW REPORT

**Project:** Mechanical Pro Notes  
**Application Scope:** Personal Offline-First Mechanical Reference Android App  
**Phase:** 05 — Material 3 UI & Compose Reference Screens  
**Date:** 12 November 2026  

---

## 1. Architectural Pattern: Clean Architecture + MVVM + UDF (DEC-013)

The application strictly implements Unidirectional Data Flow (UDF):

```
       [User Interaction / Navigation]
                     │
                     ▼ (Intent / Method Call)
               [ ViewModel ]
                     │
                     ├── Mutates private MutableStateFlow<UiState>
                     └── Interacts with Domain Repositories (Coroutines)
                     │
                     ▼ (Emits immutable StateFlow<UiState>)
              [ Compose Screen ]
                     │
                     ▼
            (Recomposes cleanly)
```

---

## 2. ViewModel Specifications & State Models

| ViewModel | State Class | Emitted State Properties | Repositories Connected |
|---|---|---|---|
| `HomeViewModel` | `HomeUiState` | `isLoading`, `totalEquipmentCount`, `totalBoilerCount`, `recentEquipment`, `errorMessage` | `EquipmentRepository`, `BoilerRepository` |
| `EquipmentViewModel` | `EquipmentListUiState`, `EquipmentDetailUiState` | `selectedFamilyId`, `systemFamilies`, `equipmentList`, `equipment`, `selectedTab`, `isBookmarked`, `userNotes` | `EquipmentRepository`, `UserDataRepository` |
| `BoilerViewModel` | `BoilerUiState` | `isLoading`, `gasBoilers`, `jhutBoilers`, `selectedTab` | `BoilerRepository` |
| `CalculationViewModel` | `CalculationListUiState`, `CalculationRunnerUiState` | `calculations`, `definition`, `inputValues`, `validationErrors`, `result`, `isExecuting`, `historySaved` | `CalculationRepository`, `UserDataRepository` |
| `SearchViewModel` | `SearchUiState` | `query`, `results`, `isLoading`, `hasSearched`, `selectedFamilyFilter` | `SearchRepository` |
| `NotesViewModel` | `NotesUiState` | `notes`, `bookmarks`, `isLoading`, `isEditorOpen`, `editorTitle`, `editorBody`, `editingNoteId` | `UserDataRepository` |
| `ChecklistViewModel` | `ChecklistUiState` | `checklists`, `selectedChecklistId`, `completedItemIds` | Built-in Industrial Operating Procedures |
| `SettingsViewModel` | `SettingsUiState` | `themeMode`, `isLargePlantFloorFont`, `isExporting`, `exportedJson`, `totalNotesCount`, `totalBookmarksCount` | `UserDataRepository` |

---

## 3. Asynchronous Flow & Concurrency Discipline

- **Coroutines:** All database queries and computations run on `Dispatchers.IO` via repository implementations, exposing lightweight `Flow<T>` streams to the presentation layer.
- **Debounced Search:** `SearchViewModel` uses `.debounce(300)` on search inputs to eliminate unnecessary database query churn during typing.
- **Safety Validations:** Formula input boundaries and physical constraints (`validatePositiveFinite`, `validateNonNegativeFinite`) are verified before mathematical execution.
