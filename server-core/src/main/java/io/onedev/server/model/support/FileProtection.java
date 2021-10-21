package io.onedev.server.model.support;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.server.model.Project;
import io.onedev.server.util.reviewrequirement.ReviewRequirement;
import io.onedev.server.util.validation.Validatable;
import io.onedev.server.util.validation.annotation.ClassValidating;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.JobChoice;
import io.onedev.server.web.editable.annotation.NameOfEmptyValue;
import io.onedev.server.web.editable.annotation.Patterns;
import io.onedev.server.web.util.SuggestionUtils;

@Editable
@ClassValidating
public class FileProtection implements Serializable, Validatable {

	private static final long serialVersionUID = 1L;
	
	private String paths;
	
	private String reviewRequirement;
	
	private transient ReviewRequirement parsedReviewRequirement;
	
	private List<String> jobNames = new ArrayList<>();
	
	@Editable(order=100,name="文件路径", description="指定要保护的空格分隔路径. 使用 '**', '*' 或者 '?' 用于 <b><i>路径通配符匹配</i></b>. "
			+ "以'-'为前缀来排除")
	@Patterns(suggester = "suggestPaths", path=true)
	@NotEmpty(message="不能为空")
	public String getPaths() {
		return paths;
	}

	public void setPaths(String paths) {
		this.paths = paths;
	}
	
	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestPaths(String matchWith) {
		if (Project.get() != null)
			return SuggestionUtils.suggestBlobs(Project.get(), matchWith);
		else
			return new ArrayList<>();
	}

	@Editable(order=200, name="审阅者", description="若指定的路径文件改变，需要指定审阅者。"
			+ "若用户提交了改变，会视为自动审阅了改变。")
	@io.onedev.server.web.editable.annotation.ReviewRequirement
	public String getReviewRequirement() {
		return reviewRequirement;
	}

	public void setReviewRequirement(String reviewRequirement) {
		this.reviewRequirement = reviewRequirement;
	}
	
	public ReviewRequirement getParsedReviewRequirement() {
		if (parsedReviewRequirement == null)
			parsedReviewRequirement = ReviewRequirement.parse(reviewRequirement, true);
		return parsedReviewRequirement;
	}
	
	public void setParsedReviewRequirement(ReviewRequirement parsedReviewRequirement) {
		this.parsedReviewRequirement = parsedReviewRequirement;
		reviewRequirement = parsedReviewRequirement.toString();
	}
	
	@Editable(order=500, name="所需的构建", description="可选")
	@JobChoice
	@NameOfEmptyValue("没有")
	public List<String> getJobNames() {
		return jobNames;
	}

	public void setJobNames(List<String> jobNames) {
		this.jobNames = jobNames;
	}

	@Override
	public boolean isValid(ConstraintValidatorContext context) {
		if (getJobNames().isEmpty() && getReviewRequirement() == null) {
			context.disableDefaultConstraintViolation();
			String message = "审阅者和所需的构建至少指定一项";
			context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
			return false;
		} else {
			return true;
		}
	}
	
}
