package io.onedev.server.web.page.admin.databasebackup;

import java.io.Serializable;

import io.onedev.server.model.support.administration.BackupSetting;
import io.onedev.server.web.editable.annotation.Editable;

@Editable
public class BackupSettingHolder implements Serializable {

	private static final long serialVersionUID = 1L;

	private BackupSetting backupSetting;

	@Editable(name="启用自动备份")
	public BackupSetting getBackupSetting() {
		return backupSetting;
	}

	public void setBackupSetting(BackupSetting backupSetting) {
		this.backupSetting = backupSetting;
	}
	
}
