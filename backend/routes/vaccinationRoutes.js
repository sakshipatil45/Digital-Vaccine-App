const express = require("express");
const router = express.Router();
const vaccinationController = require("../controllers/vaccinationController");
const authMiddleware = require("../middleware/authMiddleware");

// All routes are protected by authMiddleware
router.use(authMiddleware);

// 1. POST /api/vaccinations/add-vaccination
router.post("/add-vaccination", vaccinationController.addVaccination);

// 2. GET /api/vaccinations/get-vaccinations
router.get("/get-vaccinations", vaccinationController.getVaccinations);

// 3. PUT /api/vaccinations/update-vaccination/:id
router.put("/update-vaccination/:id", vaccinationController.updateVaccination);

// 4. DELETE /api/vaccinations/delete-vaccination/:id
router.delete("/delete-vaccination/:id", vaccinationController.deleteVaccination);

module.exports = router;
