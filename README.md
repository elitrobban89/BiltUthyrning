# BilUthyrning

![Build & Test](https://github.com/elitrobban89/BiltUthyrning/actions/workflows/maven.yml/badge.svg)

Biluthyrningssystem byggt med JavaFX och Spring Boot.

---

## ⚠️ Två versioner — två separata databaser

Det finns **två sätt** att köra systemet. De delar **inte** databas med varandra — ändringar i ett system syns **inte** i det andra.

### 🌐 Webbversion (live på internet)
- **URL:** https://elitrobban.se/biluthyrning/
- **Backend:** https://biltuthyrning.onrender.com
- **Databas:** PostgreSQL i molnet (Render)
- **Driftsatt via:** Docker på Render (byggs automatiskt vid push till `main`)
- Inloggning: **admin** / **admin123**

### 🖥️ JavaFX-appen (lokal desktop)
- **Databas:** SQLite — filen `biltuthyrning.db` på din lokala dator
- **Starta:** Kör `run.bat` (eller `mvn javafx:run`). Kräver Java 17+ och Maven — JavaFX hanteras automatiskt av Maven.
- Inloggning: **admin** / **admin123**

---

## Changelog

### Prisbadge klipps inte på smala mobiler (2026-07-19)
- Vid ~320px (iPhone SE) klipptes prisbadgen av kortkanten när flottans långa modellnamn
  ("Volkswagen e-Transporter Kombi L2 160 kW 64 kWh") inte fick plats. Bilnamnet krymper
  och radbryts nu i stället (`.car-info` min-width 0 + overflow-wrap), prisbadgen kan
  aldrig krympa (flex-shrink 0), och badge-remsan smalnas av under 360px
- Det tidigare rapporterade "klippet vid 390px" visade sig vara en artefakt i headless
  Edge-skärmdumpar (viewporten renderas ~100px bredare än `--window-size` så bilden
  beskärs till höger) — riktiga 390px-layouten var redan hel. Verifierat vid 320 och
  390px med iframe-harness och riktiga flottnamn

### Unika modellnamn i flottan (2026-07-18)
- EV-listan har två varianter med visningsnamnet "Rolls-Royce Spectre Series II" → flottan
  fick två identiska bilar. `selectSpread` dedupar nu på modellnamn, och boten städar bort
  befintliga dubbletter vid boot (äldsta raden behålls, bilar med bokningar rörs aldrig)
  och fyller upp till 100 med nästa unika kandidat. 67 tester.

### Ultralyx-prisnivå + omprisning vid boot (2026-07-18)
- Rolls-Royce Spectre hyrdes ut för 1 099 kr/dag — ultralyxmärken (Rolls-Royce, Bentley,
  Ferrari, Lamborghini, Aston, McLaren m.fl.) får nu +2 500 kr i stället för premiumens +250 kr
- Delade bilar (år "–") omprisas vid varje boot utifrån aktuell heuristik — prisändringar
  når därmed befintliga databasrader utan manuell migrering
- 66 tester (2 nya: ultralyxnivån, drivlina ur motorfältet)

### Delad flotta från CarAdvice + tester & CI (2026-07-18)
- **`FleetSyncService`**: flottan fylls vid start upp till 100 bilar ur CarAdvice-backendens
  publika API:er — verifierad förbrukning i motorfältet, max 2 per märke, deterministiska
  demo-priser, fail-silent vid API-fel
- **Batch-tillgänglighet**: `GET /api/bookings/availability` svarar för hela flottan i ett
  anrop (dashboarden gjorde ett anrop per bil — ohållbart med ~100 bilar mot en pool på 3)
- **Buggfix**: EV/PHEV-badgen styrs nu av motorfältet, inte modellnamnet ("Lexus" innehåller "EX")
- Drivmedelsfiltret utökat med Diesel och Hybrid; deterministisk färgpalett för nya märken
- **Tester & CI**: 64 tester (24 nya: FleetSync-logik + HTTP-stubb, MockMvc för alla
  controllers, CarUtils) + GitHub Actions-workflow med badge

### UI-polish + layout-fix + auto-rensning (2026-06-11)
- **Aurora-bakgrund** — 3 animerade färgblobbar (blå, lila, cyan) bakom hela appen, rör sig sakta i loop
- **Space Grotesk** ersätter Inter — skarpare och mer karakteristisk font
- **Staggerade entrance-animationer** — header, bilkort och höger panel glider in vid sidladdning
- **Shimmer-effekt** på primärknapp vid hover — vit reflex sveper igenom
- **Gradient-text** på logotyp och inloggningsrubrik
- **Accentbar** framför korttitlar (3px blå linje med glow)
- **Inloggningssidan polerad** — glödande logo-cirkel med pulsande ringar, tagline, bredare kort, accentlinje, subtil punktgrid i bakgrunden
- **Stats i headern** — bilar / aktiva bokningar / total intäkt visas som chips i headerraden
- **Alla bokningar rensas vid utloggning** — Spring Security `LogoutHandler` anropar `bookingService.deleteAllBookings()`
- **Ingen sidscroll** — `body: height 100vh`, `min-height: 0` på flex-containrar, bokningstabell fyller kvarvarande höjd

### Glassmorphism redesign (2026-06-11)
- Mörk gradient-bakgrund med blå/lila/cyan färgblobs
- Frosted glass-effekt på alla kort, paneler och header (`backdrop-filter: blur`)
- Glödande hover-effekter på bilkort och knappar
- Halvtransparenta formulärfält med mörkt tema
- Inloggningssidan fick djupt glasskort med stark bakgrundsoskärpa
- CSS-variabelsystem för konsekvent glasdesign

### Bokningsfilter + laddningsindikator (2026-06-11)
- Bokningstabell kan nu filtreras på **kundnamn** (textsökning i realtid)
- Bokningstabell kan nu filtreras på **datumintervall** (från–till startdatum)
- Status-, kund- och datumfilter fungerar tillsammans
- Laddningsindikator i WordPress-iframe under Render cold-start (~30s)

### Webb-UI redesign (2026-06-11)
- Nytt typsnitt: **Inter** (Google Fonts) — skarpare och modernare
- Header med blå gradient (`#1a365d → #2b6cb0`)
- Kort med mjukare skuggor och rundare hörn (12px)
- Knappar med gradient och hover-animation (lyft + skugga)
- Formulärfält med focus-glow (blå ring)
- Priskortets bakgrund i blå gradient
- Statusknappar i Bekräftad/Avbokad/Avslutad — uppdaterade färger
- Inloggningssidan fick blå gradient-bakgrund
- Responsiv layout — panelerna staplas vertikalt under 960px bredd

### Tre nya bilar i flottan (2026-06-04)
- **Volvo EX30 Twin Motor** (2024) — El 315 kW Twin Motor — 1 099 kr/dag — teal-grön färg + EV ⚡-badge
- **Volvo V60 B6** (2023) — 3.0L B6 Bensin + dragkrok — 1 199 kr/dag — burgunder-röd kombiprofil
- **Volvo XC60 T8 Recharge** (2024) — T8 Laddhybrid 340 kW — 1 349 kr/dag — PHEV 🔌-badge
- `DataInitializer` lägger nu till bilar per modellnamn (idempotent — säker att köra mot befintlig databas)

### UX-förbättringar & bokningshantering (2026-06-04)
- Tillgänglighetsbadge per bil i listan — visar "Ledig" (grön) eller "Uppbokad" (röd) baserat på valda datum, uppdateras live
- Live tillgänglighets- och prisindikator i bokningsformuläret — "Uppbokad"-bilen låser bekräfta-knappen automatiskt
- Datumvalidering i realtid — röd varningstext visas direkt om slutdatum är före startdatum
- Statusfilter på bokningstabell — dropdown: Alla / Bekräftade / Avbokade / Avslutade
- Ny "Markera som klar"-knapp (grön) — sätter bokning till `COMPLETED`
- Avboka- och Markera-knapparna är inaktiva tills en `CONFIRMED` bokning väljs i tabellen

### GUI-redesign & nya bilar (2026-05-31)
- Omdesignad UI med modernt blått tema (Volvo-inspirerat)
- Varje bil i listan visas nu med en JavaFX Canvas-ritad sidoprofil anpassad per biltyp:
  - SUV-profil för XC40, XC60 och EX60
  - Stor SUV-profil för XC90
  - Sedanprofil för S90
  - Kombiprofil för V90
- EX60 visas med grön färg och "EV ⚡"-badge
- Bokningslistan är nu en TableView (rad-markering för avbokning, inte manuellt ID)
- Inloggningsfönstret har fått blå banner och renare layout
- Lade till tre nya bilar i flottan:
  - **Volvo EX60 P6** (2025) — El 300 kW — 1 199 kr/dag
  - **Volvo XC60 B4** (2024) — 2.0L B4 Mild Hybrid — 999 kr/dag
  - **Volvo XC60 D5** (2022) — 2.0L D5 Diesel — 949 kr/dag
- Uppdaterade motorbeteckningar till Volvos nuvarande namnstandard (B4 AWD, B5 AWD)
- Appnamnet rättat till **BilUthyrning**

### Inloggningsfunktion (2026-05-31)
- Lade till `User` JPA-entitet med användarnamn och SHA-256-hashat lösenord
- Lade till `UserRepository` (Spring Data JPA)
- Lade till `UserService` med inloggningsvalidering och registrering
- Lade till `LoginGUI` — inloggnings-/registreringsskärm med två flikar
- `CarRentalGUI` startar nu endast efter lyckad inloggning
- `DataInitializer` skapar standardanvändare: **admin** / **admin123**

---

## Bilflotta

Flottan består av två delar:

1. **Kurerad basflotta** — 10 Volvo-modeller med handsatta priser (tabellen nedan).
2. **Delad flotta från CarAdvice** — vid start fylls flottan upp till **100 bilar** ur
   CarAdvice-backendens publika API:er (`/api/ev-consumption` + `/api/ice-consumption`,
   samma databas som Bilresa-kalkylatorn konsumerar). `FleetSyncService` blandar el och
   fossilt varannan bil, tar max 2 per märke och sätter deterministiska demo-dagspriser
   (bas per drivlina + premiumpåslag 250 kr eller ultralyxpåslag 2 500 kr för
   Rolls-Royce/Bentley/Ferrari m.fl. + hashvariation per namn; delade bilar omprisas
   vid varje boot så heuristikändringar når befintliga rader). Motorfältet får bilens
   **verifierade förbrukning** (t.ex. `El · 1,55 kWh/mil`, `Diesel · 0,62 l/mil`).
   Fail-silent: är CarAdvice nere behålls basflottan som den är.

| Modell | År | Motor | Pris/dag |
|---|---|---|---|
| Volvo XC40 | 2023 | B4 AWD | 899 kr |
| Volvo XC60 B4 | 2024 | 2.0L B4 Mild Hybrid | 999 kr |
| Volvo XC60 D5 | 2022 | 2.0L D5 Diesel | 949 kr |
| Volvo XC60 T8 Recharge | 2024 | T8 Laddhybrid — 340 kW | 1 349 kr |
| Volvo XC90 | 2025 | 1.5L B5 | 1 499 kr |
| Volvo EX30 Twin Motor | 2024 | El — 315 kW Twin Motor | 1 099 kr |
| Volvo EX60 P6 | 2025 | El — 300 kW | 1 199 kr |
| Volvo S90 | 2023 | B5 AWD | 1 299 kr |
| Volvo V60 B6 | 2023 | 3.0L B6 Bensin + dragkrok | 1 199 kr |
| Volvo V90 | 2022 | 2.0L B4 | 1 099 kr |

---

## Arkitektur

| Lager | Klasser |
|---|---|
| GUI | `CarRentalGUI`, `LoginGUI` |
| Tjänster | `CarService`, `BookingService`, `UserService`, `FleetSyncService` |
| Modell | `Car`, `Booking`, `User` |
| Repository | `CarRepository`, `BookingRepository`, `UserRepository` |
| REST API | `CarController`, `BookingController` |
| Initiering | `DataInitializer`, `BiltUthyrningApplication` |

## Teknikstack

| | JavaFX (lokal) | Webb (Render) |
|---|---|---|
| Databas | SQLite (`biltuthyrning.db`) | PostgreSQL (moln) |
| Bygge | `mvn javafx:run` / `run.bat` | Docker, `-P web` |
| Profil | *(standard)* | `prod` |

- **Java 17** / **JavaFX 21**
- **Spring Boot 3.2.5** (Web, Data JPA, Security, Thymeleaf)
- **Maven** för bygge och beroenden

## REST API

| Metod | Endpoint | Beskrivning |
|---|---|---|
| GET | `/api/bookings` | Hämta alla bokningar |
| GET | `/api/bookings/{id}` | Hämta bokning |
| POST | `/api/bookings` | Skapa bokning |
| DELETE | `/api/bookings/{id}` | Avboka |
| GET | `/api/bookings/cars/{carId}/availability` | Kontrollera tillgänglighet för en bil |
| GET | `/api/bookings/availability?start=&end=` | Tillgänglighet för HELA flottan i ett svar (karta bil-id → ledig) — dashboarden gjorde tidigare ett anrop per bil, ohållbart med ~100 bilar |
| GET | `/api/cars` | Hämta alla bilar |
| GET | `/health` | Hälsokontroll för UptimeRobot (returnerar HTTP 200 OK) |

## Tester & CI

67 tester i tre lager — ren logik, HTTP-felvägar mot lokal stubbserver och controller-lagret (MockMvc, tjänsterna mockas):

| Testklass | Täcker |
|---|---|
| `BookingServiceTest` (21) | Tillgänglighet, prisberäkning, bokningslivscykel (skapa/avboka/avsluta), batch-tillgänglighet för hela flottan |
| `BookingRepositoryTest` (11) | Integrationstester mot SQLite in-memory: överlappsquery för konflikter, statusfiltrering |
| `UserServiceTest` (10) | Registrering, lösenordshashning, valideringar |
| `FleetSyncServiceTest` (9) | Delade flottan: JSON-parsning av EV/ICE-listor, svensk decimalformatering, deterministisk prisheuristik med premium- och ultralyxpåslag, drivlina ur motorfältet, märkesspridning, namn-dedup och limit |
| `WebControllerTest` (5) | MockMvc: dashboardens statistik (avbokade exkluderas ur intäkt), health, boknings-flash (lyckad + uppbokad), registrering |
| `BookingControllerTest` (3) | MockMvc: batch-tillgänglighet som JSON-karta, per-bil-tillgänglighet, bokningslistan |
| `FleetSyncServiceHttpTest` (3) | HTTP-felvägar mot lokal stubbserver: lyckad hämtning, 500 → tom lista utan exception, limit 0 anropar aldrig nätet |
| `CarUtilsTest` (3) | EV/PHEV-badge styrs av motorfält (Lexus-fällan), fem drivmedelstyper, deterministisk färgpalett |
| `CarControllerTest` (2) | MockMvc: flottan som JSON med förbrukning i motorfältet, enskild bil |

GitHub Actions ([maven.yml](.github/workflows/maven.yml)) kör testerna på varje push — badgen överst visar status.

## Databas

### Lokal (JavaFX)
- Fil: `biltuthyrning.db` (SQLite, skapas automatiskt vid start)
- Konfiguration: `src/main/resources/application.properties`

### Moln (Webb / Render)
- PostgreSQL — connection string sätts via miljövariabeln `DATABASE_URL` i Render-dashboarden
- Konfiguration: `src/main/resources/application-prod.properties`
- Schema skapas automatiskt av Hibernate (`ddl-auto=update`)

Tabeller (båda): `cars`, `bookings`, `users`

---

## Kända förbättringsområden

| # | Område | Beskrivning |
|---|---|---|
| 1 | **Säkerhet** | Lösenord hashas med SHA-256 utan salt — bör bytas till BCrypt |
| 2 | **Adminpanel** | Bilar kan inte läggas till/redigeras/tas bort via webbgränssnittet |
| 3 | **Felsida** | Whitelabel Error Page visas vid fel — bör ersättas med egen sida |
| 4 | **Laddningsindikator** | ✅ Åtgärdad — UptimeRobot pingar `/health` var 5:e min, Render sover aldrig. Spinner visas som fallback. |
| 5 | **Paginering** | ✅ Åtgärdad — alla bokningar rensas automatiskt vid utloggning, tabellen hålls alltid kort |
| 6 | **Datumfilter** | ✅ Åtgärdad — filtrering på kundnamn och datumintervall tillagt |
| 7 | **Databas-expiry** | Render free-tier PostgreSQL raderas efter 90 dagar — bör säkerhetskopieras eller uppgraderas |
