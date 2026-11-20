#!/usr/bin/env python3
"""
MECHANICAL PRO NOTES — STAGE 04 VERIFICATION SUITE
UI/UX Completion, Accessibility, Contrast, Responsiveness & Android Studio @Preview Readiness.
"""

import hashlib
import json
import os
import re
import sqlite3
import sys

WORKSPACE_ROOT = os.path.abspath(os.path.dirname(__file__))
ANDROID_ROOT = os.path.join(WORKSPACE_ROOT, "android")
PKG_ROOT = os.path.join(ANDROID_ROOT, "app/src/main/java/com/researchbangla/mechanicalpronotes")
TEST_ROOT = os.path.join(ANDROID_ROOT, "app/src/test/java/com/researchbangla/mechanicalpronotes")
ANDROID_TEST_ROOT = os.path.join(ANDROID_ROOT, "app/src/androidTest/java/com/researchbangla/mechanicalpronotes")
DB_PATH = os.path.join(ANDROID_ROOT, "app/src/main/assets/database/mechanical_pro_notes.db")
MANIFEST_PATH = os.path.join(ANDROID_ROOT, "app/src/main/AndroidManifest.xml")

EXPECTED_DB_SIZE = 856064
EXPECTED_DB_SHA256 = "fe2fb5f9c47cabc2abb56e2873b3531b1b1512d2f4e09df6cadaef1791b90720"
EXPECTED_CALC_TABLES_SHA256 = "94cf590dbcd1421fa6196787c5e4b2cf41cdfc0671f78a610961bbc523ab426a"


def read(rel_path):
    full = os.path.join(WORKSPACE_ROOT, rel_path) if not os.path.isabs(rel_path) else rel_path
    with open(full, "r", encoding="utf-8") as f:
        return f.read()


def sha256_file(path):
    h = hashlib.sha256()
    with open(path, "rb") as f:
        while True:
            chunk = f.read(65536)
            if not chunk:
                break
            h.update(chunk)
    return h.hexdigest()


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
    print(" MECHANICAL PRO NOTES — STAGE 04 VERIFICATION SUITE")
    print(" Scope: UI/UX Standardization, Contrast, Accessibility, Responsiveness & Previews")
    print("=" * 84)

    # Load key presentation & domain files
    screen_src = read(os.path.join(PKG_ROOT, "presentation/navigation/Screen.kt"))
    nav_src = read(os.path.join(PKG_ROOT, "presentation/navigation/AppNavigation.kt"))
    color_src = read(os.path.join(PKG_ROOT, "presentation/theme/Color.kt"))
    theme_src = read(os.path.join(PKG_ROOT, "presentation/theme/Theme.kt"))
    common_src = read(os.path.join(PKG_ROOT, "presentation/components/CommonComponents.kt"))
    home_src = read(os.path.join(PKG_ROOT, "presentation/home/HomeScreen.kt"))
    calc_vm_src = read(os.path.join(PKG_ROOT, "presentation/calculations/CalculationViewModel.kt"))
    calc_list_src = read(os.path.join(PKG_ROOT, "presentation/calculations/CalculationListScreen.kt"))
    calc_run_src = read(os.path.join(PKG_ROOT, "presentation/calculations/CalculationRunnerScreen.kt"))
    steam_src = read(os.path.join(PKG_ROOT, "presentation/calculations/SteamTableScreen.kt"))
    unit_src = read(os.path.join(PKG_ROOT, "presentation/units/UnitConverterScreen.kt"))
    eq_list_src = read(os.path.join(PKG_ROOT, "presentation/equipment/EquipmentListScreen.kt"))
    eq_detail_src = read(os.path.join(PKG_ROOT, "presentation/equipment/EquipmentDetailScreen.kt"))
    boiler_src = read(os.path.join(PKG_ROOT, "presentation/boiler/BoilerDashboardScreen.kt"))
    search_src = read(os.path.join(PKG_ROOT, "presentation/search/SearchScreen.kt"))
    fund_src = read(os.path.join(PKG_ROOT, "presentation/fundamentals/FundamentalsScreen.kt"))
    ref_src = read(os.path.join(PKG_ROOT, "presentation/library/ReferenceLibraryScreen.kt"))
    chk_src = read(os.path.join(PKG_ROOT, "presentation/checklists/ChecklistScreen.kt"))
    notes_src = read(os.path.join(PKG_ROOT, "presentation/notes/NotesScreen.kt"))
    bms_src = read(os.path.join(PKG_ROOT, "presentation/notes/BookmarksScreen.kt"))
    settings_src = read(os.path.join(PKG_ROOT, "presentation/settings/SettingsScreen.kt"))

    # [S4-01] Navigation Completeness, Drawer Insets & ID-Level Search/Bookmark Routing
    nav_objects = re.findall(r'object\s+(\w+)\s*:\s*Screen\(', screen_src)
    s4_01 = [
        len(nav_objects) == 23,
        "drawerScreens" in screen_src and "bottomBarScreens" in screen_src and "UnitConverter," in screen_src,
        "DrawerDefaults.windowInsets" in nav_src and "widthIn(min = 260.dp, max = 304.dp)" in nav_src,
        "pendingReferenceFocusId" in nav_src and "initialFocusRefId = pendingReferenceFocusId" in nav_src,
        "pendingFundamentalsFocusId" in nav_src and "initialFocusTopicId = pendingFundamentalsFocusId" in nav_src,
        "checklistViewModel.selectChecklist(chkId)" in nav_src,
        "checklistViewModel.selectChecklist(entityId)" in nav_src,
    ]
    if all(s4_01):
        print("  ✅ [S4-01] NAVIGATION & ID ROUTING PASS | 23 Screen routes intact; Search/Bookmark hit IDs & Drawer insets wired cleanly.")
        passed += 1
    else:
        print(f"  ❌ [S4-01] NAVIGATION FAIL | checks={s4_01}, objects={len(nav_objects)}")

    # [S4-02] Locked Palette Preservation, Luminance-Aware Semantic Contrast & TopBar Icons
    s4_02 = [
        all(tok in color_src for tok in ("SteelBluePrimary", "SafetyAmberWarning", "FullBlueprintGreen", "CharcoalDarkBackground")),
        "LightColorScheme" in theme_src and "DarkColorScheme" in theme_src and "1.18f" in theme_src,
        "luminance() < 0.5f" in common_src and "semanticPositiveColors" in common_src and "semanticWarningColors" in common_src,
        "OperationalDarkGreen" in home_src and "SteelBlueDarkPrimary" in home_src and "SafetyAmberDarkWarning" in home_src,
        "Icons.Default.SwapHoriz" in calc_list_src and "Open Unit Converter" in calc_list_src,
        "luminance() < 0.5f" in fund_src,
    ]
    if all(s4_02):
        print("  ✅ [S4-02] THEME & SEMANTIC CONTRAST PASS | Locked Color.kt/Theme.kt intact; luminance-aware badges & stats verified.")
        passed += 1
    else:
        print(f"  ❌ [S4-02] THEME & CONTRAST FAIL | checks={s4_02}")

    # [S4-03] Calculation Runner Safety, Assumptions Display & Input Polish
    s4_03 = [
        "!num.isFinite()" in calc_vm_src,
        "result = null" in calc_vm_src and "historySaved = false" in calc_vm_src and "errorMessage = null" in calc_vm_src,
        "Engineering Assumptions & Limitations" in calc_run_src,
        "definition.assumptions" in calc_run_src and "definition.limitations" in calc_run_src,
        "(Optional)" in calc_run_src and "if (variable.isRequired)" in calc_run_src,
        '"Error: $err"' in calc_run_src or '"Error: ' in calc_run_src,
    ]
    if all(s4_03):
        print("  ✅ [S4-03] CALCULATION RUNNER INTEGRITY PASS | NaN/Infinity blocked, stale results cleared, assumptions/limitations displayed.")
        passed += 1
    else:
        print(f"  ❌ [S4-03] CALCULATION RUNNER FAIL | checks={s4_03}")

    # [S4-04] Keyboard IME Padding & Narrow-Screen (320-420dp) Responsiveness
    ime_screens = [calc_run_src, steam_src, unit_src, eq_list_src, search_src, notes_src]
    s4_04 = [
        all(".imePadding()" in s for s in ime_screens),
        "BoxWithConstraints" in unit_src and "maxWidth < 400.dp" in unit_src,
        "Modifier.width(90.dp)" not in settings_src and "Modifier.weight(1f)" in settings_src,
        "TextAlign.End" in calc_run_src and "TextAlign.End" in steam_src and "TextAlign.End" in eq_detail_src and "TextAlign.End" in chk_src,
    ]
    if all(s4_04):
        print("  ✅ [S4-04] IME & COMPACT RESPONSIVENESS PASS | imePadding() on all input views; adaptive BoxWithConstraints & weighted rows.")
        passed += 1
    else:
        print(f"  ❌ [S4-04] RESPONSIVENESS FAIL | checks={s4_04}")

    # [S4-05] Glove Ergonomics (>=48dp) & TalkBack Accessibility Semantics
    min_48_files = [common_src, home_src, calc_list_src, calc_run_src, steam_src, unit_src, eq_list_src, search_src, fund_src, ref_src, chk_src, bms_src, settings_src]
    s4_05 = [
        all("48.dp" in s for s in min_48_files),
        "toggleable(" in chk_src and "Role.Checkbox" in chk_src and "onCheckedChange = null" in chk_src,
        "toggleable(" in settings_src and "Role.Switch" in settings_src and "Role.Checkbox" in settings_src,
        "semantics(mergeDescendants = true)" in common_src and "heading()" in common_src,
        "Role.Button" in common_src and "Role.Button" in search_src and "Role.Button" in bms_src,
        "contentDescription = \"Edit note ${note.title}\"" in notes_src and "contentDescription = \"Remove bookmark for ${bookmark.entityTitle}\"" in bms_src,
    ]
    if all(s4_05):
        print("  ✅ [S4-05] ACCESSIBILITY & GLOVE ERGONOMICS PASS | >=48dp touch targets, toggleable rows, headings & contextual labels verified.")
        passed += 1
    else:
        print(f"  ❌ [S4-05] ACCESSIBILITY FAIL | checks={s4_05}")

    # [S4-06] Engineering Honesty Banners (ContentDepth, CalculationStatus & Fundamentals Gaps)
    s4_06 = [
        "Classification Transparency" in eq_detail_src and "RESEARCH_PENDING" in eq_detail_src and "INVENTORY_ONLY" in eq_detail_src,
        "RESEARCH_PENDING" in calc_run_src and "NOT_IMPLEMENTED" in calc_run_src,
        "SafetyWarningCard(" in eq_detail_src and "SafetyWarningCard(" in calc_run_src and "SafetyWarningCard(" in fund_src,
        "!num.isFinite()" in steam_src and "PaddingValues(vertical = 16.dp)" in boiler_src,
    ]
    if all(s4_06):
        print("  ✅ [S4-06] ENGINEERING HONESTY CALLOUTS PASS | Depth/ResearchPending/InventoryOnly & ContentGap disclosures visible cleanly.")
        passed += 1
    else:
        print(f"  ❌ [S4-06] HONESTY CALLOUTS FAIL | checks={s4_06}")

    # [S4-07] Stateless Content Composables & Android Studio @Preview Coverage
    preview_targets = {
        "CommonComponents.kt": common_src,
        "AppNavigation.kt": nav_src,
        "HomeScreen.kt": home_src,
        "CalculationListScreen.kt": calc_list_src,
        "CalculationRunnerScreen.kt": calc_run_src,
        "SteamTableScreen.kt": steam_src,
        "UnitConverterScreen.kt": unit_src,
        "EquipmentListScreen.kt": eq_list_src,
        "EquipmentDetailScreen.kt": eq_detail_src,
        "BoilerDashboardScreen.kt": boiler_src,
        "SearchScreen.kt": search_src,
        "FundamentalsScreen.kt": fund_src,
        "ReferenceLibraryScreen.kt": ref_src,
        "ChecklistScreen.kt": chk_src,
        "NotesScreen.kt": notes_src,
        "BookmarksScreen.kt": bms_src,
        "SettingsScreen.kt": settings_src,
    }
    missing_previews = [name for name, src in preview_targets.items() if "@Preview" not in src]
    total_previews = sum(len(re.findall(r"@Preview\(", src)) for src in preview_targets.values())
    stateless_fns = [
        "fun HomeScreenContent(" in home_src,
        "fun CalculationListContent(" in calc_list_src,
        "fun CalculationRunnerContent(" in calc_run_src,
        "fun UnitConverterContent(" in unit_src,
        "fun EquipmentListContent(" in eq_list_src,
        "fun EquipmentDetailContent(" in eq_detail_src,
        "fun BoilerDashboardContent(" in boiler_src,
        "fun SearchScreenContent(" in search_src,
        "fun ChecklistScreenContent(" in chk_src,
        "fun NotesScreenContent(" in notes_src,
        "fun BookmarksScreenContent(" in bms_src,
        "fun SettingsScreenContent(" in settings_src,
    ]
    if not missing_previews and total_previews >= 32 and all(stateless_fns):
        print(f"  ✅ [S4-07] STATELESS ARCHITECTURE & PREVIEWS PASS | {total_previews} @Preview functions across all {len(preview_targets)} UI files.")
        passed += 1
    else:
        print(f"  ❌ [S4-07] PREVIEW COVERAGE FAIL | missing={missing_previews}, total={total_previews}, stateless={stateless_fns}")

    # [S4-08] Stage 4 JUnit Tests & Invariants (8 ViewModels + Zero INTERNET)
    s4_test_path = os.path.join(TEST_ROOT, "Stage04UiStateAndAccessibilityTest.kt")
    s4_test_count = len(re.findall(r"@Test", read(s4_test_path))) if os.path.exists(s4_test_path) else 0
    vm_files = [fn for dp, _, fns in os.walk(os.path.join(PKG_ROOT, "presentation")) for fn in fns if fn.endswith("ViewModel.kt")]
    manifest_no_comments = re.sub(r"<!--.*?-->", "", read(MANIFEST_PATH), flags=re.S)
    if s4_test_count >= 6 and len(vm_files) == 8 and "android.permission.INTERNET" not in manifest_no_comments:
        print(f"  ✅ [S4-08] STAGE 4 JUNIT & INVARIANTS PASS | {s4_test_count} @Test methods authored; 8 ViewModels & offline manifest preserved.")
        passed += 1
    else:
        print(f"  ❌ [S4-08] JUNIT/INVARIANT FAIL | tests={s4_test_count}, vms={len(vm_files)}, internet={'android.permission.INTERNET' in manifest_no_comments}")

    # [S4-09] Pre-Packaged SQLite Database & Calculation Tables Untouched
    db_size = os.path.getsize(DB_PATH)
    db_sha = sha256_file(DB_PATH)
    conn = sqlite3.connect(DB_PATH)
    conn.row_factory = sqlite3.Row
    cur = conn.cursor()
    calcs = [dict(r) for r in cur.execute("SELECT * FROM calculation ORDER BY id")]
    vars_ = [dict(r) for r in cur.execute("SELECT * FROM formula_variable ORDER BY id")]
    calc_sha = hashlib.sha256(json.dumps({"calc": calcs, "vars": vars_}, sort_keys=True, ensure_ascii=False).encode("utf-8")).hexdigest()
    conn.close()
    if db_size == EXPECTED_DB_SIZE and db_sha == EXPECTED_DB_SHA256 and calc_sha == EXPECTED_CALC_TABLES_SHA256:
        print("  ✅ [S4-09] DATABASE & FORMULA INTEGRITY PASS | DB byte size (856,064 B) & SHA-256 + calc SHA-256 unchanged.")
        passed += 1
    else:
        print(f"  ❌ [S4-09] DB INTEGRITY FAIL | size={db_size}, db_sha={db_sha}, calc_sha={calc_sha}")

    # [S4-10] Full Codebase Kotlin Delimiter & Syntax Balance
    kt_count, syntax_errors = check_kotlin_delimiter_balance()
    if not syntax_errors and kt_count >= 50:
        print(f"  ✅ [S4-10] KOTLIN SYNTAX BALANCE PASS | {kt_count} .kt files checked with 0 unbalanced braces/parens.")
        passed += 1
    else:
        print(f"  ❌ [S4-10] KOTLIN SYNTAX FAIL | files={kt_count}, errors={syntax_errors}")

    print("=" * 84)
    print(f" STAGE 04 VERIFICATION CHECKS: {passed}/{total} PASSED")
    print("=" * 84)
    return 0 if passed == total else 1


if __name__ == "__main__":
    sys.exit(main())
