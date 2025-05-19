package com.example.mockvoting.exception;

import org.springframework.http.HttpStatus;

public class BusinessLogicException extends CustomException {
  public BusinessLogicException(String message) {
    super(message, HttpStatus.BAD_REQUEST, "BUSINESS_LOGIC_ERROR");
  }

  public BusinessLogicException(String message, HttpStatus status) {
    super(message, status, "BUSINESS_LOGIC_ERROR");
  }
}
