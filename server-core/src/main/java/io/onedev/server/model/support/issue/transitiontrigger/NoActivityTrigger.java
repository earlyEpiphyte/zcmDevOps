package io.onedev.server.model.support.issue.transitiontrigger;

import javax.validation.constraints.Min;

import io.onedev.server.web.editable.annotation.Editable;

@Editable(order=600, name="一段时间无事件")
public class NoActivityTrigger extends TransitionTrigger {

	private static final long serialVersionUID = 1L;

	private int days;

	@Editable(order=200, name="无事件的天数")
	@Min(1)
	public int getDays() {
		return days;
	}

	public void setDays(int days) {
		this.days = days;
	}
	
	@Override
	public String getDescription() {
		return "" + days + "天无事件"; 
	}
	
}
