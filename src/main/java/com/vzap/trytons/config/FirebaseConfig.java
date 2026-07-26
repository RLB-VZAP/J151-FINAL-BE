package com.vzap.trytons.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Initializes the Firebase Admin SDK once at application startup.
 * <p>
 * FCM is entirely optional: when {@code FIREBASE_ENABLED} is not {@code true},
 * or the credentials file is missing, the SDK is never initialized and
 * {@link #isEnabled()} returns {@code false}. Messaging then degrades to a
 * no-op (see {@code FcmServiceImpl}) without affecting the rest of the app.
 * <p>
 * To turn it on, set in the {@code .env} file:
 * <pre>
 *   FIREBASE_ENABLED=true
 *   FIREBASE_CREDENTIALS_PATH=/absolute/path/to/service-account.json
 * </pre>
 */
@Singleton
@Startup
public class FirebaseConfig {

    public static final String APP_NAME = "trytons-fcm";

    private static final Logger LOG = Logger.getLogger(FirebaseConfig.class.getName());

    private boolean enabled;

    @PostConstruct
    void init() {
        boolean requested = DotEnvConfig.getOptionalBoolean("FIREBASE_ENABLED", false);
        if (!requested) {
            LOG.info("FIREBASE_ENABLED is not true; FCM push is disabled (messages still work).");
            enabled = false;
            return;
        }

        String credentialsPath = DotEnvConfig.getOptional("FIREBASE_CREDENTIALS_PATH", null);
        if (credentialsPath == null || credentialsPath.isBlank()) {
            LOG.warning("FIREBASE_ENABLED is true but FIREBASE_CREDENTIALS_PATH is not set; FCM push stays disabled.");
            enabled = false;
            return;
        }

        try (InputStream serviceAccount = new FileInputStream(credentialsPath)) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            if (FirebaseApp.getApps().stream().noneMatch(app -> APP_NAME.equals(app.getName()))) {
                FirebaseApp.initializeApp(options, APP_NAME);
            }
            enabled = true;
            LOG.info("Firebase Admin SDK initialized; FCM push is enabled.");
        } catch (Exception e) {
            enabled = false;
            LOG.log(Level.SEVERE, "Failed to initialize Firebase; FCM push stays disabled.", e);
        }
    }

    public boolean isEnabled() {
        return enabled;
    }
}
