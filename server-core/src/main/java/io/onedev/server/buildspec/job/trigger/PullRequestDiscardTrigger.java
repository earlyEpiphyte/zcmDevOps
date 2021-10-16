package io.onedev.server.buildspec.job.trigger;

import io.onedev.server.buildspec.job.Job;
import io.onedev.server.buildspec.job.SubmitReason;
import io.onedev.server.event.ProjectEvent;
import io.onedev.server.event.pullrequest.PullRequestChangeEvent;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestDiscardData;
import io.onedev.server.web.editable.annotation.Editable;

@Editable(order=320, name="丢弃拉取请求", description="作业将在目标分支的头部提交上运行")
public class PullRequestDiscardTrigger extends PullRequestTrigger {

	private static final long serialVersionUID = 1L;

	@Override
	public SubmitReason triggerMatches(ProjectEvent event, Job job) {
		if (event instanceof PullRequestChangeEvent) {
			PullRequestChangeEvent pullRequestChangeEvent = (PullRequestChangeEvent) event;
			if (pullRequestChangeEvent.getChange().getData() instanceof PullRequestDiscardData)
				return triggerMatches(pullRequestChangeEvent.getRequest());
		}
		return null;
	}

	@Override
	public String getTriggerDescription() {
		return getTriggerDescription("丢弃");
	}

}
