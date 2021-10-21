package io.onedev.server.plugin.report.markdown;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.LockUtils;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.buildspec.step.PublishReportStep;
import io.onedev.server.model.Build;
import io.onedev.server.util.SimpleLogger;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Interpolative;

@Editable(order=120, name="发布Markdown报告(用于拉取请求)", description="如果构建属于拉取请求，此报告将显示在拉取请求概览页面中")
public class PublishPullRequestMarkdownReportStep extends PublishReportStep {

	private static final long serialVersionUID = 1L;
	
	public static final String DIR = "pull-request-markdown-reports";
	
	public static final String FILE = "content.md";

	private String file;
	
	@Editable(order=1100, name="文件",description="指定相对于要发布的存储库工作区的 Markdown 文件")
	@Interpolative(variableSuggester="suggestVariables")
	@NotEmpty(message="不能为空")
	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}

	@Override
	public String getFilePatterns() {
		return getFile();
	}

	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestVariables(String matchWith) {
		return BuildSpec.suggestVariables(matchWith, true, true);
	}

	@Override
	public Map<String, byte[]> run(Build build, File workspace, SimpleLogger logger) {
		if (build.getRequest() != null) {
			File reportDir = new File(build.getReportCategoryDir(DIR), getReportName());

			LockUtils.write(build.getReportCategoryLockKey(DIR), new Callable<Void>() {

				@Override
				public Void call() throws Exception {
					File file = new File(workspace, getFile()); 
					if (file.exists()) {
						String markdown = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
						FileUtils.createDir(reportDir);
						FileUtils.writeFile(new File(reportDir, FILE), markdown, StandardCharsets.UTF_8.name());
					} else {
						logger.log("WARNING: 未找到Markdown报告文件: " + file.getAbsolutePath());
					}
					return null;
				}
				
			});
		}
		return null;
	}

}
