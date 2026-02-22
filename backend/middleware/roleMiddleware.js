const { db } = require("../config/firebase");

/**
 * Middleware to restrict access to admin users only.
 * Must be used AFTER authMiddleware (to have req.user populated).
 */
const adminMiddleware = async (req, res, next) => {
    try {
        const userId = req.user.uid;

        // Fetch user document from Firestore
        const userDoc = await db.collection("users").doc(userId).get();

        if (!userDoc.exists) {
            return res.status(403).json({
                success: false,
                message: "Forbidden: User record not found",
                data: null
            });
        }

        const userData = userDoc.data();

        if (userData.role !== "admin") {
            return res.status(403).json({
                success: false,
                message: "Forbidden: Admin access required",
                data: null
            });
        }

        // Attach role to req.user for further use if needed
        req.user.role = userData.role;
        next();
    } catch (error) {
        console.error("Role Middleware Error:", error);
        res.status(500).json({
            success: false,
            message: "Internal Server Error in authorization check",
            data: null
        });
    }
};

module.exports = adminMiddleware;
