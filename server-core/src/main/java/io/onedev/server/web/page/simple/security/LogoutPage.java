package io.onedev.server.web.page.simple.security;

import java.io.IOException;

import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.web.WebSession;
import io.onedev.server.web.page.base.BasePage;

@SuppressWarnings("serial")
public class LogoutPage extends BasePage {

	public LogoutPage(PageParameters params) {
		super(params);

		String loginId = (String) WebSession.get().getAttribute("loginId");
		
		Request request = Request.Post("http://172.168.1.44:8443/sso/logout");
		String body = "loginId=" + loginId + "&secretkey=kQwIOrYvnXmSDkwEiFngrKidMcdrgKor";
		request.bodyString(body,ContentType.APPLICATION_FORM_URLENCODED);
		request.setHeader("User-Agent", "Apipost client Runtime/+https://www.apipost.cn/");
		request.setHeader("Content-Type", "application/x-www-form-urlencoded");
		try {
			request.execute();
		}catch (IOException e) {
			// TODO: handle exception
		}
		
		WebSession.get().logout();
		
		
		if (getLoginUser() != null || OneDev.getInstance(SettingManager.class).getSecuritySetting().isEnableAnonymousAccess())
			getSession().warn("您已经注销了");
        
		if (OneDev.getInstance(SettingManager.class).getSecuritySetting().isEnableAnonymousAccess())
			throw new RestartResponseException(getApplication().getHomePage());
		else
			throw new RestartResponseException(LoginPage.class);

	}
	
}
