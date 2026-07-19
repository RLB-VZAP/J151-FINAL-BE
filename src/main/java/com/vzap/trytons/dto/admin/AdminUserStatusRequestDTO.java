package com.vzap.trytons.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder

public class AdminUserStatusRequestDTO {

    private Boolean isActive;
}
