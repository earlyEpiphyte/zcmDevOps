package io.onedev.server.plugin.report.jest;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.LockUtils;
import io.onedev.server.OneDev;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.buildspec.step.PublishReportStep;
import io.onedev.server.model.Build;
import io.onedev.server.model.JestTestMetric;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.util.SimpleLogger;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Interpolative;
import io.onedev.server.web.editable.annotation.Patterns;

@Editable(order=300, name="发布Jest测试报告")
public class PublishJestTestReportStep extends PublishReportStep {

	private static final long serialVersionUID = 1L;
	
	public static final String DIR = "jest-reports";
	
	@Editable(order=100,name="文件模式",description="以相对于作业工作空间的json格式指定Jest测试结果文件. "
			+ "这个文件可以通过 Jest 选项生成 <tt>'--json'</tt> 和 <tt>'--outputFile'</tt>. 使用 * 或 ? 用于模式匹配")
	@Interpolative(variableSuggester="suggestVariables")
	@Patterns(path=true)
	@NotEmpty(message="不能为空")
	@Override
	public String getFilePatterns() {
		return super.getFilePatterns();
	}

	@Override
	public void setFilePatterns(String filePatterns) {
		super.setFilePatterns(filePatterns);
	}
	
	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestVariables(String matchWith) {
		return BuildSpec.suggestVariables(matchWith, true, true);
	}

	@Override
	public Map<String, byte[]> run(Build build, File filesDir, SimpleLogger logger) {
		File reportDir = new File(build.getReportCategoryDir(DIR), getReportName());

		JestTestReportData report = LockUtils.write(build.getReportCategoryLockKey(DIR), new Callable<JestTestReportData>() {

			@Override
			public JestTestReportData call() throws Exception {
				ObjectMapper mapper = OneDev.getInstance(ObjectMapper.class);
				
				Collection<JsonNode> rootNodes = new ArrayList<>();
				int baseLen = filesDir.getAbsolutePath().length()+1;
				for (File file: filesDir.listFiles()) {
					logger.log("处理jest测试报告: " + file.getAbsolutePath().substring(baseLen));
					try {
						rootNodes.add(mapper.readTree(file));
					} catch (Exception e) {
						throw ExceptionUtils.unchecked(e);
					}
				}
				if (!rootNodes.isEmpty()) {
					FileUtils.createDir(reportDir);
					JestTestReportData report = new JestTestReportData(build, rootNodes);
					report.writeTo(reportDir);
					return report;
				} else {
					return null;
				}
			}
			
		});
		
		if (report != null) {
			JestTestMetric metric = new JestTestMetric();
			metric.setBuild(build);
			metric.setReportName(getReportName());
			metric.setTestCaseSuccessRate(report.getTestCaseSuccessRate());
			metric.setTestSuiteSuccessRate(report.getTestSuiteSuccessRate());
			metric.setNumOfTestCases(report.getNumOfTestCases());
			metric.setNumOfTestSuites(report.getNumOfTestSuites());
			metric.setTotalTestDuration(report.getTotalTestDuration());
			OneDev.getInstance(Dao.class).persist(metric);
		}	
		
		return null;
	}

}
