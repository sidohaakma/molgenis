package org.molgenis.beacon.model.exceptions;

import org.molgenis.beacon.controller.model.BeaconAlleleResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class BeaconExceptionHandler
{
	private static final Logger LOG = LoggerFactory.getLogger(BeaconExceptionHandler.class);

	@ExceptionHandler(BeaconException.class)
	@ResponseBody
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public BeaconAlleleResponse handleBeaconException(BeaconException e)
	{
		LOG.info(e.getMessage(), e);
		return BeaconAlleleResponse.create(e.getBeaconId(), null,
				BeaconError.create(HttpStatus.BAD_REQUEST.value(), e.getExceptionMessage()), e.getRequest());
	}
}
