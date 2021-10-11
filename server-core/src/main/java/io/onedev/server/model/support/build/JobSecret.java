package io.onedev.server.model.support.build;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.server.model.Project;
import io.onedev.server.util.validation.annotation.SecretName;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Multiline;
import io.onedev.server.web.editable.annotation.NameOfEmptyValue;
import io.onedev.server.web.editable.annotation.Patterns;
import io.onedev.server.web.util.SuggestionUtils;

@Editable
public class JobSecret implements Serializable {

	private static final long serialVersionUID = 1L;

	private String name;

	private String value;
	
	private String authorizedBranches;
	
	@Editable(order=100,name="名称")
	@NotEmpty
	@SecretName
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@Editable(order=200,name="值")
	@NotEmpty
	@Multiline
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Editable(order=300, name="授权的分支",description=""
			+ "可选的指定分支进行授权，空格隔开。\n"
			+ "只有授权的分支才可以获取这个秘密。\n"
			+ "使用 '**', '*' 或者 '?' 用于 <b><i>路径通配符匹配</i></b>。"
			+ "'-'开头表示排除。留空表示授权所有分支 。")
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
	
}
