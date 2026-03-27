package com.school.dto.response;
import lombok.*;
@Data @AllArgsConstructor @NoArgsConstructor
public class ApiResponse {
    private String message;
    private boolean success;
    public static ApiResponse ok(String msg){return new ApiResponse(msg,true);}
}
