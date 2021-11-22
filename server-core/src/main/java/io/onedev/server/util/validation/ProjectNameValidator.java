package io.onedev.server.util.validation;

import java.util.regex.Pattern;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import io.onedev.server.util.validation.annotation.ProjectName;

public class ProjectNameValidator implements ConstraintValidator<ProjectName, String> {

	public static final Pattern PATTERN = Pattern.compile("[\\w\\u4e00-\\u9fa5]([\\w-\\.\\s\\u4e00-\\u9fa5]*[\\w\\u4e00-\\u9fa5])?");
	
	private String message;
	
	@Override
	public void initialize(ProjectName constaintAnnotation) {
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
				message = "应该以字母、数字、下划线或者汉字开始和结束。只允许字母数字、下划线、破折号、点和汉字在中间。";
			}
			constraintContext.buildConstraintViolationWithTemplate(message).addConstraintViolation();
			return false;
		} else if (value.equals("new")) {
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
