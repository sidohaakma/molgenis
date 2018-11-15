package org.molgenis.security.captcha;


public class ReCaptchaValidationException extends Exception {

  private static final long serialVersionUID = 1L;

  public ReCaptchaValidationException(String message) {
    super(message);
  }
}
