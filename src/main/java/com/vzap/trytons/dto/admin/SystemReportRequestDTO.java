package com.vzap.trytons.dto.admin;

import com.vzap.trytons.enums.SystemReportType;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder

public class SystemReportRequestDTO {
    private SystemReportType reportType;
    private String reportTitle;
    private String parametersJson;
}
