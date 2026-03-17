const { db } = require("../config/firebase");

/**
 * Get current user profile from 'users' collection
 */
exports.getProfile = async (req, res, next) => {
    try {
        const userId = req.user.uid;
        const userDoc = await db.collection("users").doc(userId).get();

        if (!userDoc.exists) {
            // If doc doesn't exist, return default or empty
            return res.status(200).json({
                success: true,
                message: "Profile not found, returning defaults",
                data: { uid: userId, email: req.user.email, role: "user" }
            });
        }

        return res.status(200).json({
            success: true,
            data: userDoc.data()
        });
    } catch (error) {
        next(error);
    }
};

/**
 * Update current user profile
 */
exports.updateProfile = async (req, res, next) => {
    try {
        const userId = req.user.uid;
        const { name, phone, age, address } = req.body;

        const profileData = {
            uid: userId,
            email: req.user.email,
            name: name || "",
            phone: phone || "",
            age: age || "",
            address: address || "",
            updatedAt: new Date().toISOString()
        };

        await db.collection("users").doc(userId).set(profileData, { merge: true });

        return res.status(200).json({
            success: true,
            message: "Profile updated successfully",
            data: profileData
        });
    } catch (error) {
        next(error);
    }
};
