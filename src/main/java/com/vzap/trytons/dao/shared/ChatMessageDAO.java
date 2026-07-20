package com.vzap.trytons.dao.shared;

import com.vzap.trytons.model.shared.ChatMessage;
import com.vzap.trytons.model.shared.LeagueChatActivity;

import java.util.List;

public interface ChatMessageDAO {
    List<ChatMessage> findRecentMessages(int limit);
    List<LeagueChatActivity> countMessagesByLeague();
}
