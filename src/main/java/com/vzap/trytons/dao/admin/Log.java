package com.vzap.trytons.dao.admin;


import com.vzap.trytons.model.admin.LogActionCount;

import java.util.List;

public interface Log  {
    List<com.vzap.trytons.model.admin.Log> findRecentLogs(int limit);
    List<LogActionCount> countByActionType();
}
