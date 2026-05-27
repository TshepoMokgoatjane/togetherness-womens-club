package za.co.twc.togetherness.womens.club.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "contributions", indexes = {
        @Index(name = "idx_contribution_member_month_status", columnList = "member_id, contributionMonth, status")
})
public class Contribution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal amount;

    private LocalDate paymentDate;

    private String reference;

    @Enumerated(EnumType.STRING)
    private ContributionStatus status;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Convert(converter = YearMonthAttributeConverter.class)
    private YearMonth contributionMonth;
}
