package org.molgenis.security.captcha;

import static java.util.Objects.requireNonNull;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URI;
import java.util.Arrays;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;

@Service
public class ReCaptchaV3Service {

  private final CloseableHttpClient httpClient;

  public ReCaptchaV3Service(CloseableHttpClient httpClient) {
    this.httpClient = requireNonNull(httpClient);
  }

  public boolean validate(String token) throws Exception {
    // todo move to factory to remove new
    URI uri = new URI("https://www.google.com/recaptcha/api/siteverify");
    HttpPost httpPost = new HttpPost(uri);
    NameValuePair secret =
        new BasicNameValuePair("secret", "6LdPwngUAAAAAPgDkvhzkLJ61FzEBGCfv4ar26lk");
    NameValuePair responseToken = new BasicNameValuePair("response", token);
    httpPost.setEntity(new UrlEncodedFormEntity(Arrays.asList(secret, responseToken)));

    boolean isValid;

    try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
      int statusCode = response.getStatusLine().getStatusCode();
      if (statusCode >= 200 && statusCode < 300) {
        String result = EntityUtils.toString(response.getEntity());
        Gson gson = new Gson();
        JsonParser parser = new JsonParser();
        JsonObject object = (JsonObject) parser.parse(result); // response will be the json String
        ReCaptchaV3ValidationResponse validationResult =
            gson.fromJson(object, ReCaptchaV3ValidationResponse.class);

        isValid = validationResult.isSuccess() && validationResult.getScore() > 0.5;

      } else if (statusCode == 400) {
        // do error stuff
        System.out.println("failed reCaptcha, code: " + statusCode);
        throw new Exception("did not see this code coming");
      } else {
        throw new Exception("it does not work");
      }
    }
    return isValid;
  }
}
