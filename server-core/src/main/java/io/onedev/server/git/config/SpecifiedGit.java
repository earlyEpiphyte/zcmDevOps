package io.onedev.server.git.config;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.web.editable.annotation.Editable;

@Editable(name="使用指定的git", order=200)
public class SpecifiedGit extends GitConfig {

	private static final long serialVersionUID = 1L;
	
	private String gitPath;
	
	@Editable(name="git路径",description="指定git可执行的路径,例如: <tt>/usr/bin/git</tt>")
	@NotEmpty(message="不能为空")
	public String getGitPath() {
		return gitPath;
	}

	public void setGitPath(String gitPath) {
		this.gitPath = gitPath;
	}

	@Override
	public String getExecutable() {
		return gitPath;
	}

}
