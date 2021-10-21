package io.onedev.server.model.support.administration;

import java.io.Serializable;
import java.util.List;

import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.server.util.EditContext;
import io.onedev.server.util.match.Matcher;
import io.onedev.server.util.match.PathMatcher;
import io.onedev.server.util.patternset.PatternSet;
import io.onedev.server.util.script.identity.JobIdentity;
import io.onedev.server.util.script.identity.ScriptIdentity;
import io.onedev.server.util.script.identity.SiteAdministrator;
import io.onedev.server.util.usage.Usage;
import io.onedev.server.util.validation.annotation.RegEx;
import io.onedev.server.web.editable.annotation.Code;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.NameOfEmptyValue;
import io.onedev.server.web.editable.annotation.Patterns;
import io.onedev.server.web.editable.annotation.ShowCondition;
import io.onedev.server.web.util.SuggestionUtils;

@Editable
public class GroovyScript implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String BUILTIN_PREFIX = "builtin:";

	private String name;
	
	private List<String> content;
	
	private boolean canBeUsedByBuildJobs = true;
	
	private String allowedProjects;
	
	private String allowedBranches;

	@Editable(order=100,name="脚本名")
	@RegEx(pattern="^(?!" + BUILTIN_PREFIX + ").*$", message="脚本名不允许以'" + BUILTIN_PREFIX + "'开头")
	@NotEmpty(message="不能为空")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Editable(order=300,name="内容")
	@Code(language = Code.GROOVY)
	@Size(min=1, message="不可为空")
	public List<String> getContent() {
		return content;
	}

	public void setContent(List<String> content) {
		this.content = content;
	}
	
	@Editable(order=350,name="能被构建作业使用")
	public boolean isCanBeUsedByBuildJobs() {
		return canBeUsedByBuildJobs;
	}

	public void setCanBeUsedByBuildJobs(boolean canBeUsedByBuildJobs) {
		this.canBeUsedByBuildJobs = canBeUsedByBuildJobs;
	}
	
	@SuppressWarnings("unused")
	private static boolean isCanBeUsedByBuildJobsEnabled() {
		return (boolean) EditContext.get().getInputValue("canBeUsedByBuildJobs");
	}

	@Editable(order=400,name="可使用的项目", description="指定以空格分隔的作业。使用 '*' 或者 '?' 用于通配符匹配。'-'开头表示排除。留空表示授权所有分支。")
	@Patterns(suggester = "suggestProjects")
	@ShowCondition("isCanBeUsedByBuildJobsEnabled")
	@NameOfEmptyValue("所有")
	public String getAllowedProjects() {
		return allowedProjects;
	}
	
	public void setAllowedProjects(String allowedProjects) {
		this.allowedProjects = allowedProjects;
	}
	
	@Editable(order=500,name="可使用的分支", description="可选的指定分支进行授权，空格隔开。\n"
			+ "只有授权的分支才可以获取这个秘密。\n"
			+ "使用 '**', '*' 或者 '?' 用于 <b><i>路径通配符匹配</i></b>。"
			+ "'-'开头表示排除。留空表示授权所有分支 。")
	@Patterns(path=true)
	@ShowCondition("isCanBeUsedByBuildJobsEnabled")
	@NameOfEmptyValue("所有")
	public String getAllowedBranches() {
		return allowedBranches;
	}
	
	public void setAllowedBranches(String allowedBranches) {
		this.allowedBranches = allowedBranches;
	}
	
	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestProjects(String matchWith) {
		return SuggestionUtils.suggestProjects(matchWith);
	}
		
	public final boolean isAuthorized(ScriptIdentity identity) {
		if (identity instanceof SiteAdministrator) { 
			return true;
		} else if (isCanBeUsedByBuildJobs() && identity instanceof JobIdentity) {
			JobIdentity jobIdentity = (JobIdentity) identity;
			Matcher matcher = new PathMatcher();
			return (getAllowedProjects() == null || PatternSet.parse(getAllowedProjects()).matches(matcher, jobIdentity.getProject().getName()))
					&& (getAllowedBranches() == null || jobIdentity.getProject().isCommitOnBranches(jobIdentity.getCommitId(), getAllowedBranches()));
		} else {
			return false;
		}
	}
	
	public Usage onDeleteProject(String projectName, int index) {
		Usage usage = new Usage();
		if (getAllowedProjects() != null) {
			PatternSet patternSet = PatternSet.parse(getAllowedProjects());
			if (patternSet.getIncludes().contains(projectName) || patternSet.getExcludes().contains(projectName))
				usage.add("allowed projects");
		} 
		return usage.prefix("groovy script #" + index);
	}
	
	public void onRenameProject(String oldName, String newName) {
		PatternSet patternSet = PatternSet.parse(getAllowedProjects());
		if (patternSet.getIncludes().remove(oldName))
			patternSet.getIncludes().add(newName);
		if (patternSet.getExcludes().remove(oldName))
			patternSet.getExcludes().add(newName);
		setAllowedProjects(patternSet.toString());
		if (getAllowedProjects().length() == 0)
			setAllowedProjects(null);
	}

}
