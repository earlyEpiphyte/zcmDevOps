package io.onedev.server.web.page.simple.security;

import java.util.Map;

import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.eclipse.jetty.util.ajax.JSON;

import io.onedev.server.util.HttpRequest;
import io.onedev.server.web.WebSession;
import io.onedev.server.web.page.simple.SimplePage;

@SuppressWarnings("serial")
public class ssoPage extends SimplePage{
	private String ticket;
	
	public ssoPage(PageParameters params) {
		super(params);
		ticket = params.get("ticket").toString();
		Map<?, ?> result = (Map<?, ?>) JSON.parse(HttpRequest.sendGet(
				"http://59.69.105.174:8080/doLoginByTicket","ticket=" + ticket));
		if((long)result.get("code") == 200L) {
			String saToken = (String)result.get("data");//获取satoken
			WebSession.get().setAttribute("satoken", saToken);
			Map<?, ?> userInfo = (Map<?, ?>) JSON.parse(HttpRequest.sendGet(
					"http://59.69.105.174:8080/sso/myinfo",null));
			System.out.println(userInfo.get("data"));
			//SecurityUtils.getSubject().login(new MyAuthenticationToken(username, saToken));
			
			//setResponsePage(ProjectListPage.class);
		}
		
	}

	
	@Override
	protected void onInitialize() {
		// TODO Auto-generated method stub
		super.onInitialize();
	}


	@Override
	protected String getTitle() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String getSubTitle() {
		// TODO Auto-generated method stub
		return null;
	}
}
