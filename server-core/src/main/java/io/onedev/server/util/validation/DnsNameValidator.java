package io.onedev.server.util.validation;

import java.util.regex.Pattern;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import io.onedev.server.util.validation.annotation.DnsName;

public class DnsNameValidator implements ConstraintValidator<DnsName, String> {

	private static final Pattern PATTERN = Pattern.compile("[a-zA-Z0-9]([-a-zA-Z0-9]*[a-zA-Z0-9])?");
	
	private String message;
	
	@Override
	public void initialize(DnsName constaintAnnotation) {
		message = constaintAnnotation.message();
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext constraintContext) {
		if (value == null)
			return true;
		
		if (!PATTERN.matcher(value).matches()) {
			String message = this.message;
			if (message.length() == 0) {
				message = "只能包含字母数字或者'-',并且只能以字母数字开头";
			}
			constraintContext.buildConstraintViolationWithTemplate(message).addConstraintViolation();
			return false;
		} else {
			return true;
		}
	}
	
}
