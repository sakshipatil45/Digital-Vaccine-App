const { db } = require("../config/firebase");

/**
 * 1. GET /admin/total-users
 */
exports.getTotalUsers = async (req, res, next) => {
    try {
        const snapshot = await db.collection("users").get();
        res.status(200).json({
            success: true,
            message: "Total users retrieved successfully",
            data: { count: snapshot.size }
        });
    } catch (error) {
        next(error);
    }
};

/**
 * 2. GET /admin/total-vaccinations
 */
exports.getTotalVaccinations = async (req, res, next) => {
    try {
        const snapshot = await db.collection("vaccinations").get();
        res.status(200).json({
            success: true,
            message: "Total vaccinations retrieved successfully",
            data: { count: snapshot.size }
        });
    } catch (error) {
        next(error);
    }
};

/**
 * 3. GET /admin/pending-vaccinations
 */
exports.getPendingVaccinations = async (req, res, next) => {
    try {
        const snapshot = await db.collection("vaccinations")
            .where("status", "==", "pending")
            .get();
        res.status(200).json({
            success: true,
            message: "Total pending vaccinations retrieved successfully",
            data: { count: snapshot.size }
        });
    } catch (error) {
        next(error);
    }
};

/**
 * 4. GET /admin/upcoming-due-vaccinations
 */
exports.getUpcomingDueVaccinations = async (req, res, next) => {
    try {
        const today = new Date();
        const nextSevenDays = new Date();
        nextSevenDays.setDate(today.getDate() + 7);

        const snapshot = await db.collection("vaccinations")
            .where("status", "==", "pending")
            .where("nextDueDate", ">=", today)
            .where("nextDueDate", "<=", nextSevenDays)
            .get();

        res.status(200).json({
            success: true,
            message: "Upcoming due vaccinations retrieved successfully",
            data: { count: snapshot.size }
        });
    } catch (error) {
        next(error);
    }
};
