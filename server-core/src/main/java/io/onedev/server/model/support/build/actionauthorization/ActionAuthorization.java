package io.onedev.server.model.support.build.actionauthorization;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.server.model.Project;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.NameOfEmptyValue;
import io.onedev.server.web.editable.annotation.Patterns;
import io.onedev.server.web.util.SuggestionUtils;

@Editable
public abstract class ActionAuthorization implements Serializable {

	private static final long serialVersionUID = 1L;

	private String authorizedBranches;

	@Editable(order=1000,name="已授权分支", description="当且仅当构建运行在指定分支上，行动才被允许。"
			+ "多个分支空格隔开。"
			+ "指定以空格分隔的标签名称。 使用 '**', '*' 或者 '?' 用于<b><i>路径通配符匹配</i></b>。"
			+ "以'-'为前缀来排除。 留空以匹配所有")
	@Patterns(suggester = "suggestBranches", path=true)
	@NameOfEmptyValue("All")
	public String getAuthorizedBranches() {
		return authorizedBranches;
	}

	public void setAuthorizedBranches(String authorizedBranches) {
		this.authorizedBranches = authorizedBranches;
	}
	
	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestBranches(String matchWith) {
		Project project = Project.get();
		if (project != null)
			return SuggestionUtils.suggestBranches(project, matchWith);
		else
			return new ArrayList<>();
	}
	
	public abstract String getActionDescription();
	
}
