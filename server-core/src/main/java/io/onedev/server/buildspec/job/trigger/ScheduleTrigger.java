package io.onedev.server.buildspec.job.trigger;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.buildspec.job.Job;
import io.onedev.server.buildspec.job.SubmitReason;
import io.onedev.server.event.ProjectEvent;
import io.onedev.server.event.ScheduledTimeReaches;
import io.onedev.server.git.GitUtils;
import io.onedev.server.model.PullRequest;
import io.onedev.server.util.validation.annotation.CronExpression;
import io.onedev.server.web.editable.annotation.Editable;

@Editable(order=600, name="Cron计划")
public class ScheduleTrigger extends JobTrigger {

	private static final long serialVersionUID = 1L;

	private String cronExpression;
	
	@Editable(order=100, name="Cron表达式", description="指定一个注意：<a target='_blank' href='http://www.quartz-scheduler.org/documentation/quartz-2.3.0/tutorials/crontrigger.html#format'>cron 计划</a>"
			+ "来自动触发作业. <b>注意:</b> 这只适用于默认分支")
	@CronExpression
	@NotEmpty(message="不能为空")
	public String getCronExpression() {
		return cronExpression;
	}

	public void setCronExpression(String cronExpression) {
		this.cronExpression = cronExpression;
	}

	@Override
	public SubmitReason triggerMatches(ProjectEvent event, Job job) {
		if (event instanceof ScheduledTimeReaches) {
			return new SubmitReason() {

				@Override
				public String getRefName() {
					return GitUtils.branch2ref(event.getProject().getDefaultBranch());
				}

				@Override
				public PullRequest getPullRequest() {
					return null;
				}

				@Override
				public String getDescription() {
					return "预定时间到达";
				}
				
			};
		} else {
			return null;
		}
	}

	@Override
	public String getTriggerDescription() {
		return "计划在" + cronExpression;
	}

}
