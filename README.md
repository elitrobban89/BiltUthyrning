# BilUthyrning
Biluthyrningssystem byggt med JavaFX och Spring Boot.

## Starta appen
Kör `run.bat` i projektmappen. Kräver Java 21+ och JavaFX SDK (sökväg konfigureras i `run.bat`).

Standardinloggning: **admin** / **admin123**

---

## Changelog

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
| Volvo XC90 | 2025 | 1.5L B5 | 1 499 kr |
| Volvo EX60 P6 | 2025 | El — 300 kW | 1 199 kr |
| Volvo S90 | 2023 | B5 AWD | 1 299 kr |
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
- **Java 21** / **JavaFX 21**
- **Spring Boot 3.x** (Web, Data JPA)
- **SQLite** via `sqlite-jdbc` + Hibernate Community Dialects
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
- Fil: `biltuthyrning.db` (SQLite, skapas automatiskt vid start)
- Tabeller: `cars`, `bookings`, `users`
- `spring.jpa.hibernate.ddl-auto=update` — schemat uppdateras automatiskt
