package com.fluxbanker.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fluxbanker.api.entity.User;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse {
    private String id;
    private String firstName;
    private String lastName;
    private String address1;
    private String city;
    private String state;
    private String pinCode;
    private String dateOfBirth;
    private String aadhaar;
    private String email;
    private String profilePic;
    private String role;
    private boolean isEmailVerified;

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
