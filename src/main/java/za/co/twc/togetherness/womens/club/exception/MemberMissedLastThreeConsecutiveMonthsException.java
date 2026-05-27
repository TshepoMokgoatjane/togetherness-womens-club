package za.co.twc.togetherness.womens.club.exception;

import lombok.Getter;
import za.co.twc.togetherness.womens.club.domain.Member;

@Getter
public class MemberMissedLastThreeConsecutiveMonthsException extends RuntimeException {

    private final Long memberId;

    public MemberMissedLastThreeConsecutiveMonthsException(Member member) {

        super("Member ID=" + member.getMemberNumber() + " not eligible for claim. Must have paid last 3 months.");
        this.memberId = member.getId();
    }
}
