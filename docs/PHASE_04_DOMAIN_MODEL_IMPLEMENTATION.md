# PHASE 04: DOMAIN MODEL IMPLEMENTATION REPORT
**Document Identifier:** DOC-MPN-021  
**Project:** Mechanical Pro Notes (Personal Reference App)  
**Classification Standard:** Strict 6-Tier Evidence Classification  
**Status:** IMPLEMENTED & VERIFIED  
**Date:** 2026-11-12  

---

## 1. Domain Layer Design & Independence

In accordance with Clean Architecture principles (DEC-013), the Domain layer in `android/app/src/main/java/com/researchbangla/mechanicalpronotes/domain/` is written in **pure Kotlin** with zero dependencies on the Android framework, SQLite, or Room.

### Key Domain Modules:
1. **`ContentDepth`:** Rigid 4-tier enum (`FULL_BLUEPRINT`, `PARTIAL_BLUEPRINT`, `INVENTORY_ONLY`, `RESEARCH_PENDING`). Prevents premature or accidental promotion of inventory records.
2. **`EquipmentModels`:** Rich data classes representing `SystemFamily`, `Equipment`, `Component`, `TechnicalParameter`, `KnowledgeSection` (bounded to sections 1..21), `FailureMode`, and `MaintenanceProcedure`.
3. **`CalculationModels`:** Strongly typed mathematical execution contracts with explicit `CalculationStatus` (`IMPLEMENTED`, `RESEARCH_PENDING`, `NOT_IMPLEMENTED`).
4. **`UserDataModels`:** Encapsulates `UserNote`, `UserBookmark`, and `CalculationHistoryItem`.
5. **`UnitConverters`:** Physical boundary validators and unit conversion engine.

---

## 2. Engineering Unit & Thermodynamic Rules Foundation

The domain unit converter enforces strict physical constraints:

1. **Absolute vs Differential Temperature:**
   - Absolute conversion: $T(K) = T(^\circ\text{C}) + 273.15$ (rejects $T < -273.15^\circ\text{C}$).
   - Differential conversion: $\Delta T(K) = \Delta T(^\circ\text{C})$ (identical interval).
2. **Absolute vs Gauge Pressure:**
   - $P_{abs} = P_{gauge} + 1.01325\text{ bar}$. Rejects gauge pressure below $-1.01325\text{ bar}$ (perfect vacuum).
3. **Physical Boundary Guards:**
   - Division-by-zero is strictly trapped.
   - For boiler blowdown, feedwater TDS must never exceed drum TDS.
   - For steam generation, steam enthalpy must exceed feedwater enthalpy.
   - If inputs violate thermodynamic realities, the domain emits an explicit `ValidationResult.Invalid` rather than producing an unphysical calculation output.
