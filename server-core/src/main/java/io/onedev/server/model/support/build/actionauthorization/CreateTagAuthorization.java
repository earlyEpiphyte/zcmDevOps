package io.onedev.server.model.support.build.actionauthorization;

import java.util.ArrayList;
import java.util.List;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.server.model.Project;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.NameOfEmptyValue;
import io.onedev.server.web.editable.annotation.Patterns;
import io.onedev.server.web.util.SuggestionUtils;

@Editable(order=20, name="创建标签")
public class CreateTagAuthorization extends ActionAuthorization {

	private static final long serialVersionUID = 1L;

	private String tagNames;

	@Editable(order=100,name="标签名称", description="指定以空格分隔的标签名称。 使用 '**', '*' 或者 '?' 用于<b><i>路径通配符匹配</i></b>。 "
			+ "以'-'为前缀来排除。 留空以匹配所有")
	@Patterns(suggester = "suggestTags", path=true)
	@NameOfEmptyValue("所有")
	public String getTagNames() {
		return tagNames;
	}

	public void setTagNames(String tagNames) {
		this.tagNames = tagNames;
	}
	
	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestTags(String matchWith) {
		if (Project.get() != null)
			return SuggestionUtils.suggestTags(Project.get(), matchWith);
		else
			return new ArrayList<>();
	}

	@Override
	public String getActionDescription() {
		if (tagNames != null)
			return "创建匹配'" + tagNames + "'名称的标签";
		else
			return "创建标签";
	}
	
}
