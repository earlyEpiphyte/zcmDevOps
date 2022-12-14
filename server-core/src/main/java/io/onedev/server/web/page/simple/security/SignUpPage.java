package io.onedev.server.web.page.simple.security;

import org.apache.shiro.authc.credential.PasswordService;
import org.apache.shiro.authz.UnauthenticatedException;
import org.apache.wicket.Session;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.commons.launcher.loader.AppLoader;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.User;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.Path;
import io.onedev.server.util.PathNode;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.page.my.avatar.MyAvatarPage;
import io.onedev.server.web.page.project.ProjectListPage;
import io.onedev.server.web.page.simple.SimplePage;

@SuppressWarnings("serial")
public class SignUpPage extends SimplePage {
	
	public SignUpPage(PageParameters params) {
		super(params);
		
		if (!OneDev.getInstance(SettingManager.class).getSecuritySetting().isEnableSelfRegister())
			throw new UnauthenticatedException("用户注册被禁用");
		if (getLoginUser() != null)
			throw new IllegalStateException("用户登录后无法注册");
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
	
		User user = new User();
		BeanEditor editor = BeanContext.edit("editor", user);
		
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				
				UserManager userManager = OneDev.getInstance(UserManager.class);
				User userWithSameName = userManager.findByName(user.getName());
				if (userWithSameName != null) {
					editor.error(new Path(new PathNode.Named("name")),
							"此名称已被其他用户使用");
				} 
				User userWithSameEmail = userManager.findByEmail(user.getEmail());
				if (userWithSameEmail != null) {
					editor.error(new Path(new PathNode.Named("email")),
							"此邮箱已被其他用户使用");
				} 
				if (editor.isValid()) {
					user.setPassword(AppLoader.getInstance(PasswordService.class).encryptPassword(user.getPassword()));
					userManager.save(user, null);
					Session.get().success("Welcome to OneDev");
					SecurityUtils.getSubject().runAs(user.getPrincipals());
					setResponsePage(MyAvatarPage.class);
				}
			}
			
		};
		form.add(editor);
		
		form.add(new SubmitLink("save"));
		form.add(new Link<Void>("cancel") {

			@Override
			public void onClick() {
				setResponsePage(ProjectListPage.class);
			}
			
		});
		add(form);
	}

	@Override
	protected String getTitle() {
		return "注 册";
	}

	@Override
	protected String getSubTitle() {
		return "输入您的详细信息来创建您的帐户";
	}

}
