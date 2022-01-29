package io.onedev.server.web.page.simple.security;

import org.apache.wicket.RestartResponseException;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.util.HttpRequest;
import io.onedev.server.web.WebSession;
import io.onedev.server.web.page.base.BasePage;

@SuppressWarnings("serial")
public class LogoutPage extends BasePage {

	public LogoutPage(PageParameters params) {
		super(params);

		Long loginId = (Long) WebSession.get().getAttribute("loginId");
		HttpRequest.sendGet("http://localhost:8443/sso/logout",
				"loginId=" + loginId + "&secretkey=kQwIOrYvnXmSDkwEiFngrKidMcdrgKor");
		
		WebSession.get().logout();
		
		
		if (getLoginUser() != null || OneDev.getInstance(SettingManager.class).getSecuritySetting().isEnableAnonymousAccess())
			getSession().warn("您已经注销了");
        
		if (OneDev.getInstance(SettingManager.class).getSecuritySetting().isEnableAnonymousAccess())
			throw new RestartResponseException(getApplication().getHomePage());
		else
			throw new RestartResponseException(LoginPage.class);

	}
	
}
