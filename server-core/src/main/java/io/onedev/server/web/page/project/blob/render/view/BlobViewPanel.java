package io.onedev.server.web.page.project.blob.render.view;


import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.wicket.Session;
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
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import io.onedev.commons.utils.FileUtils;
import io.onedev.server.OneDev;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.buildspec.job.SubmitReason;
import io.onedev.server.entitymanager.BuildManager;
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

	private void newChangeActions(@Nullable IPartialPageRequestHandler target){
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
				public void onClick(AjaxRequestTarget target){
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
							return "online-compile";
						}
						
					};
					ObjectId preCommitId = context.getCommit().copy();
					ObjectId newCommitId = null;
					String jobName = "online-compile";
					Repository repository = project.getRepository();
					String refName = project.getRefName(revision);
					Set<String> oldPaths = new HashSet<>();
					Map<String, BlobContent> newBlobs = new HashMap<>();
					String fileName = context.getBlobIdent().getName();
					String imageName = "";//镜像名称
					int dotIndex =  fileName.lastIndexOf(".");
					String suffix = "NO_DOT";
					if( dotIndex > -1) {
						suffix = fileName.substring(dotIndex);
					}
					if("NO_DOT".equals(suffix) || (!"NO_DOT".equals(suffix) && !suffix.equals(".cpp") && !suffix.equals(".c") && !suffix.equals(".java") && !suffix.equals(".py"))){
						Session.get().fatal("仅支持.c,.cpp,.java后缀的文件编译！");//要么无逗号；要么有逗号，但不是.c,.cpp,.java,.py后缀
					}
					else {
						//根据不同的后缀，改变commands和imageName的值
						String commands = "";
						if(suffix.equals(".cpp")) {
							commands = 
									"    - g++ -o obj "+ fileName +"\n" + 
									"    - ./obj\n";
							imageName = "cpp";
						}
						else if(suffix.equals(".c")) {
							commands = 
									"    - gcc -o obj "+ fileName +"\n" + 
									"    - ./obj\n";
							imageName = "c";
						}
						else if(suffix.equals(".java")) {
							commands = 
									"    - javac "+ fileName +"\n" + 
									"    - java "+ fileName.substring(0,dotIndex) +"\n";
							imageName = "java";
						}
						else if(suffix.equals(".py")) {
							commands = 
									"    - python "+ fileName +"\n";
							imageName = "python";
						}
						String compileJob = 
								"- name: " + jobName + "\n" + 
								"  steps:\n" + 
								"  - !CheckoutStep\n" + 
								"    name: checkout\n" + 
								"    cloneCredential: !DefaultCredential {}\n" + 
								"    condition: ALL_PREVIOUS_STEPS_WERE_SUCCESSFUL\n" + 
								"  - !CommandStep\n" + 
								"    name: run\n" + 
								"    image: "+ imageName +"\n" + 
								"    commands:\n" + commands +
								"    condition: ALL_PREVIOUS_STEPS_WERE_SUCCESSFUL\n" + 
								"  retryCondition: never\n" + 
								"  maxRetries: 3\n" + 
								"  retryDelay: 30\n" + 
								"  cpuRequirement: 250m\n" + 
								"  memoryRequirement: 128m\n" + 
								"  timeout: 3600";
								
						String content = "version: 6\n" + 
								"jobs:\n" + compileJob;
						int state = -1;
						String oldBuilSpec = "";//用于复原无所需的job 
						/*
						 * 三种状态：无.onedev-build.yml文件(0);空job(1);无所需的job(2);若有所需的job，无论command是否正确，更新
						 */
						TreeWalk exist = null;
						try (RevWalk revWalk = new RevWalk(repository)) {
							RevTree revTree = revWalk.parseCommit(preCommitId).getTree();
							exist = TreeWalk.forPath(repository, BuildSpec.BLOB_PATH, revTree);
						} catch (MissingObjectException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IncorrectObjectTypeException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} 
						if(exist != null) {//不是空文件;若是空文件的话无需操作
							BuildSpec buildSpec = project.getBuildSpec(preCommitId);
							if(buildSpec.getJobs().size() == 0) {//空job
								oldPaths.add(BuildSpec.BLOB_PATH);
								state = 1;
							}
							else if(buildSpec.getJobMap().get(jobName) == null) {//无所需的job 
								oldBuilSpec = project.getBlob(new BlobIdent(revision, BuildSpec.BLOB_PATH, FileMode.REGULAR_FILE.getBits()), true).getText().getContent();
								content = oldBuilSpec + compileJob;//将特定的job加到原buildspec后面
								state = 2;
							}
						}
						else {
							state = 0;
						}
						//提交或修改.onedev-build.yml文件（添加指定job）
						newBlobs.put(BuildSpec.BLOB_PATH,new BlobContent.Immutable(content.getBytes(), FileMode.REGULAR_FILE));
						newCommitId = new BlobEdits(oldPaths, newBlobs).commit(repository, refName, 
								preCommitId, preCommitId, user.asPerson(), "在线编译-提交/修改.onedev-build.yml文件");
						
						//复原编译前状态
						newBlobs = new HashMap<>();
						oldPaths = new HashSet<>();
						oldPaths.add(BuildSpec.BLOB_PATH);
						if(state == 0) {//无.onedev-build.yml文件
						}
						else if(state == 1) {//空job
							newBlobs.put(BuildSpec.BLOB_PATH,new BlobContent.Immutable("version: 6".getBytes(), FileMode.REGULAR_FILE));
						}
						else if(state == 2) {//无所需的job
							newBlobs.put(BuildSpec.BLOB_PATH,new BlobContent.Immutable(oldBuilSpec.getBytes(), FileMode.REGULAR_FILE));
						}
						new BlobEdits(oldPaths, newBlobs).commit(repository, refName, 
								newCommitId, newCommitId, user.asPerson(), "在线编译-复原编译前状态");
						//构建编译
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
						OneDev.getInstance(BuildManager.class).create(build);
						setResponsePage(BuildLogPage.class, BuildLogPage.paramsOf(build));
					}
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
