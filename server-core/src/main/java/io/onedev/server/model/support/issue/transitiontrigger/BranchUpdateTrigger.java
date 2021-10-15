package io.onedev.server.model.support.issue.transitiontrigger;

import java.util.ArrayList;
import java.util.List;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.server.model.Project;
import io.onedev.server.search.entity.issue.IssueQueryLexer;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.IssueQuery;
import io.onedev.server.web.editable.annotation.NameOfEmptyValue;
import io.onedev.server.web.editable.annotation.Patterns;
import io.onedev.server.web.util.SuggestionUtils;

@Editable(order=500, name="代码已提交")
public class BranchUpdateTrigger extends TransitionTrigger {

	private static final long serialVersionUID = 1L;

	private String branches;

	public BranchUpdateTrigger() {
		setIssueQuery(io.onedev.server.search.entity.issue.IssueQuery
				.getRuleName(IssueQueryLexer.FixedInCurrentCommit));		
	}
	
	@Editable(order=200, name="适用于分支", description="指定以空格分隔的分支。 使用 '**','*'或者 '?'用于 <b><i>路径通配符匹配</i></b>。 "
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

	@Editable(order=1000, name="适用问题", description="（可选）指定适用于此转换的问题.为所有问题留空")
	@IssueQuery(withOrder = false, withCurrentUserCriteria = false, withCurrentBuildCriteria = false, 
			withCurrentPullRequestCriteria = false, withCurrentCommitCriteria = true)
	@NameOfEmptyValue("所有")
	@Override
	public String getIssueQuery() {
		return super.getIssueQuery();
	}

	public void setIssueQuery(String issueQuery) {
		super.setIssueQuery(issueQuery);
	}
	
	@Override
	public String getDescription() {
		if (branches != null)
			return "提交到'" + branches + "'分支";
		else
			return "提交到某个分支";
	}
	
}
