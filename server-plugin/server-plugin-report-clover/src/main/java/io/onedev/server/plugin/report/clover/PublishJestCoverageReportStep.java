package io.onedev.server.plugin.report.clover;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Interpolative;
import io.onedev.server.web.editable.annotation.Patterns;

@Editable(order=400, name="发布Jest覆盖率报告")
public class PublishJestCoverageReportStep extends PublishCloverReportStep {

	private static final long serialVersionUID = 1L;
	
	@Editable(order=100, name="文件模式", description="以相对于作业工作区的clover格式指定 Jest 覆盖率报告文件, "
			+ "例如 <tt>coverage/clover.xml</tt>. 可以使用 Jest 选项生成此文件 <tt>'--coverage'</tt>. "
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
