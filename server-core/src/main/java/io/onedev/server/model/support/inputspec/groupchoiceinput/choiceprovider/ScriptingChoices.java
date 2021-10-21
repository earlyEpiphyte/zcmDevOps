package io.onedev.server.model.support.inputspec.groupchoiceinput.choiceprovider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.onedev.server.util.GroovyUtils;
import io.onedev.server.model.Group;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.OmitName;
import io.onedev.server.web.editable.annotation.ScriptChoice;

@Editable(order=300, name="评估脚本来获取选项")
public class ScriptingChoices implements ChoiceProvider {

	private static final long serialVersionUID = 1L;
	
	private static final Logger logger = LoggerFactory.getLogger(ScriptingChoices.class);

	private String scriptName;

	@Editable(name="脚本",description="用于评估的Groovy脚本。它应该返回存储group的数组。查看用户手册<b><i>脚本帮助</i></b>查看详情")
	@ScriptChoice
	@OmitName
	@NotEmpty(message="不能为空")
	public String getScriptName() {
		return scriptName;
	}

	public void setScriptName(String scriptName) {
		this.scriptName = scriptName;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Group> getChoices(boolean allPossible) {
		Map<String, Object> variables = new HashMap<>();
		variables.put("allPossible", allPossible);
		try {
			return (List<Group>) GroovyUtils.evalScriptByName(scriptName, variables);
		} catch (RuntimeException e) {
			if (allPossible) {
				logger.error("Error getting all possible choices", e);
				return new ArrayList<>();
			} else {
				throw e;
			}
		}
	}

}
