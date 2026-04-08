<div align="center">
  
# 💉 Digital Vaccine Administration System 

**A high-performance, full-stack Android application built to bridge the gap between rural citizens and Accredited Social Health Activists (ASHA).**

[![Android](https://img.shields.io/badge/Platform-Android-3DDC84?style=flat&logo=android)](#)
[![Java](https://img.shields.io/badge/Language-Java-007396?style=flat&logo=java)](#)
[![Node.js](https://img.shields.io/badge/Backend-Node.js-339933?style=flat&logo=node.js)](#)
[![Firebase](https://img.shields.io/badge/Database-Firestore-FFCA28?style=flat&logo=firebase)](#)

</div>

<br/>

## 📖 Overview

The Digital Vaccine Administration System (DVAS) is a dual-persona mobile architecture engineered to digitize healthcare tracking. Built on strong **MVC separation**, it empowers standard users to track their family's health trajectories, while simultaneously offering a completely isolated, enterprise-scale CRM (Customer Relationship Management) system for authorized Health Workers to aggregate field analytics.

---

## 🌟 Key Features

### 👤 Citizen Capabilities
*   **Family Health Tracking:** Seamlessly add "Dependents" (children, parents) to a master account.
*   **Intuitive Dashboards:** View dynamic progress indicators and visual vaccination timelines.
*   **Automated Reminders:** (In Development) A background system designed to track overdue doses and broadcast notifications.
*   **Intuitive Dashboards:** View dynamic progress indicators and visual vaccination timelines.

### 🏥 ASHA Worker CRM (Enterprise Layer)
*   **Isolated Workspace Framework:** Health workers operate on an entirely separate UI layout and data ledger.
*   **Advanced Beneficiary Management:** 
    *   *Real-Time Reactive Search* directly filters patient lists in active memory.
    *   *Dual-Mode Edit Forms* allow pre-filling and overwriting existing patient demographics securely.
    *   *Cloud-Native Deletion* aggressively clears data directly from the isolated Firestore ledger.
*   **Live Analytics Engine:** An autonomous Android thread aggregates data across nested NoSQL collections to generate Total Citizen Counts, Demographics, and Dosage Sums, rendered beautifully through **Material Design 3** stat cards and shimmer animations.

---

## 🏗️ Architecture & Core Tech Stack

### 🚀 Backend Infrastructure
*   **Framework:** Node.js + Express.js
*   **Database Integration:** Serverless Google Firestore via the Admin SDK
*   **Auth / Messaging:** Firebase Authentication & Firebase Cloud Messaging (FCM)
*   **Task Scheduling:** `node-cron` (Planned)

### 📱 Android Client Structure
*   **SDK Base:** Android SDK 36+ (Java natively)
*   **Network Layer:** Retrofit2 + OkHttp (Custom Token Interceptors)
*   **UI/UX Paradigm:** Material Design 3. Involves dynamic Recycler List sliding animations, Snackbar interactions, CardView shadowing, and zero-state UI validation.
*   **Domain Isolation:** Strict Package Refactoring routes processing by scope:
```text
app/src/main/java/com/example/digitalvaccineapp/
├── auth/           # Login, Registration, Splash verification
├── asha/           # The exclusive Health Worker tracker module
├── citizen/        # Standard Dashboard, Reminders, Dependents
├── shared/         # Cross-entity logic (Profile, Global Models)
└── core/           # System networks (Retrofit, Firebase Services)
```

---

## ⚙️ Installation & Setup

### 1. Boot the Backend Node Server
1. Navigate to the `/backend` directory.
2. Run `npm install` to hydrate all package modules.
3. Import your `serviceAccountKey.json` directly from your Firebase Console.
4. Forge a `.env` file declaring your active port (`PORT=5000`).
5. Execute `node server.js`.

### 2. Compile the Android Client
1. Mount the `/app` project locally using Android Studio.
2. Drop your configuration `google-services.json` securely into the `app/` folder.
3. Allow the Gradle Daemon to resolve all dependencies.
4. Execute `Run -> App` (or `assembleDebug`) to boot to your Emulator or external Android Target.

---

## 🔒 Security Principles
*   **Real-time Firestore Subcollections:** ASHA Workers store assigned patients under an isolated map: `users/{ashaId}/beneficiaries/{beneficiaryId}`. Citizens manage dependents equivalently: `users/{citizenId}/family/...`. This eliminates global data extraction.
*   **RBAC (Role-Based Access Control):** The backend node limits execution pathways natively depending on the security clearance stored universally on the Firebase Token interceptor. 
*   **Strong Field Sanitizations:** Enforced dual-layer (XML + Java) 10-digit mobile verification, preventing null pointer crashes and dirty input values.

<br/>

> **Developed as a comprehensive Mobile Healthcare Application architecture study.**
