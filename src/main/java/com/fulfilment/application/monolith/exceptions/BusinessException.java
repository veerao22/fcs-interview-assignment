package com.fulfilment.application.monolith.exceptions;

/**
 * Base for all business rule violations. Subtypes carry an error code and HTTP status so a single
 * BusinessExceptionMapper can map them to API responses.
 */
public abstract class BusinessException extends RuntimeException {

  private final String errorCode;
  private final int status;

  protected BusinessException(String message, String errorCode, int status) {
    super(message);
    this.errorCode = errorCode;
    this.status = status;
  }

  public String getErrorCode() {
    return errorCode;
  }

  public int getStatus() {
    return status;
  }
}
