package io.onedev.server.buildspec.job.trigger;

import java.util.List;

import org.eclipse.jgit.lib.ObjectId;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.server.buildspec.job.Job;
import io.onedev.server.buildspec.job.SubmitReason;
import io.onedev.server.event.ProjectEvent;
import io.onedev.server.event.RefUpdated;
import io.onedev.server.git.GitUtils;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.util.match.PathMatcher;
import io.onedev.server.util.patternset.PatternSet;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.NameOfEmptyValue;
import io.onedev.server.web.editable.annotation.Patterns;
import io.onedev.server.web.util.SuggestionUtils;

@Editable(order=200, name="创建标签")
public class TagCreateTrigger extends JobTrigger {

	private static final long serialVersionUID = 1L;

	private String tags;
	
	private String branches;
	
	@Editable(name="标签", order=100, description="（可选）指定要检查的以空格分隔的标签. "
			+ "使用“**”、“*”或“?”做<b><i>路径通配符匹配</b></i>. "
			+ "以“-”为前缀来排除.空白以匹配所有标签")
	@Patterns(suggester="suggestTags", path=true)
	@NameOfEmptyValue("任何标签")
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

	@Editable(name="在分支上", order=200, description="此触发器仅适用于标签提交位于此处指定的分支上的情况. 多个分支应该用空格隔开. "
			+ "使用“**”、“*”或“?”做<b><i>路径通配符匹配</b></i>. "
			+ "以“-”为前缀来排除.空白以匹配所有标签")
	@Patterns(suggester="suggestBranches", path=true)
	@NameOfEmptyValue("任何分支")
	public String getBranches() {
		return branches;
	}

	public void setBranches(String branches) {
		this.branches = branches;
	}

	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestBranches(String matchWith) {
		return SuggestionUtils.suggestBranches(Project.get(), matchWith);
	}
	
	@Override
	public SubmitReason triggerMatches(ProjectEvent event, Job job) {
		if (event instanceof RefUpdated) {
			RefUpdated refUpdated = (RefUpdated) event;
			String updatedTag = GitUtils.ref2tag(refUpdated.getRefName());
			ObjectId commitId = refUpdated.getNewCommitId();
			Project project = event.getProject();
			if (updatedTag != null && !commitId.equals(ObjectId.zeroId()) 
					&& (tags == null || PatternSet.parse(tags).matches(new PathMatcher(), updatedTag))
					&& (branches == null || project.isCommitOnBranches(commitId, branches))) {
				return new SubmitReason() {

					@Override
					public String getRefName() {
						return refUpdated.getRefName();
					}

					@Override
					public PullRequest getPullRequest() {
						return null;
					}

					@Override
					public String getDescription() {
						return "标签'" + updatedTag + "'已创建";
					}
					
				};
			}
		}
		return null;
	}

	@Override
	public String getTriggerDescription() {
		String description = "当创建标签";
		if (tags != null)
			description += " '" + tags + "'";
		if (branches != null)
			description += " 在分支 '" + branches + "'";
		return description;
	}

}
