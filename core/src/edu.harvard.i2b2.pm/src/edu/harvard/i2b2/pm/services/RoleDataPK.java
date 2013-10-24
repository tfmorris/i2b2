package edu.harvard.i2b2.pm.services;

import java.io.Serializable;

public class RoleDataPK implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// every persistent object needs an identifier

    private String role = new String();
    private String project = new String();
    private String user = new String();

    public RoleDataPK() {
    }

    public RoleDataPK(RoleDataPK roleDataPK) {
    	this.role = roleDataPK.role;
    	this.project = roleDataPK.project;
    	this.user = roleDataPK.user;
    }
    
    public boolean equals(RoleDataPK roleDataPK) {
    	return (this.role.equals(roleDataPK.role) &&
    	this.project.equals(roleDataPK.project) &&
    	this.user.equals(roleDataPK.user));
    	}
   
    public boolean equals(Object obj) {
    	if(this == obj)
    	return true;
    	if((obj == null) || (obj.getClass() != this.getClass()))
    	return false;
    	// object must be Test at this point
    	RoleDataPK roleDataPK = (RoleDataPK)obj;
    	return (this.role.equals(roleDataPK.role) &&
    	this.project.equals(roleDataPK.project) &&
    	this.user.equals(roleDataPK.user));
    	}
    public int hashCode () {
    	return new HashCodeBuilder().
    	append(getRole()).
    	append(getProject()).
    	append(getUser()).getHashCode();
    	}
	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public String getProject() {
		return project;
	}
	public void setProject(String project) {
		this.project = project;
	}
	
}