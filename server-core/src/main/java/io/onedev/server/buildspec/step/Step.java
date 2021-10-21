package io.onedev.server.buildspec.step;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.k8shelper.Action;
import io.onedev.k8shelper.Executable;
import io.onedev.k8shelper.ExecuteCondition;
import io.onedev.server.buildspec.param.ParamCombination;
import io.onedev.server.model.Build;
import io.onedev.server.web.editable.annotation.Editable;

@Editable
public abstract class Step implements Serializable {

	private static final long serialVersionUID = 1L;

	private ExecuteCondition condition = ExecuteCondition.ALL_PREVIOUS_STEPS_WERE_SUCCESSFUL;
	
	public abstract Executable getExecutable(Build build, String jobToken, ParamCombination paramCombination);
	
	private String name;

	@Editable(order=10, name="名称")
	@NotEmpty(message="不能为空")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Editable(order=10000,name="条件",description="在什么条件下应该运行这一步")
	@NotNull
	public ExecuteCondition getCondition() {
		return condition;
	}

	public void setCondition(ExecuteCondition condition) {
		this.condition = condition;
	}

	public Action getAction(String name, Build build, String jobToken, ParamCombination paramCombination) {
		return new Action(name, getExecutable(build, jobToken, paramCombination), condition);
	}
	
	public Action getAction(Build build, String jobToken, ParamCombination paramCombination) {
		return getAction(name, build, jobToken, paramCombination);
	}
	
}
