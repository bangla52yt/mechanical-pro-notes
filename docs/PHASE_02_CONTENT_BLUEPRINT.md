# PHASE 02 CONTENT BLUEPRINT: 21-SECTION EQUIPMENT SPECIFICATIONS
**Document Identifier:** DOC-MPN-012  
**Project:** Mechanical Pro Notes (Personal Reference App)  
**Classification Standard:** Strict 6-Tier Evidence Classification  
**Status:** VALIDATED CONTENT BLUEPRINT - MASTER PROMPT 02-A  
**Scope:** Complete Execution of the 21-Section Standard Template on Key Core Washing Plant Machinery  
**Last Updated:** 2026-11-11  

---

## 1. Blueprint Structure & Content Depth Alignment
In accordance with **DEC-009** and **MASTER PROMPT 02-A**, every significant machine in Mechanical Pro Notes will be documented using the standard 21-section technical engineering template. 

### Content Depth Classification in Master Inventory (64 Total Equipment Records):
- **`[FULL CONTENT BLUEPRINT]` (2 records):**
  1. **EQ-A01:** Horizontal Front-Loading Belly Washer (Production & Washing Machinery)
  2. **EQ-B01:** Vertical Basket Centrifugal Hydro Extractor (Extraction Systems)
- **`[PARTIAL CONTENT BLUEPRINT]` (9 records):**
  - Detailed in `docs/BOILER_MODULE_PLAN.md` and specialized subsystem plans:
    1. **EQ-A04:** Atmospheric Sample Dyeing & Washing Machine
    2. **EQ-B02:** Open-Pocket Self-Balancing High-Speed Extractor
    3. **EQ-B03:** Continuous Rotary Screw Cake De-Watering Extractor
    4. **EQ-D02:** Multi-Stage Centrifugal Boiler Feed Water Pump
    5. **EQ-F02:** Water-Cooled Rotary Screw Air Compressor
    6. **EQ-G01:** Natural Gas-Fired Three-Pass Wet-Back Packaged Steam Boiler
    7. **EQ-G02:** Biomass/Jhut/Solid-Fuel Hand/Stoker-Fired Steam Boiler
    8. **EQ-J01:** Automated Dissolved Air Flotation (DAF) Clarifier Unit
    9. **EQ-L01:** Double-Gimbal Fabric Tension & Ballooning Machine
- **`[RESEARCH PENDING]` (7 records):**
  - Specific plant-dependent items awaiting owner plant-specific verification:
    1. **EQ-A05:** Ultrasonic Enzyme Washing Reactor
    2. **EQ-B04:** Tilting Drum Centrifugal Extractor
    3. **EQ-B05:** Pneumatic Squeezer Roll Padder Extractor
    4. **EQ-C04:** Conveyorized Continuous Tunnel Finishing Dryer
    5. **EQ-C05:** Infrared (IR) Radiant Pre-Drying Tunnel
    6. **EQ-H03:** Ceramic Catalytic Gas Combustion Heat Exchanger
    7. **EQ-K02:** Robotic Automatic Garment Spraying Booth
- **`[INVENTORY ONLY]` (46 records):**
  - Fully cataloged with 24 technical parameters in `docs/WASH_PLANT_EQUIPMENT_COVERAGE_MATRIX.md` (spanning Washing, Drying, Pumps, Air, Thermal, Piping, ETP, Finishing, Electrical, Workshop, Safety, and Fire systems).
- **Inventory Reconciliation Total:** $2 + 9 + 7 + 46 = 64$ verified unique equipment records.

---

## 2. Deep Equipment Blueprint 01: Horizontal Front-Loading Belly Washer (`EQ-A01`)

### Section 01: Introduction & Purpose
The Horizontal Front-Loading Belly Washer is the primary mechanical processing machine in a garment washing plant. It provides controlled tumbling, mechanical friction, chemical interaction, and thermal treatment to bulk garments (denim jeans, knitwear, shirts) to achieve desizing, scouring, enzyme bio-polishing, bleaching, stone washing, and chemical softening.

### Section 02: Applications in Washing Plants
- **Desizing:** Removal of starch size at 60°C with alpha-amylase enzymes.
- **Enzyme Washing / Bio-Polishing:** Cellulase enzyme treatment at 45°C–55°C (pH 4.5–5.5) for surface pill removal and localized color fading.
- **Pumice Stone Washing:** Tumbling with volcanic pumice stone for authentic denim abrasion.
- **Bleaching:** Sodium hypochlorite or hydrogen peroxide bleaching at 70°C–85°C.
- **Tinting & Softening:** Final application of cationic silicone softeners.

### Section 03: Working Principle
Garments and chemical bath liquor are tumbled inside a horizontally mounted perforated cylindrical stainless steel basket. The inner drum features internal longitudinal lifter ribs (baffles). As the drum rotates at a critical tumbling speed (typically $Froude\ number \approx 0.6\text{–}0.8$, corresponding to 25–32 RPM), garments are lifted upwards by the baffles and dropped back into the bath. Periodic reversing rotation (e.g. 15 seconds clockwise, 3 seconds stop, 15 seconds counter-clockwise) prevents garment roping, knotting, and uneven processing.

### Section 04: Construction & Component Identification
- **Outer Stationary Shell:** Heavy cylindrical structural jacket with front loading door, bottom pneumatic drain sump, water inlet manifold, and direct steam injection sparge pipe.
- **Inner Rotating Basket:** Perforated high-grade AISI 316L stainless steel cylinder with laser-punched holes ($4\text{–}6\text{ mm}$ with rounded edges to prevent fabric snagging) and 3 to 4 welded hollow SS lifter baffles.
- **Main Trunnion Shaft:** Heavy-duty forged alloy steel shaft (EN8 / EN19) pressed and welded to the reinforced rear spider arm of the inner basket.
- **Bearing Assembly:** Twin split plummer block housings (SNL/SNH series) accommodating double-row spherical roller bearings (222xx series).
- **Shaft Seal Gland:** Cartridge mechanical face seal or PTFE-impregnated braided graphite gland packing with water quench ring preventing liquor leakage along the shaft.
- **Drive System:** 3-phase induction motor driving a shaft-mounted helical reduction gearbox or multi-groove V-belt transmission modulated by a Variable Frequency Drive (VFD).
- **Loading Door:** Large hinged front door with tempered borosilicate sight glass, silicone rubber seal ring, and dual pneumatic/mechanical safety interlocks.
- **Pneumatic Drain Valve:** Large-bore ($3"\text{–}6"$) angle-seat or butterfly drain valve for rapid dumping of wash liquor.

### Section 05: Component Functions & Interactions
- **Lifter Baffles:** Lift wet fabric to the 11 o'clock position before dropping, generating the mechanical impact energy necessary for fiber flexure and stone abrasion.
- **Trunnion Shaft & Spider:** Supports the entire cantilevered load of the rotating basket, garments, water, and pumice stones (often up to 1500 kg total dynamic load).
- **Mechanical Seal:** Prevents corrosive, hot, chemical-laden wash water from reaching the plummer block bearings.
- **VFD Drive:** Allows seamless adjustment between slow loading speed (5 RPM), washing speed (28 RPM), and intermediate extraction speed (350 RPM) while eliminating mechanical transmission shock during reversal.

### Section 06: Technical Specifications
- **Dry Fabric Capacity:** 150 kg – 350 kg dry weight per batch.
- **Drum Diameter:** 1200 mm to 1600 mm; Drum Length: 1200 mm to 2000 mm.
- **Drum Material:** AISI 316L (EN 1.4404) Austenitic Stainless Steel (minimum 2.5 mm sheet thickness).
- **Rotational Speed:** Wash mode: 24–32 RPM; Reversal pause: 2–4 seconds; Extraction mode: 300–450 RPM.
- **Drive Motor Power:** 11 kW to 22 kW (15 HP to 30 HP), 415V, 50 Hz, IP55, Class F insulation.
- **Steam Consumption:** 150 kg/hr to 300 kg/hr dry saturated steam at 3–4 bar(g).
- **Water Inlet Connections:** 2" BSP / NPT (Cold water & Softened water).
- **Drain Valve Size:** DN80 to DN125 (3" to 5") pneumatic fail-safe spring-return valve.

### Section 07: Selection Criteria
- **Liquor Ratio (L:R):** Modern open-pocket machines achieve low liquor ratios (1:4 to 1:6), saving 40% water and steam compared to conventional side-loaders (1:8 to 1:10).
- **Metallurgy:** Pure AISI 316L is non-negotiable for denim processing due to aggressive hypochlorite bleaching and acid washing; AISI 304 will suffer severe pitting within 6–12 months.
- **G-Factor Rating:** Higher extraction G-factor ($> 150\text{G}$) reduces subsequent hydro extraction time.

### Section 08: Drawings & Exploded Diagrams
- Required drawings:
  1. Overall General Arrangement (GA) drawing showing service connection points (Steam, Water, Drain, Air, Power).
  2. Exploded cross-section of Rear Trunnion Shaft, Spider Arm, Plummer Blocks, and Mechanical Seal Assembly.
  3. Pneumatic Piping & Instrumentation Diagram (P&ID) showing steam injection, water inlet, and drain valve logic.

### Section 09: Installation Protocols
- **Foundation:** Reinforced concrete foundation slab (minimum 300 mm thickness, 25 MPa grade) isolated from building structural columns to prevent vibration transmission.
- **Anchor Bolting:** Heavy anchor studs (M20–M24) grouted with non-shrink epoxy grout.
- **Drain Trench:** Minimum 1.5% fall in wash floor drain trench directly beneath the drain valve outlet.
- **Piping Expansion:** Stainless steel corrugated flexible braided hoses installed on steam and water supply lines to isolate machine vibration from rigid headers.

### Section 10: Alignment & Commissioning
- **Leveling:** Precision machinist level placed on top of outer shell and on the trunnion shaft journal; leveling accuracy must be within $\le 0.1\text{ mm/m}$.
- **V-Belt Pulley Alignment:** Laser or straightedge pulley alignment ensuring parallel offset $< 0.5\text{ mm}$ and angular misalignment $< 0.25^\circ$.
- **Cold Water Test Run:** Fill drum to 50% level, verify zero leakage at door gasket and rear shaft seal.
- **Reversal Rotation Verification:** Confirm drum runs 15 seconds clockwise, ramps down smoothly via VFD DC-braking, pauses 3 seconds, and ramps up counter-clockwise without motor current spike.

### Section 11: Operating Principles & Shift Procedures
- **Pre-Start Checks:** Check pneumatic air pressure ($\ge 6\text{ bar}$), verify steam line condensate is drained via upstream trap bypass, inspect drum for loose bolts or debris.
- **Loading:** Load dry garments evenly; do not exceed rated dry capacity (overloading causes poor wash uniformity and motor trip; underloading causes harsh fiber tearing).
- **Heating Sequence:** Add water to required dip level before opening steam valve; never inject live steam into a dry or stationary drum (causes thermal drum distortion).

### Section 12: Preventive Maintenance (PM)
- **Daily:** Inspect pneumatic drain valve seal for wash liquor leakage; inspect door seal rubber for tears; check for unusual rumbling noise from bearings.
- **Weekly:** Grease plummer block bearings with 3–4 pumps of EP2 Lithium Complex grease; check V-belt tension (deflection $10\text{–}15\text{ mm}$ under thumb pressure); test door safety limit switch.
- **Monthly:** Inspect shaft mechanical seal face for weepage; retighten all plummer block mounting bolts to specified torque ($280\text{ Nm}$ for M20 Grade 8.8); inspect drum lifter baffles for weld cracks.
- **Semi-Annual:** Clean motor cooling fan cowl; check VFD terminal connections; drain and replace oil in reduction gearbox (ISO VG 220).

### Section 13: Predictive & Condition-Based Maintenance (CBM)
- **Vibration Analysis (ISO 20816-3:2026):** Measure vibration overall velocity (RMS mm/s) on plummer block housings in horizontal, vertical, and axial directions. Maximum allowable for rigid foundation: Zone C boundary $\le 4.5\text{ mm/s RMS}$; Zone D (Trip / Danger) $> 4.5\text{ mm/s RMS}$. Peak frequency at 1X RPM indicates dynamic unbalance; high frequency peak at bearing BPFO/BPFI indicates race defect.
- **Infrared Thermography:** Scan plummer block bearing housings during full-load wash. Maximum permissible temperature: $75^\circ\text{C}$ (differential $< 15^\circ\text{C}$ between front and rear bearings).

### Section 14: Lubrication Requirements & Schedule
- **Bearings:** Lithium Complex EP2 grease (NLGI Grade 2 with water washout resistance $> 5\%$, base oil viscosity $220\text{ cSt}$ at 40°C per SKF guidelines). Re-greasing: $30\text{ grams}$ per bearing housing every 200 operating hours.
- **Gearbox:** Industrial Extreme Pressure synthetic gear oil (ISO VG 220, e.g. Mobilgear 600 XP 220). Initial oil change at 500 hours; subsequent changes every 4000 hours or annually.
- **Door Hinges & Swivels:** Multi-purpose lithium grease monthly.

### Section 15: Inspection Checklist
- [ ] Air supply pressure confirmed at 6.0 bar.
- [ ] Front door pneumatic latch interlocked and functioning.
- [ ] Zero wash liquor dripping from rear mechanical seal gland.
- [ ] Water inlet and steam angle seat valves closing bubble-tight.
- [ ] Drum reversing cycle timing accurate (15s CW / 3s stop / 15s CCW).
- [ ] Emergency Stop pushbuttons operational on front control panel.

### Section 16: Troubleshooting & Symptom Matrix
- **Symptom 1: Water leaking from rear trunnion during wash.**
  - *Probable Cause:* Worn mechanical seal faces or degraded PTFE packing; grooved shaft sleeve.
  - *Action:* Tighten gland follower evenly by 1/2 turn; if leak persists, replace mechanical seal / sleeve.
- **Symptom 2: Heavy rumbling / knocking sound during drum rotation.**
  - *Probable Cause:* Plummer block spherical roller bearing inner race cracked; loose adapter sleeve; broken drum lifter baffle.
  - *Action:* Stop machine immediately; inspect internal baffles; check bearing clearance with feeler gauge.
- **Symptom 3: Drum does not reverse; trips VFD on overcurrent (OC).**
  - *Probable Cause:* Deceleration ramp time too short causing regenerative voltage spike; VFD braking resistor blown; garments tangled into heavy ball.
  - *Action:* Increase VFD deceleration time parameter from 3s to 5s; check external braking resistor resistance; untangle load.

### Section 17: Failure Analysis & Root Cause Determination
- **Case: Trunnion Shaft Catastrophic Fatigue Fracture:**
  - *Root Cause Analysis:* Initiated at the sharp transition radius between the shaft journal and the spider flange. The absence of a generous fillet radius ($r \ge 12\text{ mm}$) created a high stress concentration factor ($K_t \approx 2.4$). Cyclic alternating bending stresses from heavy, wet, unbalance stone washing exceeded the endurance limit of the normalized EN8 carbon steel shaft after 4 years of two-shift operation.
  - *Permanent Corrective Action:* Machine replacement shaft from forged 42CrMo4 (EN19) quenched and tempered, with CNC-machined polished transition fillet radius ($r = 15\text{ mm}$) and ultrasonic inspection prior to assembly.

### Section 18: Spare Parts & Replacement Protocols
- Front door silicone rubber gasket (profile specific).
- Mechanical shaft seal cartridge assembly (shaft size specific, e.g. $\varnothing 80\text{ mm}$ or $\varnothing 100\text{ mm}$).
- Plummer block bearing inserts: Spherical roller bearings (e.g. 22218 EK + H 318 adapter sleeve).
- Pneumatic angle-seat valve PTFE rebuild seal kit.
- Set of matched V-belts (e.g. 4 pieces of SPA 2240).

### Section 19: Safety Precautions & Interlocks
- **Door Interlock Protocol:** Machine drum drive motor power MUST be positively isolated through a Category 3 dual safety limit switch whenever the loading door is open. Under no circumstances may the door interlock switch be bypassed or jumpered.
- **Confined Space Entry:** When entering the inner drum for baffle welding or stone cleaning: Lock-Out Tag-Out (LOTO) main electrical breaker, disconnect main compressed air supply, and lock manual steam isolation ball valve with padlocks.

### Section 20: Engineering Calculations
- **Drum Peripheral Speed:**
  $$v = \frac{\pi \cdot D \cdot N}{60} = \frac{\pi \times 1.4\text{ m} \times 28\text{ RPM}}{60} = 2.05\text{ m/s}$$
- **Froude Number (Tumbling Verification):**
  $$Fr = \frac{v^2}{g \cdot R} = \frac{2.05^2}{9.81 \times 0.70} = \frac{4.20}{6.87} = 0.61 \quad (\text{Ideal tumbling: } 0.5 < Fr < 0.8)$$
- **Batch Steam Heating Requirement (40°C to 70°C in 15 min):**
  - Total mass $M = 300\text{ kg fabric} + 1500\text{ kg water} = 1800\text{ kg}$.
  - Heat required: $Q = 1800 \times 4.186 \times (70 - 40) = 226,044\text{ kJ}$.
  - Steam required (latent heat $h_{fg}$ at 3.5 bar $= 2148\text{ kJ/kg}$):  
    $$m_s = \frac{226,044}{2148 \times 0.90 \text{ (efficiency)}} = 116.9\text{ kg steam}$$
  - Average steam flow rate: $\dot{m}_s = \frac{116.9}{0.25\text{ hr}} = 467.6\text{ kg/hr}$.

### Section 21: Practical Field Engineering Notes
- **Door Gasket Longevity Tip:** Wipe door silicone seal weekly with dilute silicone oil emulsion; never allow caustic soda to dry and crystallize on the rubber.
- **Pumice Stone Trap:** Ensure perforated lint basket in the floor drain is emptied every 3 batches during stone wash to prevent pumice sand from choking ETP pumps.

---

## 3. Deep Equipment Blueprint 02: Vertical Centrifugal Hydro Extractor (`EQ-B01`)

### Section 01: Introduction & Purpose
The Vertical Centrifugal Hydro Extractor is an essential mechanical dewatering machine that uses high centrifugal force ($G\text{-force} \approx 300\text{–}600\text{G}$) to separate water from washed textile garments, reducing garment moisture content from $> 120\%$ dry basis down to $45\%–55\%$ within 5 to 8 minutes, dramatically slashing subsequent steam and fuel consumption in tumbler dryers.

### Section 02: Applications in Washing Plants
Installed immediately downstream of industrial washers; processes wet batches of denim jeans, twill pants, towels, and knit fabric before transfer to steam tumbler dryers.

### Section 03: Working Principle
Wet garments are packed evenly inside a vertically mounted perforated stainless steel basket. The basket is accelerated to 750–1000 RPM by a high-torque electric motor. The centrifugal force ($F_c = m \cdot r \cdot \omega^2$) forces water outward through the garment fibers and basket perforations into the outer stationary cast-iron casing, where it drains by gravity. The machine assembly is suspended by three heavy cast-iron pendulum suspension links with heavy coil springs that absorb dynamic unbalance forces during acceleration and spin.

### Section 04: Construction & Component Identification
- **Outer Casing:** Heavy cast-iron or heavy steel circular housing with bottom drain spout.
- **Perforated Rotating Basket:** Heavy-gauge AISI 304/316 stainless steel basket with rolled reinforcement top lip, perforated sidewalls, and heavy cast steel bottom spider hub.
- **Vertical Center Spindle:** Forged alloy steel shaft supported by two heavy-duty spherical roller thrust bearings and cylindrical roller bearings in a grease-packed center housing.
- **3-Point Suspension Links:** Three steel pendulum hanging rods anchored to external cast-iron pillar columns with heavy compression springs and spherical ball seats.
- **Mechanical Friction Brake:** Foot-pedal or pneumatic cylinder actuated external brake shoes contracting onto a cast-iron brake drum on the main vertical spindle.
- **Safety Interlocked Top Cover:** Heavy hinged lid with positive pneumatic/solenoid interlock preventing the lid from opening until the basket has come to a complete dead stop.

### Section 05: Component Functions & Interactions
- **Suspension Springs:** Allow the entire rotating center to pivot slightly around its center of gravity, preventing destructive unbalance vibration from transmitting to the building foundation.
- **Brake Drum:** Dissipates the immense kinetic energy ($E_k = \frac{1}{2} I \omega^2$) of the rotating 1000 kg basket within 30–45 seconds during emergency stops.

### Section 06: Technical Specifications
- **Basket Diameter:** 1000 mm, 1200 mm, or 1500 mm.
- **Rotational Speed:** 750 RPM (1500 mm) to 950 RPM (1000 mm).
- **Centrifugal G-Factor:** $350\text{G} - 550\text{G}$.
- **Drive Motor:** 7.5 kW to 18.5 kW high-slip / inverter-duty induction motor.
- **Batch Capacity:** 100 kg to 250 kg wet fabric.
- **Drainage Outlet:** DN100 (4") or DN150 (6") gravity discharge.

### Section 07: Selection Criteria
- **G-Factor vs Basket Diameter:** Larger diameter produces higher G-factor at lower RPM, but increases basket shell hoop stress exponentially ($\sigma \propto r^2 \omega^2$).

### Section 08: Drawings & Exploded Diagrams
- Required drawings:
  1. Sectional elevation showing 3-point suspension columns, ball-and-socket links, and compression springs.
  2. Spindle bearing housing assembly showing upper and lower roller bearings, labyrinth seals, and grease paths.
  3. Basket seam welding and top reinforcement ring structural detail.

### Section 09: Installation Protocols
- **Foundation Isolation:** Must be mounted on a monolithic concrete block with vibration damping cork/rubber isolation pads. Minimum clearance of 800 mm around the machine to accommodate pendulum sway without striking walls.

### Section 10: Alignment & Commissioning
- **Plumb Verification:** Check vertical spindle alignment using a precision plumb bob or digital inclinometer across three axes ($\le 0.05^\circ$ verticality).
- **Suspension Spring Equalization:** Measure installed compressed height of all three suspension springs with a vernier caliper; adjust adjusting nuts until all three spring heights are identical within $\pm 0.5\text{ mm}$.

### Section 11: Operating Principles & Shift Procedures
- **Even Loading Mandate:** Garments must be loaded circumferentially around the perimeter with balanced weight distribution; never dump garments in one lump.
- **Spin Cycle:** Accelerate smoothly; observe vibration through startup passing critical speed (typically 120–180 RPM); run at full speed for 5–7 minutes until water discharge ceases.

### Section 12: Preventive Maintenance
- **Daily:** Test mechanical brake holding power; verify lid safety interlock operates (motor cuts off immediately when lid latch is released).
- **Weekly:** Check suspension link ball seats for wear and lubricate with heavy grease; inspect basket interior for loose rivets or cracked welds.
- **Monthly:** Check vertical spindle runout with dial indicator ($< 0.15\text{ mm}$); re-grease spindle bearings with high-temperature, water-resistant lithium complex grease.

### Section 13: Predictive Maintenance
- **Vibration Velocity Monitoring (ISO 20816-3:2026):** Measure overall RMS velocity on top bearing housing at full speed. Machines on flexible support: Zone A ($\le 2.3\text{ mm/s RMS}$), Zone B ($\le 4.5\text{ mm/s RMS}$), Zone C (Alert: $\le 7.1\text{ mm/s RMS}$), Zone D (Emergency Trip / Danger: $> 7.1\text{ mm/s RMS}$).

### Section 14: Lubrication Requirements
- **Spindle Bearings:** Lithium Complex EP2 grease with solid anti-wear additives ($40\text{ grams}$ per month per SKF specs).
- **Suspension Ball Joints:** Heavy calcium/graphite grease weekly.

### Section 15: Inspection Checklist
- [ ] Lid interlock physically prevents lid from opening while basket rotates.
- [ ] Brake brings basket from full speed to stop within 40 seconds.
- [ ] Drain spout clear of fabric threads.
- [ ] All three suspension spring heights verified equal.

### Section 16: Troubleshooting
- **Symptom: Violent knocking and machine rocking during acceleration.**
  - *Root Cause:* Uneven garment loading; broken suspension spring; unequal spring tension.
  - *Remedy:* Stop immediately via foot brake; redistribute garments evenly; inspect and equalize springs.

### Section 17: Failure Analysis & Root Cause Determination
- **Catastrophic Failure Mode: Basket Shell Rupture (Explosion Hazard):**
  - *Analysis:* Centrifugal stress produces immense hoop stress ($\sigma_h = \rho v^2$). Micro-cracking at basket perforation holes combined with chloride stress corrosion cracking (SCC) from bleach liquor leads to fast brittle basket seam fracture at 1000 RPM, throwing metal fragments.
  - *Prevention:* Annual dye penetrant testing (PT) of basket welded seams; immediate retirement of baskets showing crack propagation.

### Section 18: Spare Parts & Replacement Protocols
- Set of 3 matched suspension compression springs.
- Replacement brake shoes with woven brass-wire-reinforced friction lining.
- Vertical spindle upper spherical roller thrust bearing (e.g. 29416) and lower roller bearing (e.g. 22216).

### Section 19: Safety Precautions & Interlocks
- **CRITICAL LIFE SAFETY DIRECTIVE:** The interlock preventing lid opening during rotation is the primary defense against arm amputation fatalities. Defeating, tying off, or bypassing the lid switch is grounds for immediate disciplinary termination.

### Section 20: Engineering Calculations
- **Centrifugal Acceleration (G-Factor):**
  $$G = \frac{r \cdot \omega^2}{g} = \frac{0.60\text{ m} \times \left(\frac{2\pi \times 850}{60}\right)^2}{9.81} = \frac{0.60 \times 7921}{9.81} = 484.5\text{ G}$$
- **Basket Shell Hoop Stress:**
  $$v = \frac{\pi \times 1.2\text{ m} \times 850}{60} = 53.4\text{ m/s}$$
  $$\sigma = \rho_{ss} \cdot v^2 = 7930\text{ kg/m}^3 \times (53.4)^2 = 22.61\text{ MPa} \quad (\text{Well within yield strength } 205\text{ MPa})$$

### Section 21: Practical Field Engineering Notes
- Mark a bold red alignment dot on the basket rim and casing rim; operators can easily see when the basket has come to a complete stop before unlocking the lid.
