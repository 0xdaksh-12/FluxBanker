package com.fluxbanker.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fluxbanker.api.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse {
    private final String id;
    private final String firstName;
    private final String lastName;
    private final String address1;
    private final String city;
    private final String state;
    private final String pinCode;
    private final String dateOfBirth;
    private final String aadhaar;
    private final String email;
    private final String profilePic;
    private final String role;
    private final boolean isEmailVerified;

    public static UserResponse fromEntity(User user) {
        return UserResponse.builder()
                .id(user.getId().toString())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .address1(user.getAddress1())
                .city(user.getCity())
                .state(user.getState())
                .pinCode(user.getPinCode())
                .dateOfBirth(user.getDateOfBirth())
                .aadhaar(user.getAadhaar())
                .email(user.getEmail())
                .profilePic(user.getProfilePic())
                .role(user.getRole().name())
                .isEmailVerified(user.isEmailVerified())
                .build();
    }
}
