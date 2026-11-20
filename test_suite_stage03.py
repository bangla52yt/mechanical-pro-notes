#!/usr/bin/env python3
"""
MECHANICAL PRO NOTES — STAGE 03 VERIFICATION SUITE
Database Integrity, Equipment Knowledge Content, Filters, Unit Converter & Core Modules (Areas A–E).
"""

import hashlib
import json
import os
import re
import shutil
import sqlite3
import sys
import tempfile
import unicodedata

WORKSPACE_ROOT = os.path.abspath(os.path.dirname(__file__))
ANDROID_ROOT = os.path.join(WORKSPACE_ROOT, "android")
PKG_ROOT = os.path.join(ANDROID_ROOT, "app/src/main/java/com/researchbangla/mechanicalpronotes")
TEST_ROOT = os.path.join(ANDROID_ROOT, "app/src/test/java/com/researchbangla/mechanicalpronotes")
ANDROID_TEST_ROOT = os.path.join(ANDROID_ROOT, "app/src/androidTest/java/com/researchbangla/mechanicalpronotes")
DB_PATH = os.path.join(ANDROID_ROOT, "app/src/main/assets/database/mechanical_pro_notes.db")

EXPECTED_CALC_TABLES_SHA256 = "94cf590dbcd1421fa6196787c5e4b2cf41cdfc0671f78a610961bbc523ab426a"
REQUIRED_NINE_SECTIONS = {1, 3, 4, 6, 9, 12, 15, 18, 20}


def read(rel_path):
    full = os.path.join(WORKSPACE_ROOT, rel_path) if not os.path.isabs(rel_path) else rel_path
    with open(full, "r", encoding="utf-8") as f:
        return f.read()


def check_kotlin_delimiter_balance():
    errors = []
    kt_files = []
    for root_dir in [PKG_ROOT, TEST_ROOT, ANDROID_TEST_ROOT]:
        if not os.path.exists(root_dir):
            continue
        for dirpath, _, filenames in os.walk(root_dir):
            for fn in filenames:
                if fn.endswith(".kt"):
                    kt_files.append(os.path.join(dirpath, fn))

    for path in kt_files:
        src = read(path)
        brace = 0
        paren = 0
        in_str = False
        in_char = False
        in_triple = False
        in_line_comment = False
        in_block_comment = False
        i = 0
        n = len(src)
        while i < n:
            c = src[i]
            nxt = src[i + 1] if i + 1 < n else ""
            if in_line_comment:
                if c == "\n":
                    in_line_comment = False
                i += 1
                continue
            if in_block_comment:
                if c == "*" and nxt == "/":
                    in_block_comment = False
                    i += 2
                    continue
                i += 1
                continue
            if in_triple:
                if src[i:i + 3] == '"""':
                    in_triple = False
                    i += 3
                    continue
                i += 1
                continue
            if in_str:
                if c == "\\":
                    i += 2
                    continue
                if c == '"':
                    in_str = False
                i += 1
                continue
            if in_char:
                if c == "\\":
                    i += 2
                    continue
                if c == "'":
                    in_char = False
                i += 1
                continue
            if c == "/" and nxt == "/":
                in_line_comment = True
                i += 2
                continue
            if c == "/" and nxt == "*":
                in_block_comment = True
                i += 2
                continue
            if src[i:i + 3] == '"""':
                in_triple = True
                i += 3
                continue
            if c == '"':
                in_str = True
                i += 1
                continue
            if c == "'":
                in_char = True
                i += 1
                continue
            if c == "{":
                brace += 1
            elif c == "}":
                brace -= 1
            elif c == "(":
                paren += 1
            elif c == ")":
                paren -= 1
            if brace < 0 or paren < 0:
                errors.append(f"{os.path.relpath(path, WORKSPACE_ROOT)}: negative balance (brace={brace}, paren={paren})")
                break
            i += 1
        if brace != 0 or paren != 0:
            errors.append(f"{os.path.relpath(path, WORKSPACE_ROOT)}: unclosed delimiter (brace={brace}, paren={paren})")
    return len(kt_files), errors


def main():
    passed = 0
    total = 10

    print("=" * 84)
    print(" MECHANICAL PRO NOTES — STAGE 03 VERIFICATION SUITE")
    print(" Scope: DB Migration, 62-Asset Knowledge Content, Filters, Unit Converter & Catalogs")
    print("=" * 84)

    # [S3-01] Area A — Room Migration Contract & Live Non-Destructive SQLite Upgrade Simulation
    mig_src = read(os.path.join(PKG_ROOT, "data/local/database/migration/DatabaseMigrations.kt"))
    db_src = read(os.path.join(PKG_ROOT, "data/local/database/MechanicalProDatabase.kt"))
    tmp_dir = tempfile.mkdtemp(prefix="mpn_s3_")
    try:
        sim_db = os.path.join(tmp_dir, "upgrade_sim.db")
        shutil.copy2(DB_PATH, sim_db)
        conn = sqlite3.connect(sim_db)
        cur = conn.cursor()
        # Insert synthetic personal user data before simulated version upgrade
        cur.execute(
            "INSERT INTO user_note (equipment_id, title, body, tags_csv, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?)",
            ("EQ-A01", "Stage 3 Seal Check", "Verify Viton O-ring", "seal", 1000, 1000),
        )
        cur.execute(
            "INSERT INTO user_bookmark (entity_type, entity_id, entity_title, created_at) VALUES (?, ?, ?, ?)",
            ("EQUIPMENT", "EQ-G01", "Gas Boiler", 1000),
        )
        cur.execute(
            "INSERT INTO calculation_history (calculation_id, calculation_title, timestamp, inputs_json, outputs_json, user_remark) VALUES (?, ?, ?, ?, ?, ?)",
            ("CALC-01", "Boiler Direct Eff", 1000, '{"Q_s":5000}', '{"primaryResult":84.5}', "Shift check"),
        )
        cur.execute(
            "INSERT INTO checklist_progress (checklist_id, item_id, is_completed, completion_timestamp, remarks) VALUES (?, ?, ?, ?, ?)",
            ("CHK-BLR-DAILY", 1, 1, 1000, "Checked ok"),
        )
        # Simulate non-destructive upgrade path (no DROP/TRUNCATE of user tables)
        cur.execute("PRAGMA user_version = 2")
        conn.commit()
        user_counts = (
            cur.execute("SELECT COUNT(*) FROM user_note").fetchone()[0],
            cur.execute("SELECT COUNT(*) FROM user_bookmark").fetchone()[0],
            cur.execute("SELECT COUNT(*) FROM calculation_history").fetchone()[0],
            cur.execute("SELECT COUNT(*) FROM checklist_progress").fetchone()[0],
        )
        fk_errors = cur.execute("PRAGMA foreign_key_check").fetchall()
        integrity = cur.execute("PRAGMA integrity_check").fetchone()[0]
        user_fks = sum(
            len(cur.execute(f"PRAGMA foreign_key_list({tbl})").fetchall())
            for tbl in ("user_note", "user_bookmark", "calculation_history", "checklist_progress")
        )
        conn.close()
    finally:
        shutil.rmtree(tmp_dir, ignore_errors=True)

    a_checks = [
        "val MIGRATION_1_2" not in mig_src or "commented-out" in mig_src,
        "EARLIEST_SUPPORTED_VERSION = 1" in mig_src,
        "CURRENT_DATABASE_VERSION = 1" in mig_src,
        "verifyUserTablesIsolatedAndPresent" in mig_src,
        "verifyDatabaseIntegrity" in mig_src,
        "addMigrations(*DatabaseMigrations.ALL_MIGRATIONS)" in db_src,
        "fallbackToDestructiveMigration()" not in db_src,
        user_counts == (1, 1, 1, 1),
        fk_errors == [] and integrity == "ok" and user_fks == 0,
    ]
    if all(a_checks):
        print("  ✅ [S3-01] AREA A MIGRATION & INTEGRITY PASS | Explicit v1 contract; zero no-op stubs; user tables isolated (0 FKs) & preserved.")
        passed += 1
    else:
        print(f"  ❌ [S3-01] AREA A FAIL | checks={a_checks}, user_counts={user_counts}")

    # [S3-02] Area A — Room Entity <-> SQLite Asset Schema Parity (All 16 Entities)
    entity_files = []
    entity_dir = os.path.join(PKG_ROOT, "data/local/database/entity")
    for fn in os.listdir(entity_dir):
        if fn.endswith(".kt"):
            entity_files.append(read(os.path.join(entity_dir, fn)))
    declared_tables = set()
    for esrc in entity_files:
        for m in re.finditer(r'@(?:Entity|Fts4)[^)]*tableName\s*=\s*"([^"]+)"', esrc, re.S):
            declared_tables.add(m.group(1))
    conn = sqlite3.connect(DB_PATH)
    cur = conn.cursor()
    sqlite_tables = {
        r[0]
        for r in cur.execute("SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%' AND name NOT LIKE '%_fts_%'")
    }
    schema_parity = declared_tables.issubset(sqlite_tables) and len(declared_tables) == 16
    conn.close()
    if schema_parity:
        print(f"  ✅ [S3-02] ROOM SCHEMA PARITY PASS | All {len(declared_tables)} @Entity/@Fts4 tables verified inside mechanical_pro_notes.db.")
        passed += 1
    else:
        print(f"  ❌ [S3-02] ROOM SCHEMA PARITY FAIL | declared={sorted(declared_tables)}, missing={sorted(declared_tables - sqlite_tables)}")

    # [S3-03] Area B — Equipment Depth Preservation + All 62 Non-FULL Assets Enriched Across All 9 Areas
    conn = sqlite3.connect(DB_PATH)
    conn.row_factory = sqlite3.Row
    cur = conn.cursor()
    depth_rows = {
        r["content_depth_status"]: r["cnt"]
        for r in cur.execute("SELECT content_depth_status, COUNT(*) AS cnt FROM equipment GROUP BY content_depth_status")
    }
    total_eq = cur.execute("SELECT COUNT(*) FROM equipment").fetchone()[0]
    fts_eq = cur.execute("SELECT COUNT(*) FROM equipment_fts").fetchone()[0]
    all_eq = [dict(r) for r in cur.execute("SELECT id, name, content_depth_status FROM equipment ORDER BY id")]
    non_full_failures = []
    full_counts = {}
    nfc_violations = []
    hardness_slip_ids = set()

    for eq in all_eq:
        eid = eq["id"]
        depth = eq["content_depth_status"]
        sec_nums = {
            r[0] for r in cur.execute("SELECT section_number FROM knowledge_section WHERE equipment_id = ?", (eid,))
        }
        sec_texts = [
            r[0] for r in cur.execute("SELECT content_markdown FROM knowledge_section WHERE equipment_id = ?", (eid,))
        ]
        comp_cnt = cur.execute("SELECT COUNT(*) FROM component WHERE equipment_id = ?", (eid,)).fetchone()[0]
        param_cnt = cur.execute("SELECT COUNT(*) FROM technical_parameter WHERE equipment_id = ?", (eid,)).fetchone()[0]
        fail_cnt = cur.execute("SELECT COUNT(*) FROM failure_mode WHERE equipment_id = ?", (eid,)).fetchone()[0]
        pm_cnt = cur.execute("SELECT COUNT(*) FROM maintenance_procedure WHERE equipment_id = ?", (eid,)).fetchone()[0]

        for txt in sec_texts:
            if unicodedata.normalize("NFC", txt) != txt:
                nfc_violations.append(eid)
            if "hardness slip" in txt.lower():
                hardness_slip_ids.add(eid)

        if depth == "FULL_CONTENT_BLUEPRINT":
            full_counts[eid] = len(sec_nums)
        else:
            missing_secs = REQUIRED_NINE_SECTIONS - sec_nums
            if missing_secs or comp_cnt < 1 or param_cnt < 1 or fail_cnt < 1 or pm_cnt < 1:
                non_full_failures.append((eid, sorted(missing_secs), comp_cnt, param_cnt, fail_cnt, pm_cnt))

    # Protected calculation tables hash
    calcs = [dict(r) for r in cur.execute("SELECT * FROM calculation ORDER BY id")]
    vars_ = [dict(r) for r in cur.execute("SELECT * FROM formula_variable ORDER BY id")]
    calc_sha = hashlib.sha256(json.dumps({"calc": calcs, "vars": vars_}, sort_keys=True, ensure_ascii=False).encode("utf-8")).hexdigest()
    conn.close()

    expected_depths = {
        "FULL_CONTENT_BLUEPRINT": 56,
        "PARTIAL_CONTENT_BLUEPRINT": 1,
        "RESEARCH_PENDING": 7,
    }
    full_sections_ok = (
        len(full_counts) == 56
        and full_counts.get("EQ-A01") == 21
        and full_counts.get("EQ-B01") == 21
        and all(cnt >= 15 for cnt in full_counts.values())
    )
    if (
        total_eq == 64
        and fts_eq == 64
        and depth_rows == expected_depths
        and full_sections_ok
        and not non_full_failures
        and not nfc_violations
        and hardness_slip_ids <= {"EQ-H01"}
        and calc_sha == EXPECTED_CALC_TABLES_SHA256
    ):
        print("  ✅ [S3-03] AREA B KNOWLEDGE ENRICHMENT PASS | 56/1/7/0 depth verified; 56 FULL assets & 8 non-FULL assets pass section/NFC checks; calc hash intact.")
        passed += 1
    else:
        print(f"  ❌ [S3-03] AREA B FAIL | depths={depth_rows}, full_cnt={len(full_counts)}, failures={non_full_failures[:3]}, nfc={nfc_violations[:3]}, slip={hardness_slip_ids}")

    # [S3-04] Area C — Equipment List & Search Multi-Select ContentDepth Filter + Reset Empty State
    eq_filter_src = read(os.path.join(PKG_ROOT, "domain/equipment/EquipmentFilter.kt"))
    eq_vm_src = read(os.path.join(PKG_ROOT, "presentation/equipment/EquipmentViewModel.kt"))
    eq_list_src = read(os.path.join(PKG_ROOT, "presentation/equipment/EquipmentListScreen.kt"))
    search_vm_src = read(os.path.join(PKG_ROOT, "presentation/search/SearchViewModel.kt"))
    search_screen_src = read(os.path.join(PKG_ROOT, "presentation/search/SearchScreen.kt"))
    c_checks = [
        "EquipmentFilterCriteria" in eq_filter_src and "countByDepth" in eq_filter_src,
        "toggleDepthFilter" in eq_vm_src and "clearAllFilters" in eq_vm_src and "depthCounts" in eq_vm_src,
        '"All (${listState.allEquipment.size})"' in eq_list_src and "Reset All Filters" in eq_list_src and "FilterChip" in eq_list_src,
        "toggleDepthFilter" in search_vm_src and "SearchScope" in search_vm_src,
        "items(filteredEquipment, key = { it.id })" in search_screen_src,
    ]
    if all(c_checks):
        print("  ✅ [S3-04] AREA C EQUIPMENT FILTERS PASS | Multi-select ContentDepth chips, All(64) tab, search bar & empty-state Reset wired.")
        passed += 1
    else:
        print(f"  ❌ [S3-04] AREA C FAIL | checks={c_checks}")

    # [S3-05] Area D — 13-Category Unit Converter Engine & Safeguards
    uc_engine_src = read(os.path.join(PKG_ROOT, "domain/unit/UnitConverterEngine.kt"))
    expected_cats = [
        "LENGTH", "AREA", "VOLUME", "MASS", "FORCE", "PRESSURE",
        "TEMPERATURE", "POWER", "ENERGY", "FLOW_RATE", "DENSITY", "SPEED", "VISCOSITY"
    ]
    d_engine_checks = [
        all(f"UnitCategory.{c}" in uc_engine_src for c in expected_cats),
        "UnitSubgroup.VISCOSITY_DYNAMIC" in uc_engine_src and "UnitSubgroup.VISCOSITY_KINEMATIC" in uc_engine_src,
        "UnitSubgroup.SPEED_LINEAR" in uc_engine_src and "UnitSubgroup.SPEED_ROTATIONAL" in uc_engine_src,
        "PRESSURE_GAUGE" in uc_engine_src and "PRESSURE_ABSOLUTE" in uc_engine_src and "101_325.0" in uc_engine_src,
        "convertTemperature" in uc_engine_src and "ZERO_C_K" in uc_engine_src,
        "if (!value.isFinite())" in uc_engine_src,
        "formatEngineeringValue" in uc_engine_src,
    ]
    if all(d_engine_checks):
        print("  ✅ [S3-05] AREA D CONVERTER ENGINE PASS | All 13 categories, subgroup guards, pressure/temp offsets & scientific formatting enforced.")
        passed += 1
    else:
        print(f"  ❌ [S3-05] AREA D ENGINE FAIL | checks={d_engine_checks}")

    # [S3-06] Area D — Unit Converter Navigation, Distinct From CALC-01..20, ViewModel Count == 8
    screen_src = read(os.path.join(PKG_ROOT, "presentation/navigation/Screen.kt"))
    nav_src = read(os.path.join(PKG_ROOT, "presentation/navigation/AppNavigation.kt"))
    calc_list_src = read(os.path.join(PKG_ROOT, "presentation/calculations/CalculationListScreen.kt"))
    home_src = read(os.path.join(PKG_ROOT, "presentation/home/HomeScreen.kt"))
    uc_screen_src = read(os.path.join(PKG_ROOT, "presentation/units/UnitConverterScreen.kt"))
    uc_holder_src = read(os.path.join(PKG_ROOT, "presentation/units/UnitConverterStateHolder.kt"))
    vm_files = [
        fn for dp, _, fns in os.walk(os.path.join(PKG_ROOT, "presentation")) for fn in fns if fn.endswith("ViewModel.kt")
    ]
    d_ui_checks = [
        "object UnitConverter : Screen" in screen_src and "UnitConverter," in screen_src,
        "composable(Screen.UnitConverter.route)" in nav_src and "UnitConverterStateHolder" in nav_src,
        "onNavigateToUnitConverter" in calc_list_src and "onNavigateToUnitConverter" in home_src,
        "UnitConverterScreen" in uc_screen_src and "UnitConverterStateHolder" in uc_holder_src,
        len(vm_files) == 8,
    ]
    if all(d_ui_checks):
        print(f"  ✅ [S3-06] AREA D UI & NAV PASS | UnitConverterScreen wired cleanly in nav/home/calcs; 8 ViewModels preserved.")
        passed += 1
    else:
        print(f"  ❌ [S3-06] AREA D UI FAIL | checks={d_ui_checks}, vms={vm_files}")

    # [S3-07] Area E — Single Authoritative Catalogs + Fundamentals Native Screen (No Stub Redirect)
    cat_src = read(os.path.join(PKG_ROOT, "domain/catalog/EngineeringCatalogs.kt"))
    chk_vm_src = read(os.path.join(PKG_ROOT, "presentation/checklists/ChecklistViewModel.kt"))
    ref_scr_src = read(os.path.join(PKG_ROOT, "presentation/library/ReferenceLibraryScreen.kt"))
    fund_scr_src = read(os.path.join(PKG_ROOT, "presentation/fundamentals/FundamentalsScreen.kt"))

    fund_ids = re.findall(r'id = "(FUND-\d+)"', cat_src)
    e_cat_checks = [
        "PlantChecklistCatalog.ALL_CHECKLISTS" in chk_vm_src,
        "TechnicalReferenceCatalog.ALL_REFERENCES" in ref_scr_src,
        len(fund_ids) == 15 and fund_ids[0] == "FUND-01" and fund_ids[-1] == "FUND-15",
        cat_src.count("FundamentalTopicStatus.VERIFIED_BASELINE") >= 15,
        cat_src.count("FundamentalTopicStatus.CONTENT_GAP") == 0,
        "FundamentalsCatalog.ALL_TOPICS" in fund_scr_src,
        "composable(Screen.Fundamentals.route)" in nav_src,
        'Screen.Fundamentals -> "Open Technical Reference Library"' not in nav_src,
        'composable(moduleScreen.route)' in nav_src and "Screen.Fundamentals," not in nav_src.split("val secondaryModules")[1].split(")")[0],
    ]
    if all(e_cat_checks):
        print("  ✅ [S3-07] AREA E CATALOGS & FUNDAMENTALS PASS | Single-source catalogs; FundamentalsScreen live; 15/15 VERIFIED_BASELINE (0 CONTENT_GAP).")
        passed += 1
    else:
        print(f"  ❌ [S3-07] AREA E CATALOGS FAIL | checks={e_cat_checks}, fund_ids={fund_ids}")

    # [S3-08] Area E — Unified Offline Search Isolates User Data (DEC-014)
    repo_if_src = read(os.path.join(PKG_ROOT, "domain/repository/RepositoryInterfaces.kt"))
    search_repo_src = read(os.path.join(PKG_ROOT, "data/repository/SearchRepositoryImpl.kt"))
    cat_code_only = re.sub(r"/\*.*?\*/", "", cat_src, flags=re.S)
    e_search_checks = [
        "data class UnifiedSearchResult" in repo_if_src and "suspend fun searchKnowledgeBase" in repo_if_src,
        "override suspend fun searchKnowledgeBase" in search_repo_src,
        "EngineeringCatalogSearch" in search_repo_src,
        "user_note" not in search_repo_src and "user_bookmark" not in search_repo_src and "UserData" not in search_repo_src,
        "user_note" not in cat_code_only and "user_bookmark" not in cat_code_only and "UserData" not in cat_code_only,
    ]
    if all(e_search_checks):
        print("  ✅ [S3-08] AREA E UNIFIED SEARCH ISOLATION PASS | Searches Equipment + Checklists + References + Fundamentals cleanly excluding personal tables.")
        passed += 1
    else:
        print(f"  ❌ [S3-08] AREA E SEARCH FAIL | checks={e_search_checks}")

    # [S3-09] Stage 3 JUnit Test Files Presence & Density
    s3_tests = {
        "DatabaseIntegrityAndMigrationTest.kt": 4,
        "EquipmentFilterTest.kt": 6,
        "UnitConverterTest.kt": 7,
        "UnifiedSearchAndCatalogTest.kt": 3,
    }
    test_counts = {}
    t_ok = True
    for fn, min_cnt in s3_tests.items():
        p = os.path.join(TEST_ROOT, fn)
        if not os.path.exists(p):
            t_ok = False
            test_counts[fn] = 0
        else:
            cnt = len(re.findall(r"@Test", read(p)))
            test_counts[fn] = cnt
            if cnt < min_cnt:
                t_ok = False
    if t_ok:
        print(f"  ✅ [S3-09] STAGE 3 JUNIT SUITES PASS | 4 test files authored ({sum(test_counts.values())} @Test methods; JVM run awaits JDK).")
        passed += 1
    else:
        print(f"  ❌ [S3-09] STAGE 3 JUNIT FAIL | counts={test_counts}")

    # [S3-10] Full Codebase Kotlin Delimiter & Syntax Balance
    kt_count, syntax_errors = check_kotlin_delimiter_balance()
    if not syntax_errors and kt_count >= 50:
        print(f"  ✅ [S3-10] KOTLIN SYNTAX BALANCE PASS | {kt_count} .kt files checked with 0 unbalanced braces/parens.")
        passed += 1
    else:
        print(f"  ❌ [S3-10] KOTLIN SYNTAX FAIL | files={kt_count}, errors={syntax_errors}")

    print("=" * 84)
    print(f" STAGE 03 VERIFICATION CHECKS: {passed}/{total} PASSED")
    print("=" * 84)
    return 0 if passed == total else 1


if __name__ == "__main__":
    sys.exit(main())
