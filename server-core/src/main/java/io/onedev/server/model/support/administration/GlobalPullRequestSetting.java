package io.onedev.server.model.support.administration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import io.onedev.server.model.support.pullrequest.NamedPullRequestQuery;

public class GlobalPullRequestSetting implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private List<NamedPullRequestQuery> namedQueries = new ArrayList<>();
	
	public GlobalPullRequestSetting() {
		namedQueries.add(new NamedPullRequestQuery("开放的", "open"));
		namedQueries.add(new NamedPullRequestQuery("由我改变的", "submitted by me and someone requested for changes"));
		namedQueries.add(new NamedPullRequestQuery("由我提交的", "submitted by me"));
		namedQueries.add(new NamedPullRequestQuery("最近提交的", "\"Submit Date\" is since \"last week\""));
		namedQueries.add(new NamedPullRequestQuery("最近更新的", "\"Update Date\" is since \"last week\""));
		namedQueries.add(new NamedPullRequestQuery("已关闭的", "merged or discarded"));
		namedQueries.add(new NamedPullRequestQuery("所有", null));
	}
	
	public List<NamedPullRequestQuery> getNamedQueries() {
		return namedQueries;
	}

	public void setNamedQueries(List<NamedPullRequestQuery> namedQueries) {
		this.namedQueries = namedQueries;
	}
	
	@Nullable
	public NamedPullRequestQuery getNamedQuery(String name) {
		for (NamedPullRequestQuery namedQuery: getNamedQueries()) {
			if (namedQuery.getName().equals(name))
				return namedQuery;
		}
		return null;
	}
	
}
