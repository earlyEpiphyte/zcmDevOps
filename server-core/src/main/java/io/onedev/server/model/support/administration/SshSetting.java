package io.onedev.server.model.support.administration;

import java.io.IOException;
import java.io.Serializable;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.validation.ConstraintValidatorContext;

import org.apache.commons.lang3.StringUtils;
import org.apache.sshd.common.config.keys.KeyUtils;
import org.apache.sshd.common.digest.BuiltinDigests;
import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.ssh.SshKeyUtils;
import io.onedev.server.util.validation.Validatable;
import io.onedev.server.util.validation.annotation.ClassValidating;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Multiline;

@Editable
@ClassValidating
public class SshSetting implements Serializable, Validatable {

	private static final long serialVersionUID = 1L;
	
    private String serverUrl;

    private String pemPrivateKey;
    
	@Editable(name="SSH服务器地址", order=90, description="这个属性将被用作构建各种ssh服务(如使用ssh的git)的基础url")
	@NotEmpty
    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    @Editable(name="服务器私钥", order=100, description="明确与客户端建立连接的ssh服务器的私钥(PEM格式)")
    @Multiline
    @NotEmpty
    public String getPemPrivateKey() {
        return pemPrivateKey;
    }

    public void setPemPrivateKey(String pemPrivateKey) {
        this.pemPrivateKey = pemPrivateKey;
    }
    
    public String getFingerPrint() {
        try {
			PrivateKey privateKey = SshKeyUtils.decodePEMPrivateKey(pemPrivateKey);
			PublicKey publicKey = KeyUtils.recoverPublicKey(privateKey);
			return KeyUtils.getFingerPrint(BuiltinDigests.sha256, publicKey);
		} catch (IOException | GeneralSecurityException e) {
			throw new RuntimeException(e);
		}
    }
    
    public PrivateKey getPrivateKey() {
		try {
			return SshKeyUtils.decodePEMPrivateKey(pemPrivateKey);
		} catch (IOException | GeneralSecurityException e) {
			throw new RuntimeException(e);
		}
    }
    
    public PublicKey getPublicKey() {
		try {
			return KeyUtils.recoverPublicKey(getPrivateKey());
		} catch (GeneralSecurityException e) {
			throw new RuntimeException(e);
		}
    }
    
    public String getServerName() {
    	String serverName = serverUrl;
    	if (serverName.startsWith("ssh://"))
    		serverName = serverName.substring("ssh://".length());
    	serverName = StringUtils.stripEnd(serverName, "/\\");
    	return StringUtils.substringBefore(serverName, ":");
    }
    
    @Override
    public boolean isValid(ConstraintValidatorContext context) {
		if (serverUrl != null)
			serverUrl = StringUtils.stripEnd(serverUrl, "/\\");
    	
        boolean hasErrors = false;
        String propertyNode = "pemPrivateKey";
        try {
            SshKeyUtils.decodePEMPrivateKey(pemPrivateKey);
        } catch (Exception e) {
            context.buildConstraintViolationWithTemplate("所给的密钥不合规则。请检查并再次尝试")
                    .addPropertyNode(propertyNode).addConstraintViolation()
                    .disableDefaultConstraintViolation();
            hasErrors = true;
        }
        return !hasErrors;
    }
}
