package com.infy.newgen.service;

import com.infy.newgen.dto.ProfileResponseDTO;
import com.infy.newgen.dto.UpdateProfileDTO;

public interface ProfileService {
    public ProfileResponseDTO getProfileByEmail(String email, String role);

    public ProfileResponseDTO updateProfile(Integer id, String email, String role, UpdateProfileDTO dto);
}