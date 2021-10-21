package io.onedev.server.model.support;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.web.editable.annotation.CodeCommentQuery;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.NameOfEmptyValue;

@Editable
public class NamedCodeCommentQuery implements NamedQuery {
	
	private static final long serialVersionUID = 1L;

	private String name;
	
	private String query;
	
	public NamedCodeCommentQuery(String name, String query) {
		this.name = name;
		this.query = query;
	}
	
	public NamedCodeCommentQuery() {
	}

	@Editable(name="查询名称")
	@NotEmpty(message="不能为空")
	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Editable(name="查询语句")
	@CodeCommentQuery
	@NameOfEmptyValue("所有")
	@Override
	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

}