package com.infy.newgen.api;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.infy.newgen.dto.PurchasePolicyDTO;
import com.infy.newgen.dto.RenewPolicyDTO;
import com.infy.newgen.enums.PolicyStatus;
import com.infy.newgen.enums.PolicyType;
import com.infy.newgen.exception.NewGenException;
import com.infy.newgen.service.PolicyService;
import com.infy.newgen.utility.JwtUtil;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(PolicyAPI.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = { "Service.POLICY_PURCHASE_SUCCESS=Policy purchased successfully with policy ID:",
        "Service.POLICY_RENEW_SUCCESS=Policy renewed successfully for policy ID:" })
class PolicyApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PolicyService policyService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @Test
    void purchasePolicySuccess() throws Exception {
        PurchasePolicyDTO dto = new PurchasePolicyDTO();
        dto.setNominee("John Doe");
        dto.setPolicyStartDate(LocalDate.now());
        dto.setPolicyTerm(5);
        dto.setPolicyType(PolicyType.HEALTH_INSURANCE);
        dto.setPremiumAmount(1000);

        Claims claims = Jwts.claims();

        claims.put("id", 1);

        when(jwtUtil.extractAllClaims("token")).thenReturn(claims);
        when(policyService.purchasePolicy(dto, 1)).thenReturn("POL123");

        mockMvc.perform(post("/policies/purchase").header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))).andExpect(status().isCreated())
                .andExpect((ResultMatcher) content().string("Policy purchased successfully with policy ID:POL123"));
    }

    @Test
    void purchasePolicyException() throws Exception {
        PurchasePolicyDTO dto = new PurchasePolicyDTO();
        dto.setNominee("John Doe");
        dto.setPolicyStartDate(LocalDate.now());
        dto.setPolicyTerm(5);
        dto.setPolicyType(PolicyType.HEALTH_INSURANCE);
        dto.setPremiumAmount(1000);

        Claims claims = Jwts.claims();
        claims.put("id", 1);

        when(jwtUtil.extractAllClaims("token")).thenReturn(claims);

        when(policyService.purchasePolicy(any(), anyInt()))
                .thenThrow(new NewGenException("Service.CUSTOMER_NOT_FOUND"));
        mockMvc.perform(post("/policies/purchase").header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))).andExpect(status().isBadRequest());
    }

    @Test
    void renewPolicySuccess1() throws Exception {
        RenewPolicyDTO dto = new RenewPolicyDTO();
        dto.setPolicyId("POL-123");
        dto.setPolicyStatus(PolicyStatus.LAPSED);
        dto.setCardNumber("1234123412341234");
        dto.setCvv("123");

        when(policyService.renewPolicy(dto)).thenReturn("POL-123");

        mockMvc.perform(put("/policies/renew")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().string("Policy renewed successfully with penalty for policy ID:POL-123"));

    }

    @Test
    void renewPolicySuccess2() throws Exception {
        RenewPolicyDTO dto = new RenewPolicyDTO();
        dto.setPolicyId("POL-123");
        dto.setPolicyStatus(PolicyStatus.ACTIVE);
        dto.setCardNumber("1234123412341234");
        dto.setCvv("123");

        when(policyService.renewPolicy(dto)).thenReturn("POL-123");

        mockMvc.perform(put("/policies/renew")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().string("Policy renewed successfully without penalty for policy ID:POL-123"));

    }

    @Test
    void renewPolicyException() throws Exception {
        RenewPolicyDTO dto = new RenewPolicyDTO();
        dto.setPolicyId("POL123"); // Invalid Policy ID
        dto.setPolicyStatus(PolicyStatus.LAPSED);
        dto.setCardNumber("1234123412341234");
        dto.setCvv("123");

        when(policyService.renewPolicy(dto)).thenThrow(new NewGenException("Service.POLICY_NOT_FOUND"));

        mockMvc.perform(put("/policies/renew").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value(400))
                .andExpect(jsonPath("$.errorMessage").value("Policy not found"));
    }

}