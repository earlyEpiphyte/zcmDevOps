package io.onedev.server.buildspec.job;

import static io.onedev.server.model.Build.NAME_BRANCH;
import static io.onedev.server.model.Build.NAME_COMMIT;
import static io.onedev.server.model.Build.NAME_JOB;
import static io.onedev.server.model.Build.NAME_PULL_REQUEST;
import static io.onedev.server.model.Build.NAME_TAG;
import static io.onedev.server.search.entity.build.BuildQuery.getRuleName;
import static io.onedev.server.search.entity.build.BuildQueryLexer.And;
import static io.onedev.server.search.entity.build.BuildQueryLexer.Is;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import javax.ws.rs.core.HttpHeaders;

import org.apache.wicket.Component;
import org.eclipse.jgit.lib.ObjectId;
import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.commons.codeassist.InputCompletion;
import io.onedev.commons.codeassist.InputStatus;
import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.k8shelper.KubernetesHelper;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.buildspec.BuildSpecAware;
import io.onedev.server.buildspec.NamedElement;
import io.onedev.server.buildspec.job.action.PostBuildAction;
import io.onedev.server.buildspec.job.trigger.JobTrigger;
import io.onedev.server.buildspec.param.ParamUtils;
import io.onedev.server.buildspec.param.spec.ParamSpec;
import io.onedev.server.buildspec.step.Step;
import io.onedev.server.event.ProjectEvent;
import io.onedev.server.git.GitUtils;
import io.onedev.server.model.PullRequest;
import io.onedev.server.util.ComponentContext;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.util.validation.Validatable;
import io.onedev.server.util.validation.annotation.ClassValidating;
import io.onedev.server.web.editable.annotation.ChoiceProvider;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Interpolative;
import io.onedev.server.web.editable.annotation.RetryCondition;
import io.onedev.server.web.editable.annotation.SuggestionProvider;
import io.onedev.server.web.util.WicketUtils;

@Editable
@ClassValidating
public class Job implements NamedElement, Serializable, Validatable {

	private static final long serialVersionUID = 1L;
	
	public static final String SELECTION_PREFIX = "jobs/";
	
	public static final String PROP_JOB_DEPENDENCIES = "jobDependencies";
	
	public static final String PROP_REQUIRED_SERVICES = "requiredServices";
	
	public static final String PROP_TRIGGERS = "triggers";
	
	public static final String PROP_STEPS = "steps";
	
	public static final String PROP_RETRY_CONDITION = "retryCondition";
	
	public static final String PROP_POST_BUILD_ACTIONS = "postBuildActions";
	
	private String name;
	
	private List<Step> steps = new ArrayList<>();
	
	private List<ParamSpec> paramSpecs = new ArrayList<>();
	
	private List<JobDependency> jobDependencies = new ArrayList<>();
	
	private List<ProjectDependency> projectDependencies = new ArrayList<>();
	
	private List<String> requiredServices = new ArrayList<>();
	
	private List<JobTrigger> triggers = new ArrayList<>();
	
	private List<CacheSpec> caches = new ArrayList<>();

	private String cpuRequirement = "250m";
	
	private String memoryRequirement = "128m";
	
	private long timeout = 3600;
	
	private List<PostBuildAction> postBuildActions = new ArrayList<>();
	
	private String retryCondition = "never";
	
	private int maxRetries = 3;
	
	private int retryDelay = 30;
	
	private transient Map<String, ParamSpec> paramSpecMap;
	
	@Editable(order=100, name="名称", description="指定作业名称")
	@SuggestionProvider("getNameSuggestions")
	@NotEmpty
	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@SuppressWarnings("unused")
	private static List<InputCompletion> getNameSuggestions(InputStatus status) {
		BuildSpec buildSpec = BuildSpec.get();
		if (buildSpec != null) {
			List<String> candidates = new ArrayList<>(buildSpec.getJobMap().keySet());
			buildSpec.getJobs().forEach(it->candidates.remove(it.getName()));
			return BuildSpec.suggestOverrides(candidates, status);
		}
		return new ArrayList<>();
	}

	@Editable(order=200, name="步骤", description="步骤将在同一个节点上串行执行,共享同一个<a href='$docRoot/pages/concepts.md#job-workspace'>作业空间</a>")
	@Size(min=1, max=1000, message="至少需要配置一个步骤")
	public List<Step> getSteps() {
		return steps;
	}
	
	public void setSteps(List<Step> steps) {
		this.steps = steps;
	}

	@Editable(name="参数规格", order=400, group="参数和触发器", description="可选地定义作业的参数规范")
	@Valid
	public List<ParamSpec> getParamSpecs() {
		return paramSpecs;
	}

	public void setParamSpecs(List<ParamSpec> paramSpecs) {
		this.paramSpecs = paramSpecs;
	}

	@Editable(name="触发器", order=500, group="参数和触发器", description="使用触发器在特定条件下自动运行作业")
	@Valid
	public List<JobTrigger> getTriggers() {
		return triggers;
	}

	public void setTriggers(List<JobTrigger> triggers) {
		this.triggers = triggers;
	}

	@Editable(name="作业依赖", order=9110, group="依赖和服务", description="作业依赖性决定了运行不同作业时的顺序和并发性。您还可以指定要从上游作业中检索的工件")
	@Valid
	public List<JobDependency> getJobDependencies() {
		return jobDependencies;
	}

	public void setJobDependencies(List<JobDependency> jobDependencies) {
		this.jobDependencies = jobDependencies;
	}

	@Editable(name="项目依赖", order=9112, group="依赖和服务", description="使用项目依赖从其他项目中检索工件")
	@Valid
	public List<ProjectDependency> getProjectDependencies() {
		return projectDependencies;
	}

	public void setProjectDependencies(List<ProjectDependency> projectDependencies) {
		this.projectDependencies = projectDependencies;
	}

	@Editable(name="所需服务",order=9114, group="依赖和服务", description="（可选）指定此作业所需的服务")
	@ChoiceProvider("getServiceChoices")
	public List<String> getRequiredServices() {
		return requiredServices;
	}

	public void setRequiredServices(List<String> requiredServices) {
		this.requiredServices = requiredServices;
	}
	
	@SuppressWarnings("unused")
	private static List<String> getServiceChoices() {
		List<String> choices = new ArrayList<>();
		Component component = ComponentContext.get().getComponent();
		BuildSpecAware buildSpecAware = WicketUtils.findInnermost(component, BuildSpecAware.class);
		if (buildSpecAware != null) {
			BuildSpec buildSpec = buildSpecAware.getBuildSpec();
			if (buildSpec != null) { 
				choices.addAll(buildSpec.getServiceMap().values().stream()
						.map(it->it.getName()).collect(Collectors.toList()));
			}
		}
		return choices;
	}

	@Editable(name="重试条件", order=9400, group="更多设置", description="指定条件以在失败时重试构建")
	@NotEmpty
	@RetryCondition
	public String getRetryCondition() {
		return retryCondition;
	}

	public void setRetryCondition(String retryCondition) {
		this.retryCondition = retryCondition;
	}

	@Editable(name="最大重试次数", order=9410, group="更多设置", description="最大重试次数")
	@Min(value=1, message="此值不应小于 1")
	public int getMaxRetries() {
		return maxRetries;
	}

	public void setMaxRetries(int maxRetries) {
		this.maxRetries = maxRetries;
	}

	@Editable(name="重试延迟", order=9420, group="更多设置", description="延迟第一次重试以秒为单位。后续重试的延迟将使用基于此延迟的指数退避计算")
			
	@Min(value=1, message="此值不应小于 1")
	public int getRetryDelay() {
		return retryDelay;
	}

	public void setRetryDelay(int retryDelay) {
		this.retryDelay = retryDelay;
	}
	
	@Editable(order=10050, name="CPU要求", group="更多设置", description="指定作业所需的CPU,"
			+ "有关详细信息,请参阅 <a href='https://kubernetes.io/docs/concepts/configuration/manage-compute-resources-container/#meaning-of-cpu' target='_blank'>kubernetes 文档</a>")
	@Interpolative(variableSuggester="suggestVariables")
	@NotEmpty
	public String getCpuRequirement() {
		return cpuRequirement;
	}

	public void setCpuRequirement(String cpuRequirement) {
		this.cpuRequirement = cpuRequirement;
	}

	@Editable(name="内存要求", order=10060, group="更多设置", description="指定作业所需的内存,"
			+ "有关详细信息,请参阅 <a href='https://kubernetes.io/docs/concepts/configuration/manage-compute-resources-container/#meaning-of-memory' target='_blank'>kubernetes 文档</a>")
	@Interpolative(variableSuggester="suggestVariables")
	@NotEmpty
	public String getMemoryRequirement() {
		return memoryRequirement;
	}

	public void setMemoryRequirement(String memoryRequirement) {
		this.memoryRequirement = memoryRequirement;
	}

	@Editable(name="缓存", order=10100, group="更多设置", description="缓存特定路径以加快作业执行速度。"
			+ "例如，对于 node.js 项目，您可以缓存文件夹 <tt>/root/.npm</tt> 以避免为后续作业执行下载节点模块")
	@Valid
	public List<CacheSpec> getCaches() {
		return caches;
	}

	public void setCaches(List<CacheSpec> caches) {
		this.caches = caches;
	}

	@Editable(name="超时", order=10500, group="更多设置", description="以秒为单位指定超时时间")
	public long getTimeout() {
		return timeout;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}
	
	@Editable(order=10600, name="构建后操作", group="更多设置")
	@Valid
	public List<PostBuildAction> getPostBuildActions() {
		return postBuildActions;
	}
	
	public void setPostBuildActions(List<PostBuildAction> postBuildActions) {
		this.postBuildActions = postBuildActions;
	}
	
	@Nullable
	public JobTriggerMatch getTriggerMatch(ProjectEvent event) {
		for (JobTrigger trigger: getTriggers()) {
			SubmitReason reason = trigger.matches(event, this);
			if (reason != null)
				return new JobTriggerMatch(trigger, reason);
		}
		return null;
	}

	@Override
	public boolean isValid(ConstraintValidatorContext context) {
		boolean isValid = true;
		
		Set<String> keys = new HashSet<>();
		Set<String> paths = new HashSet<>();
		for (CacheSpec cache: caches) {
			if (!keys.add(cache.getKey())) {
				isValid = false;
				context.buildConstraintViolationWithTemplate("Duplicate key (" + cache.getKey() + ")")
						.addPropertyNode("caches").addConstraintViolation();
			}
			if (!paths.add(cache.getPath())) {
				isValid = false;
				context.buildConstraintViolationWithTemplate("Duplicate path (" + cache.getPath() + ")")
						.addPropertyNode("caches").addConstraintViolation();
			} 
		}

		Set<String> dependencyJobNames = new HashSet<>();
		for (JobDependency dependency: jobDependencies) {
			if (!dependencyJobNames.add(dependency.getJobName())) {
				isValid = false;
				context.buildConstraintViolationWithTemplate("Duplicate dependency (" + dependency.getJobName() + ")")
						.addPropertyNode("jobDependencies").addConstraintViolation();
			} 
		}
		
		Set<String> dependencyProjectNames = new HashSet<>();
		for (ProjectDependency dependency: projectDependencies) {
			if (!dependencyProjectNames.add(dependency.getProjectName())) {
				isValid = false;
				context.buildConstraintViolationWithTemplate("Duplicate dependency (" + dependency.getProjectName() + ")")
						.addPropertyNode("projectDependencies").addConstraintViolation();
			}
		}
		
		Set<String> paramSpecNames = new HashSet<>();
		for (ParamSpec paramSpec: paramSpecs) {
			if (!paramSpecNames.add(paramSpec.getName())) {
				isValid = false;
				context.buildConstraintViolationWithTemplate("Duplicate parameter spec (" + paramSpec.getName() + ")")
						.addPropertyNode("paramSpecs").addConstraintViolation();
			} 
		}
		
		if (getRetryCondition() != null) { 
			try {
				io.onedev.server.buildspec.job.retrycondition.RetryCondition.parse(this, getRetryCondition());
			} catch (Exception e) {
				String message = e.getMessage();
				if (message == null)
					message = "Malformed retry condition";
				context.buildConstraintViolationWithTemplate(message)
						.addPropertyNode(PROP_RETRY_CONDITION)
						.addConstraintViolation();
				isValid = false;
			}
		}
		
		if (isValid) {
			for (int triggerIndex=0; triggerIndex<getTriggers().size(); triggerIndex++) {
				JobTrigger trigger = getTriggers().get(triggerIndex);
				try {
					ParamUtils.validateParams(getParamSpecs(), trigger.getParams());
				} catch (Exception e) {
					String errorMessage = String.format("Error validating job parameters (item: #%s, error message: %s)", 
							(triggerIndex+1), e.getMessage());
					context.buildConstraintViolationWithTemplate(errorMessage)
							.addPropertyNode(PROP_TRIGGERS)
							.addConstraintViolation();
					isValid = false;
				}
			}
		}
		
		if (!isValid)
			context.disableDefaultConstraintViolation();
		
		return isValid;
	}
	
	public Map<String, ParamSpec> getParamSpecMap() {
		if (paramSpecMap == null)
			paramSpecMap = ParamUtils.getParamSpecMap(paramSpecs);
		return paramSpecMap;
	}
	
	public static String getBuildQuery(ObjectId commitId, String jobName, 
			@Nullable String refName, @Nullable PullRequest request) {
		String query = "" 
				+ Criteria.quote(NAME_COMMIT) + " " + getRuleName(Is) + " " + Criteria.quote(commitId.name()) 
				+ " " + getRuleName(And) + " "
				+ Criteria.quote(NAME_JOB) + " " + getRuleName(Is) + " " + Criteria.quote(jobName);
		if (request != null) {
			query = query 
					+ " " + getRuleName(And) + " " 
					+ Criteria.quote(NAME_PULL_REQUEST) + " " + getRuleName(Is) + " " + Criteria.quote("#" + request.getNumber());
		}
		if (refName != null) {
			String branch = GitUtils.ref2branch(refName);
			if (branch != null) {
				query = query 
					+ " " + getRuleName(And) + " " 
					+ Criteria.quote(NAME_BRANCH) + " " + getRuleName(Is) + " " + Criteria.quote(branch);
			} 
			String tag = GitUtils.ref2tag(refName);
			if (tag != null) {
				query = query 
					+ " " + getRuleName(And) + " " 
					+ Criteria.quote(NAME_TAG) + " " + getRuleName(Is) + " " + Criteria.quote(tag);
			} 
		}
		return query;
	}
	
	public static List<String> getChoices() {
		List<String> choices = new ArrayList<>();
		Component component = ComponentContext.get().getComponent();
		BuildSpecAware buildSpecAware = WicketUtils.findInnermost(component, BuildSpecAware.class);
		if (buildSpecAware != null) {
			BuildSpec buildSpec = buildSpecAware.getBuildSpec();
			if (buildSpec != null) {
				choices.addAll(buildSpec.getJobMap().values().stream()
						.map(it->it.getName()).collect(Collectors.toList()));
			}
			JobAware jobAware = WicketUtils.findInnermost(component, JobAware.class);
			if (jobAware != null) {
				Job job = jobAware.getJob();
				if (job != null)
					choices.remove(job.getName());
			}
		}
		return choices;
	}

	@Nullable
	public static String getToken(HttpServletRequest request) {
		String bearer = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (bearer != null && bearer.startsWith(KubernetesHelper.BEARER + " "))
			return bearer.substring(KubernetesHelper.BEARER.length() + 1);
		else
			return null;
	}
	
	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestVariables(String matchWith) {
		return BuildSpec.suggestVariables(matchWith, false, false);
	}
	
}
