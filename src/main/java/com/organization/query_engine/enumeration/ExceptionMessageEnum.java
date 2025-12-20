package com.organization.query_engine.enumeration;

public enum ExceptionMessageEnum {
  NOT_FOUND_EXCEPTION("Data not found with: {0}"),
  CLIENT_EXCEPTION("Unexpected client exception");

  private final String message;

  ExceptionMessageEnum(String message) {
    this.message = message;
  }

  public String getMessage() {
    return message;
  }
}
