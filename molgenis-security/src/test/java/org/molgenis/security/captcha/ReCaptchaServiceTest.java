package org.molgenis.security.captcha;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.mockito.Mock;
import org.molgenis.settings.AppSettings;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URI;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertTrue;

public class ReCaptchaServiceTest extends AbstractMockitoTest {

  private ReCaptchaService reCaptchaService;

  @Mock
  private AppSettings appSettings;

  @BeforeMethod
  public void setUpBeforeMethod() {
    when(appSettings.getRecaptchaVerifyURI()).thenReturn("http://verify.test.org");

    reCaptchaService = new ReCaptchaService(HttpClients.createDefault(), appSettings);
  }

  @Test(expectedExceptions = ReCaptchaValidationException.class)
  public void testValidateErrorURI() {
    String token =
        "03AMGVjXjgx0m4ZxomoqKdH0pjem8YjmnYfY5HPsTtV7pPG1AOvV_lMH275-cRLN4QbRSs3u7tkOJCNQAHAiV2LCEX3-pYM0WzdcsXmTcZ33TwApp5hAaVNRRUZPJOnBWjvWjEUZzWDogEvQlj_kbxYWAm_qLNcO4g63qOEiJWpvz-JRO6SCgdRayKtI5UjCtNl9ihjR34x8hWgk5DRsnE6B1_cP6F2Tv7JPRFxNNbGJbBIBDu5y4MjN6dscka0jnxY3Zh1uk-uPB6kLVZtrK0hq1tA9NmKrKRwA";
     assertTrue(reCaptchaService.validate(token));
  }
}
