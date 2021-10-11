package io.onedev.server.model.support.build;

import java.io.Serializable;

import io.onedev.server.web.editable.annotation.BuildQuery;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.NameOfEmptyValue;

@Editable
public class BuildPreservation implements Serializable {

	private static final long serialVersionUID = 1L;

	private String condition;
	
	private Integer count;

	@Editable(order=100,name="条件", description="指定保留构建必须匹配的条件")
	@BuildQuery(withOrder = false, withCurrentUserCriteria = false, withUnfinishedCriteria = false)
	@NameOfEmptyValue("全部")
	public String getCondition() {
		return condition;
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}

	@Editable(order=200, name="数量",description="要保留的构建数量")
	@NameOfEmptyValue("无限制")
	public Integer getCount() {
		return count;
	}

	public void setCount(Integer count) {
		this.count = count;
	}
	
}
