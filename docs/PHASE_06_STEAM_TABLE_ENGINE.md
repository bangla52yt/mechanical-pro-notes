# PHASE 06 STEAM TABLE ENGINE
## Offline Thermodynamic Saturated Steam Properties Engine (IAPWS-IF97 Region 4)

**Document Identifier:** DOC-MPN-016  
**Project:** Mechanical Pro Notes (Personal Offline-First Mechanical Reference Android App)  
**Phase:** Phase 06 & Batch 14 — Thermodynamic Assessment & Remediation  
**Status:** IMPLEMENTED & MATHEMATICALLY BENCHMARKED AGAINST IAPWS-IF97 REGION 4 EQUATIONS *(External NIST REFPROP binary audit remains external to offline container)*  
**Date:** 18 November 2026  
**Governing Standard:** IAPWS-IF97 (*Revised Release on the IAPWS Industrial Formulation 1997 for the Thermodynamic Properties of Water and Steam*, August 2007), Region 4 Saturation Boundary ONLY  
**Classification:** [A. VERIFIED CURRENT FACT] / [C. OWNER-APPROVED REQUIREMENT]

---

## 1. Thermodynamic Scope & Explicit Unsupported Regions

Industrial textile washing plants and garment dyeing facilities operate saturated steam boilers and distribution headers within $0.50 - 20.00\text{ bar(a)}$:
- Deaeration and vacuum-break preheating: $0.50 - 1.80\text{ bar(a)}$ ($81.32^\circ\text{C} - 116.92^\circ\text{C}$)
- Garment washing direct sparge and process coils: $2.50 - 4.50\text{ bar(a)}$ ($127.41^\circ\text{C} - 147.91^\circ\text{C}$)
- Distribution headers and industrial ironers: $6.00 - 8.00\text{ bar(a)}$ ($158.83^\circ\text{C} - 170.41^\circ\text{C}$)
- Firetube packaged boiler generation: $8.00 - 12.00\text{ bar(a)}$ ($170.41^\circ\text{C} - 187.96^\circ\text{C}$)
- Maximum engine envelope: $20.00\text{ bar(a)}$ ($212.38^\circ\text{C}$)

### Explicitly Unsupported Regions (Out of Scope)
`IapwsIf97SteamEngine.kt` implements **saturation properties along the Region 4 vapor–liquid boundary ($0.50\text{–}20.00\text{ bar a}$) ONLY** and does **NOT** calculate:
- **Sub-atmospheric vacuum:** $P < 0.50\text{ bar(a)}$ (rejected by `IllegalArgumentException`)
- **Region 1:** Compressed / subcooled liquid water ($T < T_{sat}(P)$)
- **Region 2:** Superheated steam ($T > T_{sat}(P)$)
- **Region 3:** Near-critical and supercritical fluid ($P > 20.00\text{ bar a}$ up to $P_c = 220.64\text{ bar}$, $T_c = 373.946^\circ\text{C}$)
- **Region 5:** High-temperature steam above $800^\circ\text{C}$
- **Backward equations:** $T(p,h)$, $T(p,s)$

---

## 2. Mathematical Formulation & Interpolation Architecture

### 2.1 Saturation Temperature — Exact IAPWS-IF97 Region 4 Equation 31
Batch 14 replaced linear grid $T_{sat}$ interpolation (which reached $-0.0511^\circ\text{C}$ midpoint error at $4.25\text{ bar a}$, exceeding the original $\pm 0.05^\circ\text{C}$ KDoc claim) with the **exact IAPWS-IF97 Region 4 backward saturation equation (Eq. 31)** over $P_{\text{MPa}} = 0.1 \cdot P_{\text{bar a}}$:
$$\beta = P_{\text{MPa}}^{1/4}, \quad E = \beta^2 + n_3 \beta + n_6, \quad F = n_1 \beta^2 + n_4 \beta + n_7, \quad G = n_2 \beta^2 + n_5 \beta + n_8$$
$$D = \frac{2G}{-F - \sqrt{F^2 - 4EG}}, \quad T_{sat}(\text{K}) = \frac{n_{10} + D - \sqrt{(n_{10} + D)^2 - 4(n_9 + n_{10} D)}}{2}$$
Verified against IAPWS-IF97 Table 36 test points (`0.1 MPa -> 372.755919 K`, `1.0 MPa -> 453.035632 K`, `10.0 MPa -> 584.149488 K`) within $\le 3.9 \times 10^{-7}\text{ K}$.

### 2.2 56-Node High-Density Saturation Grid
Instead of sparse 18-node Hermite splines, `IapwsIf97SteamEngine.kt` embeds **56 verified saturation anchor rows** from `0.50` to `20.00 bar(a)` (including standard atmosphere `1.01325 bar a`). Representative anchor nodes match ITS-90 / IAPWS-IF97 (`max |ΔT_sat| = 0.0048 °C`, `|h_f + h_fg - h_g| <= 0.1 kJ/kg` on all 56 rows):

| Node $P\text{ (bar a)}$ | $T_{sat}\text{ (}^\circ\text{C)}$ | $h_f\text{ (kJ/kg)}$ | $h_{fg}\text{ (kJ/kg)}$ | $h_g\text{ (kJ/kg)}$ | $v_g\text{ (m}^3\text{/kg)}$ | $v_f\text{ (m}^3\text{/kg)}$ |
|---|---|---|---|---|---|---|
| **0.50** | 81.32 | 340.54 | 2304.8 | 2645.3 | 3.2402 | 0.0010299 |
| **0.75** | 91.76 | 384.42 | 2278.0 | 2662.4 | 2.2171 | 0.0010372 |
| **1.00** | 99.61 | 417.50 | 2257.6 | 2675.1 | 1.6941 | 0.0010432 |
| **1.01325 (Atm)** | 99.97 | 419.05 | 2256.7 | 2675.8 | 1.6734 | 0.0010434 |
| **2.50** | 127.41 | 535.35 | 2181.3 | 2716.6 | 0.7187 | 0.0010672 |
| **5.00** | 151.84 | 640.07 | 2108.2 | 2748.3 | 0.3748 | 0.0010926 |
| **10.00** | 179.89 | 762.48 | 2014.6 | 2777.1 | 0.1943 | 0.0011272 |
| **15.00** | 198.30 | 844.50 | 1946.4 | 2790.9 | 0.1317 | 0.0011539 |
| **20.00** | 212.38 | 908.42 | 1889.8 | 2798.2 | 0.0996 | 0.0011767 |

### 2.3 Interpolation & Thermodynamic Consistency
- **Enthalpies ($h_f, h_{fg}$) & Liquid Specific Volume ($v_f$):** Piecewise linear-pressure interpolation across the 56 dense nodes (`linearFraction = (P - P_0)/(P_1 - P_0)`), giving $\le 0.03\%$ error at all midpoints.
- **Saturated Vapor Specific Volume ($v_g$):** Batch 14 corrected a defect where linear $P$-fraction was previously passed into `interpolateLog(...)` (producing $+0.145\%$ midpoint curvature error). It now uses true **logarithmic-pressure / logarithmic-$v_g$ interpolation**:
  $$\ln v_g(P) = \ln v_{g,0} + \frac{\ln(P / P_0)}{\ln(P_1 / P_0)} \cdot (\ln v_{g,1} - \ln v_{g,0}), \qquad v_g(P) = \exp(\ln v_g(P))$$
  Reducing $v_g$ interpolation error across all intervals to $\le 0.02\%$.
- **Exact Identities & Input Safety:**
  $$h_g(P) \equiv h_f(P) + h_{fg}(P), \qquad \rho_g(P) \equiv \frac{1}{v_g(P)}$$
  Non-finite (`NaN`, `±Infinity`) inputs and out-of-range pressures reject immediately via `require(...)`, with IEEE-754 boundary clamping after gauge-to-absolute addition.

---

## 3. Benchmark Verification Summary

| Operating / Midpoint Case | Target Pressure | Engine $T_{sat}\text{ (}^\circ\text{C)}$ | Engine $h_g\text{ (kJ/kg)}$ | Engine $v_g\text{ (m}^3\text{/kg)}$ | Verified Delta vs IAPWS-IF97 |
|---|---|---|---|---|---|
| Low Vacuum Boundary | $0.50\text{ bar(a)}$ | $81.32$ | $2645.30$ | $3.2402$ | $< 0.005^\circ\text{C}$, $\le 0.01\%$ |
| Standard Atmosphere | $1.01325\text{ bar(a)}$ | $99.97$ | $2675.80$ | $1.6734$ | $< 0.005^\circ\text{C}$, $\le 0.01\%$ |
| Midpoint ($4.00\text{–}4.50$) | $4.25\text{ bar(a)}$ | $145.81$ | $2740.85$ | $0.4375$ | Fixed from $-0.0511^\circ\text{C}$ / $+0.145\%$ to $< 0.005^\circ\text{C}$ / $< 0.02\%$ |
| Typical Factory Boiler ($7\text{ bar g}$) | $8.01325\text{ bar(a)}$ | $170.48$ | $2768.40$ | $0.2399$ | $< 0.005^\circ\text{C}$, $\le 0.01\%$ |
| Midpoint ($10.00\text{–}11.00$) | $10.50\text{ bar(a)}$ | $182.02$ | $2778.85$ | $0.1855$ | $< 0.005^\circ\text{C}$, $\le 0.02\%$ |
| Upper Grid Boundary | $20.00\text{ bar(a)}$ | $212.38$ | $2798.20$ | $0.0996$ | $< 0.005^\circ\text{C}$, $\le 0.01\%$ |

*Note on NIST REFPROP / CoolProp:* Neither NIST REFPROP nor CoolProp Python binaries are installed inside the offline workspace container. Note also that REFPROP uses IAPWS-95 for pure water (which differs from IAPWS-IF97 by $< 0.01\%$ in this industrial range). An independent desktop NIST REFPROP run remains tracked externally under `P7-P5-03`.

---

## 4. UI Integration & Offline Operation

- **Standalone Tool:** Accessible via `Screen.SteamTable.route` from `CalculationListScreen.kt` and `BoilerDashboardScreen.kt` (`SteamTableScreen.kt`).
- **Chained Consumers:** `CALC-01` (`BoilerThermalCalculations.kt`), `CALC-06` (Flash steam), `CALC-08` (Deaerator balance), and `CALC-14` (`WashPlantMaintenanceCalculations.kt` steam pipe velocity/sizing).
- **Offline & Private:** Embedded entirely inside compiled Kotlin domain code with zero network calls or database reads.
