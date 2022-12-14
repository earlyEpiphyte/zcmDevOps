package io.onedev.server.web.component.savedquery;

import java.io.Serializable;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.OmitName;

@Editable
public class SaveQueryBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private String name;
	
	@Editable(description="指定已保存查询的名称")
	@NotEmpty(message="不能为空")
	@OmitName
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
}