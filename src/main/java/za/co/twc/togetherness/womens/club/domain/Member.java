package za.co.twc.togetherness.womens.club.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "member")
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_number", unique = true, nullable = false)
    @NotBlank
    private String memberNumber;

    @Column(name = "first_name", nullable = false)
    @NotBlank
    private String firstName;

    @Column(name = "last_name", nullable = false)
    @NotBlank
    private String lastName;

    @Column(name = "id_number", nullable = false, unique = true, length = 13)
    @NotBlank
    private String idNumber;

    @Column(name = "email", nullable = false, unique = true, length = 50)
    @NotBlank
    private String email;

    @Column(name = "physical_address", nullable = false, length = 250)
    private String physicalAddress;

    @Column(name = "phone_number", nullable = false, unique = true, length = 10)
    @NotBlank
    private String phoneNumber;

    @Column(name = "alternative_phone_number", unique = true, length = 10)
    private String alternativePhoneNumber;

    @Column(name = "birth_date", nullable = false)
    @NotNull
    private LocalDate birthDate;

    @Column(name = "date_joined", nullable = false)
    @NotNull
    private LocalDate joinDate;

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

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
