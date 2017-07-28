package org.molgenis.security.twofactor;

import org.molgenis.security.google.GoogleAuthenticatorService;
import org.molgenis.security.login.MolgenisLoginController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/2fa")
public class TwoFactorAuthenticationController
{
	public static final String URI = "/2fa";
	public static final String TWO_FACTOR_ENABLED_URI = "/enabled";
	public static final String TWO_FACTOR_INITIAL_URI = "/initial";
	private static final String TWO_FACTOR_VALIDATION_URI = "/validate";
	private static final String TWO_FACTOR_SECRET_URI = "/secret";

	private static final String ATTRIBUTE_2FA_IS_INITIAL = "is2faInitial";
	private static final String ATTRIBUTE_2FA_IS_ENABLED = "is2faEnabled";
	private static final String ATTRIBUTE_2FA_SECRET_KEY = "secretKey";
	private static final String ATTRIBUTE_2FA_AUTHENTICATOR_URI = "authenticatorURI";
	private static final String ATTRIBUTE_HEADER_2FA_INITIAL = "setup2faHeader";
	private static final String ATTRIBUTE_HEADER_2FA_VERIFY_KEY = "verifyKeyHeader";

	private static final String HEADER_VALUE_VERIFY_KEY = "Verification code";
	private static final String HEADER_VALUE_INITIAL = "Setup 2 factor authentication";

	private TwoFactorAuthenticationService twoFactorAuthenticationService;
	private OTPService otpService;
	private GoogleAuthenticatorService googleAuthenticatorService;

	@Autowired
	public TwoFactorAuthenticationController(TwoFactorAuthenticationService twoFactorAuthenticationService,
			OTPService otpService, GoogleAuthenticatorService googleAuthenticatorService)
	{
		this.twoFactorAuthenticationService = twoFactorAuthenticationService;
		this.otpService = otpService;
		this.googleAuthenticatorService = googleAuthenticatorService;
	}

	@RequestMapping(method = RequestMethod.GET, value = TWO_FACTOR_ENABLED_URI)
	public String enabled(Model model)
	{
		model.addAttribute(ATTRIBUTE_HEADER_2FA_VERIFY_KEY, HEADER_VALUE_VERIFY_KEY);
		model.addAttribute(ATTRIBUTE_2FA_IS_ENABLED, true);
		return MolgenisLoginController.VIEW_LOGIN;
	}

	@RequestMapping(method = RequestMethod.POST, value = TWO_FACTOR_VALIDATION_URI)
	public String validateVerificationCodeAndAuthenticate(Model model, @RequestParam String verificationCode)
	{
		String redirectUri = "redirect:/";
		try
		{
			if (twoFactorAuthenticationService.isVerificationCodeValidForUser(verificationCode))
			{
				twoFactorAuthenticationService.authenticate();
			}
		}
		catch (Exception er)
		{
			model.addAttribute(ATTRIBUTE_2FA_IS_ENABLED, true);
			model.addAttribute(ATTRIBUTE_HEADER_2FA_VERIFY_KEY, HEADER_VALUE_VERIFY_KEY);
			model.addAttribute(MolgenisLoginController.ERROR_MESSAGE_ATTRIBUTE, "No valid verification code entered!");
			redirectUri = MolgenisLoginController.VIEW_LOGIN;
		}

		return redirectUri;
	}

	@RequestMapping(method = RequestMethod.GET, value = TWO_FACTOR_INITIAL_URI)
	public String initial(Model model)
	{

		model.addAttribute(ATTRIBUTE_HEADER_2FA_INITIAL, HEADER_VALUE_INITIAL);
		model.addAttribute(ATTRIBUTE_2FA_IS_INITIAL, true);

		try
		{
			String secretKey = twoFactorAuthenticationService.generateSecretKey();
			model.addAttribute(ATTRIBUTE_2FA_SECRET_KEY, secretKey);
			model.addAttribute(ATTRIBUTE_2FA_AUTHENTICATOR_URI,
					googleAuthenticatorService.getGoogleAuthenticatorURI(secretKey));
		}
		catch (UsernameNotFoundException err)
		{
			model.addAttribute(MolgenisLoginController.ERROR_MESSAGE_ATTRIBUTE, "No user found!");
		}

		return MolgenisLoginController.VIEW_LOGIN;
	}

	@RequestMapping(method = RequestMethod.POST, value = TWO_FACTOR_SECRET_URI)
	public String setSecret(Model model, @RequestParam String verificationCode, @RequestParam String secretKey)
	{
		String redirectUrl = "redirect:/";

		try
		{
			otpService.tryVerificationCode(verificationCode, secretKey);
			twoFactorAuthenticationService.setSecretKey(secretKey);
			twoFactorAuthenticationService.authenticate();
		}
		catch (Exception e)
		{
			model.addAttribute(ATTRIBUTE_2FA_IS_INITIAL, true);
			model.addAttribute(ATTRIBUTE_HEADER_2FA_INITIAL, HEADER_VALUE_INITIAL);
			model.addAttribute(ATTRIBUTE_2FA_SECRET_KEY, secretKey);
			model.addAttribute(ATTRIBUTE_2FA_AUTHENTICATOR_URI,
					googleAuthenticatorService.getGoogleAuthenticatorURI(secretKey));
			model.addAttribute(MolgenisLoginController.ERROR_MESSAGE_ATTRIBUTE, "No valid verification code entered!");
			redirectUrl = MolgenisLoginController.VIEW_LOGIN;
		}

		return redirectUrl;
	}

}
