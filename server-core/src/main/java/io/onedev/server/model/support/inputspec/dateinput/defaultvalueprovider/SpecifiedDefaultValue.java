package io.onedev.server.model.support.inputspec.dateinput.defaultvalueprovider;

import java.util.Date;

import javax.validation.constraints.NotNull;

import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.OmitName;

@Editable(order=100, name="使用指定的默认值")
public class SpecifiedDefaultValue implements DefaultValueProvider {

	private static final long serialVersionUID = 1L;

	private Date value;

	@Editable(name="指定默认值")
	@NotNull(message="不能为空")
	@OmitName
	public Date getValue() {
		return value;
	}

	public void setValue(Date value) {
		this.value = value;
	}

	@Override
	public Date getDefaultValue() {
		return getValue();
	}

}
