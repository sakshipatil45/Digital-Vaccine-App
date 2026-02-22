const { admin } = require("../config/firebase");

/**
 * Sends a push notification to a specific FCM token
 * @param {string} fcmToken - Target device token
 * @param {string} title - Notification title
 * @param {string} body - Notification body
 * @param {object} data - Optional data payload
 */
const sendNotification = async (fcmToken, title, body, data = {}) => {
    try {
        const message = {
            notification: {
                title,
                body,
            },
            data: data,
            token: fcmToken,
        };

        const response = await admin.messaging().send(message);
        console.log("Successfully sent notification:", response);
        return response;
    } catch (error) {
        console.error("Error sending notification:", error);
        throw error;
    }
};

module.exports = sendNotification;
