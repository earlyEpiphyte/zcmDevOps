package io.onedev.server.security.realm;

import java.util.concurrent.Callable;

import javax.inject.Inject;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UnknownAccountException;
import io.onedev.server.entitymanager.GroupManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.User;
import io.onedev.server.persistence.SessionManager;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.security.MyAuthenticationToken;

public class MyAuthenticationRealm extends AbstractAuthorizingRealm {
	private final TransactionManager transactionManager;
	
	@Inject
	public MyAuthenticationRealm(UserManager userManager, GroupManager groupManager, ProjectManager projectManager,
			SessionManager sessionManager, SettingManager settingManager,TransactionManager transactionManager) {
		super(userManager, groupManager, projectManager, sessionManager, settingManager);
		// TODO Auto-generated constructor stub
		this.transactionManager = transactionManager;
	}

	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
		// TODO Auto-generated method stub
		return transactionManager.call(new Callable<AuthenticationInfo>() {

			@Override
			public AuthenticationInfo call() {
				String userName = (String) token.getPrincipal();
				User user = userManager.findByName(userName);	
		    	if(user == null) {
		    		throw new UnknownAccountException();
		    	}
		    	else {
		    		MyAuthenticationToken myToken = (MyAuthenticationToken)token;
					myToken.setPassword(user.getPassword().toCharArray());
		    		return user;
		    	}
			}
		});
	}

	@Override
	public boolean supports(AuthenticationToken token) {
		return token != null && token instanceof MyAuthenticationToken;
	}
}
