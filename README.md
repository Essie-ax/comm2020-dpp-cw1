# COMM2020 Digital Product Passport Builder League (v1.0.0)

A web application for creating, validating, scoring, and comparing Digital Product Passports (DPPs). Players build passports and submit them to challenges; Game Keepers publish challenges and scoring rules; the system scores submissions automatically.

## Repository Status

- **Current milestone: CW2 final prototype (v1.0.0)**
- Stable end-to-end workflow: authoring → validation → scoring → comparison → analytics.
- 53 automated JUnit 5 tests, 8+ manual end-to-end scenarios.
- Seeded dataset: 150 products, 200 passports, 300 evidence items, 6 challenges, 30 submissions.

## Features

| Feature | Description |
|---------|-------------|
| Passport Authoring | Create/edit passports with category-specific fields (Battery, Textiles, Cosmetics, Electronics) |
| Validation | 12+ rules covering field presence, range/type, cross-field consistency, and evidence requirements |
| Evidence Attachment | Link certificates, test reports, audits, and supplier statements to passport fields |
| Scoring | Completeness score + confidence/evidence score with transparent rules and bonus points |
| Consumer View | Clean passport summary with all fields displayed |
| Passport Comparison | Side-by-side field diff of two passports with score comparison |
| Challenge System | Game Keepers define challenges with required fields, evidence types, and scoring bonuses |
| Leaderboard | Per-challenge rankings sorted by score |
| Analytics Dashboard | Aggregate statistics: totals, averages, pass rate, score distribution, per-challenge breakdown |
| Mobile Accessibility | Responsive layout, semantic HTML, ARIA attributes, keyboard navigation |

## Tech Stack

- **Backend**: Java 17+ (com.sun.net.httpserver)
- **Database**: MySQL 8 (optional — runs in-memory by default)
- **Build tool**: Maven
- **Testing**: JUnit 5
- **Frontend**: Plain HTML + vanilla JavaScript (no frameworks)

## Quick Start (In-Memory Mode)

No database required. The system uses in-memory DAOs with seeded demo data.

### Prerequisites

- JDK 17 or above (JDK 21 recommended)
- IntelliJ IDEA (Community edition is fine)

### Steps

```bash
git clone https://github.com/Essie-ax/comm2020-dpp-cw1.git
cd comm2020-dpp-cw1
```

**Option A — IntelliJ IDEA (recommended):**

1. File → Open → select the project folder
2. Wait for Maven dependency resolution to complete
3. Navigate to `src/main/java/uk/ac/comm2020/WebApp.java`
4. Right-click → Run 'WebApp.main()'
5. Open `http://localhost:8080/login.html`

**Option B — Command line:**

```bash
mvn clean compile exec:java -Dexec.mainClass=uk.ac.comm2020.WebApp
```

### Demo Accounts

| Username | Password | Role |
|----------|----------|------|
| player1 | password | PLAYER |
| gamekeeper1 | password | GAME_KEEPER |

## Setup with MySQL

1. Copy `.env.example` to `.env` and fill in your database credentials:

```
DB_URL=jdbc:mysql://localhost:3306/comm2020_dpp?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
DB_USER=comm2020
DB_PASS=your_password
USE_DB=true
```

2. Run the SQL scripts in order:

```bash
mysql -u comm2020 -p comm2020_dpp < db/migrations/001_init.sql
mysql -u comm2020 -p comm2020_dpp < db/seed/seed.sql
mysql -u comm2020 -p comm2020_dpp < db/migrations/003_extend_challenge.sql
mysql -u comm2020 -p comm2020_dpp < db/seed/seed_cw2.sql
mysql -u comm2020 -p comm2020_dpp < db/seed/seed_full.sql
```

3. Start the server — it will detect the database and switch to MySQL mode automatically.

## How to Run Tests

```bash
mvn test
```

This runs all 53 JUnit 5 tests covering:
- Authentication and session management
- Passport creation, validation, and scoring
- Challenge creation and submission workflow
- Passport comparison logic
- Analytics aggregation
- Environment configuration
- Database DAO operations

## Project Structure

```
src/main/java/uk/ac/comm2020/
├── WebApp.java                  # Entry point and route registration
├── controller/                  # HTTP request handlers
│   ├── AuthController.java
│   ├── ChallengeController.java
│   ├── ComparisonController.java
│   ├── AnalyticsController.java
│   ├── PassportAuthorController.java
│   ├── PassportReadController.java
│   ├── PassportValidationController.java
│   └── LeaderboardController.java
├── service/                     # Business logic
├── dao/                         # Data access (InMemory + MySQL implementations)
├── model/                       # Domain models (Passport, Evidence, etc.)
├── config/                      # Environment configuration
└── util/                        # HTTP utilities, JSON helpers

src/main/resources/static/       # Frontend HTML + JS
├── login.html
├── player.html
├── authoring.html
├── gamekeeper.html
├── consumer.html
├── compare.html
├── leaderboard.html
├── analytics.html
└── js/

src/test/java/                   # JUnit 5 test suite

db/
├── migrations/                  # Schema DDL
│   ├── 001_init.sql
│   └── 003_extend_challenge.sql
└── seed/                        # Seed data
    ├── seed.sql
    ├── seed_cw2.sql
    └── seed_full.sql            # Full dataset (150 products, 200 passports, 300 evidence)
```

## Team

| Member | Key Contributions |
|--------|-------------------|
| zzzcdsv | Passport comparison, challenge enhancement, analytics dashboard |
| Essie-ax | Database schema/seed, UX improvements (authoring, navigation), deployment |
| vannybowl | Mobile accessibility, responsive design, ARIA/semantic HTML |
| srsforever | Automated test suite (53 tests), test documentation |

## Maintenance Guide

### Adding a new product category

1. Add seed data in `db/seed/` with products, templates, and passports for the new category
2. Update `InMemoryPassportDao.seedDemoData()` if in-memory demo data is needed
3. Add the category option in `authoring.html` dropdown

### Adding new validation rules

1. Edit `src/main/java/uk/ac/comm2020/service/ValidationService.java`
2. Add corresponding test cases in `src/test/java/uk/ac/comm2020/ValidationServiceTest.java`
3. For database mode, insert rules into the `validation_rule` table

### Changing the server port

Set the `APP_PORT` environment variable or the `PORT` environment variable before starting.
