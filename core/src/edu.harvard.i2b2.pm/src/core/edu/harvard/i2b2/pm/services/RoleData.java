package edu.harvard.i2b2.pm.services;

public class RoleData {
    // every persistent object needs an identifier
    
	private String oid = null;
    private String role = new String();
    private String project = new String();
    private String user = new String();
	public String getOid() {
		return oid;
	}
	public void setOid(String oid) {
		this.oid = oid;
	}
	public String getProject() {
		return project;
	}
	public void setProject(String project) {
		this.project = project;
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
}
