package com.wallet.service.prototype.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "customers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_ref", unique = true, nullable = false)
    private String customerRef;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "middle_name")
    private String middleName;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "phone_number", nullable = false, unique = true)
    private String phoneNumber;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "gender")
    private String gender; // M, F, or O

    @Column(name = "bvn", unique = true)
    private String bvn;

    @Column(name = "nin", unique = true)
    private String nin;

    @Column(name = "residential_address")
    private String residentialAddress;

    @Column(name = "state_of_origin")
    private String stateOfOrigin;

    @Column(name = "lga_of_origin")
    private String lgaOfOrigin;

    @Column(name = "nationality", nullable = false)
    private String nationality;

    @Column(name = "id_type")
    private String idType; // e.g., NIN, Driver’s License, etc.

    @Column(name = "id_number")
    private String idNumber;

    @Column(name = "id_issue_date")
    private LocalDate idIssueDate;

    @Column(name = "id_expiry_date")
    private LocalDate idExpiryDate;

    @Column(name = "status")
    private String status; // e.g., ACTIVE, INACTIVE, SUSPENDED

    @Column(name = "date_created", updatable = false)
    private LocalDate dateCreated;

    @Column(name = "date_updated")
    private LocalDate dateUpdated;

    @PrePersist
    public void onCreate() {
        this.dateCreated = LocalDate.now();
        this.dateUpdated = LocalDate.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.dateUpdated = LocalDate.now();
    }
}
