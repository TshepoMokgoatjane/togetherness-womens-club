package za.co.twc.togetherness.womens.club.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import za.co.twc.togetherness.womens.club.utilities.SaIdUtils;
import za.co.twc.togetherness.womens.club.validation.ValidSAId;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

@Entity
@Table(name = "dependents")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Dependent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "first_name", nullable = false)
    private String firstName;

    @NotBlank
    @Column(name = "last_name", nullable = false)
    private String lastName;

    @ValidSAId
    @NotBlank
    @Column(name = "id_number", nullable = false, unique = true, length = 13)
    private String idNumber;

    @Email(message = "Invalid email format")
    @Column(name = "email", nullable = false, length = 50)
    @NotBlank
    private String email;

    @NotBlank(message = "Physical address is required")
    @Column(name = "physical_address", nullable = false, length = 250)
    private String physicalAddress;

    @Pattern(regexp = "\\d{10}", message = "Phone number must be 10 digits long.")
    @Column(name = "phone_number", nullable = false, length = 10)
    @NotBlank
    private String phoneNumber;

    @Pattern(regexp = "\\d{10}", message = "Alternative phone number must be 10 digits long.")
    @Column(name = "alternative_phone_number", length = 10)
    private String alternativePhoneNumber;

    @NotNull
    @Column(name = "relationship", nullable = false)
    @Enumerated(EnumType.STRING)
    private RelationshipType relationship;

    // Relationship to Member
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "deleted", nullable = false)
    private boolean deleted;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @Transient
    public LocalDate getDateOfBirth() {
        if (idNumber != null && idNumber.length() >= 6) {
            return SaIdUtils.extractDobFromId(idNumber);
        }
        return null;
    }

    @Transient
    public Integer getAge() {
        LocalDate dob = getDateOfBirth();
        if (dob != null) {
            return Period.between(dob, LocalDate.now()).getYears();
        }
        return null;
    }
}
