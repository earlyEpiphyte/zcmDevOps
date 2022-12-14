package io.onedev.server.buildspec.job;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.buildspec.BuildSpecAware;
import io.onedev.server.buildspec.param.spec.ParamSpec;
import io.onedev.server.buildspec.param.supply.ParamSupply;
import io.onedev.server.util.ComponentContext;
import io.onedev.server.util.EditContext;
import io.onedev.server.web.editable.annotation.ChoiceProvider;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Interpolative;
import io.onedev.server.web.editable.annotation.NameOfEmptyValue;
import io.onedev.server.web.editable.annotation.OmitName;
import io.onedev.server.web.editable.annotation.ParamSpecProvider;
import io.onedev.server.web.editable.annotation.Patterns;
import io.onedev.server.web.editable.annotation.VariableOption;
import io.onedev.server.web.util.WicketUtils;

@Editable
public class JobDependency implements Serializable {

	private static final long serialVersionUID = 1L;

	private String jobName;
	
	private boolean requireSuccessful = true;
	
	private List<ParamSupply> jobParams = new ArrayList<>();
	
	private String artifacts = "**";
	
	// change Named("jobName") also if change name of this property 
	@Editable(order=100, name="作业")
	@ChoiceProvider("getJobChoices")
	@NotEmpty(message="不能为空")
	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	@Editable(order=150, name="需要成功", description="是否需要此依赖项成功")
	public boolean isRequireSuccessful() {
		return requireSuccessful;
	}

	public void setRequireSuccessful(boolean requireSuccessful) {
		this.requireSuccessful = requireSuccessful;
	}

	@Editable(order=200, name="作业参数")
	@ParamSpecProvider("getParamSpecs")
	@VariableOption(withBuildVersion=false, withFile=false)
	@OmitName
	public List<ParamSupply> getJobParams() {
		return jobParams;
	}

	public void setJobParams(List<ParamSupply> jobParams) {
		this.jobParams = jobParams;
	}
	
	@Editable(order=300, name="检索的artifacts", description="（可选）指定要从依赖项检索到 "
			+ "作业工作区的工件. 空白以不检索任何工件")
	@Interpolative(variableSuggester="suggestVariables")
	@Patterns(path=true)
	@NameOfEmptyValue("不检索")
	public String getArtifacts() {
		return artifacts;
	}

	public void setArtifacts(String artifacts) {
		this.artifacts = artifacts;
	}

	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestVariables(String matchWith) {
		return BuildSpec.suggestVariables(matchWith, false, false);
	}
	
	@SuppressWarnings("unused")
	private static List<String> getJobChoices() {
		return Job.getChoices();
	}
	
	@SuppressWarnings("unused")
	private static List<ParamSpec> getParamSpecs() {
		String jobName = (String) EditContext.get().getInputValue("jobName");
		if (jobName != null) {
			Component component = ComponentContext.get().getComponent();
			BuildSpecAware buildSpecAware = WicketUtils.findInnermost(component, BuildSpecAware.class);
			if (buildSpecAware != null) {
				BuildSpec buildSpec = buildSpecAware.getBuildSpec();
				if (buildSpec != null) {
					Job job = buildSpec.getJobMap().get(jobName);
					if (job != null)
						return job.getParamSpecs();
				}
			}
		} 
		return new ArrayList<>();
	}

}
