package io.onedev.server.web.page.simple.security;

import java.io.IOException;

import org.apache.shiro.SecurityUtils;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import io.onedev.server.security.MyAuthenticationToken;
import io.onedev.server.web.WebSession;
import io.onedev.server.web.page.simple.SimpleCssResourceReference;
import io.onedev.server.web.page.simple.SimplePage;
import redis.clients.jedis.Jedis;

import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.http.util.EntityUtils; 

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
			Request request = Request.Post("http://172.168.1.44:8443/sso/checkTicket");
			String body = "ticket=" + ticket;
			request.bodyString(body,ContentType.APPLICATION_FORM_URLENCODED);
			request.setHeader("User-Agent", "Apipost client Runtime/+https://www.apipost.cn/");
			request.setHeader("Content-Type", "application/x-www-form-urlencoded");
			try {
				HttpResponse httpResponse = request.execute().returnResponse();
				if (httpResponse.getEntity() != null) {
					String loginId = EntityUtils.toString(httpResponse.getEntity());
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
			} catch (IOException e) {
				// TODO: handle exception
			}
			
		}

		ExternalLink ssoButton = new ExternalLink("ssoButtons", 
				Model.of("http://111.4.83.55:8443/sso/auth?redirect=http://111.4.83.55:46610/login"));
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