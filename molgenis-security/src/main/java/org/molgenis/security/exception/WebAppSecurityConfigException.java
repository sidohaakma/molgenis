package org.molgenis.security.exception;

import static java.util.Objects.requireNonNull;

import org.molgenis.i18n.CodedRuntimeException;

public class WebAppSecurityConfigException extends CodedRuntimeException {

  private static final String ERROR_CODE = "SEC05";
  private final Throwable cause;

  public WebAppSecurityConfigException(Throwable cause) {
    super(ERROR_CODE, cause);
    this.cause = requireNonNull(cause);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {cause.getLocalizedMessage()};
  }
}
