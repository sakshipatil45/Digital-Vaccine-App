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
        const { vaccineName, doseNumber, dateTaken, nextDueDate, hospitalName, status, dependentName } = req.body;
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
            dependentName: dependentName || "Self",
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
        const { dependentName } = req.query;

        let query = vaccinationRef.where("userId", "==", userId);

        if (dependentName) {
            query = query.where("dependentName", "==", dependentName);
        }

        const snapshot = await query.orderBy("createdAt", "desc").get();

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

/**
 * 5. Get Certificate Summary for QR Code
 */
exports.getCertificateSummary = async (req, res, next) => {
    try {
        const userId = req.user.uid;
        
        // Fetch User Info
        const userDoc = await db.collection("users").doc(userId).get();
        const userName = userDoc.exists ? userDoc.data().name : "User";

        // Fetch Vaccinations
        const snapshot = await vaccinationRef.where("userId", "==", userId).get();
        
        let latestVaccine = "N/A";
        let latestId = null;
        let maxDose = 0;
        let records = [];

        snapshot.forEach((doc) => {
            const data = doc.data();
            records.push(data);
            if (data.doseNumber > maxDose) {
                maxDose = data.doseNumber;
                latestVaccine = data.vaccineName;
                latestId = doc.id;
            }
        });

        const status = maxDose >= 2 ? "Fully Vaccinated" : (maxDose === 1 ? "Partially Vaccinated" : "Not Vaccinated");

        return sendResponse(res, 200, true, "Certificate data retrieved", {
            name: userName,
            vaccine: latestVaccine,
            dose: maxDose,
            status: status,
            vaccinationId: latestId,
            verifiedOn: new Date().toLocaleDateString()
        });
    } catch (error) {
        next(error);
    }
};
