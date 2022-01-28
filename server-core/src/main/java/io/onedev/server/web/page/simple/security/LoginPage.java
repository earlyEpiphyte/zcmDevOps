package io.onedev.server.web.page.simple.security;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.StatelessForm;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.alibaba.fastjson.JSONObject;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.web.WebSession;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.page.simple.SimpleCssResourceReference;
import io.onedev.server.web.page.simple.SimplePage;
import redis.clients.jedis.Jedis;

@SuppressWarnings("serial")
public class LoginPage extends SimplePage {

	private String userName;

	private String password;

	private boolean rememberMe;

	private String errorMessage;
	
	private String back;
	
	private String ticket;
	
	public LoginPage(PageParameters params) {
		super(params);
		this.back = params.get("back").toString();
		this.ticket = params.get("ticket").toString();
		if (SecurityUtils.getSubject().isAuthenticated())
			throw new RestartResponseException(getApplication().getHomePage());
	}

	public LoginPage(String errorMessage) {
		super(new PageParameters());
		this.errorMessage = errorMessage;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		if(back != null && !"".equals(back) && ticket != null && !"".equals(ticket)) {//sso认证中心已登录
			Jedis jedis = new Jedis("localhost");
			String sessionId = jedis.get("satoken:ticket:" + ticket);
			String sessionInfo = jedis.get("satoken:login:session:" + sessionId);
			JSONObject jsonObject = JSONObject.parseObject(sessionInfo);
			String loginName = jsonObject.getJSONObject("dataMap").getString("name");
			System.out.println(loginName);
			jedis.close();
		}

		StatelessForm<?> form = new StatelessForm<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				try {
					WebSession.get().login(new UsernamePasswordToken(userName, password, rememberMe));
					continueToOriginalDestination();
					setResponsePage(getApplication().getHomePage());
				} catch (IncorrectCredentialsException e) {
					error("密码不正确");
				} catch (UnknownAccountException e) {
					error("未知的用户名");
				} catch (AuthenticationException ae) {
					error(ae.getMessage());
				}
			}

		};

		form.add(new FencedFeedbackPanel("feedback"));

		if (errorMessage != null)
			form.error(errorMessage);

		form.add(new TextField<String>("userName", new IModel<String>() {

			@Override
			public void detach() {
			}

			@Override
			public String getObject() {
				return userName;
			}

			@Override
			public void setObject(String object) {
				userName = object;
			}

		}).setLabel(Model.of("User name")).setRequired(true));

		form.add(new PasswordTextField("password", new IModel<String>() {

			@Override
			public void detach() {
			}

			@Override
			public String getObject() {
				return password;
			}

			@Override
			public void setObject(String object) {
				password = object;
			}

		}).setLabel(Model.of("Password")).setRequired(true));

		form.add(new CheckBox("rememberMe", new IModel<Boolean>() {

			@Override
			public void detach() {
			}

			@Override
			public Boolean getObject() {
				return rememberMe;
			}

			@Override
			public void setObject(Boolean object) {
				rememberMe = object;
			}

		}));

		form.add(new ViewStateAwarePageLink<Void>("forgetPassword", PasswordResetPage.class) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(OneDev.getInstance(SettingManager.class).getMailSetting() != null);
			}

		});

		add(form);

		SettingManager settingManager = OneDev.getInstance(SettingManager.class);

		boolean enableSelfRegister = settingManager.getSecuritySetting().isEnableSelfRegister();

		add(new ViewStateAwarePageLink<Void>("registerUser", SignUpPage.class).setVisible(enableSelfRegister));

		String serverUrl = settingManager.getSystemSetting().getServerUrl();
		serverUrl = "http://localhost:6610";
		ExternalLink ssoButton = new ExternalLink("ssoButtons", 
				Model.of("http://localhost:8443/sso/auth?redirect=" + serverUrl + "/login"
						+ "?back=" + serverUrl));
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
		return "输入详细信息以登录账户";
	}

}