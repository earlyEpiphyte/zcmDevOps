package io.onedev.server.model.support.administration;

import java.io.Serializable;

import io.onedev.server.web.editable.annotation.Editable;

@Editable
public class SecuritySetting implements Serializable {

	private static final long serialVersionUID = 1L;

	private boolean enableAnonymousAccess = false;
	
	private boolean enableSelfRegister = false;
	
	@Editable(name="允许匿名登录",order=100, description="是否允许不登录访问服务器")
	public boolean isEnableAnonymousAccess() {
		return enableAnonymousAccess;
	}

	public void setEnableAnonymousAccess(boolean enableAnonymousAccess) {
		this.enableAnonymousAccess = enableAnonymousAccess;
	}

	@Editable(order=200, name="允许自我注册", description="若此选项开启，用户可以注册")
	public boolean isEnableSelfRegister() {
		return enableSelfRegister;
	}

	public void setEnableSelfRegister(boolean enableSelfRegister) {
		this.enableSelfRegister = enableSelfRegister;
	}

}
