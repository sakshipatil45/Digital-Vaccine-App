# Digital Vaccination Management System

A full-stack solution for managing vaccination records, featuring a Node.js Express backend and an Android Java client, integrated with Firebase.

## 🚀 Features

- **Secure Authentication**: Firebase ID Token verification for all API requests.
- **Vaccination CRUD**: Complete management of vaccination records (Add, View, Update, Delete).
- **Automated Reminders**: Daily cron job that sends push notifications (FCM) for upcoming doses.
- **RBAC (Role-Based Access Control)**: Specialized admin dashboard with platform-wide statistics.
- **Modern Android Client**: Retrofit-based networking with automated token injection.

## 🛠 Tech Stack

### Backend
- **Framework**: Node.js / Express.js
- **Database**: Firestore (Firebase Admin SDK)
- **Auth & Notifications**: Firebase Auth & Firebase Cloud Messaging (FCM)
- **Scheduler**: node-cron

### Android App
- **Language**: Java
- **Networking**: Retrofit 2 + OkHttp
- **JSON Parsing**: GSON
- **UI**: RecyclerView + CardView Material Design

## 📂 Project Structure

```
DigitalVaccineApp/
├── backend/                # Node.js Express API
│   ├── config/             # Firebase Admin config
│   ├── controllers/        # Business logic
│   ├── middleware/         # Auth & Role checks
│   ├── routes/             # API Endpoints
│   ├── services/           # Automated services (Reminders)
│   ├── utils/              # Helper functions
│   └── server.js           # Entry point
└── app/                    # Android Application
    ├── src/main/java/.../
    │   ├── adapter/        # RecyclerView Adapters
    │   ├── models/         # POJOs
    │   ├── network/        # Retrofit & Interceptors
    │   └── ui/             # Activities
    └── build.gradle        # Dependencies
```

## ⚙️ Setup & Installation

### Backend Setup
1. Navigate to the `backend` folder.
2. Install dependencies: `npm install`.
3. Add your `serviceAccountKey.json` from Firebase Console to the `backend` root.
4. Create a `.env` file and set your `PORT` (default 5000).
5. Start the server: `node server.js`.

### Android Setup
1. Open the project in Android Studio.
2. Add your `google-services.json` to the `app/` directory.
3. Sync Gradle dependencies.
4. If using an emulator, the `RetrofitClient` is pre-configured to `http://10.0.2.2:5000`.

## 🔒 Security
- **User Privacy**: Users can only access and modify their own records using their unique Firebase `uid`.
- **Admin Access**: Specific endpoints under `/api/admin` require a `role: "admin"` field in the user's Firestore document.

## 📈 API Endpoints

### Vaccinations
- `POST /api/vaccinations/add-vaccination`
- `GET /api/vaccinations/get-vaccinations`
- `PUT /api/vaccinations/update-vaccination/:id`
- `DELETE /api/vaccinations/delete-vaccination/:id`

### Admin
- `GET /api/admin/total-users`
- `GET /api/admin/total-vaccinations`
- `GET /api/admin/pending-vaccinations`

## 📄 License
This project is for educational purposes as part of a Mobile Application Development program.
