package io.onedev.server.git.config;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.web.editable.annotation.Editable;

@Editable(name="使用指定的curl", order=200)
public class SpecifiedCurl extends CurlConfig {

	private static final long serialVersionUID = 1L;
	
	private String curlPath;
	
	@Editable(name="curl路径",description="指定curl可执行的路径,例如: <tt>/usr/bin/curl</tt>")
	@NotEmpty(message="不能为空")
	public String getCurlPath() {
		return curlPath;
	}

	public void setCurlPath(String curlPath) {
		this.curlPath = curlPath;
	}

	@Override
	public String getExecutable() {
		return curlPath;
	}

}
