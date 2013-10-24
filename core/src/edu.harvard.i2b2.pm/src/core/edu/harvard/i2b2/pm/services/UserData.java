package edu.harvard.i2b2.pm.services;

public class UserData {
    // every persistent object needs an identifier
    private String oid = null;

    private String name = new String();
    private String value = new String();
    private String project = new String();
    private String user = new String();
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getOid() {
		return oid;
	}
	public void setOid(String oid) {
		this.oid = oid;
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
