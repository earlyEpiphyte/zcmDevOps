package io.onedev.server.web.page.admin.authenticator;

import java.io.Serializable;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Password;

@Editable
public class AuthenticationToken implements Serializable {

	private static final long serialVersionUID = 1L;

	private String userName;
	
	private String password;

	@Editable(order=100, description="指定用户名以进行身份验证")
	@NotEmpty
	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	@Editable(order=200, description="指定密码进行身份验证")
	@Password
	@NotEmpty
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	
}
