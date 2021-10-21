package io.onedev.server.buildspec.step;

import java.util.List;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.k8shelper.ExecuteCondition;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.util.patternset.PatternSet;
import io.onedev.server.util.validation.annotation.PathSegment;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Interpolative;
import io.onedev.server.web.editable.annotation.Patterns;

@Editable
public abstract class PublishReportStep extends ServerStep {

	private static final long serialVersionUID = 1L;
	
	private String reportName;
	
	private String filePatterns;
	
	private transient PatternSet patternSet;

	@Editable(order=50,name="报告名称",description="指定报告名称")
	@PathSegment
	@NotEmpty(message="不能为空")
	public String getReportName() {
		return reportName;
	}

	public void setReportName(String reportName) {
		this.reportName = reportName;
	}
	
	public PublishReportStep() {
		setCondition(ExecuteCondition.ALWAYS);
	}
	
	@Override
	protected PatternSet getFiles() {
		return PatternSet.parse(getFilePatterns());
	}
	
	@Editable(order=100,name="文件模式",description="指定与作业工作区相关的文件以进行发布. 使用 * 或 ? 用于模式匹配")
	@Interpolative(variableSuggester="suggestVariables")
	@Patterns(path=true)
	@NotEmpty(message="不能为空")
	public String getFilePatterns() {
		return filePatterns;
	}

	public void setFilePatterns(String filePatterns) {
		this.filePatterns = filePatterns;
	}
	
	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestVariables(String matchWith) {
		return BuildSpec.suggestVariables(matchWith, true, true);
	}

	public PatternSet getPatternSet() {
		if (patternSet == null)
			patternSet = PatternSet.parse(getFilePatterns());
		return patternSet;
	}
	
}
