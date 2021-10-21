package io.onedev.server.model.support.inputspec.booleaninput.defaultvalueprovider;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.util.GroovyUtils;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.OmitName;
import io.onedev.server.web.editable.annotation.ScriptChoice;

@Editable(order=400, name="评估脚本来获取默认值")
public class ScriptingDefaultValue implements DefaultValueProvider {

	private static final long serialVersionUID = 1L;

	private String scriptName;

	@Editable(name="脚本",description="用于评估的Groovy脚本。它应该返回<i>boolean</i>值。查看用户手册<b><i>脚本帮助</i></b>查看详情")
	@ScriptChoice
	@OmitName
	@NotEmpty(message="不能为空")
	public String getScriptName() {
		return scriptName;
	}

	public void setScriptName(String scriptName) {
		this.scriptName = scriptName;
	}

	@Override
	public boolean getDefaultValue() {
		return (boolean) GroovyUtils.evalScriptByName(scriptName);
	}

}
