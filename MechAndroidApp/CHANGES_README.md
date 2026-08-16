# MachFind — Testing Build: What Changed

This zip is the same project you uploaded, with the "pure code, no external
deps" gaps built in. It has **not been compiled** (no Android SDK / network in
this environment) — open it in Android Studio and let Gradle sync before
assuming it's error-free. Everything below is real, functional code following
the app's existing single-Activity / ViewFlipper pattern, not stubs.

## 1. Tow-truck-specific booking flow ✅
- New form: `res/layout/activity_book_tow.xml`
- New function: `navigateToBookTowUi()` in `MainActivity.kt`
- Fields: vehicle make, **pickup point**, **drop-off/destination garage**,
  **drivable / not drivable** radio choice, free-text issue notes.
- `navigateToBookMechanicUi()` now checks `targetType == "TowTruck"` and
  routes here automatically instead of showing the mechanic appointment form.
- Writes to the same `TowTruckBookings` table as before, just with the new
  fields populated (`pickupLocation`, `dropoffLocation`, `vehicleCondition`).

## 2. Mechanic-side booking management ✅
- New screen: `res/layout/activity_mechanic_bookings.xml`
- New function: `navigateToMechanicBookingsUi()`
- Pulls every booking (from both `MechanicBookings` and `TowTruckBookings`)
  addressed to the signed-in provider, sorted pending-first.
- Real **Accept / Decline** buttons on pending requests, **Mark Completed**
  on accepted ones — each writes the new `status` field back to Firebase and
  queues an in-app notification for the customer.
- Wired up from the mechanic dashboard: the "See All" link and the "Jobs"
  quick-action tile now both open this real list (the static demo card above
  them is left as visual filler only — it's not live data).

## 3. Booking status tracking ✅
- `BookingDto` gained a `status` field: `pending` → `accepted`/`declined` →
  `completed` (or `cancelled`), always starts `pending`.
- Customer side: new **My Bookings** screen
  (`res/layout/activity_my_bookings.xml`, `navigateToMyBookingsUi()`),
  reachable from a new button on the Profile screen. Shows every booking the
  user made and its live status with a status color.

## 4. Ratings / reviews ✅
- New `RatingDto` (bookingId, mechanicId, customerId, stars 1-5, comment,
  timestamp), stored at `Ratings/{mechanicId}/{ratingId}`.
- Once a booking is `completed`, "My Bookings" shows a **Rate this service**
  button → a star-rating + comment dialog (`showRatingDialog()`) → writes the
  rating and flags the booking `isRated = true` so it isn't prompted twice.
- The mechanic profile screen no longer shows the hardcoded "4.8 (120
  Reviews)" — it now fetches real ratings for that provider and computes a
  live average (`tvMechanicRating`).

## 5. Admin / verification ✅ (minimal, by design)
- `RegistrationDto` gained `isVerified` (default `false`) and `isAdmin`
  (default `false`).
- Registration auto-sets `isVerified = true` for plain **User** accounts
  (they don't provide a service, nothing to vet) and leaves it `false` for
  **Mechanic** / **TowTruck** accounts.
- Mechanic search/matching (`findSuggestedMechanicFromDatabase`) and the map
  search now **only show verified providers**.
- New admin screen: `res/layout/activity_admin_verification.xml`,
  `navigateToAdminVerificationUi()` — lists every unverified
  Mechanic/TowTruck with a one-tap **Approve** button.
- **There is no self-serve way to become an admin.** `isAdmin` is never set
  from the client UI. To test the admin screen, open the Firebase console,
  find your test account under `RegisteredUsers/{yourNodeKey}`, and manually
  add `"isAdmin": true`. The button on the Profile screen only appears for
  that account.
- **Important side effect:** any Mechanic/TowTruck test accounts you already
  created in Firebase before this change won't have `isVerified` set, so
  they'll disappear from search until you either approve them via the new
  admin screen or set `isVerified: true` on them directly in the console.

## What was intentionally NOT touched (still on you)

These need real credentials, real human action, or a rewrite too risky to do
blind without a compiler in front of me — I didn't fake any of them:

- **Payment gateway** — still the same non-functional mock payment screen.
  Needs PayHere/Stripe sandbox credentials before it can charge anything real.
- **Firebase test data** — no mechanics/tow trucks exist in your DB. You (or
  test registrations through the app) still need to create some, and now also
  get them `isVerified: true` (via the new admin screen or manually) or
  they won't appear in search.
- **`google-services.json`** — still the file from the zip you gave me. Swap
  it for your real Firebase project's file before submission.
- **Gemini API key** — still hardcoded in plaintext at
  `app/src/main/java/com/version1/test1/ChatModelTrainer.kt:19`
  (`secureApiKeyToken`). I did not move it, since doing so safely requires
  your actual secrets setup (local.properties / BuildConfig / restricted key
  in Google Cloud console) — moving it blind risks breaking the chat feature
  with no way for me to verify.
- **MVVM / ViewModels** — `MainActivity.kt` is now ~800 lines longer (single
  file, same pattern as before). I did not attempt a full architecture
  rewrite; doing that safely on a 2,900+ line file with no compiler to check
  my work would risk breaking far more than it fixes.
- **Automated tests** — none added, for the same reason: I can't verify
  they'd even compile/run here.
- **Not yet compiled** — I have no Android SDK or network access in this
  sandbox, so this has never actually been built. Open it in Android Studio,
  let Gradle sync, and expect to fix at least minor issues (imports, a
  mistyped id, etc.) — I did do a manual brace/paren-balance and
  id-cross-reference check against every layout, but that's not a substitute
  for a real compile.
