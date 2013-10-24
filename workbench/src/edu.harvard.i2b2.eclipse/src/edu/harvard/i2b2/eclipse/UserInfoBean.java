/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors:
 *     Mike Mendis - initial API and implementation
 */

package edu.harvard.i2b2.eclipse;

//import java.util.Hashtable;
import java.util.ArrayList;
import java.util.List;

import edu.harvard.i2b2.pm.datavo.pm.CellDataType;
import edu.harvard.i2b2.pm.datavo.pm.CellDatasType;
import edu.harvard.i2b2.pm.datavo.pm.ParamType;
import edu.harvard.i2b2.pm.datavo.pm.ProjectType;

/**
 * class to store user details from web service
 * @author Michael Mendis
 *
 */
public class UserInfoBean {

	private static UserInfoBean instance = null;

	private static String userName;
	private static String userPassword;
	private static String userFullName;
	private static String userDomain;

	private static String environment;

	private static String helpURL;

	private static String pmResponse;
	public static void pmResponse(String str) {pmResponse = new String(str);}
	public static String pmResponse() {return pmResponse;}

	private static ProjectType selectedProject;
	public static void selectedProject(ProjectType p) {selectedProject=p;}
	public static ProjectType selectedProject() {return selectedProject;}

	public static String selectedProjectID() {return selectedProject.getId();}

	private static List<String> projectList;
	private static List<String> cellList;
	private static List<ProjectType> projects;

	private static CellDatasType cellDatas;

	private static List<ParamType> globals;

	public static UserInfoBean getInstance() {
		if (instance == null)
			instance = new UserInfoBean();
		return instance;
	}

	public String getProjectId() {
		return selectedProject.getId();
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		UserInfoBean.userName = userName;
	}

	public CellDataType getCellData(String id) {
		if (cellDatas == null)
			return null;
		for (CellDataType cellData :cellDatas.getCellData())
		{
			if (cellData.getId().toLowerCase().equals(id.toLowerCase()))
				return cellData;
		}
		return null;
	}

	public List<String> getCellList()
	{
		//if (cellList == null)
		//	cellList = new ArrayList<String>();
		return cellList; 
	}

	public String getCellDataSpecial(String id) {
		if (cellDatas == null)
			return null;
		for (CellDataType cellData :cellDatas.getCellData())
		{
			if (cellData.getId().toLowerCase().equals(id.toLowerCase()))
				return cellData.getSpecial();
		}
		return null;
	}

	public String getSelectedProjectParam(String name){
		List<ParamType> params = selectedProject.getParam();
		for(int i=0; i<params.size(); i++) {
			ParamType param = params.get(i);
			if (param.getName().toLowerCase().equals(name.toLowerCase()))
				return param.getValue();
		}
		return null;
	}
	public String getCellDataParam(String id, String name) {
		if (cellDatas == null)
			return null;
		for (CellDataType cellData :cellDatas.getCellData())
			if (cellData.getId().toLowerCase().equals(id.toLowerCase()))
			{
				for (ParamType param :cellData.getParam())
				{
					if (param.getName().toLowerCase().equals(name.toLowerCase()))
						return param.getValue();
				}
			}
		return null;
	}

	public String getCellName(String id) {
		if (cellDatas == null)
			return null;
		for (CellDataType cellData :cellDatas.getCellData())
		{
			if (cellData.getId().toLowerCase().equals(id.toLowerCase()))
				return cellData.getName();
		}
		return null;
	}

	public boolean isCoreCell(String id)
	{
		if ((id.equalsIgnoreCase("ONT")) ||
				(id.equalsIgnoreCase("FR")) ||
				(id.equalsIgnoreCase("CRC")) ||
				(id.equalsIgnoreCase("WORK")) )
			return true;
		else
			return false;
	}
	public String getCellDataUrl(String id) {
		if (cellDatas == null)
			return null;
		for (CellDataType cellData :cellDatas.getCellData())
		{
			if (cellData.getId().toLowerCase().equals(id.toLowerCase()))
				return cellData.getUrl();
		}
		return null;
	}

	public String getCellDataMethod(String id) {
		if (cellDatas == null)
			return null;
		for (CellDataType cellData :cellDatas.getCellData())
		{
			if (cellData.getId().toLowerCase().equals(id.toLowerCase()))
				return cellData.getMethod();
		}
		return null;
	}

	public void setCellDatas(CellDatasType cellDatas) {
		cellList = new ArrayList<String>();
		for (CellDataType cellData :cellDatas.getCellData())
		{
			cellList.add(cellData.getId());
		}

		UserInfoBean.cellDatas = cellDatas;
	}

	public List<String> getProjectRoles(String project) {
		for (ProjectType param :projects)
			if (param.getId().toLowerCase().equals(project.toLowerCase()))
				return param.getRole();
		return null;

	}

	public boolean isRoleInProject(String project)
	{
		if (
				selectedProject().getRole().contains(project.toUpperCase())
		)
			return true;
		else
			return false;
	}
	public List<String> getProjectRoles() {
		return selectedProject().getRole();
	}

	public List<String> getProjectList() {
		if (projectList == null) {
			projectList = new ArrayList<String>();
		}
		return projectList;
	}

	public String getUserPassword() {
		return userPassword;
	}

	public void setUserPassword(String userPassword) {
		UserInfoBean.userPassword = userPassword;
	}

	public String getHelpURL() {
		return helpURL;
	}

	public void setHelpURL(String helpURL) {
		UserInfoBean.helpURL = helpURL;
	}

	public String getEnvironment() {
		return environment;
	}

	public void setEnvironment(String environment) {
		UserInfoBean.environment = environment;
	}

	public String getUserFullName() {
		return userFullName;
	}

	public void setUserFullName(String userFullName) {
		UserInfoBean.userFullName = userFullName;
	}


	/**
	 * constructor
	 */
	//public UserInfoBean() {

	//}

	public List<ProjectType> getProjects() {
		return projects;
	}

	public void setProjects(List<ProjectType> pType) {
		projectList = new ArrayList<String>();
		for (ProjectType project :pType)
			projectList.add(project.getId());

		UserInfoBean.projects = pType;
	}

	public String getGlobals(String name) {
		for (ParamType param :globals)
		{
			if (param.getName().toLowerCase().equals(name.toLowerCase()))
				return param.getValue();
		}
		return null;
	}

	public void setGlobals(String name, String value) {
		if (globals == null)
			globals = new ArrayList<ParamType>();

		ParamType pt = new ParamType();
		pt.setName(name);
		pt.setValue(value);

		globals.add(pt);
	}

	public String getUserDomain() {
		return userDomain;
	}

	public void setUserDomain(String userDomain) {
		UserInfoBean.userDomain = userDomain;
	}

}