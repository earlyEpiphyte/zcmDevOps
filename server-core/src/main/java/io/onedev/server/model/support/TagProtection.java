package io.onedev.server.model.support;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.server.model.Project;
import io.onedev.server.util.patternset.PatternSet;
import io.onedev.server.util.usage.Usage;
import io.onedev.server.util.usermatch.Anyone;
import io.onedev.server.util.usermatch.UserMatch;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Horizontal;
import io.onedev.server.web.editable.annotation.Patterns;
import io.onedev.server.web.util.SuggestionUtils;

@Editable
@Horizontal
public class TagProtection implements Serializable {

	private static final long serialVersionUID = 1L;

	private boolean enabled = true;
	
	private String tags;
	
	private String userMatch = new Anyone().toString();
	
	private boolean preventUpdate = true;
	
	private boolean preventDeletion = true;
	
	private boolean preventCreation = true;
	
	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Editable(order=100,name="标签名", description="指定要保护的空格分隔标签。使用 '**', '*' 或者 '?' 用于 <b><i>路径通配符匹配</i></b>。"
			+ "以'-'为前缀来排除")
	@Patterns(suggester = "suggestTags", path=true)
	@NotEmpty(message="不能为空")
	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}

	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestTags(String matchWith) {
		return SuggestionUtils.suggestTags(Project.get(), matchWith);
	}
	
	@Editable(order=150, name="适用用户", description="如果用户操作标签匹配此处指定的标准，则规则将适用")
	@io.onedev.server.web.editable.annotation.UserMatch
	@NotEmpty(message="不能为空")
	public String getUserMatch() {
		return userMatch;
	}

	public void setUserMatch(String userMatch) {
		this.userMatch = userMatch;
	}

	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestBranches(String matchWith) {
		Project project = Project.get();
		if (project != null)
			return SuggestionUtils.suggestBranches(project, matchWith);
		else
			return new ArrayList<>();
	}
	
	@Editable(order=200,name="防止标签更新", description="选中此项以防止标签更新")
	public boolean isPreventUpdate() {
		return preventUpdate;
	}

	public void setPreventUpdate(boolean preventUpdate) {
		this.preventUpdate = preventUpdate;
	}

	@Editable(order=300,name="防止标签删除", description="选中此项以防止标签删除")
	public boolean isPreventDeletion() {
		return preventDeletion;
	}

	public void setPreventDeletion(boolean preventDeletion) {
		this.preventDeletion = preventDeletion;
	}

	@Editable(order=400,name="防止创建标签", description="选中此项以防止创建标签")
	public boolean isPreventCreation() {
		return preventCreation;
	}

	public void setPreventCreation(boolean preventCreation) {
		this.preventCreation = preventCreation;
	}

	public void onRenameGroup(String oldName, String newName) {
		userMatch = UserMatch.onRenameGroup(userMatch, oldName, newName);
	}
	
	public Usage onDeleteGroup(String groupName) {
		Usage usage = new Usage();
		if (UserMatch.isUsingGroup(userMatch, groupName))
			usage.add("applicable users");
		return usage.prefix("tag protection '" + getTags() + "'");
	}
	
	public void onRenameUser(String oldName, String newName) {
		userMatch = UserMatch.onRenameUser(userMatch, oldName, newName);
	}
	
	public Usage onDeleteUser(String userName) {
		Usage usage = new Usage();
		if (UserMatch.isUsingUser(userMatch, userName))
			usage.add("applicable users");
		return usage.prefix("tag protection '" + getTags() + "'");
	}

	public Usage getTagUsage(String tagName) {
		Usage usage = new Usage();
		PatternSet patternSet = PatternSet.parse(getTags());
		if (patternSet.getIncludes().contains(tagName) || patternSet.getExcludes().contains(tagName))
			usage.add("tag protection '" + getTags() + "'");
		return usage;
	}
	
}
