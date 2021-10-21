package io.onedev.server.plugin.report.clover;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
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
import io.onedev.server.model.Build;
import io.onedev.server.model.CloverMetric;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.util.Coverage;
import io.onedev.server.util.SimpleLogger;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Interpolative;
import io.onedev.server.web.editable.annotation.Patterns;

@Editable(order=410, name="发布Clover覆盖报告")
public class PublishCloverReportStep extends PublishReportStep {

	private static final long serialVersionUID = 1L;
	
	public static final String DIR = "clover-reports2";
	
	public static final String TEST_COUNTS_DIR = "test-count";
	
	@Editable(order=100,name="文件模式",description="指定与作业工作区相关的clover覆盖率 xml 报告文件, "
			+ "例如, <tt>target/site/clover/clover.xml</tt>. "
			+ "有关如何生成clover xml 文件的信息，请参阅<a href='https://openclover.org/documentation'>OpenClover 文档.</a> "
			+ "使用 * 或 ? 用于模式匹配.")
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
		
		CloverReportData reportData = LockUtils.write(build.getReportCategoryLockKey(DIR), new Callable<CloverReportData>() {

			@Override
			public CloverReportData call() throws Exception {
				int baseLen = filesDir.getAbsolutePath().length() + 1;
				SAXReader reader = new SAXReader();
				
				// Prevent XXE attack as the xml might be provided by malicious users
				reader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);

				int totalStatements = 0;
				int coveredStatements = 0;
				int totalMethods = 0;
				int coveredMethods = 0;
				int totalBranches = 0;
				int coveredBranches = 0;
				int totalLines = 0;
				int coveredLines = 0;

				List<PackageCoverageInfo> packageCoverages = new ArrayList<>();
				
				for (File file: getPatternSet().listFiles(filesDir)) {
					logger.log("处理clover报告: " + file.getAbsolutePath().substring(baseLen));
					Document doc = reader.read(file);
					for (Element projectElement: doc.getRootElement().elements("project")) {
						Element metricsElement = projectElement.element("metrics");
						
						totalStatements += Integer.parseInt(metricsElement.attributeValue("statements"));
						totalMethods += Integer.parseInt(metricsElement.attributeValue("methods"));
						totalBranches += Integer.parseInt(metricsElement.attributeValue("conditionals"));
						
						coveredStatements += Integer.parseInt(metricsElement.attributeValue("coveredstatements"));
						coveredMethods += Integer.parseInt(metricsElement.attributeValue("coveredmethods"));
						coveredBranches += Integer.parseInt(metricsElement.attributeValue("coveredconditionals"));
						
						for (Element packageElement: projectElement.elements("package")) {
							String packageName = packageElement.attributeValue("name");

							metricsElement = packageElement.element("metrics");
							
							Coverage packageStatementCoverage = new Coverage(
									Integer.parseInt(metricsElement.attributeValue("statements")),
									Integer.parseInt(metricsElement.attributeValue("coveredstatements")));
							Coverage packageMethodCoverage = new Coverage(
									Integer.parseInt(metricsElement.attributeValue("methods")),
									Integer.parseInt(metricsElement.attributeValue("coveredmethods")));
							Coverage packageBranchCoverage = new Coverage(
									Integer.parseInt(metricsElement.attributeValue("conditionals")), 
									Integer.parseInt(metricsElement.attributeValue("coveredconditionals")));

							int packageTotalLines = 0;
							int packageCoveredLines = 0;
						
							List<FileCoverageInfo> fileCoverages = new ArrayList<>();
							for (Element fileElement: packageElement.elements("file")) {
								String fileName = fileElement.attributeValue("name");
								String filePath = fileElement.attributeValue("path");
								if (build.getJobWorkspace() != null && filePath.startsWith(build.getJobWorkspace())) { 
									filePath = filePath.substring(build.getJobWorkspace().length()+1);

									metricsElement = fileElement.element("metrics");
									
									Coverage fileStatementCoverage = new Coverage(
											Integer.parseInt(metricsElement.attributeValue("statements")),
											Integer.parseInt(metricsElement.attributeValue("coveredstatements")));
									Coverage fileMethodCoverage = new Coverage(
											Integer.parseInt(metricsElement.attributeValue("methods")),
											Integer.parseInt(metricsElement.attributeValue("coveredmethods")));
									Coverage fileBranchCoverage = new Coverage(
											Integer.parseInt(metricsElement.attributeValue("conditionals")), 
											Integer.parseInt(metricsElement.attributeValue("coveredconditionals")));
									
									Map<Integer, Integer> lineCoverages = new HashMap<>();
									for (Element lineElement: fileElement.elements("line")) {
										int lineNum = Integer.parseInt(lineElement.attributeValue("num")) - 1;
										String testCountStr = lineElement.attributeValue("count");
										if (testCountStr != null) {
											int testCount = Integer.parseInt(testCountStr);
											lineCoverages.merge(lineNum, testCount, (v1, v2)->v1+v2);
										}
									}
									int fileTotalLines = lineCoverages.size();
									int fileCoveredLines = (int) lineCoverages.entrySet().stream().filter(it->it.getValue()!=0).count();
									
									Coverage fileLineCoverage = new Coverage(fileTotalLines, fileCoveredLines);
									
									packageTotalLines += fileTotalLines;
									packageCoveredLines += fileCoveredLines;
									
									File testCountFile = new File(reportDir, TEST_COUNTS_DIR + "/" + filePath);
									FileUtils.createDir(testCountFile.getParentFile());
									try (OutputStream os = new FileOutputStream(testCountFile)) {
										SerializationUtils.serialize((Serializable) lineCoverages, os);
									} catch (IOException e) {
										throw new RuntimeException(e);
									};
									fileCoverages.add(new FileCoverageInfo(fileName, 
											fileStatementCoverage, fileMethodCoverage, fileBranchCoverage, fileLineCoverage, 
											filePath));
								}
							}
							
							Coverage packageLineCoverage = new Coverage(packageTotalLines, packageCoveredLines);
							
							packageCoverages.add(new PackageCoverageInfo(
									packageName,
									packageStatementCoverage, packageMethodCoverage, 
									packageBranchCoverage, packageLineCoverage, 
									fileCoverages));
							
							totalLines += packageTotalLines;
							coveredLines += packageCoveredLines;
						}
					}
				}
				
				CoverageInfo coverageInfo = new CoverageInfo(
						new Coverage(totalStatements, coveredStatements), 
						new Coverage(totalMethods, coveredMethods), 
						new Coverage(totalBranches, coveredBranches), 
						new Coverage(totalLines, coveredLines));
				
				return new CloverReportData(coverageInfo, packageCoverages);
			}
			
		});
		
		if (!reportData.getPackageCoverages().isEmpty()) {
			FileUtils.createDir(reportDir);
			reportData.writeTo(reportDir);
			
			CloverMetric metric = new CloverMetric();
			metric.setBuild(build);
			metric.setReportName(getReportName());
			
			CoverageInfo coverages = reportData.getOverallCoverages();
			metric.setBranchCoverage(coverages.getBranchCoverage().getPercent());
			metric.setLineCoverage(coverages.getLineCoverage().getPercent());
			metric.setMethodCoverage(coverages.getMethodCoverage().getPercent());
			metric.setStatementCoverage(coverages.getStatementCoverage().getPercent());
			metric.setTotalBranches(coverages.getBranchCoverage().getTotal());
			metric.setTotalLines(coverages.getLineCoverage().getTotal());
			metric.setTotalMethods(coverages.getMethodCoverage().getTotal());
			metric.setTotalStatements(coverages.getStatementCoverage().getTotal());
			
			OneDev.getInstance(Dao.class).persist(metric);
		}
		
		return null;
	}

}
