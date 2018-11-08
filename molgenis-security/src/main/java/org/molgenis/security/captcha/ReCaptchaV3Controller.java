package org.molgenis.security.captcha;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/recaptcha")
public class ReCaptchaV3Controller {

  @Autowired private ReCaptchaV3Service reCaptchaV3Service;

  @PostMapping
  @ResponseBody
  public Boolean validateCaptcha(@RequestBody ReCaptchaV3ValidationRequest reCaptchaRequest)
      throws Exception {
    return reCaptchaV3Service.validate(reCaptchaRequest.getToken());
  }
}
