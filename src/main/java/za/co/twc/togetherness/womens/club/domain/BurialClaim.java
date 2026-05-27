package za.co.twc.togetherness.womens.club.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "burial_claims", indexes = {
        @Index(name = "idx_claim_member", columnList = "member_id"),
        @Index(name = "idx_claim_status", columnList = "status")
})
public class BurialClaim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Deceased name is required")
    @Column(name = "deceased_name", nullable = false)
    private String deceasedName;

    @Column(name = "claim_date")
    private LocalDate claimDate;

    @NotNull(message = "Claim amount is required")
    @Column(name = "claim_amount")
    private BigDecimal claimAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ClaimStatus status;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;
}
