package io.onedev.server.web.page.simple.security;

import org.apache.shiro.SecurityUtils;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.eclipse.jetty.util.ajax.JSON;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.security.MyAuthenticationToken;
import io.onedev.server.util.HttpRequest;
import io.onedev.server.web.WebSession;
import io.onedev.server.web.page.simple.SimpleCssResourceReference;
import io.onedev.server.web.page.simple.SimplePage;
import redis.clients.jedis.Jedis;

@SuppressWarnings("serial")
public class LoginPage extends SimplePage {
		
	private String ticket;
	
	public LoginPage(PageParameters params) {
		super(params);
		this.ticket = params.get("ticket").toString();
		if (SecurityUtils.getSubject().isAuthenticated())
			throw new RestartResponseException(getApplication().getHomePage());
	}

	public LoginPage(String errorMessage) {
		super(new PageParameters());
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		if(ticket != null && !"".equals(ticket)) {//sso认证中心已登录
			Long loginId = (Long) JSON.parse(HttpRequest.sendGet(
					"http://localhost:8443/sso/checkTicket","ticket=" + ticket));
			if(loginId != null) {
				Jedis jedis = new Jedis("localhost");
				String sessionInfo = jedis.get("satoken:login:session:" + loginId);
				JSONObject jsonObject = JSONObject.parseObject(sessionInfo);
				String loginName = jsonObject.getJSONObject("dataMap").getString("name");
				JSONArray array = (JSONArray) jsonObject.getJSONArray("tokenSignList").get(1);
				String saToken = ((JSONObject) array.get(0)).getString("value");
				jedis.close();
				WebSession.get().login(new MyAuthenticationToken(saToken, loginName));
				WebSession.get().setAttribute("loginId", loginId);
				continueToOriginalDestination();
				throw new RestartResponseAtInterceptPageException(getApplication().getHomePage());
			}
			
		}

		SettingManager settingManager = OneDev.getInstance(SettingManager.class);

		String serverUrl = settingManager.getSystemSetting().getServerUrl();
		serverUrl = "http://localhost:6610";
		ExternalLink ssoButton = new ExternalLink("ssoButtons", 
				Model.of("http://localhost:8443/sso/auth?redirect=" + serverUrl + "/login"));
		ssoButton.add(new Label("label", "5G+工业互联网公共服务平台统一认证中心登录"));
		add(ssoButton);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new SimpleCssResourceReference()));
	}

	@Override
	protected String getTitle() {
		return "登录";
	}

	@Override
	protected String getSubTitle() {
		return "";
	}

}