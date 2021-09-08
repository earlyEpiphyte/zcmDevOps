package io.onedev.server.model.support.issue.field.spec;

import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import io.onedev.server.model.support.inputspec.numberinput.NumberInput;
import io.onedev.server.model.support.inputspec.numberinput.defaultvalueprovider.DefaultValueProvider;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.NameOfEmptyValue;

@Editable(order=400, name=FieldSpec.NUMBER)
public class NumberField extends FieldSpec {
	
	private static final long serialVersionUID = 1L;

	private Integer minValue;
	
	private Integer maxValue;
	
	private DefaultValueProvider defaultValueProvider;
	
	@Editable(order=1000, name="小值", description="可选择的指定最小值")
	public Integer getMinValue() {
		return minValue;
	}

	public void setMinValue(Integer minValue) {
		this.minValue = minValue;
	}

	@Editable(order=1100, name="大值", description="可选择的指定最大值")
	public Integer getMaxValue() {
		return maxValue;
	}

	public void setMaxValue(Integer maxValue) {
		this.maxValue = maxValue;
	}

	@Editable(order=1200, name="默认值")
	@NameOfEmptyValue("无默认值")
	@Valid
	public DefaultValueProvider getDefaultValueProvider() {
		return defaultValueProvider;
	}

	public void setDefaultValueProvider(DefaultValueProvider defaultValueProvider) {
		this.defaultValueProvider = defaultValueProvider;
	}

	@Override
	public String getPropertyDef(Map<String, Integer> indexes) {
		return NumberInput.getPropertyDef(this, indexes, minValue, maxValue, defaultValueProvider);
	}

	@Override
	public Object convertToObject(List<String> strings) {
		return NumberInput.convertToObject(strings);
	}

	@Override
	public List<String> convertToStrings(Object value) {
		return NumberInput.convertToStrings(value);
	}

	@Editable
	@Override
	public boolean isAllowMultiple() {
		return false;
	}

	@Override
	public long getOrdinal(String fieldValue) {
		if (fieldValue != null)
			return Integer.parseInt(fieldValue);
		else
			return super.getOrdinal(fieldValue);
	}
	
}
