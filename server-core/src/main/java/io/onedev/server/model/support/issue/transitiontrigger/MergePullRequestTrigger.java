package io.onedev.server.model.support.issue.transitiontrigger;

import io.onedev.server.web.editable.annotation.Editable;

@Editable(order=250, name="拉取请求被合并")
public class MergePullRequestTrigger extends PullRequestTrigger {

	private static final long serialVersionUID = 1L;

	@Override
	public String getDescription() {
		if (getBranches() != null)
			return "分支'" + getBranches() + "'上的拉取请求被合并";
		else
			return "任意分支上的拉取请求被合并";
	}

}
