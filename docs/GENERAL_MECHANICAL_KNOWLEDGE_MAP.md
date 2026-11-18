# GENERAL MECHANICAL ENGINEERING KNOWLEDGE MAP
**Document Identifier:** DOC-MPN-009  
**Project:** Mechanical Pro Notes (Personal Reference App)  
**Classification Standard:** Strict 6-Tier Evidence Classification  
**Status:** COMPLETE 15-DISCIPLINE KNOWLEDGE MAP — MASTER PROMPT 02-A + BATCH 12 (GAP-12)  
**Purpose:** Mapping Theoretical Mechanical Engineering Disciplines to Practical Textile/Garment Washing Plant Equipment and Maintenance  
**Last Updated:** 2026-11-17  

---

## 1. Executive Purpose & Architecture
The General Mechanical Engineering Knowledge Map bridges the gap between fundamental mechanical engineering sciences and practical shop-floor equipment operation, troubleshooting, and maintenance in a textile/garment washing plant. 

Each engineering discipline is detailed with:
1. **Core Theoretical Foundations:** Academic and engineering principles.
2. **Direct Washing Plant Applications:** Real-world machines, utilities, and components where the theory is actively applied.
3. **Key Governing Formulas & Field Relationships:** Essential formulas used for sizing, capacity verification, and performance evaluation.
4. **Governing Failure Mechanisms:** Root causes of equipment breakdown when physical principles are violated.
5. **Cross-References:** Direct linkages to Equipment Matrix IDs (`docs/WASH_PLANT_EQUIPMENT_COVERAGE_MATRIX.md`), Boiler Plan (`docs/BOILER_MODULE_PLAN.md`), and Verified Sources (`docs/TECHNICAL_RESEARCH_SOURCE_REGISTER.md`).

---

## 2. Discipline-to-Equipment Mapping Matrix

### 2.1 Applied Thermodynamics
- **Theoretical Foundations:** First & Second Laws of Thermodynamics; Carnot cycle; Rankine steam cycle; Properties of pure substances (IAPWS-IF97 steam tables, Mollier diagram); Sensible and latent heat ($h_{fg}$); Psychrometrics (moisture in air).
- **Direct Washing Plant Applications:**
  - Gas-fired and Jhut steam boilers (Steam generation at 6–10 bar).
  - Industrial tumbler dryers (Steam-to-air finned radiator heat transfer).
  - Washing machines (Live steam injection heating of wash bath to 40°C–90°C for desizing and enzyme action).
  - Flash steam recovery vessels and condensate return lines.
  - Refrigerated compressed air dryers (Moisture condensation via thermodynamic refrigeration).
- **Key Field Formulas & Relationships:**
  - Heat absorbed by water/steam: $Q = \dot{m} \cdot (h_2 - h_1)$
  - Boiler direct thermal efficiency: $\eta = \frac{Q_s \cdot (h_g - h_f)}{q_{fuel} \cdot \text{GCV}} \times 100$
  - Dryer heating load: $Q_{dry} = \dot{m}_{air} \cdot C_{p,air} \cdot \Delta T + \dot{m}_{evap} \cdot h_{fg}$
- **Governing Failure Mechanisms:** Thermal shock, steam collapse / water hammer in cold lines, loss of boiler efficiency due to high flue gas temperature, wet steam causing poor fabric drying.
- **Cross-References:** `EQ-B03` (Steam Tumbler Dryer), `EQ-G01` (Gas Boiler), `EQ-G02` (Jhut Boiler), `EQ-F04` (Condensate Recovery), `EQ-J02` (Refrigerated Air Dryer).

---

### 2.2 Fluid Mechanics & Hydraulic Machinery
- **Theoretical Foundations:** Fluid statics; Pascal's Law; Continuity equation; Bernoulli's theorem; Darcy-Weisbach friction head loss; Reynolds number ($Re$); Laminar vs turbulent flow; Centrifugal pump affinity laws; Net Positive Suction Head ($\text{NPSH}$).
- **Direct Washing Plant Applications:**
  - High-pressure multistage centrifugal boiler feed pumps.
  - Raw, softened, and hot water distribution pumps.
  - Air-Operated Double Diaphragm (AODD) pumps for viscous chemicals and abrasive ETP sludge.
  - Pneumatic angle-seat valves and process piping friction loss calculations.
  - Tilting hydro extractor hydraulic power packs (pumps, cylinders, directional spool valves).
- **Key Field Formulas & Relationships:**
  - Darcy-Weisbach head loss: $h_f = f \cdot \frac{L}{D} \cdot \frac{v^2}{2g}$
  - Pump Hydraulic Power: $P_h = \frac{\rho \cdot g \cdot Q \cdot H}{1000}$ (kW)
  - Pump Affinity Laws: $\frac{Q_1}{Q_2} = \frac{N_1}{N_2}$, $\frac{H_1}{H_2} = \left(\frac{N_1}{N_2}\right)^2$, $\frac{P_1}{P_2} = \left(\frac{N_1}{N_2}\right)^3$
  - Cavitation criterion: $\text{NPSH}_{available} \ge \text{NPSH}_{required} + 0.5\text{ m}$
- **Governing Failure Mechanisms:** Cavitation pitting on pump impellers, pipe water hammer causing blown gaskets, severe line pressure drops starving washing machines, hydraulic seal blowouts.
- **Cross-References:** `EQ-D01` (Water Supply Pumps), `EQ-D02` (Boiler Feed Pumps), `EQ-D03` (AODD Pumps), `EQ-F01` (Pneumatic Valves), `EQ-A04` (Hydraulic Tilting Systems).

---

### 2.3 Heat Transfer (Conduction, Convection, Radiation)
- **Theoretical Foundations:** Fourier’s Law of thermal conduction; Newton’s Law of cooling (convection); Stefan-Boltzmann radiation law; Overall heat transfer coefficient ($U$); Logarithmic Mean Temperature Difference (LMTD); Heat exchanger effectiveness ($\epsilon$-NTU).
- **Direct Washing Plant Applications:**
  - Plate Heat Exchangers (PHE) for drain heat recovery (hot wash effluent pre-heating incoming cold water).
  - Fire-tube gas boiler smoke tube heat transfer (hot flue gas to boiler water).
  - Refractory furnace radiation in jhut/biomass boilers.
  - Steam pipe thermal insulation (Rockwool / calcium silicate cladding minimizing heat loss).
  - Tumbler dryer finned-tube steam heating coils.
- **Key Field Formulas & Relationships:**
  - Heat Exchanger duty: $Q = U \cdot A \cdot \Delta T_{lm}$
  - Logarithmic Mean Temp Difference: $\Delta T_{lm} = \frac{\Delta T_1 - \Delta T_2}{\ln(\Delta T_1 / \Delta T_2)}$
  - Pipe surface heat loss: $q = \frac{2\pi k L (T_{in} - T_{out})}{\ln(r_2 / r_1)}$
- **Governing Failure Mechanisms:** Fouling and scale deposition on heat exchanger plates reducing heat transfer, broken pipe insulation causing massive steam thermal loss, dry spots on boiler tubes leading to overheating.
- **Cross-References:** `EQ-B03` (Dryer Steam Coils), `EQ-G01` (Boiler Fire-tubes), `EQ-F05` (Drain Heat Recovery PHE), `EQ-F06` (Thermal Pipe Cladding).

---

### 2.4 Strength of Materials & Solid Mechanics
- **Theoretical Foundations:** Tensile, compressive, and shear stress/strain; Hooke’s Law; Young's modulus ($E$); Mohr’s circle; Thin and thick cylinder stress (Hoop stress $\sigma_h = \frac{p \cdot d}{2t}$, Longitudinal stress $\sigma_l = \frac{p \cdot d}{4t}$); Torsion of solid and hollow shafts; Bending moment and shear force diagrams (SFD/BMD); Euler column buckling.
- **Direct Washing Plant Applications:**
  - Boiler pressure shell and smoke tube thickness under internal/external pressure.
  - Industrial washing machine main cantilevered trunnion shaft under alternating bending and torsion.
  - Centrifugal hydro extractor basket hoop stress at 1000 RPM.
  - Overhead lifting gantry beams and hoist structural runway tracks.
  - High-pressure steam pipeline wall thickness and thermal expansion stresses.
- **Key Field Formulas & Relationships:**
  - Boiler shell thickness (ASME Sec I / BS EN 12953): $t = \frac{P \cdot R}{S \cdot E - 0.6 P} + C$
  - Shaft combined stress (Tresca/von Mises): $\tau_{max} = \sqrt{\left(\frac{\sigma_b}{2}\right)^2 + \tau_t^2}$, where $\sigma_b = \frac{32 M}{\pi d^3}$ and $\tau_t = \frac{16 T}{\pi d^3}$
  - Rotating ring hoop stress (Hydro basket): $\sigma = \rho \cdot v^2 = \rho \cdot (\omega \cdot r)^2$
- **Governing Failure Mechanisms:** Trunnion shaft fatigue fracture due to cyclic loading from wet garments, catastrophic hydro extractor basket burst from stress-corrosion cracking, boiler tube bulging/rupture from overpressure or localized overheating.
- **Cross-References:** `EQ-A01` (Washer Trunnion Shaft), `EQ-B01` (Hydro Basket Shell), `EQ-G01` (Boiler Pressure Vessel), `EQ-M01` (Overhead Hoist Gantry).

---

### 2.5 Theory of Machines & Mechanical Vibration (ISO 20816-3:2026 Standards)
- **Theoretical Foundations:** Kinematic links and mechanisms; Four-bar chains; Slider-crank mechanisms; Gear trains and velocity ratios; Dynamic unbalance of rotating masses; Static vs dynamic balancing; Critical shaft speed (Whirling); Vibration resonance; Spring-mass-damper systems; Vibration transmissibility; **ISO 20816-3:2026** broad-band vibration evaluation for industrial machinery $>15\text{ kW}$.
- **Direct Washing Plant Applications:**
  - Hydro extractor three-point suspended pendulum damping links accommodating uneven garment load.
  - Washing machine drum reversing motion and shock-absorber suspension.
  - High-speed centrifugal ventilation blowers and ID/FD fans balancing.
  - Rotary screw air compressor vibration isolation mounts.
  - Gearbox backlash, tooth engagement, and torsional resonance.
- **Key Field Formulas & Standards:**
  - Centrifugal unbalance force: $F_{unbalance} = m_{unbalance} \cdot r \cdot \omega^2$
  - Natural frequency: $\omega_n = \sqrt{\frac{k}{m}}$ (rad/s)
  - Critical shaft speed: $N_c = \frac{30}{\pi} \sqrt{\frac{g}{\delta}}$ (RPM)
  - **ISO 20816-3:2026 Severity Evaluation (Machines $>15\text{ kW}$, 120–30,000 RPM):**
    - *Rigid Foundation:* Zone A (Newly commissioned: $\le 1.4\text{ mm/s RMS}$), Zone B (Unrestricted long-term operation: $\le 2.8\text{ mm/s RMS}$), Zone C (Alert / limited operation: $\le 4.5\text{ mm/s RMS}$), Zone D (Trip / Danger: $> 4.5\text{ mm/s RMS}$).
    - *Flexible Foundation:* Zone A ($\le 2.3\text{ mm/s RMS}$), Zone B ($\le 4.5\text{ mm/s RMS}$), Zone C ($\le 7.1\text{ mm/s RMS}$), Zone D ($> 7.1\text{ mm/s RMS}$).
- **Governing Failure Mechanisms:** Resonant vibration shaking hydro extractors off foundation bolts, high blower vibration destroying motor bearings within weeks, excessive gearbox backlash causing tooth pitting under reversing loads.
- **Cross-References:** `EQ-A01` (Washer Suspension), `EQ-B01` (Hydro Extractor Suspended Links), `EQ-E02` (Gearboxes), `EQ-P01` (Centrifugal Fans & Blowers), `SRC-009` (ISO 20816-3:2026).

---

### 2.6 Machine Design & Mechanical Components
- **Theoretical Foundations:** Design for static and fatigue strength (Goodman, Soderberg criteria); Stress concentration factors ($K_t$); Rolling contact bearings (L10 life per ISO 281, dynamic load rating $C$); Hydrodynamic journal bearings; Bolted joints, bolt pre-load, and torque tensioning; Keyways and splines; Flexible, rigid, and fluid couplings; V-belt and timing belt drive geometry; Mechanical seal face lubrication.
- **Direct Washing Plant Applications:**
  - Main spherical roller bearings supporting washer drums (222xx / 223xx series in plummer blocks).
  - V-belt drive sizing between motor and washer/blower pulleys.
  - Flexible pin-bush / tire couplings between feed pump motors and pumps.
  - Cartridge mechanical seals preventing process wash water escaping past rotating shafts.
  - High-tensile flanged pipe bolting (Grade 8.8 / ASTM A193 B7) on steam lines.
- **Key Field Formulas & Relationships:**
  - Bearing L10 rating life: $L_{10h} = \frac{10^6}{60 \cdot N} \left(\frac{C}{P}\right)^p$ ($p=3$ for ball, $p=10/3$ for roller)
  - Bolt tightening torque: $T = K \cdot d \cdot F_i$ (where $K \approx 0.15-0.20$ torque coefficient)
  - V-Belt speed ratio: $\frac{N_1}{N_2} = \frac{D_2}{D_1 \cdot (1 - s)}$
  - Power transmitted by belt: $P = (T_1 - T_2) \cdot v$ with $\frac{T_1 - m v^2}{T_2 - m v^2} = e^{\mu \theta / \sin(\beta/2)}$
- **Governing Failure Mechanisms:** Premature bearing spalling due to improper grease or water ingress, V-belt slipping and burning from low tension, mechanical seal dry-running and cracking face rings, loose foundation bolts leading to angular shaft misalignment.
- **Cross-References:** `EQ-E01` (Electric Motors), `EQ-E03` (Bearings & Pillow Blocks), `EQ-E04` (V-Belts & Pulleys), `EQ-E05` (Flexible Couplings), `EQ-E06` (Timing Belts).

---

### 2.7 Material Science & Metallurgy
- **Theoretical Foundations:** Crystal lattices; Iron-Carbon ($Fe\text{-}Fe_3C$) phase diagram; Heat treatments (Annealing, Normalizing, Quenching, Tempering); Stainless steel grades (Austenitic AISI 304, AISI 316, 316L, Duplex 2205); Corrosion mechanisms (Pitting, Crevice, Galvanic, Stress Corrosion Cracking $\text{SCC}$, Caustic embrittlement); Elastomer and polymer compatibility (PTFE, EPDM, Viton/FKM, NBR).
- **Direct Washing Plant Applications:**
  - Washing machine inner perforated drums: AISI 316L required to resist hot chloride bleach, enzymes, and organic acids.
  - Boiler pressure shell and smoke tubes: ASTM A106 Grade B, ASTM A516 Grade 70 carbon steel resisting high-temperature boiler water without caustic cracking.
  - Valve seats and seals: PTFE / RTFE for steam lines; EPDM for hot water; Viton for chemical lines.
  - ETP chemical mixing tanks: FRP (Fiber Reinforced Plastic) and SS316 resisting sulfuric acid and sodium hydroxide.
  - Machine shafts: EN8 (AISI 1040) normalized or EN19 / EN24 (AISI 4140/4340) hardened and tempered for fatigue resilience.
- **Key Field Formulas & Relationships:**
  - Pitting Resistance Equivalent Number: $\text{PREN} = \%Cr + 3.3 \cdot \%Mo + 16 \cdot \%N$ ($\text{PREN} > 23$ for AISI 316L resisting wash chlorides)
  - Corrosion rate: $CR = \frac{K \cdot \Delta W}{A \cdot t \cdot \rho}$ (mm/year)
- **Governing Failure Mechanisms:** Severe pitting corrosion of AISI 304 drums exposed to hypochlorite bleach, caustic embrittlement of boiler tube welds due to alkaline boiler water, degradation of standard NBR rubber seals in hot steam environments.
- **Cross-References:** `EQ-A01` (Washer Perforated Basket SS316L), `EQ-F01` (Angle Seat Valve PTFE Seats), `EQ-G01` (Boiler Steel Plate ASTM A516), `EQ-I01` (Chemical Mixing Tanks).

---

### 2.8 Manufacturing Technology & Workshop Practice
- **Theoretical Foundations:** Metal cutting principles (Orthogonal cutting, Taylor’s tool life equation $V \cdot T^n = C$); Turning, boring, facing, and threading on engine lathes; Drilling, reaming, and counterboring; Welding metallurgy (SMAW/MMA, GTAW/TIG, GMAW/MIG); Heat-affected zone (HAZ); Welding defects (Porosity, undercut, lack of penetration); Pipe threading (NPT vs BSPT standards); Tolerances, limits, and fits (ISO system: Clearance, Transition, Interference fits).
- **Direct Washing Plant Applications:**
  - On-site lathe machining of worn pump shafts and custom stainless steel bearing sleeves.
  - Boring and keyway broaching of replacement cast iron V-belt pulleys.
  - TIG welding of cracked SS316 washing machine drum baffles and piping manifolds.
  - Manual metal arc (MMA) structural welding of hoist runway beams and machine support frames.
  - Precision shaft alignment using dial indicators (Rim and face method) adhering to ISO fit standards (e.g. $k5/m5$ for bearing journals, $H7$ for housings).
- **Key Field Formulas & Relationships:**
  - Lathe Spindle Speed: $N = \frac{1000 \cdot V_c}{\pi \cdot D}$ (RPM)
  - Heat input in welding: $HI = \eta \cdot \frac{V \cdot I \cdot 60}{v \cdot 1000}$ (kJ/mm)
- **Governing Failure Mechanisms:** Shaft breakage due to sharp machined corners without radius (stress raisers), brittle weld failure in HAZ from improper preheat on high-carbon steel, oversized bearing journals causing inner ring cracking during press fitting.
- **Cross-References:** `EQ-N01` (Maintenance Lathe & Drill), `EQ-N02` (Pillar Drill), `EQ-N03` (Welding & Cutting Sets).

---

### 2.9 Mechanical Maintenance & Reliability Engineering
- **Theoretical Foundations:** Bathtub curve of asset failure; Failure Mode and Effects Analysis (FMEA); Root Cause Analysis (RCA - 5 Whys, Ishikawa Fishbone); Mean Time Between Failures ($\text{MTBF}$), Mean Time To Repair ($\text{MTTR}$); Overall Equipment Effectiveness ($\text{OEE}$); Condition-based maintenance (CBM); Vibration analysis per **ISO 20816-3:2026**; Infrared thermography (ASTM E1934); Oil tribology (Viscosity, particle count, moisture content).
- **Direct Washing Plant Applications:**
  - Predictive vibration monitoring of rotary screw compressor air ends and hydro extractors to detect bearing subsurface micro-spalling before catastrophic seizure.
  - Infrared thermal scanning of electrical motor terminal boxes, boiler steam trap manifolds, and pump bearings to detect localized hotspots ($> 15^\circ\text{C}$ differential).
  - Daily preventive maintenance checklists for boiler blowdown, water softener hardness testing, and air compressor drain evacuation.
  - Lubrication route scheduling (Lithium complex grease EP2 for wet bearings, synthetic ISO VG 220 gear oil for washing gearboxes).
- **Key Field Formulas & Relationships:**
  - Availability: $A = \frac{\text{MTBF}}{\text{MTBF} + \text{MTTR}} \times 100$
  - Overall Equipment Effectiveness: $\text{OEE} = \text{Availability} \times \text{Performance} \times \text{Quality}$
  - Ball Bearing Outer Race Fault Frequency: $\text{BPFO} = \frac{n \cdot f_r}{2} \left(1 - \frac{d}{D} \cos \alpha\right)$
  - Ball Bearing Inner Race Fault Frequency: $\text{BPFI} = \frac{n \cdot f_r}{2} \left(1 + \frac{d}{D} \cos \alpha\right)$
- **Governing Failure Mechanisms:** Run-to-failure strategy causing catastrophic secondary damage (e.g. unmonitored bearing collapse destroying washer trunnion shaft and end shield), over-greasing blowing out bearing seals, failed steam traps passing live steam undetected.
- **Cross-References:** `SRC-009` (ISO 20816-3:2026), `SRC-013` (Mobley Maintenance Handbook), All Equipment Maintenance Sections.

---

### 2.10 Industrial Refrigeration & Psychrometrics
- **Theoretical Foundations:** Vapor compression refrigeration cycle (Evaporator, Compressor, Condenser, Expansion device); Refrigerant thermodynamic properties (R134a, R410A, R407C); Pressure-Enthalpy ($P\text{-}h$) diagrams; Coefficient of Performance ($\text{COP}$); Psychrometric chart (Dry bulb, wet bulb, dew point, relative humidity, specific volume, enthalpy).
- **Direct Washing Plant Applications:**
  - Refrigerated compressed air dryers cooling compressed air to $3^\circ\text{C}$ pressure dew point (PDP) to drop out moisture before entering pneumatic solenoids.
  - Industrial chilled water systems (chillers) supplying $10^\circ\text{C}-15^\circ\text{C}$ water for specialized low-temperature bio-polishing and enzymatic washes.
  - Plant ventilation, moisture exhaust, and cooling towers maintaining indoor humidity below saturation.
- **Key Field Formulas & Relationships:**
  - Refrigeration effect: $q_e = h_1 - h_4$ (kJ/kg)
  - Compressor work: $w_c = h_2 - h_1$ (kJ/kg)
  - Coefficient of Performance: $\text{COP} = \frac{q_e}{w_c} = \frac{h_1 - h_4}{h_2 - h_1}$
  - Condenser heat rejection: $q_c = h_2 - h_3 = q_e + w_c$
- **Governing Failure Mechanisms:** Refrigerant gas leakage leading to high evaporator temperature and wet compressed air flooding pneumatic lines, cooling tower fill fouling causing high condensing pressure and chiller compressor tripping.
- **Cross-References:** `EQ-J02` (Refrigerated Air Dryers), `EQ-K01` (Cooling Towers & Water Chillers).

---

### 2.11 Engineering Mathematics & Applied Mechanics (`FUND-11`)
- **Theoretical Foundations:** Dimensional consistency & Buckingham $\Pi$ theorem; strict separation of absolute vs. gauge scales ($P_{\text{abs}} = P_{\text{gauge}} + P_{\text{atm}}$, $T_{\text{K}} = T_{^\circ\text{C}} + 273.15$); volumetric vs. gravimetric basis conversion ($\text{GCV}_{\text{mass}} = \text{GCV}_{\text{vol}} / \rho_{\text{gas}}$); vectors, static equilibrium matrices, and cross-sectional continuity ($Q = A \cdot v$); piecewise linear and bilinear tabular interpolation; numerical error propagation and ASME PTC 19.1 measurement uncertainty.
- **Direct Washing Plant Applications:**
  - Linear pressure/temperature interpolation across the 56 IAPWS-IF97 steam anchor rows in `SteamTableScreen`.
  - Unit-basis reconciliation in boiler indirect efficiency (`CALC-02`: converting volumetric natural gas $\text{kJ/Nm}^3$ by $\rho \approx 0.74\text{ kg/Nm}^3$ before computing mass-basis moisture losses $L_2, L_3, L_4$) and FD fan sizing (`CALC-11`: separating $0^\circ\text{C}$ Normal $\text{Nm}^3/\text{min}$ from ambient FAD $\text{m}^3/\text{min}$).
  - Steam, condensate, and compressed-air header internal diameter sizing from mass flow and specific volume (`CALC-14`, `CALC-20`).
  - Trend control limits and volumetric-to-ppm chemical dosing ratios in water treatment (`CALC-05`, `CALC-19`).
- **Key Field Formulas & Relationships:**
  - Piecewise linear interpolation: $y = y_1 + \frac{x - x_1}{x_2 - x_1}(y_2 - y_1)$
  - Gauge-to-absolute pressure: $P_{\text{abs}} = P_{\text{gauge}} + 1.01325\text{ bar}$
  - Gas calorific mass basis: $\text{GCV}_{\text{mass}} = \frac{\text{GCV}_{\text{vol}}}{\rho_{\text{gas}}}\text{ [kJ/kg]}$
  - Normal-to-FAD gas volume: $V_{\text{FAD}} = V_{\text{Nm}^3} \cdot \frac{T_{\text{amb}} + 273.15}{273.15}$
  - Circular pipe continuity: $v = \frac{4 \cdot \dot{Q}}{\pi \cdot d^2}\text{ [m/s]}$
  - **Worked Check:** Natural gas $\text{GCV}_{\text{vol}} = 39,800\text{ kJ/Nm}^3$ at $\rho = 0.74\text{ kg/Nm}^3 \implies \text{GCV}_{\text{mass}} = 53,783.78\text{ kJ/kg}$.
- **Governing Failure Mechanisms:** Mixing gauge and absolute pressures in safety-valve/enthalpy routines (~10% error at 9–10 bar); substituting $\text{kJ/Nm}^3$ into $\text{kJ/kg}$ equations (overstating $L_2/L_4$ by $1/\rho \approx 1.35\times$); mixing Normal ($0^\circ\text{C}$) vs. ambient ($30^\circ\text{C}$) air volumes (~11% fan capacity shortfall).
- **Cross-References:** `EQ-G01` (Gas Boiler), `EQ-F02` (Steam Distribution & PRVs), `EQ-D02` (Feed Pumps), `CALC-02`, `CALC-11`, `CALC-14`, `SRC-002`, `SRC-018`.

---

### 2.12 Engineering Mechanics (Statics & Dynamics) (`FUND-12`)
- **Theoretical Foundations:** Newton’s Laws of Motion; planar & 3D static equilibrium ($\sum \mathbf{F} = 0$, $\sum \mathbf{M} = 0$); Free Body Diagrams (FBD); dry Coulomb static/kinetic friction ($F_f \le \mu_s N$); belt/capstan wrap friction ($T_1 / T_2 \le e^{\mu \theta}$); center of gravity, parallel-axis theorem, and mass moment of inertia ($I = \sum m r^2$); rigid-body rotational kinematics ($\tau = I \alpha$, $\text{KE}_{\text{rot}} = \frac{1}{2} I \omega^2$); normal/centripetal acceleration ($a_n = \omega^2 r$).
- **Direct Washing Plant Applications:**
  - Two-leg and multi-leg sling load amplification on overhead electric chain hoists lifting washer drums, motors, and heat-exchanger plate packs (`EQ-M01`, ASME B30.9).
  - Centrifugal hydro extractor basket rim acceleration, G-force, and rotor kinetic energy braking demand (`EQ-B01`, `CALC-16`).
  - V-belt wrap angle ($\theta$), tight/slack side tension differential, and drive pulley speed ratio (`EQ-E04`, `CALC-15`).
  - Reversing drum inertia shock and foundation anchor-bolt shear loading on horizontal belly washers (`EQ-A01`).
- **Key Field Formulas & Relationships:**
  - Static equilibrium & two-leg sling tension: $\sum F = 0, \sum M = 0 \implies T_{\text{sling}} = \frac{W}{2 \cos\theta}$ ($\theta$ measured from vertical)
  - Rotational dynamics & stored energy: $\tau = I \alpha$, $\quad \text{KE}_{\text{rot}} = \frac{1}{2} I \omega^2$ ($\omega = \frac{2\pi N}{60}$)
  - Centrifugal dewatering G-force: $G = \frac{r \omega^2}{g} \approx 0.00001118 \cdot r_{\text{cm}} \cdot N^2$
  - Capstan / flat belt friction ratio: $\frac{T_1}{T_2} \le e^{\mu \theta}$
  - **Worked Check:** At $\theta = 60^\circ$ from vertical (included angle $120^\circ$), $\cos 60^\circ = 0.5 \implies$ each sling leg carries $100\%$ of total lifted weight ($2\times$ vertical load!).
- **Governing Failure Mechanisms:** Rigging sling rupture from excessive included angles ($>90^\circ$); anchor bolt fatigue/shear when reversing drum inertia acts against loose floor grouting; belt squeal and thermal glazing from insufficient wrap arc or slack initial tension; spindle bearing overload from unbalanced wet garment distribution.
- **Cross-References:** `EQ-A01` (Belly Washer), `EQ-B01` (Hydro Extractor), `EQ-E04` (V-Belts & Pulleys), `EQ-M01` (Hoists & Lifting), `CALC-15`, `CALC-16`, `SRC-008`, `SRC-019`.

---

### 2.13 Industrial Engineering & Plant Operations (`FUND-13`)
- **Theoretical Foundations:** Utility and production capacity balancing; Theory of Constraints & bottleneck management; setup/changeover reduction (SMED); Total Productive Maintenance (TPM) pillars and Autonomous Operator Care; structured work-order lifecycle (Request $\rightarrow$ Plan $\rightarrow$ Schedule $\rightarrow$ Execute $\rightarrow$ Closeout); Spare Parts ABC (cost) & VED (Vital/Essential/Desirable) criticality matrix; **ISO 50001:2018 / ISO 50002:2014** Energy Management & Energy Performance Indicators (EnPIs); shift handover governance and PDCA continuous improvement.
- **Direct Washing Plant Applications:**
  - Staggering cold wash-bath fill and direct-steam heating ramps across washing bays (`EQ-A01`) so simultaneous peak loads do not pull boiler header pressure below minimum process temperature (`EQ-G01`).
  - Shift-wise utility EnPI logging: Steam-to-Fuel Evaporation Ratio (`CALC-04`), Specific Steam Consumption ($\text{kg steam / kg garment}$, `CALC-17`), and Dryer Specific Moisture Extraction (`CALC-18`).
  - Scheduling ETP equalization hydraulic buffer and chemical dosing (`EQ-L01`) around dye/bleach bath dump cycles.
  - Criticality-tiered spare-part min-max levels for `WEAR_PART` and `CONSUMABLE` items (cartridge seals, V-belts, valve seats, boiler gauge glasses).
- **Key Field Formulas & Relationships:**
  - Specific Steam Consumption (EnPI): $\text{SEC}_{\text{steam}} = \frac{\dot{m}_{\text{steam,total}}}{\dot{m}_{\text{garment}}}\text{ [kg steam / kg dry garment]}$
  - Steam-to-Fuel Evaporation Ratio: $\text{ER} = \frac{m_{\text{steam}}}{m_{\text{fuel}}}$ (Gas: $11.5\text{–}13.5\text{ kg/Nm}^3$; Cotton Jhut: $3.5\text{–}4.5\text{ kg/kg}$)
  - Equipment Utilization & Availability: $U = \frac{\text{Operating Hours}}{\text{Scheduled Hours}} \times 100\%$, $\quad \text{PM Compliance} = \frac{\text{PM Completed On Time}}{\text{PM Scheduled}} \times 100\%$
- **Governing Failure Mechanisms:** Uncoordinated batch startups collapsing steam header pressure ("steam starvation" and off-shade re-washes); zero-buffer run-to-failure firefighting exhausting seals/bearings; hydraulic and pH shock loads tripping ETP biological clarifiers; silent fuel-efficiency drift hidden by absent shift logs.
- **Cross-References:** `EQ-A01` (Washing Machines), `EQ-B03` (Tumbler Dryers), `EQ-G01` (Gas Boiler), `EQ-L01` (ETP Equalization/Units), `CALC-04`, `CALC-17`, `CALC-18`, `SRC-013`, `SRC-017`, `SRC-020`.

---

### 2.14 Power Plant & Steam Utility Engineering (`FUND-14`)
- **Theoretical Foundations:** Rankine and regenerative steam cycles; fire-tube vs. water-tube steam generator design; balanced-draft vs. forced-draft furnace hydrodynamics; combustion stoichiometry, excess air optimization ($O_2 / CO_2$ vs. $CO$ breakthrough); indirect heat-loss accounting (ASME PTC 4 / BS 845-1); waste-heat recovery (flue economizers, continuous blowdown flash vessels & plate exchangers); feedwater softening & thermal/vacuum deaeration (Henry’s Law oxygen stripping at $102\text{–}105^\circ\text{C}$); chimney natural stack buoyancy draught; steam header velocity, trap drainage, and condensate lift.
- **Direct Washing Plant Applications:**
  - Integrated utility steam train: Water Softener (`EQ-H01`, $<5\text{ ppm CaCO}_3$) $\rightarrow$ Spray/Tray Deaerator (`CALC-08`, $<0.005\text{ mg/L O}_2$) $\rightarrow$ Multistage Centrifugal Feed Pump (`EQ-D02`, `CALC-07`) $\rightarrow$ Economizer (`CALC-10`) $\rightarrow$ 3-Pass Firetube Gas or Jhut Grate Boiler (`EQ-G01` / `EQ-G02`) $\rightarrow$ Header PRV & Steam Traps (`EQ-F02` / `EQ-F04`).
  - Maintaining economizer exit flue temperature strictly above sulfuric/carbonic acid dew point ($\ge 120^\circ\text{C}$ natural gas, $\ge 140^\circ\text{C}$ sulfur/solid fuels).
  - Holding slight negative furnace draft ($-2\text{ to }-5\text{ mm WC}$) on Jhut boilers via ID fan + cyclone dust collector (`CALC-12`, `EQ-G02`).
  - Maximizing continuous blowdown flash steam & sensible recovery while keeping drum TDS within ABMA/BS 2486 bounds (`CALC-05`, `CALC-06`).
- **Key Field Formulas & Relationships:**
  - Indirect Boiler Efficiency (Heat Loss): $\eta_{\text{indirect}} = 100 - (L_1 + L_2 + L_3 + L_4 + L_5 + L_6)$
  - Chimney Natural Stack Draught: $\Delta P_{\text{mm WC}} = 353 \cdot H \cdot \left(\frac{1}{T_{\text{amb,K}}} - \frac{1}{T_{\text{stack,K}}}\right)$
  - Continuous Blowdown Rate: $\%\text{BD} = \frac{\text{TDS}_{\text{feed}}}{\text{TDS}_{\text{boiler}} - \text{TDS}_{\text{feed}}} \times 100$
  - Deaerator Steam Consumption Balance: $\dot{m}_{\text{steam}} = \dot{m}_{\text{feed}} \cdot \frac{h_{f,\text{da}} - h_{\text{makeup}}}{h_{g,\text{steam}} - h_{\text{makeup}}}$
- **Governing Failure Mechanisms:** Oxygen pitting along boiler water-line & smoke tubes from cold/faulty deaeration; cold-end acid condensation eating economizer fins below acid dew point; furnace positive-pressure blowback in Jhut boilers upon ID fan/damper fault; scale-induced furnace tube overheating and wet-steam priming from skipped blowdown/softener testing.
- **Cross-References:** `EQ-D02` (Boiler Feed Pumps), `EQ-F02` (Steam Traps), `EQ-F04` (Condensate Recovery), `EQ-G01` (Gas Boiler), `EQ-G02` (Jhut Boiler), `EQ-H01` (Water Softeners), `CALC-02`, `CALC-05`, `CALC-06`, `CALC-08`, `CALC-10`, `CALC-12`, `CALC-13`, `SRC-002`, `SRC-004`, `SRC-017`, `SRC-021`.

---

### 2.15 Instrumentation & Control Fundamentals (`FUND-15`)
- **Theoretical Foundations:** Primary sensing elements — Bourdon tube / diaphragm pressure gauges (EN 837), 3-wire Pt100 RTDs per **IEC 60751** ($\alpha = 0.00385\text{ }\Omega/\Omega/^\circ\text{C}$, lead-wire compensation), Type-J/K thermocouples (Seebeck voltage & cold-junction compensation), differential-pressure orifice, vortex, and electromagnetic flow meters, Mobrey magnetic float & conductivity level electrodes; **4–20 mA DC** live-zero current loops (enabling open-circuit $<3.6\text{ mA}$ wire-break fault diagnostics vs. $0\%$ scale); control strategies (two-position hysteresis On-Off vs. PID proportional-integral-derivative tuning, fail-closed vs. fail-open spring-return actuators); Burner Management Systems (BMS per **NFPA 85**: pre-purge, pilot trial, $\le 4\text{ s}$ Flame Failure Response Time); SIL/hardwired trip interlocks and periodic zero/span calibration.
- **Direct Washing Plant Applications:**
  - Dual-chamber boiler Low-Water Cutoff (LWCO) Mobrey float/conductivity safety interlocks (`EQ-G04`) wired independently of PLC modulating feedwater control.
  - Combustible LEL gas leak detectors (`EQ-O02`) and double-block-and-bleed shutoff valves on natural gas boiler burner trains (`EQ-G01`).
  - 3-wire Pt100 RTD loops controlling wash bath enzyme/bleach hold temperatures (`EQ-A03`) and curing oven zones (`EQ-C01`).
  - Pneumatic angle-seat / globe modulating steam control valves (`EQ-F01`) and VFD header-pressure PID loops on compressed air & boiler feed pumps.
- **Key Field Formulas & Relationships:**
  - 4–20 mA Live-Zero Linear Scaling: $I_{\text{mA}} = 4 + 16 \cdot \frac{PV - LRV}{URV - LRV}$, $\quad PV = LRV + (URV - LRV) \cdot \frac{I_{\text{mA}} - 4}{16}$
  - Pt100 IEC 60751 Linear Approx ($0\text{–}200^\circ\text{C}$): $R(T) \approx 100 \cdot (1 + 0.00385 \cdot T_{^\circ\text{C}})$ ($100.00\text{ }\Omega$ at $0^\circ\text{C}$, $138.51\text{ }\Omega$ at $100^\circ\text{C}$)
  - Ideal PID Algorithm: $u(t) = K_p e(t) + K_i \int e(\tau)\,d\tau + K_d \frac{de(t)}{dt}$
  - Instrument Span Error: $\% \text{Error} = \frac{\text{Measured} - \text{Reference}}{URV - LRV} \times 100$
- **Governing Failure Mechanisms:** Sludge accumulation seizing Mobrey LWCO floats in the "water normal" position causing catastrophic dry-firing; unauthorized electrical jumpers bypassing high-pressure/LWCO/flame trips; oxidized Pt100 terminal screws adding series resistance ($\approx +2.6^\circ\text{C}$ per extra $1\text{ }\Omega$) and overheating dye/curing baths; unshielded 4–20 mA cables next to VFD leads causing hunting valves.
- **Cross-References:** `EQ-A03` (Control/Sample Washers), `EQ-C01` (Curing Ovens), `EQ-F01` (Pneumatic Valves), `EQ-G01` (Gas Boiler BMS), `EQ-G04` (Safety Gauges & LWCO), `EQ-O02` (Gas Leak Detectors), `CALC-09`, `CALC-11`, `SRC-010`, `SRC-022`.

---

## 3. Knowledge Map Coverage Summary
- **Total Disciplines Documented:** `15 / 15` (`FUND-01` through `FUND-15`), aligned 1-to-1 with `MASTER_PROJECT_PLAN.md` §4.1 and `FundamentalsCatalog` (`EngineeringCatalogs.kt`).
- **Update Synchronization (2026-11-17, Batch 12 / GAP-12):** Authored §§ 2.11–2.15 with verified physics equations, worked numerical sanity checks, washing-plant equipment links (`EQ-*`), calculation links (`CALC-*`), failure mechanisms, and source citations (`SRC-017`–`SRC-022` added to `DOC-MPN-010`).

