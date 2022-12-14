package io.onedev.server.git.config;

import io.onedev.server.web.editable.annotation.Editable;

@Editable(name="使用系统默认git路径", order=100)
public class SystemGit extends GitConfig {

	private static final long serialVersionUID = 1L;

	@Override
	public String getExecutable() {
		return "git";
	}

}
