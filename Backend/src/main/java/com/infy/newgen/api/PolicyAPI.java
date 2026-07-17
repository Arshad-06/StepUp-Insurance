package com.infy.newgen.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.infy.newgen.dto.PurchasePolicyDTO;
import com.infy.newgen.dto.RenewPolicyDTO;
import com.infy.newgen.enums.PolicyStatus;
import com.infy.newgen.exception.NewGenException;
import com.infy.newgen.service.PolicyService;
import com.infy.newgen.utility.JwtUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/policies")
@CrossOrigin(origins = "https://step-up-insurance.vercel.app/")
@Validated
public class PolicyAPI {

    @Autowired
    private PolicyService policyService;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private Environment env;

    @Operation(summary = "Purchases & Adds New Policy For Given Customer")
    @PostMapping("/purchase")
    @ApiResponse(responseCode = "200", description = "Policy Purchased Successfully")
    @ApiResponse(responseCode = "400", description = "Error In Data Validation")
    public ResponseEntity<String> purchasePolicy(@Valid @RequestBody PurchasePolicyDTO dto,
            @RequestHeader("Authorization") String authHeader) throws NewGenException {
        String token = authHeader.substring(7);
        Integer customerId = jwtUtil.extractAllClaims(token).get("id", Integer.class);
        String policyId = policyService.purchasePolicy(dto, customerId);
        String message = env.getProperty("Service.POLICY_PURCHASE_SUCCESS") + policyId;
        return new ResponseEntity<>(message, HttpStatus.CREATED);
    }

    @Operation(summary = "Renews Policy For Given Customer")
    @PutMapping("/renew")
    @ApiResponse(responseCode = "200", description = "Policy Renewed Successfully")
    @ApiResponse(responseCode = "400", description = "Error In Data Validation")
    public ResponseEntity<String> renewPolicy(@Valid @RequestBody RenewPolicyDTO dto) throws NewGenException {
        String message;
        String policyId;
        if (dto.getPolicyStatus() == PolicyStatus.ACTIVE) {
            policyId = policyService.renewPolicy(dto);
            message = env.getProperty("API.POLICY_RENEW_SUCCESS_WITHOUT_PENALTY") + policyId;
        } else {
            policyId = policyService.renewPolicy(dto);
            message = env.getProperty("Service.POLICY_RENEW_SUCCESS_WITH_PENALTY") + policyId;
        }
        return new ResponseEntity<>(message, HttpStatus.OK);

    }

}
