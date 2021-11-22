package io.onedev.server.util.validation;

import java.util.regex.Pattern;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import io.onedev.server.OneDev;
import io.onedev.server.util.validation.annotation.UserName;

public class UserNameValidator implements ConstraintValidator<UserName, String> {
	
	public static final Pattern PATTERN = Pattern.compile("[\\w\\u4e00-\\u9fa5]([\\w-\\.\\s\\u4e00-\\u9fa5]*[\\w\\u4e00-\\u9fa5])?");
	
	private String message;
	
	@Override
	public void initialize(UserName constaintAnnotation) {
		message = constaintAnnotation.message();
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext constraintContext) {
		if (value == null) 
			return true;
		
		if (!PATTERN.matcher(value).matches()) {
			constraintContext.disableDefaultConstraintViolation();
			String message = this.message;
			if (message.length() == 0) {
				message = "应该以字母、数字或下划线开始和结束。只允许字母数字、下划线、破折号和点在中间。";
			}
			constraintContext.buildConstraintViolationWithTemplate(message).addConstraintViolation();
			return false;
		} else if (value.equals("new") || value.equals(OneDev.NAME)) {
			constraintContext.disableDefaultConstraintViolation();
			String message = this.message;
			if (message.length() == 0)
				message = "'" + value + "'是保留名，不可用";
			constraintContext.buildConstraintViolationWithTemplate(message).addConstraintViolation();
			return false;
		} else {
			return true;
		}
	}
	
}
