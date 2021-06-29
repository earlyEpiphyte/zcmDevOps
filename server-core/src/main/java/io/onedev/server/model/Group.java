package io.onedev.server.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.util.EditContext;
import io.onedev.server.util.NameAware;
import io.onedev.server.web.editable.annotation.Editable;

@Entity
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
@Editable
public class Group extends AbstractEntity implements NameAware {

	private static final long serialVersionUID = 1L;

	@Column(unique=true, nullable=false)
	private String name;
	
	private String description;
	
	private boolean administrator;
	
	@OneToMany(mappedBy="group", cascade=CascadeType.REMOVE)
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
	private Collection<GroupAuthorization> authorizations = new ArrayList<>();
	
	@OneToMany(mappedBy="group", cascade=CascadeType.REMOVE)
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
	private Collection<Membership> memberships = new ArrayList<>();
	
	private transient Collection<User> members;
	
	@Editable(name="分组名称",order=100)
	@NotEmpty
	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Editable(name="描述",order=200, description="可选的描述分组")
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Editable(order=300, name="选为管理员组")
	public boolean isAdministrator() {
		return administrator;
	}

	public void setAdministrator(boolean administrator) {
		this.administrator = administrator;
	}

	@SuppressWarnings("unused")
	private static boolean isAdministratorDisabled() {
		return !(boolean) EditContext.get().getInputValue("administrator");
	}
	
	public Collection<GroupAuthorization> getAuthorizations() {
		return authorizations;
	}

	public void setAuthorizations(Collection<GroupAuthorization> authorizations) {
		this.authorizations = authorizations;
	}

	public Collection<Membership> getMemberships() {
		return memberships;
	}

	public void setMemberships(Collection<Membership> memberships) {
		this.memberships = memberships;
	}

	public Collection<User> getMembers() {
		if (members == null) {
			members = new HashSet<>();
			for (Membership membership: getMemberships()) {
				members.add(membership.getUser());
			}
		}
		return members;
	}

	@Override
	public int compareTo(AbstractEntity entity) {
		Group group = (Group) entity;
		return getName().compareTo(group.getName());
	}
	
}
