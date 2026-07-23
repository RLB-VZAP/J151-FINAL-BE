package com.vzap.trytons.dto.market;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarketDashboardDTO {
    private List<MarketPlayerDTO> trending;
    private List<MarketPlayerDTO> mostTransferredIn;
    private List<MarketPlayerDTO> mostTransferredOut;
    private List<MarketPlayerDTO> hiddenGems;
    private List<MarketPlayerDTO> overpriced;
    private List<MarketPlayerDTO> popularCaptains;
}
