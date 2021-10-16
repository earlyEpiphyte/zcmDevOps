package io.onedev.server.buildspec.step;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.k8shelper.CommandExecutable;
import io.onedev.k8shelper.Executable;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.buildspec.param.ParamCombination;
import io.onedev.server.model.Build;
import io.onedev.server.web.editable.annotation.Code;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Interpolative;

@Editable(order=10, name="执行Shell/批处理命令")
public class CommandStep extends Step {

	private static final long serialVersionUID = 1L;

	private String image;
	
	private List<String> commands = new ArrayList<>();
	
	@Editable(order=100, name="镜像",description="指定docker镜像并在里面执行命令")
	@Interpolative(variableSuggester="suggestVariables")
	@NotEmpty
	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	@Editable(order=110,name="命令",description="指定在<a href='$docRoot/pages/concepts.md#job-workspace'>作业工作空间</a>执行的 Linux shell 脚本或 Windows 命令批处理的内容")
	@Interpolative
	@Code(language=Code.SHELL, variableProvider="suggestCommandVariables")
	@Size(min=1, message="不能为空")
	public List<String> getCommands() {
		return commands;
	}

	public void setCommands(List<String> commands) {
		this.commands = commands;
	}
	
	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestVariables(String matchWith) {
		return BuildSpec.suggestVariables(matchWith, false, false);
	}
	
	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestCommandVariables(String matchWith) {
		return BuildSpec.suggestVariables(matchWith, true, true);
	}
	
	@Override
	public Executable getExecutable(Build build, String jobToken, ParamCombination paramCombination) {
		return new CommandExecutable(getImage(), getCommands());
	}
	
}
