package io.onedev.server.buildspec.job.trigger;

import java.util.Collection;
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
import io.onedev.server.util.match.Matcher;
import io.onedev.server.util.match.PathMatcher;
import io.onedev.server.util.patternset.PatternSet;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.NameOfEmptyValue;
import io.onedev.server.web.editable.annotation.Patterns;
import io.onedev.server.web.util.SuggestionUtils;

@Editable(order=100, name="更新分支")
public class BranchUpdateTrigger extends JobTrigger {

	private static final long serialVersionUID = 1L;

	private String branches;
	
	private String paths;
	
	@Editable(name="分支", order=100, description="可选择指定以空格分隔的分支进行检查. "
			+ "使用“**”、“*”或“?” 用于<b><i>路径通配符匹配</b></i>. "
			+ "以“-”为前缀来排除. 空白以匹配所有分支")
	@Patterns(suggester = "suggestBranches", path=true)
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
	
	@Editable(name="文件", order=200, 
			description="可选择指定以空格分隔的文件进行检查. "
					+ "使用“**”、“*”或“?” 用于<b><i>路径通配符匹配</b></i>. "
					+ "以“-”为前缀来排除. 空白以匹配所有文件")
	@Patterns(suggester = "getPathSuggestions", path=true)
	@NameOfEmptyValue("任何文件")
	public String getPaths() {
		return paths;
	}

	public void setPaths(String paths) {
		this.paths = paths;
	}

	@SuppressWarnings("unused")
	private static List<InputSuggestion> getPathSuggestions(String matchWith) {
		return SuggestionUtils.suggestBlobs(Project.get(), matchWith);
	}

	private boolean touchedFile(RefUpdated refUpdated) {
		if (getPaths() != null) {
			if (refUpdated.getOldCommitId().equals(ObjectId.zeroId())) {
				return true;
			} else if (refUpdated.getNewCommitId().equals(ObjectId.zeroId())) {
				return false;
			} else {
				Collection<String> changedFiles = GitUtils.getChangedFiles(refUpdated.getProject().getRepository(), 
						refUpdated.getOldCommitId(), refUpdated.getNewCommitId());
				PatternSet patternSet = PatternSet.parse(getPaths());
				Matcher matcher = new PathMatcher();
				for (String changedFile: changedFiles) {
					if (patternSet.matches(matcher, changedFile))
						return true;
				}
				return false;
			}
		} else {
			return true;
		}
	}
	
	@Override
	public SubmitReason triggerMatches(ProjectEvent event, Job job) {
		if (event instanceof RefUpdated) {
			RefUpdated refUpdated = (RefUpdated) event;
			String updatedBranch = GitUtils.ref2branch(refUpdated.getRefName());
			Matcher matcher = new PathMatcher();
			if (updatedBranch != null 
					&& (branches == null || PatternSet.parse(branches).matches(matcher, updatedBranch)) 
					&& touchedFile(refUpdated)) {
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
						return "分支'" + updatedBranch + "'已更新";
					}
					
				};
			}
		}
		return null;
	}

	@Override
	public String getTriggerDescription() {
		String description;
		if (getBranches() != null && getPaths() != null)
			description = String.format("当更新分支'%s'和文件'%s'", getBranches(), getPaths());
		else if (getBranches() != null)
			description = String.format("当更新分支'%s'", getBranches());
		else if (getPaths() != null)
			description = String.format("当更新文件'%s'", getPaths());
		else
			description = "当更新分支";
		return description;
	}

}
