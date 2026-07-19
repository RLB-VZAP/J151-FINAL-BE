package com.vzap.trytons.model.admin;

import com.vzap.trytons.enums.SystemReportType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
public class SystemReport {

    private UUID reportId;
    private SystemReportType reportType;
    private String reportTitle;
    private String parametersJson;
    private String resultJson;
    private LocalDateTime generatedAt;

    private UUID generatedByAdminUserId;
}