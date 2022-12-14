package io.onedev.server.web.editable.buildspec.job.trigger;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import io.onedev.server.buildspec.job.trigger.JobTrigger;
import io.onedev.server.web.editable.annotation.Editable;

@Editable
public class JobTriggerBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private JobTrigger trigger;

	@Editable(name="类型", order=100)
	@NotNull(message="不能为空")
	public JobTrigger getTrigger() {
		return trigger;
	}

	public void setTrigger(JobTrigger trigger) {
		this.trigger = trigger;
	}

}
