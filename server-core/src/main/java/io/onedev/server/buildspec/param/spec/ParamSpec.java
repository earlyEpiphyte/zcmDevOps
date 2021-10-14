package io.onedev.server.buildspec.param.spec;

import java.util.List;

import javax.annotation.Nullable;
import javax.validation.Valid;

import org.apache.wicket.Component;
import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.buildspec.ParamSpecAware;
import io.onedev.server.model.support.inputspec.InputSpec;
import io.onedev.server.model.support.inputspec.showcondition.ShowCondition;
import io.onedev.server.util.ComponentContext;
import io.onedev.server.util.validation.annotation.ParamName;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.NameOfEmptyValue;
import io.onedev.server.web.util.WicketUtils;

@Editable
public abstract class ParamSpec extends InputSpec {
	
	private static final long serialVersionUID = 1L;
	
	@Editable(order=10,name="参数名")
	@ParamName
	@NotEmpty
	@Override
	public String getName() {
		return super.getName();
	}

	@Override
	public void setName(String name) {
		super.setName(name);
	}

	@Editable(order=30,name="描述", description="可选地描述参数")
	@NameOfEmptyValue("没有说明")
	@Override
	public String getDescription() {
		return super.getDescription();
	}

	@Override
	public void setDescription(String description) {
		super.setDescription(description);
	}

	@Editable(order=35,name="允许多值", description="是否可以为此参数指定多个值")
	@Override
	public boolean isAllowMultiple() {
		return super.isAllowMultiple();
	}

	@Override
	public void setAllowMultiple(boolean allowMultiple) {
		super.setAllowMultiple(allowMultiple);
	}

	@Editable(order=40, name="有条件地显示", description="如果此参数的可见性取决于其他参数，则启用")
	@NameOfEmptyValue("总是")
	@Valid
	@Override
	public ShowCondition getShowCondition() {
		return super.getShowCondition();
	}

	@Override
	public void setShowCondition(ShowCondition showCondition) {
		super.setShowCondition(showCondition);
	}
	
	@Editable(order=50, name="允许空值", description="此参数是否接受空值")
	@Override
	public boolean isAllowEmpty() {
		return super.isAllowEmpty();
	}

	@Override
	public void setAllowEmpty(boolean allowEmpty) {
		super.setAllowEmpty(allowEmpty);
	}

	@Nullable
	public static List<ParamSpec> list() {
		Component component = ComponentContext.get().getComponent();
		ParamSpecAware paramSpecAware = WicketUtils.findInnermost(component, ParamSpecAware.class);
		if (paramSpecAware != null) 
			return paramSpecAware.getParamSpecs();
		else
			return null;
	}
	
	
}
