package io.onedev.server.model.support.issue.transitiontrigger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.model.support.issue.field.spec.FieldSpec;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.usage.Usage;
import io.onedev.server.web.component.issue.workflowreconcile.ReconcileUtils;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldResolution;
import io.onedev.server.web.editable.annotation.ChoiceProvider;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.IssueQuery;
import io.onedev.server.web.editable.annotation.NameOfEmptyValue;
import io.onedev.server.web.editable.annotation.RoleChoice;

@Editable(order=100, name="按下按钮")
public class PressButtonTrigger extends TransitionTrigger {

	private static final long serialVersionUID = 1L;

	private String buttonLabel;

	private List<String> authorizedRoles = new ArrayList<>();
	
	private List<String> promptFields = new ArrayList<>();
	
	@Editable(order=100,name="按钮名称")
	@NotEmpty
	public String getButtonLabel() {
		return buttonLabel;
	}

	public void setButtonLabel(String buttonLabel) {
		this.buttonLabel = buttonLabel;
	}

	@Editable(order=200,name="已授权角色", description="可选的选择角色进行授权"
			+ "若无指定，则授权所有角色")
	@RoleChoice
	public List<String> getAuthorizedRoles() {
		return authorizedRoles;
	}
	
	public void setAuthorizedRoles(List<String> authorizedRoles) {
		this.authorizedRoles = authorizedRoles;
	}

	@Editable(order=500,name="需改变值的字段", description="当按下按钮时，可选的选择需改变值的字段的值")
	@ChoiceProvider("getFieldChoices")
	@NameOfEmptyValue("无字段需改变值")
	public List<String> getPromptFields() {
		return promptFields;
	}

	public void setPromptFields(List<String> promptFields) {
		this.promptFields = promptFields;
	}
	
	@SuppressWarnings("unused")
	private static List<String> getFieldChoices() {
		List<String> fields = new ArrayList<>();
		GlobalIssueSetting issueSetting = OneDev.getInstance(SettingManager.class).getIssueSetting();
		for (FieldSpec field: issueSetting.getFieldSpecs())
			fields.add(field.getName());
		return fields;
	}

	@Override
	public Collection<String> getUndefinedFields() {
		Collection<String> undefinedFields = super.getUndefinedFields();
		GlobalIssueSetting setting = OneDev.getInstance(SettingManager.class).getIssueSetting();
		for (String field: getPromptFields()) {
			if (setting.getFieldSpec(field) == null)
				undefinedFields.add(field);
		}
		return undefinedFields;
	}

	@Override
	public boolean fixUndefinedFields(Map<String, UndefinedFieldResolution> resolutions) {
		if (!super.fixUndefinedFields(resolutions))
			return false;
		for (Map.Entry<String, UndefinedFieldResolution> entry: resolutions.entrySet()) {
			if (entry.getValue().getFixType() == UndefinedFieldResolution.FixType.CHANGE_TO_ANOTHER_FIELD) 
				ReconcileUtils.renameItem(getPromptFields(), entry.getKey(), entry.getValue().getNewField());
			else 
				getPromptFields().remove(entry.getKey());
		}
		return true;
	}

	@Override
	public void onRenameRole(String oldName, String newName) {
		int index = getAuthorizedRoles().indexOf(oldName);
		if (index != -1) 
			getAuthorizedRoles().set(index, newName);
	}

	@Override
	public Usage onDeleteRole(String roleName) {
		Usage usage = super.onDeleteRole(roleName);
		if (getAuthorizedRoles().contains(roleName))
			usage.add("authorized roles");
		return usage;
	}

	public boolean isAuthorized(Project project) {
		if (!getAuthorizedRoles().isEmpty()) {
			if (SecurityUtils.canManageIssues(Project.get())) {
				return true;
			} else {
				for (String roleName: getAuthorizedRoles()) {
					if (SecurityUtils.isAuthorizedWithRole(project, roleName))
						return true;
				}
				return false;
			}
		} else {
			return true;
		}
	}

	@Editable(order=1000, name="适用问题", description="（可选）指定适用于此转换的问题.为所有问题留空")
	@IssueQuery(withOrder = false, withCurrentUserCriteria = true, withCurrentBuildCriteria = false, 
			withCurrentPullRequestCriteria = false, withCurrentCommitCriteria = false)	
	@NameOfEmptyValue("所有")
	@Override
	public String getIssueQuery() {
		return super.getIssueQuery();
	}

	public void setIssueQuery(String issueQuery) {
		super.setIssueQuery(issueQuery);
	}
	
	@Override
	public String getDescription() {
		if (authorizedRoles.isEmpty())
			return "按钮'" + buttonLabel + "'被角色中的某个用户按下";
		else
			return "按钮'" + buttonLabel + "'被角色中的某个用户按下" + authorizedRoles;
	}
	
}
