# Digital Vaccine Administration System

A full-stack, dual-role Digital Health Platform engineered to connect everyday citizens with local ASHA (Accredited Social Health Activist) Workers. Built on a strict separation of concerns utilizing a Node.js Express backend alongside a native Android Java client, supercharged by Google Firebase.

## 🚀 Core Features & Architecture

### 1. Dual-Persona Architecture
- **Standard Citizen Role**: Citizens can manage their personal profiles, register their dependents ("Family Members"), view custom visual vaccination timelines, and receive automated dosage reminders.
- **ASHA Worker CRM**: A fully isolated workspace allowing health workers to onboard native citizens. Features a customized Beneficiary Database with real-time reactive search, secure record deletion, profile updates, and dynamic nested data injection.

### 2. High-Impact ASHA Analytics
- **Live Intelligence Dashboard**: Hardware-accelerated stats compute real-time field data. It cascades through isolated Firestore subcollections instantly to generate Total Citizen Counts, Total Doses Administered, and precise demographic splits (Child, Pregnant Woman, Adult) via native Java thread aggregation without relying on intensive server pings.

### 3. Vaccine Intelligence Center
- Includes an educational database mapping out all critical developmental vaccines (BCG, OPV, Pentavalent, Rotavirus, Measles) complete with injection routes, sites, and expected administration windows. 

### 4. Advanced System Modules
- **Official PDF Certificates**: A Node.js API endpoint dynamically compiles verified user vaccination records into formatted PDF Certificates equipped with verifiable QR Codes via `pdfkit`.
- **Automated Reminder Cron-Job**: A background `node-cron` service sweeps the database daily, triangulating overdue vaccines and pinging Android devices over Firebase Cloud Messaging (FCM).
- **Direct Cloud Integration**: Data integrity is strictly maintained by syncing directly to Firebase Firestore SDKs in real-time, completely overriding localized caching drops. 

## 🛡 Tech Stack & Security

### Backend Infrastructure
- **Framework**: Node.js / Express.js
- **Database (BaaS)**: Google Firestore (Firebase Admin SDK)
- **Auth & Notifications**: Firebase Auth & FCM
- **PDF Engine**: `pdfkit`
- **Scheduler**: `node-cron`

### Android Client
- **Core**: Java (Android SDK 36+)
- **Architecture**: Classic MVC paired with Async Firestore Listeners
- **UI/UX Revolution**: Upgraded traditional views utilizing **Material Design 3**. Features advanced RecyclerView LayoutAnimations (sliding items), actionable Snackbars over Toasts, Shimmer-style loading spinners, and immersive high-contrast CardViews. 

## 📂 Project Structure

```text
DigitalVaccineApp/
├── backend/                # Node.js Express Server
│   ├── config/             # Firebase Admin JSON config
│   ├── controllers/        # Express Business logic & PDF generation
│   ├── middleware/         # Security & RBAC Route protection
│   ├── routes/             # REST Endpoints
│   ├── services/           # Background sweepers (Reminders)
│   └── server.js           # API Ignition switch
└── app/                    # Native Android Application (Java)
    ├── src/main/java/.../
    │   ├── adapter/        # Dynamic UI Recyclers (Search Filters, ViewHolders)
    │   ├── models/         # Cross-platform serializable POJOs 
    │   ├── network/        # Retrofit Interceptors (For PDF generation API)
    │   └── ui/             # Core Android Activities (Dashboards, Overlays)
    └── res/layout/         # Material 3 XML definitions
```

## ⚙️ Build Instructions

### Backend Ignition
1. Navigate directly to the `backend` directory.
2. Run `npm install` to pull production dependencies.
3. Drop your authenticated `serviceAccountKey.json` from Firebase into the root folder.
4. Export your `PORT` variables in a `.env` file and fire the engine via `node server.js`.

### Android Assembler
1. Mount the `/app` project locally on Android Studio.
2. Insert your specific `google-services.json` into the `/app` directory to link the Cloud DB.
3. Hit `assembleDebug` or `Run -> App` to trigger Gradle sync and compile the active APK to your emulator or physical deployment target. 

## 📈 System Flow (How Data Scales)
The DVAS system leverages a strict **NoSQL Subcollection Strategy** to ensure absolute data isolation:
- Citizens live under `users/{citizenId}`.
- Citizen family members exist as `users/{citizenId}/family/...`.
- ASHA Workers capture their assigned targets under `users/{ashaId}/beneficiaries/{beneficiaryId}`.
- *Vaccines* themselves are then nested one final layer deep cleanly under whichever identity owns them: `.../vaccinations/{vaxId}`. 
This structure guarantees the ASHA Analytics engine can parallel-map across arrays efficiently without querying the master user ledger.

## 📄 License
Operated as an open Educational Mobile Architecture platform. 
