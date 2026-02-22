const { db, admin } = require("../config/firebase");

/**
 * Helper to structure responses
 */
const sendResponse = (res, statusCode, success, message, data = null) => {
    return res.status(statusCode).json({
        success,
        message,
        data,
    });
};

// Collection reference
const vaccinationRef = db.collection("vaccinations");

/**
 * 1. Add New Vaccination Record
 */
exports.addVaccination = async (req, res, next) => {
    try {
        const { vaccineName, doseNumber, dateTaken, nextDueDate, hospitalName, status } = req.body;
        const userId = req.user.uid;

        // Validation
        if (!vaccineName || !doseNumber || !dateTaken || !hospitalName || !status) {
            return sendResponse(res, 400, false, "All fields are required");
        }

        const newRecord = {
            userId,
            vaccineName,
            doseNumber: Number(doseNumber),
            dateTaken: dateTaken, // Client sends date string or ISO
            nextDueDate: nextDueDate || null,
            hospitalName,
            status,
            createdAt: admin.firestore.FieldValue.serverTimestamp(),
        };

        const docRef = await vaccinationRef.add(newRecord);

        return sendResponse(res, 201, true, "Vaccination record added successfully", {
            vaccinationId: docRef.id,
        });
    } catch (error) {
        next(error);
    }
};

/**
 * 2. Get All Vaccinations for Logged-in User
 */
exports.getVaccinations = async (req, res, next) => {
    try {
        const userId = req.user.uid;

        const snapshot = await vaccinationRef
            .where("userId", "==", userId)
            .orderBy("createdAt", "desc")
            .get();

        const vaccinations = [];
        snapshot.forEach((doc) => {
            vaccinations.push({ vaccinationId: doc.id, ...doc.data() });
        });

        return sendResponse(res, 200, true, "Vaccinations retrieved successfully", vaccinations);
    } catch (error) {
        next(error);
    }
};

/**
 * 3. Update Vaccination Record
 */
exports.updateVaccination = async (req, res, next) => {
    try {
        const { id } = req.params;
        const userId = req.user.uid;
        const updates = req.body;

        const doc = await vaccinationRef.doc(id).get();

        if (!doc.exists) {
            return sendResponse(res, 404, false, "Vaccination record not found");
        }

        // Check ownership
        if (doc.data().userId !== userId) {
            return sendResponse(res, 401, false, "Unauthorized: You can only update your own records");
        }

        await vaccinationRef.doc(id).update({
            ...updates,
            updatedAt: admin.firestore.FieldValue.serverTimestamp(),
        });

        return sendResponse(res, 200, true, "Vaccination record updated successfully");
    } catch (error) {
        next(error);
    }
};

/**
 * 4. Delete Vaccination Record
 */
exports.deleteVaccination = async (req, res, next) => {
    try {
        const { id } = req.params;
        const userId = req.user.uid;

        const doc = await vaccinationRef.doc(id).get();

        if (!doc.exists) {
            return sendResponse(res, 404, false, "Vaccination record not found");
        }

        // Check ownership
        if (doc.data().userId !== userId) {
            return sendResponse(res, 401, false, "Unauthorized: You can only delete your own records");
        }

        await vaccinationRef.doc(id).delete();

        return sendResponse(res, 200, true, "Vaccination record deleted successfully");
    } catch (error) {
        next(error);
    }
};
