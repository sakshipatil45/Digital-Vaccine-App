package com.example.digitalvaccineapp.core;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Utility to manage mock user identity for development without Firebase connectivity.
 */
public class MockUserManager {
    // Set to true to bypass Firebase and use static Guest ID
    public static final boolean USE_MOCK = true;
    
    public static final String GUEST_USER_ID = "guest_user_123";
    public static final String GUEST_NAME = "Guest Citizen";
    public static final String GUEST_ROLE = "citizen"; // "citizen" or "asha"

    /**
     * Returns the active User ID, preferring the Mock ID if enabled.
     */
    public static String getUserId() {
        if (USE_MOCK) {
            return GUEST_USER_ID;
        }
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    /**
     * Returns true if there is a valid session (either Mock or Firebase).
     */
    public static boolean isLoggedIn() {
        if (USE_MOCK) return true;
        return FirebaseAuth.getInstance().getCurrentUser() != null;
    }

    /**
     * Returns the active role.
     */
    public static String getUserRole(android.content.SharedPreferences prefs) {
        if (USE_MOCK) return GUEST_ROLE;
        return prefs.getString("userRole", "citizen");
    }
}
