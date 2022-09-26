package br.com.isaque.cursoudemy.config.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class SuccessResponse {

    private Integer status;
    private String message;

    public static SuccessResponse create(String message){
    return SuccessResponse
            .builder()
            .status(HttpStatus.OK.value())
            .message(message)
            .build();
    }

}
