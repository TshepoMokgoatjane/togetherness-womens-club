package za.co.twc.togetherness.womens.club.exception;

import za.co.twc.togetherness.womens.club.domain.BurialClaim;
import za.co.twc.togetherness.womens.club.domain.Member;

public class MemberMissedLastThreeConsecutiveMonthsException extends RuntimeException {
    public MemberMissedLastThreeConsecutiveMonthsException(Member member) {

        super("Member ID=" + member.getMemberNumber() + " not eligible for claim. Must have paid last 3 months.");
    }
}
