package io.onedev.server.plugin.authenticator.ldap;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.NameOfEmptyValue;

@Editable(name="Active Directory", order=100)
public class ActiveDirectoryAuthenticator extends LdapAuthenticator {

	private static final long serialVersionUID = 1L;

	private String groupSearchBase;
	
    @Editable(order=100, name="LDAP URL", description=
    	"指定 Active Directory 服务器的 LDAP URL, 例如: <i>ldap://ad-server</i>, or <i>ldaps://ad-server</i>")
    @NotEmpty(message="不能为空")
	@Override
	public String getLdapUrl() {
		return super.getLdapUrl();
	}

	@Override
	public void setLdapUrl(String ldapUrl) {
		super.setLdapUrl(ldapUrl);
	}

	@Editable(order=300, description=""
			+ "根据 Active Directory 对用户进行身份验证并检索关联的属性和组,"
			+ "OneDev 必须首先针对 Active Directory 服务器对自身进行身份验证，而 OneDev 通过发送 'manager' DN 和密码来做到这一点，"
			+ "'manager' DN 应该以以下形式进行指定 "
			+ "<i>&lt;account name&gt;@&lt;domain&gt;</i>, 例如: <i>onedev@example.com</i>")
	@NotEmpty(message="不能为空")
	@Override
	public String getManagerDN() {
		return super.getManagerDN();
	}

	@Override
	public void setManagerDN(String managerDN) {
		super.setManagerDN(managerDN);
	}

	@Editable(order=500, description=
		"指定用于用户搜索的基节点. 例如: <i>cn=Users, dc=example, dc=com</i>")
	@NotEmpty(message="不能为空")
	@Override
	public String getUserSearchBase() {
		return super.getUserSearchBase();
	}

	@Override
	public void setUserSearchBase(String userSearchBase) {
		super.setUserSearchBase(userSearchBase);
	}

	@Override
	public String getUserSearchFilter() {
		return "(&(sAMAccountName={0})(objectclass=user))";
	}
    
	@Override
	public void setUserSearchFilter(String userSearchFilter) {
		super.setUserSearchFilter(userSearchFilter);
	}

	@Editable(order=1000, description=""
			+ "如果要检索用户的组成员信息，可选择指定组搜索库. 例如: <i>cn=Users, dc=example, dc=com</i>. "
			+ "要为 Active Directory 组授予适当的权限，应定义一个同名的 OneDev 组. "
			+ "留空以管理 OneDev 端的组成员身份")
	@NameOfEmptyValue("不检索组")
	public String getGroupSearchBase() {
		return groupSearchBase;
	}

	public void setGroupSearchBase(String groupSearchBase) {
		this.groupSearchBase = groupSearchBase;
	}

	@Override
	public GroupRetrieval getGroupRetrieval() {
		if (getGroupSearchBase() != null) {
			SearchGroupsUsingFilter groupRetrieval = new SearchGroupsUsingFilter();
			groupRetrieval.setGroupSearchBase(getGroupSearchBase());
			groupRetrieval.setGroupSearchFilter("(&(member:1.2.840.113556.1.4.1941:={0})(objectclass=group))");
			return groupRetrieval;
		} else {
			return new DoNotRetrieveGroups();
		}
	}

}
