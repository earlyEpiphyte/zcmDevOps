package io.onedev.server.model.support.inputspec.choiceinput.defaultvalueprovider;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Validator;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.OneDev;
import io.onedev.server.model.support.inputspec.choiceinput.choiceprovider.ChoiceProvider;
import io.onedev.server.util.EditContext;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.OmitName;

@Editable(order=100, name="使用指定的默认值")
public class SpecifiedDefaultValue implements DefaultValueProvider {

	private static final long serialVersionUID = 1L;

	private String value;

	@Editable(name="字面意义的默认值")
	@io.onedev.server.web.editable.annotation.ChoiceProvider("getValueChoices")
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
	private static List<String> getValueChoices() {
		ChoiceProvider choiceProvider = (ChoiceProvider) EditContext.get(1).getInputValue("choiceProvider");
		if (choiceProvider != null && OneDev.getInstance(Validator.class).validate(choiceProvider).isEmpty()) 
			return new ArrayList<>(choiceProvider.getChoices(true).keySet());
		else
			return new ArrayList<>();
	}
	
}
