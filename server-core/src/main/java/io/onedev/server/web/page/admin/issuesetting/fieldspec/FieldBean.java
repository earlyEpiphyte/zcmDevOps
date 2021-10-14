package io.onedev.server.web.page.admin.issuesetting.fieldspec;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import io.onedev.server.model.support.issue.field.spec.FieldSpec;
import io.onedev.server.web.editable.annotation.Editable;

@Editable
public class FieldBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private FieldSpec field;

	private boolean promptUponIssueOpen = true;

	@Editable(name="类型", order=100)
	@NotNull(message="不能为空")
	public FieldSpec getField() {
		return field;
	}

	public void setField(FieldSpec field) {
		this.field = field;
	}

	@Editable(order=200,name="提示输入字段", description="如果选中，则默认情况下会在打开问题时提示用户输入此字段")
	public boolean isPromptUponIssueOpen() {
		return promptUponIssueOpen;
	}

	public void setPromptUponIssueOpen(boolean promptUponIssueOpen) {
		this.promptUponIssueOpen = promptUponIssueOpen;
	}

}
