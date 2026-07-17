package com.infy.newgen.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.infy.newgen.dto.ProfileResponseDTO;
import com.infy.newgen.dto.ResetPasswordDTO;
import com.infy.newgen.dto.UpdateProfileDTO;
import com.infy.newgen.exception.NewGenException;
import com.infy.newgen.service.ProfileService;
import com.infy.newgen.utility.JwtUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@RestController
@RequestMapping("/profile")
@CrossOrigin(origins = "http://localhost:4200")
public class ProfileAPI {
    @Autowired
    private ProfileService profileService;
    @Autowired
    private Environment environment;
    @Autowired
    private JwtUtil jwtUtil;

    @Operation(summary = "Fetches User Profile")
    @GetMapping("/fetch")
    @ApiResponse(responseCode = "200", description = "Profile Fetched Successfully")
    @ApiResponse(responseCode = "400", description = "Error In Data Validation")
    public ResponseEntity<ProfileResponseDTO> getProfile(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String role = jwtUtil.extractAllClaims(token).get("role", String.class);
        String email = jwtUtil.extractAllClaims(token).getSubject();
        return new ResponseEntity<>(profileService.getProfileByEmail(email, role), HttpStatus.OK);
    }

    @Operation(summary = "Updates User Profile")
    @PatchMapping("/update")
    @ApiResponse(responseCode = "200", description = "Profile Updated Successfully")
    @ApiResponse(responseCode = "400", description = "Error In Data Validation")
    public ResponseEntity<ProfileResponseDTO> updateProfile(@RequestHeader("Authorization") String authHeader,
            @RequestBody UpdateProfileDTO dto) {
        String token = authHeader.substring(7);
        String role = jwtUtil.extractAllClaims(token).get("role", String.class);
        Integer id = jwtUtil.extractAllClaims(token).get("id", Integer.class);
        return new ResponseEntity<>(profileService.updateProfile(id, null, role, dto), HttpStatus.OK);
    }

    @Operation(summary = "Reset Password")
    @PatchMapping("/reset-password")
    @ApiResponse(responseCode = "200", description = "Password Reset Successfully")
    @ApiResponse(responseCode = "400", description = "Error In Data Validation")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordDTO dto) throws NewGenException {
        String failureMessage = environment.getProperty("API.SESSION_INVALID");
        String successMessage = environment.getProperty("Service.PASSWORD_RESET_SUCCESS");
        if (Boolean.FALSE.equals(jwtUtil.validateResetToken(dto.getResetToken(), dto.getEmail()))) {
            return new ResponseEntity<>(failureMessage, HttpStatus.FORBIDDEN);
        }
        UpdateProfileDTO u = new UpdateProfileDTO();
        u.setPassword(dto.getNewPassword());
        profileService.updateProfile(null, dto.getEmail(), dto.getRole(), u);
        return new ResponseEntity<>(successMessage, HttpStatus.OK);
    }
}
