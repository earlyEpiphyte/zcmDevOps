package io.onedev.server.model.support.build.actionauthorization;

import java.util.ArrayList;
import java.util.List;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.server.model.Project;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.NameOfEmptyValue;
import io.onedev.server.web.editable.annotation.Patterns;
import io.onedev.server.web.util.SuggestionUtils;

@Editable(order=200, name="关闭里程碑")
public class CloseMilestoneAuthorization extends ActionAuthorization {

	private static final long serialVersionUID = 1L;

	private String milestoneNames;

	@Editable(order=100,name="里程碑名称", description="指定以空格分隔的里程碑名称. 使用 '*' 或者 '?' 用于通配符匹配. "
			+ "以'-'为前缀来排除. 留空以匹配所有")
	@Patterns(suggester = "suggestMilestones")
	@NameOfEmptyValue("所有")
	public String getMilestoneNames() {
		return milestoneNames;
	}

	public void setMilestoneNames(String milestoneNames) {
		this.milestoneNames = milestoneNames;
	}
	
	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestMilestones(String matchWith) {
		if (Project.get() != null)
			return SuggestionUtils.suggestMilestones(Project.get(), matchWith);
		else
			return new ArrayList<>();
	}

	@Override
	public String getActionDescription() {
		if (milestoneNames != null)
			return "关闭匹配'" + milestoneNames + "'名称的里程碑";
		else
			return "关闭里程碑";
	}

}
