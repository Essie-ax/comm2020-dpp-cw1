# End-to-End Test Scenarios

Manual E2E test cases for the DPP system. Run with the server started via `WebApp.java` in InMemory mode
unless the DB column specifies MySQL. Each scenario lists the steps, expected result, and pass/fail mark.

Demo accounts: `player1` / `gamekeeper1` — password: `password`

---

## Scenario 1: Player Login and Logout

**Goal:** Verify login flow works and token is cleared on logout.

**Steps:**

1. Open `http://localhost:8080/login.html`
2. Enter username `player1`, password `password`, click Log in
3. Expect redirect to `player.html`; challenge table should load
4. Click Logout
5. Browser should redirect back to `login.html`
6. Manually navigate to `http://localhost:8080/player.html` — should redirect to login

**Expected result:** Login succeeds, token is stored in localStorage, logout clears it and forces redirect.

---

## Scenario 2: GameKeeper Creates a Challenge

**Goal:** Verify that a GameKeeper can create a new challenge with required fields.

**Steps:**

1. Login as `gamekeeper1`
2. Open `http://localhost:8080/gamekeeper.html`
3. Fill in Title: `E2E Test Challenge`, Category: `Battery`, Start: `2026-03-01`, End: `2026-06-01`
4. Fill in Required Fields: `brand,origin`, Evidence Types: `CERTIFICATE`
5. Click Create Challenge
6. Check that the new challenge appears in the challenge list below

**Expected result:** Challenge is created with ID returned, listed in the table with correct title and category.

---

## Scenario 3: Player Submits a Passport to a Challenge

**Goal:** Verify that a player can submit a passport and receive a score and outcome.

**Steps:**

1. Login as `player1`, open `http://localhost:8080/player.html`
2. Find a challenge in the table, enter passport ID `1` in the input
3. Click Submit
4. The submission history table should update with the new row

**Expected result:** Score and outcome (PASS or FAIL) are shown; feedback message appears at the top.

---

## Scenario 4: Leaderboard Shows Rankings

**Goal:** Verify that the leaderboard ranks submissions correctly.

**Steps:**

1. Login as any user, open `http://localhost:8080/leaderboard.html`
2. Enter a challenge ID that has at least two submissions (use challenge 1 after Scenario 3)
3. Click Search / Load
4. Table should show ranked rows, highest score first

**Expected result:** Rows are ordered by score descending. Rank column starts at 1.

---

## Scenario 5: Passport Comparison Between Two Passports

**Goal:** Verify the compare page shows differences correctly.

**Steps:**

1. Login as `player1`, open `http://localhost:8080/compare.html`
2. Enter Passport ID 1 and Passport ID 2, click Compare
3. The field diff table should appear
4. Rows where both passports have different values should appear first (mismatch rows)
5. Rows where values are equal should appear last

**Expected result:** Two passport summary cards shown with scores; diff table renders with mismatches highlighted at the top.

---

## Scenario 6: Analytics Dashboard Shows Aggregated Data

**Goal:** Verify analytics page shows summary stats after at least one submission.

**Steps:**

1. Complete Scenario 3 first to ensure at least one submission exists
2. Login as `gamekeeper1`, open `http://localhost:8080/analytics.html`
3. Summary cards should show: Total Challenges >= 1, Total Submissions >= 1, Avg Score > 0, Pass Rate value

**Expected result:** All four stat cards display numeric values. Score distribution bars show at least one non-zero bar. Per-challenge table shows at least one row.

---

## Scenario 7: Unauthenticated Access Redirects to Login

**Goal:** Verify that protected pages redirect unauthenticated users.

**Steps:**

1. Open a private/incognito browser window (localStorage is empty)
2. Navigate directly to `http://localhost:8080/player.html`
3. Page should redirect to `login.html` without showing player content
4. Repeat for `gamekeeper.html`, `analytics.html`, `leaderboard.html`

**Expected result:** All protected pages redirect to login when no token is present.

---

## Scenario 8: Passport Validation Rejects Invalid Data

**Goal:** Verify that the validation API returns errors for bad passport data.

**Steps:**

1. Login as `gamekeeper1`
2. Send a POST to `http://localhost:8080/api/passports/validate` with body:
   ```json
   {"brand": "", "category": "Battery", "weight": -1, "recyclable_percentage": 150}
   ```
   (use browser DevTools or a REST client)
3. Response should be HTTP 400 with a list of validation errors

**Expected result:** Response contains errors for `brand`, `weight`, and `recyclable_percentage`. No 500 error.

---

## Scenario 9: Player Cannot Access GameKeeper Actions

**Goal:** Verify role separation between PLAYER and GAME_KEEPER.

**Steps:**

1. Login as `player1`
2. Attempt to POST `http://localhost:8080/api/challenges` with a valid challenge body via DevTools
3. Expect HTTP 403 response

**Expected result:** Server returns 403 Forbidden; no challenge is created.

---

## Scenario 10: Mobile Layout on Narrow Screen

**Goal:** Verify responsive design works at 375 px viewport width.

**Steps:**

1. Open `http://localhost:8080/login.html` in Chrome DevTools with device set to iPhone SE (375×667)
2. Verify the login card is centred and does not overflow horizontally
3. Open `player.html`, verify navigation links wrap and the challenge table scrolls horizontally
4. Open `gamekeeper.html`, verify form inputs stack vertically
5. Tab through the login form and check that `:focus-visible` outlines appear on inputs and button

**Expected result:** No horizontal overflow on any page at 375 px. Focus outlines visible for keyboard users.
