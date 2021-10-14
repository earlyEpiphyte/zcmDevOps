package io.onedev.server.model.support.issue;

import java.io.Serializable;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.web.editable.annotation.Color;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.NameOfEmptyValue;

@Editable
public class StateSpec implements Serializable {

	private static final long serialVersionUID = 1L;

	private String name;
	
	private String description;
	
	private String color = "#0d87e9";
	
	@Editable(order=100,name="状态名")
	@NotEmpty
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Editable(order=200,name="描述")
	@NameOfEmptyValue("没有说明")
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Editable(order=400, description="指定状态的颜色",name="颜色")
	@Color
	@NotEmpty(message="为这种状态选择一种颜色")
	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

}
