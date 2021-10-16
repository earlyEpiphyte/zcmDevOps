package io.onedev.server.buildspec.step;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.OneDev;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.entitymanager.MilestoneManager;
import io.onedev.server.model.Build;
import io.onedev.server.model.Milestone;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.util.SimpleLogger;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Interpolative;

@Editable(name="关闭里程碑", order=40)
public class CloseMilestoneStep extends ServerStep {

	private static final long serialVersionUID = 1L;
	
	private String milestoneName;
	
	@Editable(order=1000, name="里程碑名称",description="指定里程碑的名称")
	@Interpolative(variableSuggester="suggestVariables")
	@NotEmpty
	public String getMilestoneName() {
		return milestoneName;
	}

	public void setMilestoneName(String milestoneName) {
		this.milestoneName = milestoneName;
	}
	
	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestVariables(String matchWith) {
		return BuildSpec.suggestVariables(matchWith, true, true);
	}

	@Override
	public Map<String, byte[]> run(Build build, File filesDir, SimpleLogger logger) {
		OneDev.getInstance(TransactionManager.class).run(new Runnable() {

			@Override
			public void run() {
				Project project = build.getProject();
				String milestoneName = getMilestoneName();
				MilestoneManager milestoneManager = OneDev.getInstance(MilestoneManager.class);
				Milestone milestone = milestoneManager.find(project, milestoneName);
				if (milestone != null) {
					if (build.canCloseMilestone(milestoneName)) {
						milestone.setClosed(true);
						milestoneManager.save(milestone);
					} else {
						throw new ExplicitException("此构建未授权关闭里程碑 '" + milestoneName + "'");
					}
				} else {
					logger.log("WARNING: 找不到里程碑 '" + milestoneName + "'");
				}
			}
			
		});
		return null;
	}

}
