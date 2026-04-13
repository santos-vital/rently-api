package com.rently.api.exception;

public class EmailAlreadyExistsException extends ConflictException {

  public EmailAlreadyExistsException() {
    super("Email já cadastrado");
  }
}
