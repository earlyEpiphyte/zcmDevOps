package io.onedev.server.buildspec.step;

import static io.onedev.k8shelper.KubernetesHelper.BUILD_VERSION;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.launcher.loader.ListenerRegistry;
import io.onedev.server.OneDev;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.event.build.BuildUpdated;
import io.onedev.server.model.Build;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.util.SimpleLogger;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Interpolative;

@Editable(order=20, name="设置构建版本")
public class SetBuildVersionStep extends ServerStep {

	private static final long serialVersionUID = 1L;

	private String buildVersion;

	@Editable(order=100,name="构建版本")
	@Interpolative(variableSuggester="suggestVariables")
	@NotEmpty(message="不能为空")
	public String getBuildVersion() {
		return buildVersion;
	}

	public void setBuildVersion(String buildVersion) {
		this.buildVersion = buildVersion;
	}

	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestVariables(String matchWith) {
		return BuildSpec.suggestVariables(matchWith, false, true);
	}
	
	@Override
	public Map<String, byte[]> run(Build build, File filesDir, SimpleLogger jobLogger) {
		return OneDev.getInstance(TransactionManager.class).call(new Callable<Map<String, byte[]>>() {

			@Override
			public Map<String, byte[]> call() {
				build.setVersion(buildVersion);
				OneDev.getInstance(ListenerRegistry.class).post(new BuildUpdated(build));
				Map<String, byte[]> outputFiles = new HashMap<>();
				outputFiles.put(BUILD_VERSION, buildVersion.getBytes(StandardCharsets.UTF_8));
				return outputFiles;
			}
			
		});
		
	}

}
