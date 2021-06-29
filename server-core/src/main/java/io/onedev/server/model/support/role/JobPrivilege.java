package io.onedev.server.model.support.role;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.hibernate.validator.constraints.NotEmpty;

import edu.emory.mathcs.backport.java.util.Collections;
import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.util.EditContext;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Patterns;
import io.onedev.server.web.editable.annotation.ShowCondition;
import io.onedev.server.web.util.SuggestionUtils;

@Editable
public class JobPrivilege implements Serializable {

	private static final long serialVersionUID = 1L;

	private String jobNames;
	
	private boolean manageJob;
	
	private boolean runJob;
	
	private boolean accessLog;
	
	private String accessibleReports;
	
	@Editable(name="作业名称",order=100, description="明确空格隔开的任务。使用'*'或'?'通配符"
			+ "'-'前缀表示排除。<b class='text-danger'>注意: </b>即便在此没有明确其他权限，在匹配的任务中会默许授权访问构建实例的权限")
	@Patterns(suggester = "suggestJobNames")
	@NotEmpty
	public String getJobNames() {
		return jobNames;
	}

	public void setJobNames(String jobNames) {
		this.jobNames = jobNames;
	}
	
	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestJobNames(String matchWith) {
		List<String> jobNames = new ArrayList<>(OneDev.getInstance(BuildManager.class).getJobNames(null));
		Collections.sort(jobNames);
		return SuggestionUtils.suggest(jobNames, matchWith);
	}

	@Editable(name="管理任务",order=100, description="删除构建需要此权限。它包含了其他所有任务权限")
	public boolean isManageJob() {
		return manageJob;
	}

	public void setManageJob(boolean manageJob) {
		this.manageJob = manageJob;
	}
	
	@SuppressWarnings("unused")
	private static boolean isManageJobDisabled() {
		return !(boolean) EditContext.get().getInputValue("manageJob");
	}

	@Editable(name="运行任务",order=200, description="此权限可手动运行任务。 它也包含了访问构建日志和所有公布的报告的权限")
	@ShowCondition("isManageJobDisabled")
	public boolean isRunJob() {
		return runJob;
	}

	public void setRunJob(boolean runJob) {
		this.runJob = runJob;
	}

	@SuppressWarnings("unused")
	private static boolean isRunJobDisabled() {
		return !(boolean) EditContext.get().getInputValue("runJob");
	}
	
	@Editable(name="访问构建日志",order=300, description="此权限可访问构建日志。它也包含了访问所有公布的报告的权限")
	@ShowCondition("isRunJobDisabled")
	public boolean isAccessLog() {
		return accessLog;
	}

	public void setAccessLog(boolean accessLog) {
		this.accessLog = accessLog;
	}

	@SuppressWarnings("unused")
	private static boolean isAccessLogDisabled() {
		return !(boolean) EditContext.get().getInputValue("accessLog");
	}
	
	@Editable(order=400, name="访问构建报告", description="可选的明确空格隔开的报告。使用'*'或'?'通配符。" + 
			"'-'前缀表示排除。空白表示匹配全部")
	@ShowCondition("isAccessLogDisabled")
	@Patterns
	@Nullable
	public String getAccessibleReports() {
		return accessibleReports;
	}

	public void setAccessibleReports(String accessibleReports) {
		this.accessibleReports = accessibleReports;
	}

}
