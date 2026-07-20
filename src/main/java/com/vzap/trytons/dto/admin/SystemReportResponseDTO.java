package com.vzap.trytons.dto.admin;

import com.vzap.trytons.enums.SystemReportType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder

public class SystemReportResponseDTO {
    private UUID reportId;
    private SystemReportType reportType;
    private String reportTitle;
    private String parametersJson;
    private String resultJson;
    private LocalDateTime generatedAt;
    private UUID generatedByAdminUserId;
}
