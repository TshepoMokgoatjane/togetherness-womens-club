package za.co.twc.togetherness.womens.club.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import za.co.twc.togetherness.womens.club.validation.ValidSAId;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "member", indexes = {
        @Index(name = "idx_member_number", columnList = "member_number"),
        @Index(name = "idx_member_email", columnList = "email")
})
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_number", unique = true, nullable = false)
    private String memberNumber;

    @Column(name = "first_name", nullable = false)
    @NotBlank
    private String firstName;

    @Column(name = "last_name", nullable = false)
    @NotBlank
    private String lastName;

    @ValidSAId
    @Column(name = "id_number", nullable = false, unique = true, length = 13)
    @NotBlank
    private String idNumber;

    @Email(message = "Invalid email format")
    @Column(name = "email", nullable = false, unique = true, length = 50)
    @NotBlank
    private String email;

    @NotBlank(message = "Physical address is required")
    @Column(name = "physical_address", nullable = false, length = 250)
    private String physicalAddress;

    @Pattern(regexp = "\\d{10}", message = "Phone number must be 10 digits long.")
    @Column(name = "phone_number", nullable = false, unique = true, length = 10)
    @NotBlank
    private String phoneNumber;

    @Pattern(regexp = "\\d{10}", message = "Alternative phone number must be 10 digits long.")
    @Column(name = "alternative_phone_number", unique = true, length = 10)
    private String alternativePhoneNumber;

    @Column(name = "date_joined", nullable = false, updatable = false)
    private LocalDateTime joinDate;

    @Column(name = "member_status", nullable = false)
    @Enumerated(EnumType.STRING)
    @NotNull
    private MemberStatus status = MemberStatus.ACTIVE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted", nullable = false)
    private boolean deleted;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Dependent> dependents;

    @OneToOne(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)

    @PrePersist
    public void prePersist() {
        this.joinDate = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
