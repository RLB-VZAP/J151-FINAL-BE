package com.vzap.trytons.service.message;

import com.vzap.trytons.dto.message.MessageReportResponseDTO;
import com.vzap.trytons.enums.MessageScope;

import java.util.UUID;

public interface MessageReportService {

    MessageReportResponseDTO report(UUID actorUserId, MessageScope scope, UUID messageId, String reason);
}
