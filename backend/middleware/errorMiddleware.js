/**
 * Centralized error handling middleware
 */
const errorMiddleware = (err, req, res, next) => {
    console.error("Global Error Handler:", err);

    const statusCode = err.status || 500;
    const message = err.message || "Internal Server Error";

    res.status(statusCode).json({
        success: false,
        message,
        data: null,
        stack: process.env.NODE_ENV === "development" ? err.stack : undefined,
    });
};

module.exports = errorMiddleware;
