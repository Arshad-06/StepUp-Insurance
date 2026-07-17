package com.infy.newgen.service;

import com.infy.newgen.dto.AgentLoginDTO;
import com.infy.newgen.dto.AgentRegistrationDTO;
import com.infy.newgen.dto.DashboardDTO;
import com.infy.newgen.exception.NewGenException;

public interface AgentService {

    Integer registerAgent(AgentRegistrationDTO dto) throws NewGenException;

    Integer loginAgent(AgentLoginDTO dto) throws NewGenException;

    DashboardDTO getDashboard(Integer agentId) throws NewGenException;
}