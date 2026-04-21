<div align="center">
  
# 💉 Digital Vaccine Administration & Tracking System 

**A premium, enterprise-grade healthcare ecosystem designed to automate immunization management for Admins and Citizens.**

[![Android](https://img.shields.io/badge/Platform-Android-3DDC84?style=flat&logo=android)](#)
[![Java](https://img.shields.io/badge/Language-Java-007396?style=flat&logo=java)](#)
[![Node.js](https://img.shields.io/badge/Backend-Node.js-339933?style=flat&logo=node.js)](#)
[![Firebase](https://img.shields.io/badge/Database-Firestore-FFCA28?style=flat&logo=firebase)](#)
[![Material Design 3](https://img.shields.io/badge/UI-Material--3-blue?style=flat)](#)

</div>

<br/>

## 📖 Overview

The **Digital Vaccine Administration System (DVAS)** is a sophisticated dual-persona platform that digitizes the entire immunization lifecycle. It bridges the gap between healthcare administrators and the general public, providing a unified registry, automated scheduling, and real-time analytics. 

Developed with a focus on **scalability, data integrity, and high-performance mobile UX**, DVAS offers a robust Admin command center and a user-centric citizen application.

---

## 👥 Role-Based Feature Suite

### 🛠️ 1. Admin Features (Global System Controller)
The Admin persona is the "Command Center" of the system, responsible for population management and vaccine distribution logistics.

*   **💉 Master Vaccine Inventory (CRUD):**
    *   Full control over the system's vaccine database.
    *   Ability to add new vaccines with specific age-group targets (Infant, Child, Teen, Adult).
    *   Set recommended dosage timing (in months) and dose information (e.g., 0.5ml).
    *   Update or retire existing vaccine details in real-time.
*   **👤 Global User Management:**
    *   Centralized registry of all registered citizens.
    *   Advanced search by Name or Unique ID.
    *   Ability to drill down into family structures and view all linked dependents.
*   **🚨 Vaccination Record Management:**
    *   Update vaccination status (Pending/Completed) for any citizen.
    *   Record precise administration dates and hospital locations.
    *   Maintain an immutable history of all immunizations.
*   **📢 System-Wide Announcements (Broadcast):**
    *   A powerful broadcast engine to send real-time alerts to all app users.
    *   Used for new vaccine launches, health drives, or emergency alerts.
    *   History of all past announcements for transparency.
*   **📈 Real-Time Reports & Analytics:**
    *   Live tracking of total population coverage.
    *   Dynamic aggregation of "Due" vs "Completed" vaccinations across the entire system.
    *   Visual progress indicators for health worker efficiency.

### 👤 2. User Features (Citizen/Family Health Tracker)
The Citizen persona is designed for simplicity and action, empowering families to take control of their immunization schedule.

*   **👨‍👩‍👧 Family Registry (Profile Switching):**
    *   Add and manage multiple family members (Self, Children, Spouse) under a single phone number based identity.
    *   **Profile Switcher:** An intuitive dropdown to toggle the entire dashboard view between different family members.
*   **📅 Intelligent Vaccination Schedule:**
    *   Age-wise recommended schedule derived directly from the Admin's master database.
    *   Grouped by development phases (Infant to Adult) for easy navigation.
    *   Detailed view of "Why it's important" and "What to expect" for every dose.
*   **✅ Records & Progress Tracking:**
    *   View verified vaccination certificates and records.
    *   Dynamic "Progress Ring" showing completion percentage for each family member.
    *   Visual timelines of past and upcoming doses.
*   **⏰ Smart Reminders & Notifications:**
    *   Automated alerts for upcoming doses.
    *   Direct receipt of Admin announcements (Broadcasts) on the dashboard.
*   **📑 Vaccine Detail UI:**
    *   Enhanced information pages for every vaccine, listing benefits, common side effects, and administration timing.

---

## 🏗️ Technical Architecture

### 🚀 Backend Infrastructure
*   **Engine:** Node.js + Express.js (v18+)
*   **Database:** Serverless Google Cloud Firestore (NoSQL Architecture)
*   **Auth:** Firebase Authentication with Role-Based Scoping.
*   **Push Notifications:** Firebase Cloud Messaging (FCM) for instant broadcast delivery.

### 📱 Android Client Professional Architecture
*   **Paradigm:** MVC (Model-View-Controller) with Repository Pattern for data abstraction.
*   **Network:** Retrofit 2 + OkHttp 4 with custom Token Interceptors for secure API communication.
*   **UI/UX:** Full implementation of **Material Design 3 (Material You)** principles:
    *   *CoordinatorLayout* for sophisticated scrolling behavior.
    *   *MaterialCardView* for elevated, structured data presentation.
    *   *LinearProgressIndicator* for real-time visual feedback.
*   **Domain Isolation:** Strict code separation via package scoping:
```text
com.example.digitalvaccineapp/
├── admin/          # Admin-only activities, adapters, and models
├── citizen/        # Citizen dashboard, schedule, and family management
├── auth/           # Secure login/registration flows
├── shared/         # Cross-role components (Profile, Settings)
└── core/           # Networking, Firebase services, and utilities
```

---

## ⚙️ Installation & Deployment

### 1. Backend Setup
1. Clone the repository and navigate to `/backend`.
2. Run `npm install`.
3. Place your `serviceAccountKey.json` from Firebase Console into the root.
4. Configure `.env` (e.g., `PORT=5000`).
5. Start the engine: `node server.js`.

### 2. Android App Deployment
1. Open the `/app` project in Android Studio.
2. Add your `google-services.json` to the `app/` directory.
3. Sync Gradle and build the signed APK or run directly on a Physical/Virtual target.

---

## 🔒 Security & Data Integrity
*   **RBAC Logic:** The system strictly enforces roles. Citizens cannot access Admin endpoints, and Admins operate on a global registry while maintaining individual privacy.
*   **Reactive Data Flow:** Built using Firestore Snapshots, ensuring that when an Admin updates a vaccine Detail, it is instantly reflected on the Citizen's device without a refresh.
*   **Validation Engine:** Multi-layer sanitization of phone numbers, age fields, and IDs to prevent data corruption.

<br/>

> **This project represents a state-of-the-art implementation of Digital Health Management.**

---
<div align="center">
  Developed by the Digital Vaccine App Team
</div>
