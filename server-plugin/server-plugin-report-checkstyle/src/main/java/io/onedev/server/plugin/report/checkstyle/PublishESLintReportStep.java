package io.onedev.server.plugin.report.checkstyle;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Interpolative;
import io.onedev.server.web.editable.annotation.Patterns;

@Editable(order=300, name="发布ESLint报告")
public class PublishESLintReportStep extends PublishCheckstyleReportStep {

	private static final long serialVersionUID = 1L;
	
	@Editable(order=100, name="文件模式",description="以与作业工作空间相关的 checkstyle 格式指定 ESLint 报告文件."
			+ "这个文件可以用 ESLint 选项生成<tt>'-f checkstyle'</tt> 和 <tt>'-o'</tt>. "
			+ "使用 * 或 ? 用于模式匹配")
	@Interpolative(variableSuggester="suggestVariables")
	@Patterns(path=true)
	@NotEmpty(message="不能为空")
	@Override
	public String getFilePatterns() {
		return super.getFilePatterns();
	}

	@Override
	public void setFilePatterns(String filePatterns) {
		super.setFilePatterns(filePatterns);
	}

}
