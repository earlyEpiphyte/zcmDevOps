package io.onedev.server.buildspec.step;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.LockUtils;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.model.Build;
import io.onedev.server.util.SimpleLogger;
import io.onedev.server.util.patternset.PatternSet;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Interpolative;
import io.onedev.server.web.editable.annotation.Patterns;

@Editable(order=105, name="发布Artifacts")
public class PublishArtifactStep extends ServerStep {

	private static final long serialVersionUID = 1L;

	private String artifacts;
	
	@Editable(order=100, description="指定要发布为相对于作业工作区的作业artifacts."
			+ "使用 * 或 ? 用于模式匹配")
	@Interpolative(variableSuggester="suggestVariables")
	@Patterns(path=true)
	@NotEmpty(message="不能为空")
	public String getArtifacts() {
		return artifacts;
	}

	public void setArtifacts(String artifacts) {
		this.artifacts = artifacts;
	}
	
	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestVariables(String matchWith) {
		return BuildSpec.suggestVariables(matchWith, true, true);
	}
	
	@Override
	protected PatternSet getFiles() {
		return PatternSet.parse(getArtifacts());
	}

	@Override
	public Map<String, byte[]> run(Build build, File filesDir, SimpleLogger jobLogger) {
		LockUtils.write(build.getArtifactsLockKey(), new Callable<Void>() {

			@Override
			public Void call() throws Exception {
				File artifactsDir = build.getArtifactsDir();
				FileUtils.createDir(artifactsDir);
				FileUtils.copyDirectory(filesDir, artifactsDir);
				return null;
			}
			
		});
		return null;
	}

}
