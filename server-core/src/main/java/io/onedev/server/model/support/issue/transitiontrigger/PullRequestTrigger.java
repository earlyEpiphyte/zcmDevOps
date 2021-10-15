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

public abstract class PullRequestTrigger extends TransitionTrigger {

	private static final long serialVersionUID = 1L;
	
	private String branches;
	
	public PullRequestTrigger() {
		setIssueQuery(io.onedev.server.search.entity.issue.IssueQuery
				.getRuleName(IssueQueryLexer.FixedInCurrentPullRequest));		
	}
	
	@Editable(name="目标分支", order=100,description="指定以空格分隔的拉取请求中的目标分支。 使用 '**','*'或者 '?'用于 <b><i>路径通配符匹配</i></b>。 "
					+ "以'-'为前缀来排除。留空匹配所有")
	@Patterns(suggester = "suggestBranches", path=true)
	@NameOfEmptyValue("任意分支")
	public String getBranches() {
		return branches;
	}

	public void setBranches(String branches) {
		this.branches = branches;
	}

	@Editable(order=1000, name="适用问题", description="（可选）指定适用于此转换的问题.为所有问题留空")
	@IssueQuery(withOrder = false, withCurrentUserCriteria = false, withCurrentBuildCriteria = false, 
			withCurrentPullRequestCriteria = true, withCurrentCommitCriteria = false)
	@NameOfEmptyValue("所有")
	@Override
	public String getIssueQuery() {
		return super.getIssueQuery();
	}

	public void setIssueQuery(String issueQuery) {
		super.setIssueQuery(issueQuery);
	}

	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestBranches(String matchWith) {
		if (Project.get() != null)
			return SuggestionUtils.suggestBranches(Project.get(), matchWith);
		else
			return new ArrayList<>();
	}
	
	@Override
	public Usage onDeleteBranch(String branchName) {
		Usage usage = super.onDeleteBranch(branchName);
		PatternSet patternSet = PatternSet.parse(branches);
		if (patternSet.getIncludes().contains(branchName) || patternSet.getExcludes().contains(branchName))
			usage.add("target branches");
		return usage;
	}
	
}
