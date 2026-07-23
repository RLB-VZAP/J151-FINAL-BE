package com.vzap.trytons.dto.device;

import com.vzap.trytons.enums.DevicePlatform;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterDeviceRequestDTO {
    private String token;
    private DevicePlatform platform;
}
