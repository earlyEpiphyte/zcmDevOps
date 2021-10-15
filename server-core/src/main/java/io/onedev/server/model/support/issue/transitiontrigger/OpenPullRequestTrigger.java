package io.onedev.server.model.support.issue.transitiontrigger;

import io.onedev.server.web.editable.annotation.Editable;

@Editable(order=200, name="开放拉取请求")
public class OpenPullRequestTrigger extends PullRequestTrigger {

	private static final long serialVersionUID = 1L;

	@Override
	public String getDescription() {
		if (getBranches() != null)
			return "分支'" + getBranches() + "'上的拉取请求已开放";
		else
			return "任意分支上的拉取请求开发";
	}

}
