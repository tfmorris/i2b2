/*
 * Created on Jul 22, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package edu.harvard.i2b2.common.util;


import java.io.*;
import java.util.*;

import org.jdom.input.*;
import org.jdom.*;

/**
 * @author mem61
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class PatientDataXMLParser {
    private Document doc = null;
    private Document realOriginalDoc = null;
    private Date updateDate  = null;
    private Date downloadDate  = null;
    private Date importDate = null;
    private String blob = null;
    private String sourceSystem = null;

	public PatientDataXMLParser() { }

    public Element getRootElement (String xml) throws Exception {
        SAXBuilder builder = new SAXBuilder();
        doc = builder.build(new StringReader(xml));
        return doc.getRootElement(); 
    }
    
    public String[] parseXML (String xml, String parent, String child) throws Exception {
        SAXBuilder builder = new SAXBuilder();
        doc = builder.build(new StringReader(xml));
        List i2b2 = doc.getRootElement().getChildren(parent);
    
        String[] results = new String[i2b2.size()];
        int count = 0;
        for (Iterator i = i2b2.iterator(); i.hasNext();)  {
            Element e = (Element)i.next();
            results[count++] = e.getChild(child).getText();
        }
        realOriginalDoc = (Document) doc.clone();
        return results; 
    }

    public String[] parseXML (Document i2b2In, String parent, String child) throws Exception {
    	doc = i2b2In;
        List i2b2 = doc.getRootElement().getChildren(parent);
    
        String[] results = new String[i2b2.size()];
        int count = 0;
        for (Iterator i = i2b2.iterator(); i.hasNext();)  {
            Element e = (Element)i.next();
            results[count++] = e.getChild(child).getText();
        }
        realOriginalDoc = (Document) doc.clone();
        return results; 
    }
    
    public String setI2B2XML (String[] changes, String parent, String child, boolean overwriteOrig) throws Exception {

        if (doc == null) return null;
        Document tempDoc = doc;
        List i2b2 = tempDoc.getRootElement().getChildren(parent);
    
        int count = 0;
        for (Iterator i = i2b2.iterator(); i.hasNext();)  {
            Element e = (Element)i.next();
            e.getChild(child).setText(changes[count++]);
        }
        org.jdom.output.XMLOutputter out = new org.jdom.output.XMLOutputter();
        //out.setEncoding("ISO-8859-1");
        if (overwriteOrig)
            doc = tempDoc;
        return out.outputString(tempDoc);
    }

    public boolean appendI2B2XML (String[] changes, String[] units, String code, String id, String parent, String child, String unit, boolean cdata) throws Exception {

        if (realOriginalDoc == null) return false;
        Document tempDoc = (Document) realOriginalDoc.clone();
        List i2b2 = tempDoc.getRootElement().getChildren(parent);
        
        List origi2b2 = doc.getRootElement().getChildren(parent);
    
        
        int count = 0;
        for (Iterator i = i2b2.iterator(); i.hasNext();)  {
            Element e = (Element)i.next();
            //kinda workgin e.getChild(child).setText(new CDATA(changes[count++]).getText());
            if (cdata)
            	e.getChild(child).setText((new CDATA(changes[count])).toString());
            else
            	e.getChild(child).setText(changes[count]);
            e.getChild(code).setText(id) ;
        	e.getChild(unit).setText(units[count++]);

            if (updateDate != null)
                e.getChild(Constant.UPDATE_DATE_NAME).setText(Constant.timestampFormat.format(updateDate));
            if (downloadDate != null)
                e.getChild(Constant.DOWNLOAD_DATE_NAME).setText(Constant.timestampFormat.format(downloadDate));
            if (importDate != null)
                e.getChild(Constant.IMPORT_DATE_NAME).setText(Constant.timestampFormat.format(importDate));
            if (sourceSystem != null)
                e.getChild(Constant.SOURCE_SYSTEM_NAME).setText(sourceSystem);
            if (blob != null)
                e.getChild(Constant.OBSERVATION_BLOB).setText(blob);
            
           	origi2b2.add(e.clone()); 
        }
        org.jdom.output.XMLOutputter out = new org.jdom.output.XMLOutputter();

        return true;
    }
    
    public void setDownloadDate(Date s)
    {
        downloadDate = s;  
    }
    public void setImportDate(Date s)
    {
    	importDate = s;   
    }   
    public void setSourceSystem(String s)
    {
    	sourceSystem = s;   
    }
     
    public Date getDownloadDate()
    {
        return downloadDate;   
       }
    public Date getImportDate()
    {
        return importDate;   
       }
    public String getSourceSystem()
    {
     return sourceSystem;   
    }
    
    public Document toDoc()
    {
    	return doc;
    }
    
    public String toString()
    {
        org.jdom.output.XMLOutputter out = new org.jdom.output.XMLOutputter();
        return out.outputString(doc);
    }

	public Date getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}
	
    public String getBlob() {
		return blob;
	}

	public void setBlob(String blob) {
		this.blob = blob;
	}

}
