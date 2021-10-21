package io.onedev.server.model.support.inputspec.choiceinput.choiceprovider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.util.GroovyUtils;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.OmitName;
import io.onedev.server.web.editable.annotation.ScriptChoice;

@Editable(order=300, name="评估脚本来获取选项")
public class ScriptingChoices extends ChoiceProvider {

	private static final long serialVersionUID = 1L;
	
	private static final Logger logger = LoggerFactory.getLogger(ScriptingChoices.class);

	private String scriptName;

	@Editable(name="脚本",description="用于评估的Groovy脚本。它应该返回存储颜色的map，"
			+ "例如:<br>"
			+ "<code>return [\"Successful\":\"#00ff00\", \"Failed\":\"#ff0000\"]</code>, null代表无颜色。"
			+ "查看用户手册<b><i>脚本帮助</i></b>查看详情")
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
	public Map<String, String> getChoices(boolean allPossible) {
		Map<String, Object> variables = new HashMap<>();
		variables.put("allPossible", allPossible);
		
		try {
			Object result = GroovyUtils.evalScriptByName(scriptName, variables);
			if (result instanceof Map) {
				return (Map<String, String>) result;
			} else if (result instanceof List) {
				Map<String, String> choices = new HashMap<>();
				for (String item: (List<String>)result)
					choices.put(item, null);
				return choices;
			} else {
				throw new ExplicitException("Script should return either a Map or a List");
			}
		} catch (RuntimeException e) {
			if (allPossible) {
				logger.error("Error getting all possible choices", e);
				return new HashMap<>();
			} else {
				throw e;
			}
		}
	}

}
