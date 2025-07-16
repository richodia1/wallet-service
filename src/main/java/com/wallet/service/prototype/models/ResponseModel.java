package com.wallet.service.prototype.models;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Slf4j
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseModel {
    private Object data;
    private Integer statusCode;
    private String message;
    private String responseCode;
    private Boolean success;

    static public ResponseModel successResponseModel(Object data, int statusCode, String message) {
        return ResponseModel.builder().success(Boolean.TRUE).data(data).statusCode(statusCode).responseCode("00").message(message).build();
    }

    static public ResponseModel errorResponseModel(String message, int statusCode) {
        return ResponseModel.builder().success(Boolean.FALSE).data(message).statusCode(statusCode).responseCode("05").message(message).build();
    }


    static public ResponseEntity<ResponseModel> successResponse(Object data, int statusCode, String message) {
        ResponseModel response = ResponseModel.builder().success(Boolean.TRUE).data(data).statusCode(statusCode).responseCode("00").message(message).build();
        return ResponseEntity.status(statusCode).body(response);
    }

    static public ResponseEntity<ResponseModel> errorResponse(Object data, String message, HttpStatus statusCode) {
        log.error("error data :{}", data);
        log.error("error message :{}", message);
        ResponseModel response = ResponseModel.builder().success(Boolean.FALSE).data(data).statusCode(statusCode.value()).message(message).build();
        return ResponseEntity.status(statusCode.value()).body(response);
    }
}
