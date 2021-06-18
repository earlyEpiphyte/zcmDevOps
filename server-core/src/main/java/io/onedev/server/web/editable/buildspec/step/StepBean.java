package io.onedev.server.web.editable.buildspec.step;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import io.onedev.server.buildspec.step.Step;
import io.onedev.server.web.editable.annotation.Editable;

@Editable
public class StepBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private Step step;

	@Editable(name="类型", order=100)
	@NotNull(message="不能为空")
	public Step getStep() {
		return step;
	}

	public void setStep(Step step) {
		this.step = step;
	}

}
