require("dotenv").config();
const express = require("express");
const cors = require("cors");
const { db } = require("./config/firebase");

const vaccinationRoutes = require("./routes/vaccinationRoutes");
const adminRoutes = require("./routes/adminRoutes");
const certificateRoutes = require("./routes/certificateRoutes");
const errorMiddleware = require("./middleware/errorMiddleware");
const cron = require("node-cron");
const { checkAndSendReminders } = require("./services/reminderService");

const app = express();
const PORT = process.env.PORT || 5000;

// Middleware
app.use(cors());
app.use(express.json());

// Routes
app.get("/", (req, res) => {
    res.send("Digital Vaccination Management System API is running...");
});

// Register Module Routes
app.use("/api/vaccinations", vaccinationRoutes);
app.use("/api/vaccinations", certificateRoutes);
app.use("/api/admin", adminRoutes);

// Test Firestore route
app.get("/test-firestore", async (req, res) => {
    try {
        const testDoc = await db.collection("test").add({
            message: "Firestore connection is working!",
            timestamp: new Date().toISOString()
        });
        res.json({ success: true, docId: testDoc.id });
    } catch (error) {
        console.error("Firestore Error:", error);
        res.status(500).json({ success: false, error: error.message });
    }
});

// Error Handling Middleware (must be last)
app.use(errorMiddleware);

app.listen(PORT, () => {
    console.log(`Server is running on port ${PORT}`);

    // Schedule Automated Vaccination Reminders (runs daily at 9:00 AM)
    cron.schedule("0 9 * * *", () => {
        checkAndSendReminders();
    }, {
        scheduled: true,
        timezone: "Asia/Kolkata" // Adjust timezone as per requirements
    });
    console.log("Automated Vaccination Reminder Cron Job Initialized.");
});
