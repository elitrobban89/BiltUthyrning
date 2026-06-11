# BilUthyrning
Biluthyrningssystem byggt med JavaFX och Spring Boot.

---

## ⚠️ Två versioner — två separata databaser

Det finns **två sätt** att köra systemet. De delar **inte** databas med varandra — ändringar i ett system syns **inte** i det andra.

### 🌐 Webbversion (live på internet)
- **URL:** https://elitrobban.se/bilradgivning/
- **Backend:** https://biltuthyrning.onrender.com
- **Databas:** PostgreSQL i molnet (Render)
- **Driftsatt via:** Docker på Render (byggs automatiskt vid push till `main`)
- Inloggning: **admin** / **admin123**

### 🖥️ JavaFX-appen (lokal desktop)
- **Databas:** SQLite — filen `biltuthyrning.db` på din lokala dator
- **Starta:** Kör `run.bat` i projektmappen. Kräver Java 21+ och JavaFX SDK (sökväg konfigureras i `run.bat`).
- Inloggning: **admin** / **admin123**

---

---

## Changelog

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
| Tjänster | `CarService`, `BookingService`, `UserService` |
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
| GET | `/api/bookings/cars/{carId}/availability` | Kontrollera tillgänglighet |
| GET | `/api/cars` | Hämta alla bilar |

## Databas

### Lokal (JavaFX)
- Fil: `biltuthyrning.db` (SQLite, skapas automatiskt vid start)
- Konfiguration: `src/main/resources/application.properties`

### Moln (Webb / Render)
- PostgreSQL — connection string sätts via miljövariabeln `DATABASE_URL` i Render-dashboarden
- Konfiguration: `src/main/resources/application-prod.properties`
- Schema skapas automatiskt av Hibernate (`ddl-auto=update`)

Tabeller (båda): `cars`, `bookings`, `users`
