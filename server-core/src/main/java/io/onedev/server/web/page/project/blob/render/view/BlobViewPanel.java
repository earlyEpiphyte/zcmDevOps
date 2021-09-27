package io.onedev.server.web.page.project.blob.render.view;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes.Method;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.link.ResourceLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.Model;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import io.onedev.commons.utils.FileUtils;
import io.onedev.k8shelper.Executable;
import io.onedev.k8shelper.ExecuteCondition;
import io.onedev.server.OneDev;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.buildspec.job.Job;
import io.onedev.server.buildspec.job.JobManager;
import io.onedev.server.buildspec.job.SubmitReason;
import io.onedev.server.buildspec.job.gitcredential.DefaultCredential;
import io.onedev.server.buildspec.job.gitcredential.GitCredential;
import io.onedev.server.buildspec.param.ParamCombination;
import io.onedev.server.buildspec.step.CheckoutStep;
import io.onedev.server.buildspec.step.CommandStep;
import io.onedev.server.buildspec.step.Step;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.entitymanager.impl.DefaultBuildManager;
import io.onedev.server.event.RefUpdated;
import io.onedev.server.git.BlobContent;
import io.onedev.server.git.BlobEdits;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.ajaxlistener.ConfirmLeaveListener;
import io.onedev.server.web.ajaxlistener.TrackViewStateListener;
import io.onedev.server.web.component.link.ViewStateAwareAjaxLink;
import io.onedev.server.web.page.project.blob.render.BlobRenderContext;
import io.onedev.server.web.page.project.blob.render.BlobRenderContext.Mode;
import io.onedev.server.web.page.project.builds.detail.log.BuildLogPage;
import io.onedev.server.web.resource.RawBlobResource;
import io.onedev.server.web.resource.RawBlobResourceReference;

@SuppressWarnings("serial")
public abstract class BlobViewPanel extends Panel {

	protected final BlobRenderContext context;
	
	public BlobViewPanel(String id, BlobRenderContext context) {
		super(id);
		
		BlobIdent blobIdent = context.getBlobIdent();
		Preconditions.checkArgument(blobIdent.revision != null 
				&& blobIdent.path != null && blobIdent.mode != null);
		
		this.context = context;
	}
	
	protected abstract boolean isEditSupported();
	
	protected abstract boolean isViewPlainSupported();
	
	protected WebMarkupContainer newFormats(String id) {
		WebMarkupContainer options = new WebMarkupContainer(id);
		options.setVisible(false);
		return options;
	}
	
	protected WebMarkupContainer newExtraOptions(String id) {
		return new WebMarkupContainer(id);
	}

	private void newChangeActions(@Nullable IPartialPageRequestHandler target) {
		WebMarkupContainer changeActions = new WebMarkupContainer("changeActions");

		Project project = context.getProject();
		
		if (SecurityUtils.canWriteCode(project) && context.isOnBranch()) {
			User user = SecurityUtils.getUser();
			String revision = context.getBlobIdent().revision;
			String path = context.getBlobIdent().path;
			boolean reviewRequired = project.isReviewRequiredForModification(user, revision, path);
			boolean buildRequired = project.isBuildRequiredForModification(user, revision, path);
			
			
			WebMarkupContainer compiler = new WebMarkupContainer("compiler");
			changeActions.add(compiler);

			AjaxLink<Void> compile = new ViewStateAwareAjaxLink<Void>("compile") {
				
				@Override
				public void onClick(AjaxRequestTarget target) {
					//照抄RunJobLink中的reason
					SubmitReason reason = new SubmitReason() {

						@Override
						public String getRefName() {
							return Lists.newArrayList(context.getRefName()).iterator().next();
						}

						@Override
						public PullRequest getPullRequest() {
							return context.getPullRequest();
						}

						@Override
						public String getDescription() {
							return "online compile";
						}
						
					};
					//.onedev-buildspce.yml位置：src/main/resources
					ObjectId commitId = context.getCommit().copy();
					ObjectId preCommitId = context.getProject().getObjectId(revision, true);
					String jobName = "myjob";

					//删除project
					Set<String> oldPaths = new HashSet<>();
					oldPaths.add(BuildSpec.BLOB_PATH);
					Map<String, BlobContent> newBlobs = new HashMap<>();
					String content = "version: 6\n" + 
							"jobs:\n" + 
							"- name: myjob\n" + 
							"  steps:\n" + 
							"  - !CheckoutStep\n" + 
							"    name: checkout\n" + 
							"    cloneCredential: !DefaultCredential {}\n" + 
							"    condition: ALL_PREVIOUS_STEPS_WERE_SUCCESSFUL\n" + 
							"  - !CommandStep\n" + 
							"    name: run\n" + 
							"    image: alpine_cpp\n" + 
							"    commands:\n" + 
							"    - g++ -o test a.cpp\n" + 
							"    - ./test\n" + 
							"    condition: ALL_PREVIOUS_STEPS_WERE_SUCCESSFUL\n" + 
							"  retryCondition: never\n" + 
							"  maxRetries: 3\n" + 
							"  retryDelay: 30\n" + 
							"  cpuRequirement: 250m\n" + 
							"  memoryRequirement: 128m\n" + 
							"  timeout: 3600";
					newBlobs.put(BuildSpec.BLOB_PATH,new BlobContent.Immutable(content.getBytes(), FileMode.REGULAR_FILE));
					ObjectId newCommitId = new BlobEdits(oldPaths, newBlobs).commit(project.getRepository(), project.getRefName(revision), 
							preCommitId, preCommitId, user.asPerson(), "在线编译");
					
					Build build = new Build();
					build.setProject(project);
					build.setCommitHash(newCommitId.name());
					build.setJobName(jobName);
					build.setSubmitDate(new Date());
					build.setStatus(Build.Status.WAITING);
					build.setSubmitReason(reason.getDescription());
					build.setSubmitter(SecurityUtils.getUser());
					build.setRefName(reason.getRefName());
					build.setRequest(reason.getPullRequest());
					
					//target.appendJavaScript("$(window).resize();");
					//target.appendJavaScript("window.location.reload();");
					OneDev.getInstance(BuildManager.class).create(build);
					setResponsePage(BuildLogPage.class, BuildLogPage.paramsOf(build));
				}

			};
			compiler.add(compile);
			
			WebMarkupContainer edit = new WebMarkupContainer("edit");
			changeActions.add(edit);
			if (isEditSupported()) {
				String title;
				if (reviewRequired) 
					title = "Review required for this change. Submit pull request instead";
				else if (buildRequired) 
					title = "Build required for this change. Submit pull request instead";
				else 
					title = "Edit on branch " + context.getBlobIdent().revision;
				
				edit.add(AttributeAppender.append("title", title));
				
				AjaxLink<Void> link = new ViewStateAwareAjaxLink<Void>("link", true) {

					@Override
					protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
						super.updateAjaxAttributes(attributes);
						attributes.getAjaxCallListeners().add(new ConfirmLeaveListener());
					}
					
					@Override
					public void onClick(AjaxRequestTarget target) {
						context.onModeChange(target, Mode.EDIT, null);
					}
					
				};
				if (reviewRequired || buildRequired) {
					link.add(AttributeAppender.append("class", "disabled"));
					link.setEnabled(false);
				}
				
				edit.add(link);
			} else {
				edit.add(new WebMarkupContainer("link").setVisible(false));
			}
			
			WebMarkupContainer delete = new WebMarkupContainer("delete");
			changeActions.add(delete);
			
			String title;
			if (reviewRequired) 
				title = "Review required for this change. Submit pull request instead";
			else if (buildRequired) 
				title = "Build required for this change. Submit pull request instead";
			else 
				title = "Delete from branch " + context.getBlobIdent().revision;
			
			delete.add(AttributeAppender.append("title", title));
			
			AjaxLink<Void> link = new ViewStateAwareAjaxLink<Void>("link") {

				@Override
				protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
					super.updateAjaxAttributes(attributes);
					attributes.getAjaxCallListeners().add(new ConfirmLeaveListener());
				}
				
				@Override
				public void onClick(AjaxRequestTarget target) {
					context.onModeChange(target, Mode.DELETE, null);
				}

			};

			if (reviewRequired || buildRequired) {
				link.add(AttributeAppender.append("class", "disabled"));
				link.setEnabled(false);
			}
			
			delete.add(link);
			
		} else {
			changeActions.setVisible(false);
			
			WebMarkupContainer edit = new WebMarkupContainer("edit");
			edit.add(new WebMarkupContainer("link"));
			changeActions.add(edit);
			
			WebMarkupContainer delete = new WebMarkupContainer("delete");
			delete.add(new WebMarkupContainer("link"));
			changeActions.add(delete);
		}
		
		changeActions.setOutputMarkupId(true);
		
		if (target != null) {
			replace(changeActions);
			target.add(changeActions);
		} else {
			add(changeActions);
		}
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new Label("lines", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return context.getProject().getBlob(context.getBlobIdent(), true).getText().getLines().size() + " lines";
			}
			
		}) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				setVisible(context.getProject().getBlob(context.getBlobIdent(), true).getText() != null);
			}
			
		});
		
		add(new Label("charset", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return context.getProject().getBlob(context.getBlobIdent(), true).getText().getCharset().displayName();
			}
			
		}) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				setVisible(context.getProject().getBlob(context.getBlobIdent(), true).getText() != null);
			}
			
		});
		
		add(new Label("size", FileUtils.byteCountToDisplaySize(context.getProject().getBlob(context.getBlobIdent(), true).getSize())));
		
		add(newFormats("formats"));
		
		add(new ResourceLink<Void>("raw", new RawBlobResourceReference(), 
				RawBlobResource.paramsOf(context.getProject(), context.getBlobIdent())));
		add(new CheckBox("viewPlain", Model.of(context.getMode() == Mode.VIEW && context.isViewPlain())) {
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(isViewPlainSupported());
			}

		}.add(new OnChangeAjaxBehavior() {
			
			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.setMethod(Method.POST);
				attributes.getAjaxCallListeners().add(new ConfirmLeaveListener());
				attributes.getAjaxCallListeners().add(new TrackViewStateListener(true));
			}
			
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				context.onModeChange(target, Mode.VIEW, !context.isViewPlain(), null);
			}
			
		}));

		add(new CheckBox("blame", Model.of(context.getMode() == Mode.BLAME)) {
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(context.getProject().getBlob(context.getBlobIdent(), true).getText() != null);
			}

		}.add(new OnChangeAjaxBehavior() {
			
			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.setMethod(Method.POST);
				attributes.getAjaxCallListeners().add(new ConfirmLeaveListener());
				attributes.getAjaxCallListeners().add(new TrackViewStateListener(true));
			}
			
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				if (context.getMode() == Mode.BLAME)
					context.onModeChange(target, Mode.VIEW, null);
				else
					context.onModeChange(target, Mode.BLAME, null);
			}
			
		}));
		
		add(newExtraOptions("extraOptions"));
		newChangeActions(null);
		
		setOutputMarkupId(true);
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new BlobViewCssResourceReference()));
	}

	public BlobRenderContext getContext() {
		return context;
	}
	
}
