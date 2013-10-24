/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors:
 * 		Lori Phillips
 */
 package edu.harvard.i2b2.ontology.ejb;

import edu.harvard.i2b2.ontology.datavo.vdo.GetChildrenType;
import edu.harvard.i2b2.ontology.datavo.vdo.GetTermInfoType;
import edu.harvard.i2b2.ontology.datavo.vdo.VocabRequestType;
import edu.harvard.i2b2.ontology.util.StringUtil;


public class NodeType {

	protected String node;
	protected String type;
	protected Boolean blob;
	
	
    public boolean isBlob() {
        if (blob == null) {
            return false;
        } else {
            return blob;
        }
    }
    
	public void setBlob(Boolean value) {
		this.blob = value;
	}
	public String getNode() {
		return node;
	}
	public void setNode(String node) {
		this.node = node;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
	public NodeType() {}
	
	
	public NodeType(GetChildrenType childrenType) {
		node = StringUtil.getTableCd(childrenType.getParent());
		if (childrenType.getType().equals("default")){
			type = "core";
		}else {
			type = childrenType.getType();
		}
		blob = childrenType.isBlob();
	}
	
	public NodeType(GetTermInfoType infoType) {
		node = StringUtil.getTableCd(infoType.getSelf());
		if (infoType.getType().equals("default")){
			type = "core";
		}else {
			type = infoType.getType();
		}
		blob = infoType.isBlob();
	}
	
	public NodeType(VocabRequestType vocabType) {
		node = vocabType.getCategory();
		type = vocabType.getType();
		blob = vocabType.isBlob();
	}
}
