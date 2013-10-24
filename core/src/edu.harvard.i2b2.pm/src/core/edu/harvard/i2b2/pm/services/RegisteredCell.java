package edu.harvard.i2b2.pm.services;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RegisteredCell {
    // every persistent object needs an identifier
    //private String oid = new String();

    private String name = new String();
    private String url = new String();
    private String id = new String();
    private String webservice = new String();
    //private List<RegisteredCellParam> param;
    private Set params = new HashSet();
    
    public String getWebservice() {
		return webservice;
	}
	public void setWebservice(String webservice) {
		this.webservice = webservice;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	/*
	public String getOid() {
		return oid;
	}
	public void setOid(String oid) {
		this.oid = oid;
	}
	*/
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	/*
    public List<RegisteredCellParam> getParam() {
        if (param == null) {
            param = new ArrayList<RegisteredCellParam>();
        }
        return this.param;
    }
    */
	public Set getParams() {
		return params;
	}
	public void setParams(Set params) {
		this.params = params;
	}
}
