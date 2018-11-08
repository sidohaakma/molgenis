package org.molgenis.security.captcha;

import org.apache.http.impl.client.HttpClients;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ReCaptchaV3ServiceTest {

  private ReCaptchaV3Service reCaptchaV3Service;

  @BeforeMethod
  public void setUpBeforeMethod() {
    reCaptchaV3Service = new ReCaptchaV3Service(HttpClients.createDefault());
  }

  @Test
  public void testValidate() throws Exception {
    String token =
        "03AMGVjXjgx0m4ZxomoqKdH0pjem8YjmnYfY5HPsTtV7pPG1AOvV_lMH275-cRLN4QbRSs3u7tkOJCNQAHAiV2LCEX3-pYM0WzdcsXmTcZ33TwApp5hAaVNRRUZPJOnBWjvWjEUZzWDogEvQlj_kbxYWAm_qLNcO4g63qOEiJWpvz-JRO6SCgdRayKtI5UjCtNl9ihjR34x8hWgk5DRsnE6B1_cP6F2Tv7JPRFxNNbGJbBIBDu5y4MjN6dscka0jnxY3Zh1uk-uPB6kLVZtrK0hq1tA9NmKrKRwA";
    // assertTrue(reCaptchaV3Service.validate(token));
  }
}
