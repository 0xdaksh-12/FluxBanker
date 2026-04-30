package com.fluxbanker.api.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {
    private String firstName;
    private String lastName;
    private String address1;
    private String city;
    private String state;
    private String pinCode;
    private String dateOfBirth;
    private String profilePic;
}
