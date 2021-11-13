package io.onedev.server.util.validation;

import java.util.regex.Pattern;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import io.onedev.server.model.Build;
import io.onedev.server.util.validation.annotation.ParamName;

public class ParamNameValidator implements ConstraintValidator<ParamName, String> {

	private static final Pattern PATTERN = Pattern.compile("\\w([\\w-\\.\\s]*\\w)?");
	
	private String message;
	
	@Override
	public void initialize(ParamName constaintAnnotation) {
		message = constaintAnnotation.message();
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext constraintContext) {
		if (value == null) 
			return true;

		String message = this.message;
		if (!PATTERN.matcher(value).matches()) {
			if (message.length() == 0) {
				message = "应该以字母、数字或下划线开始和结束。只允许字母数字、下划线、破折号和点在中间。";
			}
			constraintContext.buildConstraintViolationWithTemplate(message).addConstraintViolation();
			return false;
		} else if (Build.ALL_FIELDS.contains(value)) {
			constraintContext.disableDefaultConstraintViolation();
			if (message.length() == 0)
				message = "'" + value + "'是保留的，不可用";
			constraintContext.buildConstraintViolationWithTemplate(message).addConstraintViolation();
			return false;
		} else {
			return true;
		}
	}
	
}
