package io.onedev.server.model.support.issue.transitiontrigger;

import java.util.ArrayList;
import java.util.List;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.server.model.Project;
import io.onedev.server.search.entity.issue.IssueQueryLexer;
import io.onedev.server.util.patternset.PatternSet;
import io.onedev.server.util.usage.Usage;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.IssueQuery;
import io.onedev.server.web.editable.annotation.NameOfEmptyValue;
import io.onedev.server.web.editable.annotation.Patterns;
import io.onedev.server.web.util.SuggestionUtils;

@Editable(order=400, name="成功构建")
public class BuildSuccessfulTrigger extends TransitionTrigger {

	private static final long serialVersionUID = 1L;
	
	private String jobNames;
	
	private String branches;
	
	public BuildSuccessfulTrigger() {
		setIssueQuery(io.onedev.server.search.entity.issue.IssueQuery
				.getRuleName(IssueQueryLexer.FixedInCurrentBuild));		
	}
	
	@Editable(order=100, name="适用于作业", description="指定以空格分隔的作业。 使用 '**','*'或者 '?'用于 <b><i>路径通配符匹配</i></b>。"+ "applicable for this trigger. 指定以空格分隔的作业。使用 '*' 或者 '?' 用于通配符匹配。'-'开头表示排除。留空表示授权所有分支。"
			+ "以'-'为前缀来排除。留空匹配所有")
	@Patterns(suggester = "suggestJobs")
	@NameOfEmptyValue("任意作业")
	public String getJobNames() {
		return jobNames;
	}

	public void setJobNames(String jobNames) {
		this.jobNames = jobNames;
	}

	@Editable(order=200, name="适用于分支", description="指定要保护的以空格分隔的分支。 使用 '**','*'或者 '?'用于 <b><i>路径通配符匹配</i></b>。 "
			+ "以'-'为前缀来排除。留空匹配所有")
	@Patterns(suggester = "suggestBranches", path=true)
	@NameOfEmptyValue("任意分支")
	public String getBranches() {
		return branches;
	}

	public void setBranches(String branches) {
		this.branches = branches;
	}
	
	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestBranches(String matchWith) {
		Project project = Project.get();
		if (project != null)
			return SuggestionUtils.suggestBranches(project, matchWith);
		else
			return new ArrayList<>();
	}
	
	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestJobs(String matchWith) {
		Project project = Project.get();
		if (project != null)
			return SuggestionUtils.suggest(project.getJobNames(), matchWith);
		else
			return new ArrayList<>();
	}
	
	@Editable(order=1000, name="适用问题", description="（可选）指定适用于此转换的问题.为所有问题留空")
	@IssueQuery(withOrder = false, withCurrentUserCriteria = false, withCurrentBuildCriteria = true, 
			withCurrentPullRequestCriteria = false, withCurrentCommitCriteria = false)
	@NameOfEmptyValue("所有")
	@Override
	public String getIssueQuery() {
		return super.getIssueQuery();
	}

	public void setIssueQuery(String issueQuery) {
		super.setIssueQuery(issueQuery);
	}

	@Override
	public Usage onDeleteBranch(String branchName) {
		Usage usage = super.onDeleteBranch(branchName);
		PatternSet patternSet = PatternSet.parse(getBranches());
		if (patternSet.getIncludes().contains(branchName) || patternSet.getExcludes().contains(branchName))
			usage.add("applicable branches");
		return usage;
	}
	
	@Override
	public String getDescription() {
		if (jobNames != null) {
			if (branches != null)
				return "分支'" + branches + "'上的作业 '" + jobNames + "'构建成功";
			else
				return "任意分支上的作业 '" + jobNames + "'构建成功";
		} else {
			if (branches != null)
				return "分支'" + branches + "'上的任意作业 '" + jobNames + "'构建成功";
			else
				return "任意分支上的任意作业构建成功";
		}
	}
	
}
