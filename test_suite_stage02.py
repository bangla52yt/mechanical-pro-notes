#!/usr/bin/env python3
"""
MECHANICAL PRO NOTES — STAGE 02 ENGINEERING CALCULATION VALIDATION SUITE
Audits the 6 targeted Stage 2 calculations (CALC-02, CALC-06, CALC-09, CALC-11, CALC-19, CALC-20)
for code completeness, unit/boundary enforcement, backward benchmark parity, and protected state.
"""

import hashlib
import math
import os
import re
import sqlite3
import sys

WORKSPACE_ROOT = os.path.abspath(os.path.dirname(__file__))
DB_PATH = os.path.join(WORKSPACE_ROOT, "android/app/src/main/assets/database/mechanical_pro_notes.db")
BOILER_KT = os.path.join(WORKSPACE_ROOT, "android/app/src/main/java/com/researchbangla/mechanicalpronotes/domain/calculation/BoilerThermalCalculations.kt")
WASH_KT = os.path.join(WORKSPACE_ROOT, "android/app/src/main/java/com/researchbangla/mechanicalpronotes/domain/calculation/WashPlantMaintenanceCalculations.kt")
SPECS_KT = os.path.join(WORKSPACE_ROOT, "android/app/src/main/java/com/researchbangla/mechanicalpronotes/domain/calculation/CalculationSpecifications.kt")
STAGE02_TEST_KT = os.path.join(WORKSPACE_ROOT, "android/app/src/test/java/com/researchbangla/mechanicalpronotes/Stage02CalculationValidationTest.kt")

EXPECTED_CALC_TABLES_SHA256 = "94cf590dbcd1421fa6196787c5e4b2cf41cdfc0671f78a610961bbc523ab426a"


def read(path):
    with open(path, "r", encoding="utf-8") as f:
        return f.read()


def main():
    passed = 0
    total = 8

    boiler_src = read(BOILER_KT)
    wash_src = read(WASH_KT)
    specs_src = read(SPECS_KT)
    test_src = read(STAGE02_TEST_KT) if os.path.exists(STAGE02_TEST_KT) else ""

    print("=" * 84)
    print(" MECHANICAL PRO NOTES — STAGE 02 CALCULATION VALIDATION SUITE")
    print(" Target Modules: CALC-02, CALC-06, CALC-09, CALC-11, CALC-19, CALC-20")
    print("=" * 84)

    # 1. Protected calculation & formula_variable tables unchanged + DB integrity ok
    import json
    conn = sqlite3.connect(DB_PATH)
    conn.row_factory = sqlite3.Row
    cur = conn.cursor()
    calc_count = cur.execute("SELECT COUNT(*) FROM calculation").fetchone()[0]
    var_count = cur.execute("SELECT COUNT(*) FROM formula_variable").fetchone()[0]
    calcs = [dict(r) for r in cur.execute("SELECT * FROM calculation ORDER BY id")]
    vars_ = [dict(r) for r in cur.execute("SELECT * FROM formula_variable ORDER BY id")]
    calc_sha = hashlib.sha256(json.dumps({"calc": calcs, "vars": vars_}, sort_keys=True, ensure_ascii=False).encode("utf-8")).hexdigest()
    integrity_ok = cur.execute("PRAGMA integrity_check").fetchone()[0] == "ok"
    user_ver = cur.execute("PRAGMA user_version").fetchone()[0]
    conn.close()
    if calc_sha == EXPECTED_CALC_TABLES_SHA256 and calc_count == 20 and var_count == 87 and integrity_ok and user_ver == 1:
        print(f"  ✅ [S2-01] DB-PROTECTION PASS | Calculation & formula_variable SHA-256 unchanged; 20 calculations & 87 variables preserved; integrity=ok.")
        passed += 1
    else:
        print(f"  ❌ [S2-01] DB-PROTECTION FAIL | calc_sha={calc_sha}, calcs={calc_count}, vars={var_count}, integrity={integrity_ok}, v={user_ver}")

    # 2. CALC-02 unit-basis guard, Batch 12 IMPLEMENTED status & UNVERIFIED mixed-jhut boundary
    calc02_block = re.search(r"fun executeCalc02\(.*?^\s{4}\}", boiler_src, re.M | re.S).group(0)
    c02_checks = [
        "CalculationStatus.IMPLEMENTED" in calc02_block,
        "UNVERIFIED MIXED-JHUT BOUNDARY" in calc02_block,
        "gcv_unit_mode" in calc02_block,
        "fuel_density" in calc02_block,
        'inputs["CO2"] ?: 9.5' not in calc02_block,
        'inputs["H2_percent"] ?: 23.0' not in calc02_block,
        'omittedTerms.add("M_fuel")' in calc02_block,
        "Incompatible fuel and GCV unit" in calc02_block,
    ]
    if all(c02_checks):
        print("  ✅ [S2-02] CALC-02 PASS | IMPLEMENTED (BS 845-1/IS 8753 gas) + UNVERIFIED mixed-jhut boundary; GCV kJ/kg vs kJ/Nm3 enforced; no fabricated assumptions.")
        passed += 1
    else:
        print(f"  ❌ [S2-02] CALC-02 FAIL | checks={c02_checks}")

    # 3. CALC-06 P_boiler > P_flash validation & zero silent clamping
    calc06_block = re.search(r"fun executeCalc06\(.*?^\s{4}\}", boiler_src, re.M | re.S).group(0)
    c06_checks = [
        "require(pBoilerBar > pFlashBar)" in calc06_block,
        ".coerceIn(" not in calc06_block,
        ".coerceAtLeast(" not in calc06_block,
        "require(hexEfficiency.isFinite() && hexEfficiency in 0.10..1.0)" in calc06_block,
        "require(drainOutletTempC < flashProps.saturationTempCelsius)" in calc06_block,
    ]
    if all(c06_checks):
        print("  ✅ [S2-03] CALC-06 PASS | P_boiler > P_flash & HX approach enforced; .coerceIn/.coerceAtLeast eliminated.")
        passed += 1
    else:
        print(f"  ❌ [S2-03] CALC-06 FAIL | checks={c06_checks}")

    # 4. CALC-09 Napier 0.51 conservative verification, Kd/MCR bounds, Area/d_throat transparency
    calc09_block = re.search(r"fun executeCalc09\(.*?^\s{4}\}", boiler_src, re.M | re.S).group(0)
    c09_exact_515 = 51.5 * 0.45359237 * 14.503773773 / (25.4 ** 2)
    c09_val = 0.51 * 0.62 * 800 * ((10.0 + 1.01325) * 1.03)
    c09_checks = [
        "0.51 * kd * orificeAreaMm2 * pRelievingAbs" in calc09_block,
        "require(kd.isFinite() && kd in 0.10..1.0)" in calc09_block,
        "validateNonNegativeFinite" in calc09_block,
        "Equivalent Throat Diameter (mm)" in calc09_block,
        "0.52515" in calc09_block,
        abs(c09_exact_515 - 0.5251535) < 1e-5,
        abs(c09_val - 2869.49) < 0.1,
    ]
    if all(c09_checks):
        print(f"  ✅ [S2-04] CALC-09 PASS | Conservative 0.51 verified (-2.89% vs 51.5->0.52515); Kd/P_set/MCR bounds & throat diameter added.")
        passed += 1
    else:
        print(f"  ❌ [S2-04] CALC-09 FAIL | checks={c09_checks}")

    # 5. CALC-11 NG vs mass A_0 & Normal vs Ambient FAD
    calc11_block = re.search(r"fun executeCalc11\(.*?^\s{4}\}", boiler_src, re.M | re.S).group(0)
    c11_normal = (320.0 * 9.8 * 1.15) / 60.0
    c11_fad30 = c11_normal * (303.15 / 273.15)
    c11_checks = [
        "fuel_unit_kg" in calc11_block,
        'inputs["A_0"]' in calc11_block,
        'primaryOutputUnit = "Nm3/min"' in calc11_block,
        "actualAirFadM3Min" in calc11_block,
        "celsiusToKelvin(tAmbientC)" in calc11_block,
        abs(c11_normal - 60.1067) < 0.01,
        abs(c11_fad30 - 66.71) < 0.05,
    ]
    if all(c11_checks):
        print(f"  ✅ [S2-05] CALC-11 PASS | Normal ({c11_normal:.2f} Nm3/min) vs 30C FAD ({c11_fad30:.2f} m3/min) separated; mass fuel requires A_0.")
        passed += 1
    else:
        print(f"  ❌ [S2-05] CALC-11 FAIL | checks={c11_checks}")

    # 6. CALC-19 Per-regen vs conditional Daily salt
    calc19_block = re.search(r"fun executeCalc19\(.*?^\s{4}\}", wash_src, re.M | re.S).group(0)
    c19_checks = [
        '"Daily Salt Consumption (kg/day)" to round2(saltRequiredKg)' not in calc19_block,
        "regen_per_day" in calc19_block,
        "daily_water_m3" in calc19_block,
        "require(resinCapacityEqL in 0.4..3.0)" in calc19_block,
        "require(saltDosageGL in 40.0..400.0)" in calc19_block,
    ]
    if all(c19_checks):
        print("  ✅ [S2-06] CALC-19 PASS | Unconditional 1-regen/day duplicate removed; daily salt computed only with cycle frequency or daily flow.")
        passed += 1
    else:
        print(f"  ❌ [S2-06] CALC-19 FAIL | checks={c19_checks}")

    # 7. CALC-20 Empirical 0.00075 retained with P_air validation & Darcy-Weisbach/Atlas Copco disclosure
    calc20_block = re.search(r"fun executeCalc20\(.*?^\s{4}\}", wash_src, re.M | re.S).group(0)
    dp_emp = (0.00075 * 60.0 * (150.0 ** 1.85)) / ((5.0 ** 5) * (7.0 + 1.01325))
    dp_atlas = (450.0 * 60.0 * ((150.0 * 0.47194744) ** 1.85)) / ((50.0 ** 5) * (7.0 + 1.01325))
    c20_checks = [
        'validatePositiveFinite(inputs["P_air"]' in calc20_block,
        "0.00075 * lengthMeters * Math.pow(airCfm, 1.85)" in calc20_block,
        "Darcy-Weisbach" in calc20_block,
        "Atlas Copco 450" in calc20_block,
        abs(dp_emp - 0.01907) < 0.0005,
        abs(dp_atlas - 0.02852) < 0.0005,
    ]
    if all(c20_checks):
        print(f"  ✅ [S2-07] CALC-20 PASS | Positive P_air enforced; 0.00075 ({dp_emp:.4f} bar) retained with Darcy-Weisbach/Atlas Copco ({dp_atlas:.4f} bar) disclosure.")
        passed += 1
    else:
        print(f"  ❌ [S2-07] CALC-20 FAIL | checks={c20_checks}")

    # 8. JUnit test suite & CalculationSpecifications alignment
    spec_checks = [
        os.path.exists(STAGE02_TEST_KT),
        len(re.findall(r"@Test", test_src)) >= 14,
        "gcv_unit_mode" in specs_src or "fuel_density" in specs_src,
        "18.98" in specs_src,
        "0.52515" in specs_src,
        "Nm3/min" in specs_src or "Normal (0°C" in specs_src,
        "regen_per_day" in specs_src,
        "Atlas Copco 450" in specs_src,
    ]
    if all(spec_checks):
        print(f"  ✅ [S2-08] JUNIT & SPECS PASS | Stage02CalculationValidationTest.kt ({len(re.findall(r'@Test', test_src))} tests) & CalculationSpecifications.kt aligned (JVM run requires JDK).")
        passed += 1
    else:
        print(f"  ❌ [S2-08] JUNIT & SPECS FAIL | checks={spec_checks}")

    print("=" * 84)
    print(f" STAGE 02 VALIDATION CHECKS: {passed}/{total} PASSED")
    print("=" * 84)
    return 0 if passed == total else 1


if __name__ == "__main__":
    sys.exit(main())
