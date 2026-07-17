package com.infy.newgen.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.infy.newgen.entity.Policy;

public interface PolicyRepository extends JpaRepository<Policy, String> {

    @Query("SELECT COUNT(p) FROM Policy p WHERE p.customer.agent.agentId = :agentId AND YEAR(p.policyStartDate) = YEAR(CURRENT_DATE)")
    Long countPoliciesInCurrentYear(@Param("agentId") Integer agentId);

    @Query("SELECT COALESCE(SUM(p.premiumAmount),0) FROM Policy p WHERE p.customer.agent.agentId = :agentId AND YEAR(p.lastPremiumPaymentDate) = YEAR(CURRENT_DATE)")
    Double sumCurrentYearPremium(@Param("agentId") Integer agentId);

    List<Policy> findByCustomerCustomerId(Integer customerId);

    Policy findTopByOrderByPolicyIdDesc();
}