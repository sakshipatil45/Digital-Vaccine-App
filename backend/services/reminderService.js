const { db } = require("../config/firebase");
const sendNotification = require("../utils/sendNotification");

/**
 * Checks for pending vaccinations due in 1-3 days and sends FCM reminders.
 */
const checkAndSendReminders = async () => {
    console.log("Running Vaccination Reminder Job...");

    try {
        const today = new Date();
        const threeDaysFromNow = new Date();
        threeDaysFromNow.setDate(today.getDate() + 3);

        // Query for pending vaccinations due within the next 3 days
        // Note: nextDueDate should be stored as a Firestore Timestamp or ISO string.
        // Assuming Timestamp for better querying.
        const snapshot = await db.collection("vaccinations")
            .where("status", "==", "pending")
            .where("nextDueDate", ">=", today)
            .where("nextDueDate", "<=", threeDaysFromNow)
            .get();

        if (snapshot.empty) {
            console.log("No pending vaccinations due soon.");
            return;
        }

        console.log(`Found ${snapshot.size} vaccinations due soon. Processing...`);

        const usersToNotify = new Map(); // Using a Map to avoid duplicate notifications if a user has multiple vaccinations due

        for (const doc of snapshot.docs) {
            const data = doc.data();
            const { userId, vaccineName, nextDueDate } = data;

            // Ensure we haven't already processed this user for the same vaccine in this run
            const notificationKey = `${userId}_${vaccineName}`;
            if (usersToNotify.has(notificationKey)) continue;

            // Fetch user's FCM token from 'users' collection
            const userDoc = await db.collection("users").doc(userId).get();

            if (!userDoc.exists) {
                console.warn(`User document not found for userId: ${userId}`);
                continue;
            }

            const { fcmToken } = userDoc.data();

            if (!fcmToken) {
                console.warn(`FCM Token not found for user: ${userId}`);
                continue;
            }

            // Format date for notification
            const dueDateString = nextDueDate.toDate ? nextDueDate.toDate().toDateString() : new Date(nextDueDate).toDateString();

            try {
                await sendNotification(
                    fcmToken,
                    "Vaccination Reminder",
                    `Your ${vaccineName} dose is due on ${dueDateString}`,
                    { vaccinationId: doc.id, type: "REMAINING_VACCINATION" }
                );
                console.log(`Reminder sent to user ${userId} for ${vaccineName}`);
                usersToNotify.set(notificationKey, true);
            } catch (err) {
                console.error(`Failed to send reminder to user ${userId}:`, err.message);
            }
        }

        console.log("Reminder Job Completed.");
    } catch (error) {
        console.error("Error in checkAndSendReminders service:", error);
    }
};

module.exports = { checkAndSendReminders };
