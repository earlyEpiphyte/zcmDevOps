package io.onedev.server.buildspec.step;

import javax.validation.constraints.NotNull;

import io.onedev.k8shelper.CheckoutExecutable;
import io.onedev.k8shelper.Executable;
import io.onedev.server.buildspec.job.gitcredential.DefaultCredential;
import io.onedev.server.buildspec.job.gitcredential.GitCredential;
import io.onedev.server.buildspec.param.ParamCombination;
import io.onedev.server.model.Build;
import io.onedev.server.web.editable.annotation.Editable;

@Editable(order=5, name="检查代码")
public class CheckoutStep extends Step {

	private static final long serialVersionUID = 1L;

	private GitCredential cloneCredential = new DefaultCredential();
	
	private Integer cloneDepth;
	
	@Editable(order=100, name="克隆凭证", description="默认情况下，代码是通过自动生成的凭据克隆的, "
			+ "只对当前项目有读权限. 如果作业需要将代码推送到服务器, 或者想要克隆私有子模块, 您应该提供具有适当权限的自定义凭证")
	@NotNull
	public GitCredential getCloneCredential() {
		return cloneCredential;
	}

	public void setCloneCredential(GitCredential cloneCredential) {
		this.cloneCredential = cloneCredential;
	}

	@Editable(order=200, name="克隆深度",description="可选地为浅层克隆指定深度以加快源检索")
	public Integer getCloneDepth() {
		return cloneDepth;
	}

	public void setCloneDepth(Integer cloneDepth) {
		this.cloneDepth = cloneDepth;
	}

	@Override
	public Executable getExecutable(Build build, String jobToken, ParamCombination paramCombination) {
		return new CheckoutExecutable(cloneDepth!=null?cloneDepth:0, cloneCredential.newCloneInfo(build, jobToken));
	}

}
