package de.bjarnerest.asechat.instruction;

public class InstructionInvalidException extends Exception {

  public InstructionInvalidException() {
  }

  public InstructionInvalidException(String message) {
    super(message);
  }

  public InstructionInvalidException(String message, Throwable cause) {
    super(message, cause);
  }

  public InstructionInvalidException(Throwable cause) {
    super(cause);
  }

  public InstructionInvalidException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
