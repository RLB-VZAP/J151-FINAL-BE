package com.vzap.trytons.dao.message;

import com.vzap.trytons.enums.MessageScope;
import com.vzap.trytons.model.message.MessageReport;

import java.util.UUID;

public interface MessageReportDAO {

    MessageReport create(MessageReport report);

    boolean existsReportForMessage(MessageScope scope, UUID messageId);

    boolean existsReportForLeague(UUID leagueId);
}
