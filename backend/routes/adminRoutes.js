const express = require("express");
const router = express.Router();
const adminController = require("../controllers/adminController");
const authMiddleware = require("../middleware/authMiddleware");
const adminMiddleware = require("../middleware/roleMiddleware");

// PROTECT ALL ADMIN ROUTES: First check token, then check admin role
router.use(authMiddleware);
router.use(adminMiddleware);

// Admin Dashboard Routes
router.get("/total-users", adminController.getTotalUsers);
router.get("/total-vaccinations", adminController.getTotalVaccinations);
router.get("/pending-vaccinations", adminController.getPendingVaccinations);
router.get("/upcoming-due-vaccinations", adminController.getUpcomingDueVaccinations);

module.exports = router;
