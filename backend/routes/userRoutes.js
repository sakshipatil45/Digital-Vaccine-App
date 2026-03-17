const express = require("express");
const router = express.Router();
const userController = require("../controllers/userController");
const verifyToken = require("../middleware/authMiddleware");

// All user routes are protected
router.use(verifyToken);

router.get("/profile", userController.getProfile);
router.put("/profile", userController.updateProfile);

module.exports = router;
