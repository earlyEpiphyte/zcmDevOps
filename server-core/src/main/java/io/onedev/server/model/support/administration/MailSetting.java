package io.onedev.server.model.support.administration;

import java.io.Serializable;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Password;

@Editable
public class MailSetting implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private String smtpHost;
	
	private int smtpPort = 587;
	
	private boolean enableStartTLS = true;
	
	private boolean sendAsHtml = true;
	
	private String smtpUser;
	
	private String smtpPassword;
	
	private String senderAddress;
	
	private int timeout = 60;

	@Editable(order=100, name="SMTP主机", description=
		"用于发送电子邮件的 SMTP主机."
		)
	@NotEmpty(message="不能为空")
	public String getSmtpHost() {
		return smtpHost;
	}

	public void setSmtpHost(String smtpHost) {
		this.smtpHost = smtpHost;
	}

	@Editable(order=200, name="SMTP端口")
	public int getSmtpPort() {
		return smtpPort;
	}

	public void setSmtpPort(int smtpPort) {
		this.smtpPort = smtpPort;
	}

	@Editable(order=250, name="启用 STARTTLS", description="是否端口上启用 STARTTLS")
	public boolean isEnableStartTLS() {
		return enableStartTLS;
	}

	public void setEnableStartTLS(boolean enableStartTLS) {
		this.enableStartTLS = enableStartTLS;
	}

	@Editable(order=260, name="以 HTML格式发送", description=
			"如果选中，邮件将以 html格式发送。否则为纯文本格式")
	public boolean isSendAsHtml() {
		return sendAsHtml;
	}

	public void setSendAsHtml(boolean sendAsHtml) {
		this.sendAsHtml = sendAsHtml;
	}

	@Editable(order=300, name="SMTP用户", description=
		"如果 SMTP主机需要身份验证，可选择在此处指定用户名"
		)
	public String getSmtpUser() {
		return smtpUser;
	}

	public void setSmtpUser(String smtpUser) {
		this.smtpUser = smtpUser;
	}

	@Editable(order=400, name="SMTP密码", description=
		"如果 SMTP主机需要身份验证，可选择在此处指定密码"
		)
	@Password(autoComplete="new-password")
	public String getSmtpPassword() {
		return smtpPassword;
	}

	public void setSmtpPassword(String smtpPassword) {
		this.smtpPassword = smtpPassword;
	}

	@Editable(order=500,name="发件人地址", description=
		"此属性是可选的。 如果指定，在发送电子邮件时使用此电子邮件作为发件人地址。 否则, 发件人地址将是" +
		"<b>test@&lt;hostname&gt;</b>, 其中 &lt;hostname&gt; " +
		"是服务器的主机名."
		)
	public String getSenderAddress() {
		return senderAddress;
	}

	public void setSenderAddress(String senderAddress) {
		this.senderAddress = senderAddress;
	}

	@Editable(order=600, name="超时时间", description="指定与 SMTP服务器通信时的超时时间（以秒为单位）. " +
			"设置 0 表示无限超时.")
	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

}
