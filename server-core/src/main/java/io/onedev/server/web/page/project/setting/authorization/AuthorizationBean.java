package io.onedev.server.web.page.project.setting.authorization;

import java.io.Serializable;

import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.RoleChoice;
import io.onedev.server.web.editable.annotation.UserChoice;

@Editable
public class AuthorizationBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private String userName;
	
	private String roleName;

	@Editable(order=100, name="User")
	@UserChoice
	@NotEmpty(message="不能为空")
	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	@Editable(order=200, name="Role")
	@RoleChoice
	@Size(min=1, message="至少需要指定一个角色")
	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}
	
}
