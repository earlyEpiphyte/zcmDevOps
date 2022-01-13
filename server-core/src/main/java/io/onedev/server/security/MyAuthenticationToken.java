package io.onedev.server.security;

import org.apache.shiro.authc.AuthenticationToken;

@SuppressWarnings("serial")
public class MyAuthenticationToken implements AuthenticationToken {
	private String saToken;
	
	private String username;
	
	private char[] password;
	
	public MyAuthenticationToken(final String saToken,final String username) {
		this.setSaToken(saToken);
		this.setUsername(username);
	}
	
	public MyAuthenticationToken(final String username) {
		this.setUsername(username);
	}
	
	@Override
	public Object getPrincipal() {
		// TODO Auto-generated method stub
		return username;
	}

	@Override
	public Object getCredentials() {
		// TODO Auto-generated method stub
		return getPassword();
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public char[] getPassword() {
		return password;
	}

	public void setPassword(char[] password) {
		this.password = password;
	}

	public String getSaToken() {
		return saToken;
	}

	public void setSaToken(String saToken) {
		this.saToken = saToken;
	}
}
