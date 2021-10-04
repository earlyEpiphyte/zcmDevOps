package io.onedev.server.web.page.admin.user.password;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.model.User;
import io.onedev.server.web.component.user.passwordedit.PasswordEditPanel;
import io.onedev.server.web.page.admin.user.UserPage;

@SuppressWarnings("serial")
public class UserPasswordPage extends UserPage {
	
	public UserPasswordPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		if (getUser().getPassword().equals(User.EXTERNAL_MANAGED)) {
			String message;
			if (getUser().getSsoInfo().getConnector() != null) {
				message = "用户当前已通过 SSO 提供商进行身份验证 '" 
						+ getUser().getSsoInfo().getConnector() 
						+ "', 请更改密码";
			} else {
				message = "用户当前已通过外部系统进行身份验证, "
						+ "请更改密码";
			}
			add(new Label("content", message).add(AttributeAppender.append("class", "alert alert-light-warning alert-notice mb-0")));
		} else {
			add(new PasswordEditPanel("content", userModel));
		}
	}

}
