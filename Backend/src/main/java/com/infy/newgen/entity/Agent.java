package com.infy.newgen.entity;

import com.infy.newgen.enums.Role;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class Agent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer agentId;

    private String name;

    @Column(unique = true)
    private String email;

    private String contact;

    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;
}