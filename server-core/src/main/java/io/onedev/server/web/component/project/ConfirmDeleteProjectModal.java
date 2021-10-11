package io.onedev.server.web.component.project;

import org.apache.wicket.ajax.AjaxRequestTarget;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.Project;
import io.onedev.server.web.component.modal.confirm.ConfirmModalPanel;

@SuppressWarnings("serial")
public abstract class ConfirmDeleteProjectModal extends ConfirmModalPanel {

	public ConfirmDeleteProjectModal(AjaxRequestTarget target) {
		super(target);
	}

	@Override
	protected void onConfirm(AjaxRequestTarget target) {
		Project project = getProject();
		
		OneDev.getInstance(ProjectManager.class).delete(project);
		getSession().success("Project '" + project.getName() + "' deleted");
		
		onDeleted(target);
	}

	protected abstract void onDeleted(AjaxRequestTarget target);
	
	@Override
	protected String getConfirmMessage() {
		return "项目内的所有东西都会被删除且无法复原, "
				+ "请下方输入项目名称 \"" + getProject().getName() + "\" 来确定删除";
	}

	@Override
	protected String getConfirmInput() {
		return getProject().getName();
	}

	protected abstract Project getProject();
	
}
