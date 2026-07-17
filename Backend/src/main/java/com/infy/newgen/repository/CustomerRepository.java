package com.infy.newgen.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.infy.newgen.entity.Customer;

public interface CustomerRepository extends CrudRepository<Customer, Integer> {
    Optional<Customer> findByEmail(String email);

    List<Customer> findByAgentAgentId(Integer agentId);
}