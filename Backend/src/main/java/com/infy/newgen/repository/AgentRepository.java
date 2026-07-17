package com.infy.newgen.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.infy.newgen.entity.Agent;

public interface AgentRepository extends CrudRepository<Agent, Integer> {
    Optional<Agent> findByEmail(String email);
}