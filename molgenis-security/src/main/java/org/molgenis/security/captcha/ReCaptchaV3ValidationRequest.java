package org.molgenis.security.captcha;

import java.util.Objects;

public class ReCaptchaV3ValidationRequest {

  private String token;

  public ReCaptchaV3ValidationRequest(String token) {
    this.token = token;
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ReCaptchaV3ValidationRequest that = (ReCaptchaV3ValidationRequest) o;
    return Objects.equals(token, that.token);
  }

  @Override
  public int hashCode() {
    return Objects.hash(token);
  }
}
