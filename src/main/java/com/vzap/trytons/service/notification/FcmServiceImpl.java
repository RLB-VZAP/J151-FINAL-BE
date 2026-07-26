package com.vzap.trytons.service.notification;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.SendResponse;
import com.vzap.trytons.config.FirebaseConfig;
import com.vzap.trytons.dao.device.DeviceTokenDAO;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FcmServiceImpl implements FcmService {

    private static final Logger LOG = Logger.getLogger(FcmServiceImpl.class.getName());

    @Inject
    private FirebaseConfig firebaseConfig;

    @Inject
    private DeviceTokenDAO deviceTokenDAO;

    @Override
    public void send(Collection<String> tokens, String title, String body, Map<String, String> data) {
        if (firebaseConfig == null || !firebaseConfig.isEnabled()) {
            LOG.log(Level.FINE, "FCM disabled; skipping push to {0} token(s).",
                    tokens == null ? 0 : tokens.size());
            return;
        }
        if (tokens == null || tokens.isEmpty()) {
            return;
        }

        List<String> tokenList = new ArrayList<>(tokens);
        try {
            MulticastMessage.Builder builder = MulticastMessage.builder()
                    .addAllTokens(tokenList)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build());
            if (data != null && !data.isEmpty()) {
                builder.putAllData(data);
            }

            FirebaseApp app = FirebaseApp.getInstance(FirebaseConfig.APP_NAME);
            BatchResponse response = FirebaseMessaging.getInstance(app).sendEachForMulticast(builder.build());

            deactivateFailedTokens(tokenList, response);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Unable to send FCM push notification", e);
        }
    }

    private void deactivateFailedTokens(List<String> tokenList, BatchResponse response) {
        List<SendResponse> responses = response.getResponses();
        for (int i = 0; i < responses.size(); i++) {
            SendResponse sendResponse = responses.get(i);
            if (sendResponse.isSuccessful() || sendResponse.getException() == null) {
                continue;
            }
            MessagingErrorCode code = sendResponse.getException().getMessagingErrorCode();
            if (code == MessagingErrorCode.UNREGISTERED || code == MessagingErrorCode.INVALID_ARGUMENT) {
                deviceTokenDAO.deactivate(tokenList.get(i));
            }
        }
    }
}
