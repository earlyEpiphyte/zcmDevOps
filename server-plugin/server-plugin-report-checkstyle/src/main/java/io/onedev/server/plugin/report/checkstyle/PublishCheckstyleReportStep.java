package io.onedev.server.plugin.report.checkstyle;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.commons.lang.SerializationUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.LockUtils;
import io.onedev.server.OneDev;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.buildspec.step.PublishReportStep;
import io.onedev.server.code.CodeProblem.Severity;
import io.onedev.server.model.Build;
import io.onedev.server.model.CheckstyleMetric;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.util.SimpleLogger;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Interpolative;
import io.onedev.server.web.editable.annotation.Patterns;

@Editable(order=310, name="发布Checkstyle报告")
public class PublishCheckstyleReportStep extends PublishReportStep {

	private static final long serialVersionUID = 1L;
	
	public static final String DIR = "checkstyle-reports2";
	
	public static final String VIOLATION_FILES = "violation-files";
	
	@Editable(order=100, name="文件模式", description="指定与作业工作区相关的 checkstyle 结果 xml 文件, "
			+ "例如, <tt>target/checkstyle-result.xml</tt>. "
			+ "有关如何生成结果 xml 文件，请参阅<a href='https://checkstyle.org/'>checkstyle 文档</a> "
			+ "使用 * 或 ? 用于模式匹配")
	@Interpolative(variableSuggester="suggestVariables")
	@Patterns(path=true)
	@NotEmpty
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
		
		CheckstyleReportData reportData = LockUtils.write(build.getReportCategoryLockKey(DIR), new Callable<CheckstyleReportData>() {

			@Override
			public CheckstyleReportData call() throws Exception {
				int baseLen = filesDir.getAbsolutePath().length() + 1;
				SAXReader reader = new SAXReader();
				
				// Prevent XXE attack as the xml might be provided by malicious users
				reader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);

				List<CheckstyleViolation> violations = new ArrayList<>();
				
				boolean hasReports = false;
				for (File file: getPatternSet().listFiles(filesDir)) {
					logger.log("处理checkstyle报告: " + file.getAbsolutePath().substring(baseLen));
					Document doc = reader.read(file);
					for (Element fileElement: doc.getRootElement().elements("file")) {
						String filePath = fileElement.attributeValue("name");
						if (build.getJobWorkspace() != null && filePath.startsWith(build.getJobWorkspace())) { 
							filePath = filePath.substring(build.getJobWorkspace().length()+1);
							List<ViolationFile.Violation> violationsOfFile = new ArrayList<>();
							for (Element violationElement: fileElement.elements()) {
								Severity severity = Severity.valueOf(violationElement.attributeValue("severity").toUpperCase());
								String message = violationElement.attributeValue("message");
								String rule = violationElement.attributeValue("source");
								String line = violationElement.attributeValue("line");
								String column = violationElement.attributeValue("column");
								violationsOfFile.add(new ViolationFile.Violation(severity, message, line, column, rule));
								violations.add(new CheckstyleViolation(severity, message, line, column, filePath, rule));
							}
							if (!violationsOfFile.isEmpty()) {
								File violationsFile = new File(reportDir, VIOLATION_FILES + "/" + filePath);
								FileUtils.createDir(violationsFile.getParentFile());
								try (OutputStream os = new FileOutputStream(violationsFile)) {
									SerializationUtils.serialize((Serializable) violationsOfFile, os);
								} catch (IOException e) {
									throw new RuntimeException(e);
								}
							}
						}
					}
					hasReports = true;
				}
				
				if (hasReports)
					return new CheckstyleReportData(violations);
				else
					return null;
			}
			
		});
		
		if (reportData != null) {
			FileUtils.createDir(reportDir);
			reportData.writeTo(reportDir);
			
			CheckstyleMetric metric = new CheckstyleMetric();
			metric.setBuild(build);
			metric.setReportName(getReportName());
			metric.setTotalErrors((int) reportData.getViolations().stream()
					.filter(it->it.getSeverity()==Severity.ERROR)
					.count());
			metric.setTotalWarnings((int) reportData.getViolations().stream()
					.filter(it->it.getSeverity()==Severity.WARNING)
					.count());
			metric.setTotalInfos((int) reportData.getViolations().stream()
					.filter(it->it.getSeverity()==Severity.INFO)
					.count());
						
			OneDev.getInstance(Dao.class).persist(metric);
		}
		
		return null;
	}

}
