package io.onedev.server.plugin.report.markdown;

import java.io.File;
import java.io.IOException;
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

@Editable(order=110, name="发布Markdown报告")
public class PublishMarkdownReportStep extends PublishReportStep {

	private static final long serialVersionUID = 1L;
	
	public static final String DIR = "markdown-reports";
	
	public static final String START_PAGE = "$onedev-markdownreport-startpage$";
	
	private String startPage;
	
	@Editable(order=1100, name="起始页",description="指定与作业工作区相关的报告的起始页, 例如: <tt>manual/index.md</tt>")
	@Interpolative(variableSuggester="suggestVariables")
	@NotEmpty(message="不能为空")
	public String getStartPage() {
		return startPage;
	}

	public void setStartPage(String startPage) {
		this.startPage = startPage;
	}

	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestVariables(String matchWith) {
		return BuildSpec.suggestVariables(matchWith, true, true);
	}

	@Override
	public Map<String, byte[]> run(Build build, File filesDir, SimpleLogger logger) {
		File reportDir = new File(build.getReportCategoryDir(DIR), getReportName());

		LockUtils.write(build.getReportCategoryLockKey(DIR), new Callable<Void>() {

			@Override
			public Void call() throws Exception {
				File startPage = new File(filesDir, getStartPage()); 
				if (startPage.exists()) {
					FileUtils.createDir(reportDir);
					File startPageFile = new File(reportDir, START_PAGE);
					FileUtils.writeFile(startPageFile, getStartPage());
					
					int baseLen = filesDir.getAbsolutePath().length() + 1;
					for (File file: getPatternSet().listFiles(filesDir)) {
						try {
							FileUtils.copyFile(file, new File(reportDir, file.getAbsolutePath().substring(baseLen)));
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					}
					
				} else {
					logger.log("WARNING: 未找到Markdown报告起始页: " + startPage.getAbsolutePath());
				}
				return null;
			}
			
		});
		
		return null;
	}

}
