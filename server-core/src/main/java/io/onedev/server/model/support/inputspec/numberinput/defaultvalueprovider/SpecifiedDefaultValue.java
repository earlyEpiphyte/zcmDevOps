package io.onedev.server.model.support.inputspec.numberinput.defaultvalueprovider;

import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.OmitName;

@Editable(order=100, name="使用指定的默认值")
public class SpecifiedDefaultValue implements DefaultValueProvider {

	private static final long serialVersionUID = 1L;

	private int value;

	@Editable(name="指定默认值")
	@OmitName
	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	@Override
	public int getDefaultValue() {
		return getValue();
	}

}
