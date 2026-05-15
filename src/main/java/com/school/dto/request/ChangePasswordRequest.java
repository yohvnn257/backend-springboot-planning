package com.school.dto.request;
import lombok.Data;
@Data
public class ChangePasswordRequest {
    private String currentPassword;
    private String newPassword;
}
