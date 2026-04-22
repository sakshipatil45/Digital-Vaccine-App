# Project Description: Digital Vaccine App (VaxSync)

A premium, real-time vaccination management system designed to bridge the gap between health authorities and citizens, ensuring timely immunization and seamless health record tracking.

## 1. Project Objectives
*   **Centralized Health Registry**: Maintain a global, real-time database of beneficiaries and their vaccination statuses.
*   **Role-Based Synchronization**: Enable health admins to schedule vaccinations and have them instantly reflected on citizen dashboards.
*   **Improved Compliance**: Automate reminder generation to reduce missed doses.
*   **Premium UX**: Provide a modern, professional, and accessible interface for all users.

## 2. System Architecture & Tech Stack
*   **Platform**: Android (Native Java)
*   **Design System**: Material Design 3 (White, Blue, and Purple Premium Theme)
*   **Backend**: Firebase Firestore (Real-time NoSQL Database)
*   **Authentication**: Firebase Auth (Secure Mobile Login)
*   **Data Flow**: Reactive Repository Pattern using Firestore SnapshotListeners for live updates.

## 3. Modules & Implementation

| Module | Features | Implementation Details |
| :--- | :--- | :--- |
| **Auth & Security** | Phone-based Login, Role Detection (Admin/Citizen), Profile Setup. | Firebase Auth + Firestore User Documents. |
| **Citizen Dashboard** | Real-time status cards (Completed/Pending), Profile Switcher, Quick Actions. | SnapshotListeners + Material Card Layouts. |
| **Admin Dashboard** | System-wide stats (Total Users, Vaccinated), Management shortcuts. | Aggregate Queries + Real-time Data Binding. |
| **Beneficiary Management** | Add/Edit Family Members, Category-based classification (Child/Adult). | Sub-collections in Firestore linked by Phone. |
| **Vaccination Records** | Digital history of doses, Hospital details, Date taken. | RecyclerView + Custom Adapters + Master Vaccine List. |
| **Scheduling & Reminders** | Admin-side "Schedule Vax" (Campaign/Individual), Citizen-side view-only reminders. | Trigger-based Document Creation + Live Reminders View. |

## 4. Roles & Feature Distribution

### 🔑 Admin (Health Authority)
*   **Schedule Management**: Set vaccination dates for specific individuals or entire age categories.
*   **User Registry**: Add new citizens and beneficiaries to the system.
*   **Vaccine Management**: Add new vaccines to the global database.
*   **System Analytics**: Monitor overall vaccination progress in the community.

### 👤 Citizen (Beneficiary)
*   **Family Health Tracking**: Manage health records for self and dependents.
*   **Record Access**: View full vaccination history with hospital details.
*   **Task Management**: View upcoming "Pending" vaccinations scheduled by the Admin.
*   **Self-Service**: Update profile and manage family member details.

## 5. Implementation Status

### ✅ Completed (V1.0)
*   Core UI Overhaul (White, Blue, Purple Theme).
*   Real-time data synchronization across all dashboards.
*   Separated Admin (Scheduling) and Citizen (Viewing) workflows.
*   Family member switcher logic.
*   Basic authentication and profile management.

### ⏳ Remaining / Future Scope
*   **PDF Generation**: Exportable vaccination certificates.
*   **Push Notifications**: System-level alerts via Firebase Cloud Messaging (FCM).
*   **Offline Mode**: Local persistence for rural areas with low connectivity.
*   **Multi-language Support**: Vernacular language integration.
