package io.onedev.server.web.page.admin.user;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.User;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.WebSession;
import io.onedev.server.web.ajaxlistener.ConfirmClickListener;
import io.onedev.server.web.behavior.OnTypingDoneBehavior;
import io.onedev.server.web.component.datatable.OneDataTable;
import io.onedev.server.web.component.link.ActionablePageLink;
import io.onedev.server.web.component.user.UserAvatar;
import io.onedev.server.web.page.admin.AdministrationPage;
import io.onedev.server.web.page.admin.user.create.NewUserPage;
import io.onedev.server.web.page.admin.user.profile.UserProfilePage;
import io.onedev.server.web.page.project.ProjectListPage;
import io.onedev.server.web.util.LoadableDetachableDataProvider;
import io.onedev.server.web.util.PagingHistorySupport;

@SuppressWarnings("serial")
public class UserListPage extends AdministrationPage {

	private static final String PARAM_PAGE = "page";
	
	private static final String PARAM_QUERY = "query";
	
	private TextField<String> searchField;
	
	private DataTable<User, Void> usersTable;
	
	private String query;
	
	private boolean typing;
	
	public UserListPage(PageParameters params) {
		super(params);
		
		query = params.get(PARAM_QUERY).toString();
	}
	
	private EntityCriteria<User> getCriteria() {
		EntityCriteria<User> criteria = EntityCriteria.of(User.class);
		criteria.add(Restrictions.not(Restrictions.eq("id", User.SYSTEM_ID)));
		if (query != null) {
			criteria.add(Restrictions.or(
					Restrictions.ilike("name", query, MatchMode.ANYWHERE), 
					Restrictions.ilike("fullName", query, MatchMode.ANYWHERE)));
		} else {
			criteria.setCacheable(true);
		}
		return criteria;
	}
	
	@Override
	protected void onPopState(AjaxRequestTarget target, Serializable data) {
		super.onPopState(target, data);
		query = (String) data;
		getPageParameters().set(PARAM_QUERY, query);
		target.add(searchField);
		target.add(usersTable);
	}

	@Override
	protected void onBeforeRender() {
		typing = false;
		super.onBeforeRender();
	}

	
	
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		
		//查询用户
		add(searchField = new TextField<String>("filterUsers", new IModel<String>() {

			@Override
			public void detach() {
			}

			@Override
			public String getObject() {
				return query;
			}

			@Override
			public void setObject(String object) {
				query = object;
				
				//获取查询用户框的输入信息
				PageParameters params = getPageParameters();
				params.set(PARAM_QUERY, query);
				params.remove(PARAM_PAGE);
				
				//获取查询用户请求的url链接
				String url = RequestCycle.get().urlFor(UserListPage.class, params).toString();
				System.out.println(url);
				
				AjaxRequestTarget target = RequestCycle.get().find(AjaxRequestTarget.class);
				
				if (typing)
					replaceState(target, url, query);
				else
					pushState(target, url, query);
				
				usersTable.setCurrentPage(0);
				target.add(usersTable);
				
				typing = true;
			}
			
		}));
		
		
		
		
		searchField.add(new OnTypingDoneBehavior(100) {

			@Override
			protected void onTypingDone(AjaxRequestTarget target) {
			}

		});
		
		
		//添加新用户
		add(new Link<Void>("addNew") {

			@Override
			public void onClick() {
				setResponsePage(NewUserPage.class);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(SecurityUtils.isAdministrator());
			}
			
		});
		
		
		
		
		List<IColumn<User, Void>> columns = new ArrayList<>();
		
		columns.add(new AbstractColumn<User, Void>(Model.of("用户名")) {

			@Override
			public void populateItem(Item<ICellPopulator<User>> cellItem, String componentId, IModel<User> rowModel) {
				
				//获取所有用户
				User user = rowModel.getObject();
				
				//每一个用户就在一个Fragment下
				Fragment fragment = new Fragment(componentId, "nameFrag", UserListPage.this);
				
				//用户信息页链接
				WebMarkupContainer link = new ActionablePageLink<Void>("link", UserProfilePage.class, UserProfilePage.paramsOf(user)) {

					@Override
					protected void doBeforeNav(AjaxRequestTarget target) {
						String redirectUrlAfterDelete = RequestCycle.get().urlFor(UserListPage.class, getPageParameters()).toString();
						WebSession.get().setRedirectUrlAfterDelete(User.class, redirectUrlAfterDelete);
					}
					
				};
				link.add(new UserAvatar("avatar", user));
				link.add(new Label("name", user.getName()));
				fragment.add(link);
				cellItem.add(fragment);
			}
		});
		
		
		
		
		columns.add(new AbstractColumn<User, Void>(Model.of("全名")) {

			@Override
			public String getCssClass() {
				return "d-none d-lg-table-cell";
			}
			
			@Override
			public void populateItem(Item<ICellPopulator<User>> cellItem, String componentId, IModel<User> rowModel) {
				cellItem.add(new Label(componentId, rowModel.getObject().getFullName()));
			}
			
		});
		
		
		
		
		columns.add(new AbstractColumn<User, Void>(Model.of("电子邮箱")) {

			@Override
			public String getCssClass() {
				return "d-none d-lg-table-cell";
			}
			
			@Override
			public void populateItem(Item<ICellPopulator<User>> cellItem, String componentId, IModel<User> rowModel) {
				cellItem.add(new Label(componentId, rowModel.getObject().getEmail()));
			}
			
		});
		
		
		
		
		columns.add(new AbstractColumn<User, Void>(Model.of("账户来源")) {

			@Override
			public String getCssClass() {
				return "d-none d-lg-table-cell";
			}
			
			@Override
			public void populateItem(Item<ICellPopulator<User>> cellItem, String componentId, IModel<User> rowModel) {
				cellItem.add(new Label(componentId, rowModel.getObject().getAuthSource()));
			}
			
		});
		
		
		
		columns.add(new AbstractColumn<User, Void>(Model.of("操作")) {

			@Override
			public void populateItem(Item<ICellPopulator<User>> cellItem, String componentId, IModel<User> rowModel) {
				Fragment fragment = new Fragment(componentId, "actionsFrag", UserListPage.this);
				
				//删除用户
				fragment.add(new AjaxLink<Void>("delete") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						User user = rowModel.getObject();
						OneDev.getInstance(UserManager.class).delete(user);
						Session.get().success("用户'" + user.getDisplayName() + "'已被删除");
						
						target.add(usersTable);
					}

					@Override
					protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
						super.updateAjaxAttributes(attributes);
						
						User user = rowModel.getObject();
						String message = "你真的想删除用户'" + user.getDisplayName() + "'吗?";
						attributes.getAjaxCallListeners().add(new ConfirmClickListener(message));
					}

					@Override
					protected void onComponentTag(ComponentTag tag) {
						super.onComponentTag(tag);
						if (!isEnabled())
							tag.put("disabled", "禁用");
						User user = rowModel.getObject();
						if (user.isRoot())
							tag.put("title", "不能删除Root用户");
						else if (user.equals(SecurityUtils.getUser()))
							tag.put("title", "你不能删除自己");
					}

					@Override
					protected void onConfigure() {
						super.onConfigure();
						User user = rowModel.getObject();
						setEnabled(!user.isRoot() && !user.equals(SecurityUtils.getUser()));
					}

				});
				
				
				//切换用户
				fragment.add(new Link<Void>("impersonate") {

					@Override
					public void onClick() {
						SecurityUtils.getSubject().runAs(rowModel.getObject().getPrincipals());
						setResponsePage(ProjectListPage.class);
					}
				});
				
				cellItem.add(fragment);
			}

			
			@Override
			public String getCssClass() {
				return "actions";
			}
			
		});
		
		
		
		
		SortableDataProvider<User, Void> dataProvider = new LoadableDetachableDataProvider<User, Void>() {

			@Override
			public Iterator<? extends User> iterator(long first, long count) {
				EntityCriteria<User> criteria = getCriteria();
				criteria.addOrder(Order.asc("name"));
				return OneDev.getInstance(UserManager.class).query(criteria, (int)first, (int)count).iterator();
			}

			@Override
			public long calcSize() {
				return OneDev.getInstance(UserManager.class).count(getCriteria());
			}

			@Override
			public IModel<User> model(User object) {
				Long id = object.getId();
				return new LoadableDetachableModel<User>() {

					@Override
					protected User load() {
						return OneDev.getInstance(UserManager.class).load(id);
					}
					
				};
			}
		};
		
		PagingHistorySupport pagingHistorySupport = new PagingHistorySupport() {
			
			@Override
			public PageParameters newPageParameters(int currentPage) {
				PageParameters params = new PageParameters();
				params.add(PARAM_PAGE, currentPage+1);
				if (query != null)
					params.add(PARAM_QUERY, query);
				return params;
			}
			
			@Override
			public int getCurrentPage() {
				return getPageParameters().get(PARAM_PAGE).toInt(1)-1;
			}
			
		};
		
		add(usersTable = new OneDataTable<User, Void>("users", columns, dataProvider, 
				WebConstants.PAGE_SIZE, pagingHistorySupport));
	}

	
	
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new UserCssResourceReference()));
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, "用户列表");
	}

}
