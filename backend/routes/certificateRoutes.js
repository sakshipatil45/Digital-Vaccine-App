const express = require("express");
const router = express.Router();
const certificateController = require("../controllers/certificateController");
const authMiddleware = require("../middleware/authMiddleware");

// Protected route
router.get("/download-certificate/:vaccinationId", authMiddleware, certificateController.downloadCertificate);

module.exports = router;
