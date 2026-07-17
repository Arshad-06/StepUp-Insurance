package com.infy.newgen.api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infy.newgen.dto.ProfileResponseDTO;
import com.infy.newgen.dto.ResetPasswordDTO;
import com.infy.newgen.dto.UpdateProfileDTO;
import com.infy.newgen.exception.NewGenException;
import com.infy.newgen.service.ProfileService;
import com.infy.newgen.utility.JwtUtil;

import io.jsonwebtoken.Claims;

@WebMvcTest(ProfileAPI.class)
@AutoConfigureMockMvc(addFilters = false)
class ProfileApiTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private ProfileService profileService;

    @MockitoBean
    private JwtUtil jwtUtil;

    // Testing on getProfile method

    @ParameterizedTest
    @ValueSource(strings = { "CUSTOMER", "AGENT" })
    void getProfileValid(String role) throws Exception {
        String token = "dummy-token";
        Claims claims = Mockito.mock(Claims.class);
        ProfileResponseDTO p = new ProfileResponseDTO();
        p.setEmail("kiranmayee@gmail.com");
        p.setRole(role);

        Mockito.when(jwtUtil.extractAllClaims(token)).thenReturn(claims);

        Mockito.when(claims.get("role", String.class)).thenReturn(role);

        Mockito.when(claims.getSubject()).thenReturn("kiranmayee@gmail.com");

        Mockito.when(profileService.getProfileByEmail("kiranmayee@gmail.com", role)).thenReturn(p);

        mockMvc.perform(MockMvcRequestBuilders.get("/profile/fetch").header("Authorization", "Bearer " + token))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.email").value("kiranmayee@gmail.com"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.role").value(role));
    }

    @Test
    void getProfileInValid() throws Exception {
        String token = "dummy-token";
        Claims claims = Mockito.mock(Claims.class);

        Mockito.when(jwtUtil.extractAllClaims(token)).thenReturn(claims);

        Mockito.when(claims.get("role", String.class)).thenReturn("CUSTOMER");

        Mockito.when(claims.getSubject()).thenReturn("kiranmayee@gmail.com");

        Mockito.when(profileService.getProfileByEmail("kiranmayee@gmail.com", "CUSTOMER"))
                .thenThrow(new NewGenException("Service.CUSTOMER_NOT_FOUND"));

        mockMvc.perform(MockMvcRequestBuilders.get("/profile/fetch").header("Authorization", "Bearer " + token))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value(400))
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage").value("Customer not found"));
    }

    // Testing on updateProfile method

    @ParameterizedTest
    @ValueSource(strings = { "CUSTOMER", "AGENT" })
    void updateProfileValid(String role) throws Exception {
        String token = "dummy-token";
        Claims claims = Mockito.mock(Claims.class);
        ProfileResponseDTO p = new ProfileResponseDTO();
        p.setEmail("kiranmayee@gmail.com");
        p.setRole(role);
        UpdateProfileDTO dto = new UpdateProfileDTO();
        dto.setEmail("kiranmayee@gmail.com");

        Mockito.when(jwtUtil.extractAllClaims(token)).thenReturn(claims);
        Mockito.when(claims.get("id", Integer.class)).thenReturn(1);
        Mockito.when(claims.get("role", String.class)).thenReturn(role);

        Mockito.when(claims.getSubject()).thenReturn("kiranmayee@gmail.com");

        Mockito.when(profileService.updateProfile(1, null, role, dto)).thenReturn(p);

        mockMvc.perform(MockMvcRequestBuilders.patch("/profile/update").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(dto)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.email").value("kiranmayee@gmail.com"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.role").value(role));
    }

    // Testing on resetPassword method

    @ParameterizedTest
    @ValueSource(strings = { "CUSTOMER", "AGENT" })
    void resetPasswordValid(String role) throws Exception {

        ResetPasswordDTO reset = new ResetPasswordDTO();
        reset.setEmail("kiranmayee@gmail.com");
        reset.setRole(role);
        reset.setResetToken("dummy-token");
        reset.setNewPassword("Kiran@123");

        Mockito.when(jwtUtil.validateResetToken(reset.getResetToken(), reset.getEmail())).thenReturn(true);

        mockMvc.perform(MockMvcRequestBuilders.patch("/profile/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reset)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("Password Reset Successfully!"));

    }

    @ParameterizedTest
    @ValueSource(strings = { "CUSTOMER", "AGENT" })
    void resetPasswordInvalid(String role) throws Exception {

        ResetPasswordDTO reset = new ResetPasswordDTO();
        reset.setEmail("kiranmayee@gmail.com");
        reset.setRole(role);
        reset.setResetToken("dummy-token");
        reset.setNewPassword("Kiran@123");

        Mockito.when(jwtUtil.validateResetToken(reset.getResetToken(), reset.getEmail())).thenReturn(false);

        mockMvc.perform(MockMvcRequestBuilders.patch("/profile/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reset)))
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.content().string("Session Expired Or Invalid!"));

    }

}