package io.onedev.server.model.support.inputspec.groupchoiceinput.defaultvalueprovider;

import java.util.List;

import javax.validation.Validator;

import org.hibernate.validator.constraints.NotEmpty;

import com.google.common.collect.Lists;

import io.onedev.server.OneDev;
import io.onedev.server.util.EditContext;
import io.onedev.server.model.Group;
import io.onedev.server.model.support.inputspec.groupchoiceinput.choiceprovider.ChoiceProvider;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.GroupChoice;
import io.onedev.server.web.editable.annotation.OmitName;

@Editable(order=100, name="使用指定的默认值")
public class SpecifiedDefaultValue implements DefaultValueProvider {

	private static final long serialVersionUID = 1L;

	private String value;

	@Editable(name="字面意义的默认值")
	@GroupChoice("getValueChoices")
	@NotEmpty(message="不能为空")
	@OmitName
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String getDefaultValue() {
		return getValue();
	}

	@SuppressWarnings("unused")
	private static List<Group> getValueChoices() {
		ChoiceProvider choiceProvider = (ChoiceProvider) EditContext.get(1).getInputValue("choiceProvider");
		if (choiceProvider != null && OneDev.getInstance(Validator.class).validate(choiceProvider).isEmpty())
			return choiceProvider.getChoices(true);
		else
			return Lists.newArrayList();
	}
	
}
