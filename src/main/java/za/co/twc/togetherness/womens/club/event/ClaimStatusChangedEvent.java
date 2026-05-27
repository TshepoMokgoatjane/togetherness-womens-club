package za.co.twc.togetherness.womens.club.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import za.co.twc.togetherness.womens.club.domain.BurialClaim;
import za.co.twc.togetherness.womens.club.domain.ClaimStatus;

@Getter
@AllArgsConstructor
public class ClaimStatusChangedEvent {
    private final BurialClaim claim;
    private final ClaimStatus claimStatus;
}
