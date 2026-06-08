package com.actisys.userservice.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
@RequiredArgsConstructor
@Builder
public class RegNewUserDTO {
    private String login;
    private String email;
    private String phone;
    private String fullName;
    private Timestamp birthDate;
}
