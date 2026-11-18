# PHASE 03: BACKUP AND DATA PROTECTION ARCHITECTURE
**Document Identifier:** DOC-MPN-017  
**Project:** Mechanical Pro Notes (Personal Reference App)  
**Classification Standard:** Strict 6-Tier Evidence Classification  
**Status:** PROPOSED BACKUP & DATA PROTECTION SPECIFICATION - PENDING OWNER APPROVAL  
**Author:** Senior Android Architect, Security & Offline Storage Specialist  
**Date:** 2026-11-12  

---

## 1. Executive Security & Privacy Posture: Absolute Isolation

Mechanical Pro Notes is a **strictly personal-use reference and plant maintenance tool**. It does not collect user analytics, does not track usage patterns, and contains zero advertising or commercial SDKs.

### Inviolable Privacy Safeguards:
1. **Zero Internet Permission (`android.permission.INTERNET` Excluded):**
   The application's `AndroidManifest.xml` will **NOT request the INTERNET permission**. This provides an unbreachable hardware-level operating system guarantee that no technical note, plant observation, or calculation data can ever be transmitted outside the owner's phone.
2. **Android Application Sandbox Protection:**
   The SQLite database, preferences, and personal notes reside exclusively within the internal application storage sandbox:
   `/data/data/com.researchbangla.mechanicalpronotes/databases/`
   On non-rooted Android devices, no other third-party application can access, read, or modify these files.

---

## 2. Dynamic vs Static Data Architecture

To design an effective backup system, data within the app is strictly partitioned:

```
+-------------------------------------------------------------------------------+
|                       MECHANICAL PRO NOTES DATA UNIVERSE                      |
+-------------------------------------------------------------------------------+
|                                                                               |
|   STATIC REFERENCE CORPUS (Pre-Packaged)    |    DYNAMIC USER DATA (Personal) |
|   - 64 Equipment Assets                     |    - Personal Equipment Notes   |
|   - 21-Section Knowledge Articles           |    - Saved Bookmarks            |
|   - 14 Boiler Engineering Calculations      |    - Custom Maintenance Logs    |
|   - The Boilers Act, 2022 Statutory Text    |    - Completed Checklists       |
|   - Technical Standards & Specifications    |    - Calculation History        |
|                                             |                                 |
|   [ Immutable, Bundled in APK Assets ]      |    [ User Created, Exportable ] |
+-------------------------------------------------------------------------------+
```

### Architectural Benefit:
Because the static reference corpus is always safely reconstructible from the application build, the backup engine **only needs to back up dynamic user data**. This makes backup files extremely lightweight (typically $10\text{–}100\text{ KB}$), human-readable, and immune to schema breaking changes.

---

## 3. Local Backup Architecture (Android Storage Access Framework)

The app utilizes the standard Android **Storage Access Framework (SAF)**, allowing the owner to choose where to save backups (Internal Storage, SD Card, or personal USB drive) without requiring dangerous storage permissions (`READ_EXTERNAL_STORAGE` / `WRITE_EXTERNAL_STORAGE`).

```
[ User Taps "Export Backup" in Settings ]
                   |
                   v
[ Android System File Picker (ACTION_CREATE_DOCUMENT) ]
                   | (User selects destination folder and file name)
                   v
+-------------------------------------------------------------------------------+
|                       ExportUserDataUseCase (Domain)                          |
|  1. Queries UserNoteDao, BookmarkDao, CalculationHistoryDao.                  |
|  2. Serializes data into structured JSON with schema version and metadata.    |
|  3. Calculates SHA-256 integrity checksum.                                    |
|  4. Writes stream directly to chosen Uri via ContentResolver.                 |
+-------------------------------------------------------------------------------+
                   |
                   v
[ Output File: MechanicalProNotes_Backup_20261112_1430.json ]
```

### Standardized Backup JSON Format:
```json
{
  "metadata": {
    "app_name": "Mechanical Pro Notes",
    "backup_version": 1,
    "app_version_code": 100,
    "export_timestamp_utc": "2026-11-12T14:30:00Z",
    "sha256_checksum": "a8f5c2d...7e9"
  },
  "user_notes": [
    {
      "equipment_id": "EQ-A01",
      "title": "Belly Washer Bearing Greasing",
      "body": "Replaced drive-end plummer block grease with Mobilith SHC 220. Noise reduced.",
      "created_at": 1794500000000,
      "updated_at": 1794500000000
    }
  ],
  "bookmarks": [
    {
      "entity_type": "EQUIPMENT",
      "entity_id": "EQ-G01",
      "created_at": 1794500000000
    }
  ],
  "calculation_history": [
    {
      "calculation_id": "CALC-05",
      "timestamp": 1794500000000,
      "inputs": { "TDS_feed": 120, "TDS_boiler": 3200 },
      "outputs": { "blowdown_rate_percent": 3.9 }
    }
  ]
}
```

---

## 4. Restore Workflow, Validation & Corruption Handling

When the user selects a backup file to restore (`ACTION_OPEN_DOCUMENT`):

1. **Format & Version Verification:**
   - The parser checks that `app_name` equals `"Mechanical Pro Notes"` and `backup_version` is supported.
   - If a newer backup version is opened on an older app release, the app displays a clear warning: *"This backup was created with a newer app version. Please update the app before restoring."*
2. **Integrity Checksum Validation:**
   - The payload SHA-256 hash is recalculated and compared with `sha256_checksum`. If corrupted or tampered, the restore process aborts immediately.
3. **Merge vs Overwrite Policy:**
   - The user is presented with two explicit choices:
     - **Merge (Recommended):** Combines backup notes with current notes on the phone. Conflicting notes with the same equipment ID are preserved with a timestamp suffix (e.g., `(Restored Copy)`).
     - **Full Overwrite:** Clears current user notes and restores the backup state.
4. **Transactional Safety:**
   - Room executes the restore inside an atomic SQLite transaction (`runInTransaction`). If an unexpected I/O error occurs halfway through, the transaction rolls back cleanly, leaving the existing user data completely intact.
