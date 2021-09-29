package io.onedev.server.model.support.administration;

import java.io.Serializable;
import java.text.ParseException;

import javax.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.NotEmpty;
import org.quartz.CronExpression;

import io.onedev.server.util.validation.Validatable;
import io.onedev.server.util.validation.annotation.ClassValidating;
import io.onedev.server.web.editable.annotation.Editable;

@Editable
@ClassValidating
public class BackupSetting implements Serializable, Validatable {
	
	private static final long serialVersionUID = 1L;
	
	private String schedule;
	
	@Editable(order=100, name="备份计划", description=
		"（可选）指定 cron 表达式以安排数据库自动备份. cron 表达式格式为 " +
		"<em>&lt;seconds&gt; &lt;minutes&gt; &lt;hours&gt; &lt;day-of-month&gt; &lt;month&gt; &lt;day-of-week&gt;</em>." +
		"例如, <em>0 0 1 * * ?</em> 表示每天 1:00am. 有关格式的详细信息，请参阅 <a href='http://www.quartz-scheduler.org/documentation/quartz-2.x/tutorials/tutorial-lesson-06'>Quartz tutorial</a>." + 
		"备份文件将被放置在 OneDev 安装目录下的 <em>db-backup</em> 文件夹中. " +
		"如果您不想启用数据库自动备份，请将此属性保留为空.")
	@NotEmpty
	public String getSchedule() {
		return schedule;
	}

	public void setSchedule(String schedule) {
		this.schedule = schedule;
	}

	@Override
	public boolean isValid(ConstraintValidatorContext context) {
		boolean hasErrors = false;
		if (schedule != null) {
			try {
				new CronExpression(schedule);
			} catch (ParseException e) {
				context.buildConstraintViolationWithTemplate(e.getMessage())
						.addPropertyNode("schedule").addConstraintViolation();
				hasErrors = true;
			}
		}
		return !hasErrors;
	}

}
