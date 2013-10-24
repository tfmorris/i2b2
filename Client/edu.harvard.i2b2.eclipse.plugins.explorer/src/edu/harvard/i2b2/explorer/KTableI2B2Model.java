/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *     Christopher D. Herrick 
 *     Wensong Pan
 *     
 */
package edu.harvard.i2b2.explorer;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.LinkedHashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Text;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

public class KTableI2B2Model implements KTableModel {

	  private int[] colWidths;
	  private int rowHeight;
	  private int rowCount;
	  private HashMap<String, Object> content;
	  private static KTableCellRenderer colorRenderer = new KTableColorCellRenderer();
	  private HashMap<RGB, String> colorMap = new HashMap<RGB, String>(150);
	  
	  /**
	   * 
	   */
	  public KTableI2B2Model() {
	    colWidths = new int[getColumnCount()];
	    colWidths[0] = 40;
	    colWidths[1] = 278;
	    colWidths[2] = 100;
	    colWidths[3] = 150;
	    colWidths[4] = 80;
	    colWidths[5] = 37;
	    
	    rowHeight = 18;
	    rowCount = 1;
	    content = new HashMap<String, Object>();
	    
	    content.put("0/0", "Row #");
	    content.put("1/0", "Drag Concepts Into Table");
	    content.put("2/0", "Value");
	    content.put("3/0", "Value Text");
	    content.put("4/0", "Height");
	    content.put("5/0", "Color");
	    
	    populateColorMap();
	  }

	  // Inhalte

	  public Object getContentAt(int col, int row) {
	     // System.out.println("col "+col+" row "+row);
		  
		  if(col == 5 && row > 0)
		  {
		    Object erg = content.get(col + "/" + row);
		    if (erg != null)
		      return (RGB) erg;
		    else		    	
			   return new RGB(0, 255, 0);
	    }
		  else {
			  String erg = (String) content.get(col + "/" + row);
			  if (erg != null)
				  return erg;
			  else 
				  return "";
		  }
	  }

	  /*
	   * overridden from superclass
	   */
	  public KTableCellEditor getCellEditor(int col, int row) {
	    if ((col == 5)&&(row>0)) 
	    {
	    	KTableCellEditorColor e = new KTableCellEditorColor();
	    	return e;
	    } else if ((col == 4)&&(row>0)) {
	    	KTableCellEditorCombo combo = new KTableCellEditorCombo();
	    	combo.setItems(new String[]{"Very Tall", "Tall", "Medium", "Low", "Very Low"});
	    	return combo;
	    } else if ((col == 2)&&(row>0)) {
	    	KTableCellEditorCombo combo = new KTableCellEditorCombo();
	    	combo.setItems(new String[]{"NVAL_NUM", "TVAL_CHAR", "Location_CD", "Confidence_NUM", 
	    			"Location_Path", "InOut_CD"});
	    	return combo;
	    } else if ((col == 3)&&(row>0)) {
	    	KTableCellEditorComboW combo = new KTableCellEditorComboW();
	    	combo.setItems(new String[]{" = ", " > ", " < ", " between and ", " like "});
	    	return combo;
	    } else {
	    	return new KTableCellEditorText();
	    }
	  }

	  /*
	   * overridden from superclass
	   */
	  public void setContentAt(int col, int row, Object value) {
		  if (row>rowCount)
			  rowCount = row;
		  else if (row==rowCount)
			  rowCount = row + 1;
	    content.put(col + "/" + row, value);
	  }
	  
	  public void populateTable(ArrayList list) {
		  int newRow = 0;
			for(int i=0; i<list.size(); i++) {
				ArrayList alist = (ArrayList) list.get(i);
				for(int j=0; j<alist.size(); j++) {
					TableRow r = (TableRow) alist.get(j); 
					newRow++;
					r.rowId = newRow;
			        setContentAt(0, newRow, new Integer(r.rowNumber).toString());
			        setContentAt(1, newRow, r.conceptName);
			        setContentAt(2, newRow, r.valueType);
			        setContentAt(3, newRow, r.valueText);			        
			        setContentAt(4, newRow, r.height);
			        setContentAt(5, newRow, r.color);
			        setContentAt(6, newRow, r.conceptXml);				        
				}
			}
	  }
	  
	  public void deleteAllRows()
	  {
		  for (int i=1; i<rowCount; i++)
		  {
			  content.remove("0/" + i);
			  content.remove("1/" + i);
			  content.remove("2/" + i);
			  content.remove("3/" + i);	
			  content.remove("4/" + i);
			  content.remove("5/" + i);
			  content.remove("6/" + i);
		  }
		  rowCount = 1;
	  }
	  
	  public void fillDataFromTable(ArrayList<ArrayList<TableRow>> list) {
		  list.clear();
		  TableRow row = null;
		  ArrayList<TableRow> group = null;
		  Integer curRow = null;
		  LinkedHashMap<Integer, ArrayList<TableRow>> rowMap = 
			  new LinkedHashMap<Integer, ArrayList<TableRow>>();
		    
		  for (int i=1; i<rowCount; i++){
			  row = new TableRow();
			  curRow = new Integer((String) content.get("0/" + i));
			  row.rowNumber = curRow.intValue();
			  if(!rowMap.containsKey(curRow)) {			 
				  group = new ArrayList<TableRow>();
				  list.add(group);
				  rowMap.put(curRow, group);
			  } else {
				  group = rowMap.get(curRow);
			  }
			  row.conceptName = (String) content.get("1/" + i);
			  row.valueType = (String) content.get("2/" + i);
			  row.valueText = (String) content.get("3/" + i);
			  row.height = (String) content.get("4/" + i);
			  row.color = (RGB) content.get("5/" + i);
			  row.conceptXml = (String) content.get("6/" + i);
			  row.rowId = i;
			  group.add(row);
		  }
	  }
	  
	  private String getFullname(String xmlcontent, SAXBuilder parser) {
		  String fullname = null;
		  try{
			  java.io.StringReader xmlStringReader = new java.io.StringReader(xmlcontent);
			  org.jdom.Document tableDoc = parser.build(xmlStringReader);
			  org.jdom.Element tableXml = tableDoc.getRootElement();
			  //List conceptChildren = tableXml.getChildren();
			
			  //for (Iterator itr=conceptChildren.iterator(); itr.hasNext();){
				
				//Element queryEntryXml = (org.jdom.Element) itr.next();
				//Element conceptXml = (Element) queryEntryXml.getChild("Concept");
				Element conTableXml = (Element) tableXml;//.getChildren().get(0);
				org.jdom.Element fullnameXml = conTableXml.getChild("key");
				fullname = fullnameXml.getText().substring(fullnameXml.getText().indexOf("\\", 2));
			//}
		  }
		  catch(Exception e) {
			  e.printStackTrace();
		  }
		  
		  return fullname;
	  }
	  
	  private String getLookupTable(String xmlcontent, SAXBuilder parser) {
		  String lookuptablename = null;
		  try{
			  java.io.StringReader xmlStringReader = new java.io.StringReader(xmlcontent);
			  org.jdom.Document tableDoc = parser.build(xmlStringReader);
			  org.jdom.Element tableXml = tableDoc.getRootElement();
			  //List conceptChildren = tableXml.getChildren();
			
			  //for (Iterator itr=conceptChildren.iterator(); itr.hasNext();){
				
				//Element queryEntryXml = (org.jdom.Element) itr.next();
				//Element conceptXml = (Element) queryEntryXml.getChild("Concept");
				//Element conTableXml = (Element) tableXml.getChildren().get(0);
				org.jdom.Element lookuptablenameXml = tableXml.getChild("tablename");
				if(lookuptablenameXml == null) {
					lookuptablename = "i2b2";
				}
				else {
					lookuptablename = lookuptablenameXml.getText();
				}
			//}
		  }
		  catch(Exception e) {
			  e.printStackTrace();
		  }
		  
		  return lookuptablename;
	  }
	  
	  public ArrayList<TimelineRow> getTimelineRows(ArrayList<ArrayList<TableRow>> rowData){
		  ArrayList<TimelineRow> rows = new  ArrayList<TimelineRow>();
		  //String curRow = null;
		  String curFullPath = null;
		  SAXBuilder parser = new SAXBuilder();
		  PDOSet pdoset = null; 
		  ValueDisplayProperty valdp; 
		  
		  for(int i=0; i<rowData.size(); i++) {
			  ArrayList<TableRow> timelineRowData = rowData.get(i);
			  TimelineRow timelineRow = new TimelineRow();
			  rows.add(timelineRow);
			  String curName = "";
			  
			  for(int j=0; j<timelineRowData.size(); j++) {
				  TableRow tableRow = timelineRowData.get(j);
				  if(curName.equalsIgnoreCase("")) {
					  curName = tableRow.conceptName;
					  timelineRow.displayName += curName;
				  }
				  else if(!tableRow.conceptName.equalsIgnoreCase(curName)) {
					  curName = tableRow.conceptName;
					  timelineRow.displayName += tableRow.conceptName;
				  }
				  
				  String tmpcurFullPath = getFullname(tableRow.conceptXml, parser);
				  String lookuptable = this.getLookupTable(tableRow.conceptXml, parser);
				  if(curFullPath == null) {
					  pdoset = new PDOSet();
					  timelineRow.pdoSets.add(pdoset);
					  curFullPath = new String(tmpcurFullPath);  
					  pdoset.fullPath = new String(tmpcurFullPath);
					  pdoset.tableType = new String(lookuptable);
					  
					  //RGB contentColor = tableRow.color;
					  if (colorMap.containsKey(tableRow.color)) {
						  pdoset.color = colorMap.get(tableRow.color);
					  }
					  else {
						  pdoset.color = "lightbrown";
					  }
	
					  pdoset.height = tableRow.height;
					  
					  if(tableRow.valueType.equalsIgnoreCase("NVAL_NUM")) {
						  valdp = new ValueDisplayProperty();
						  valdp.height = tableRow.height;
						  pdoset.hasValueDisplayProperty = true;
						  
						  RGB contentColor = tableRow.color;
						  if (colorMap.containsKey(contentColor)) {
							  valdp.color = colorMap.get(contentColor);
						  }
						  else {
							  valdp.color = "lightbrown";
						  }
						  
						  if(tableRow.valueText.indexOf("<") >= 0) {
							  String max = tableRow.valueText.substring(tableRow.valueText.indexOf("<")+1);
							  valdp.left = 0.0 - Double.MAX_VALUE;
							  valdp.right = Double.parseDouble(max);
						  }
						  else if(tableRow.valueText.indexOf(">") >= 0) {
							  String min = tableRow.valueText.substring(tableRow.valueText.indexOf("<")+1);
							  valdp.right = Integer.MAX_VALUE;
							  valdp.left = Double.parseDouble(min);
						  }
						  else if(tableRow.valueText.indexOf("between") >= 0) {
							  String min = tableRow.valueText.substring(tableRow.valueText.indexOf("between")+1,
									  tableRow.valueText.indexOf("and"));
							  String max = tableRow.valueText.substring(tableRow.valueText.indexOf("and")+1);
							  valdp.right = Double.parseDouble(max);
							  valdp.left = Double.parseDouble(min);
						  }
						  
						  pdoset.valDisplayProperties.add(valdp);						   
					  }					  
				  }
				  else {
					  tmpcurFullPath = getFullname(tableRow.conceptXml, parser);
					  if(!curFullPath.equalsIgnoreCase(tmpcurFullPath)) {
						  curFullPath = new String(tmpcurFullPath);  
						  
						  pdoset = new PDOSet();
						  pdoset.fullPath = new String(tmpcurFullPath);
						  pdoset.tableType = new String(lookuptable);
						  timelineRow.pdoSets.add(pdoset);
					  }
					  
					  // RGB contentColor = tableRow.color;
					  if (colorMap.containsKey(tableRow.color)) {
						  pdoset.color = colorMap.get(tableRow.color);
					  }
					  else {
						  pdoset.color = "lightbrown";
					  }
	
					  pdoset.height = tableRow.height;
					  
					  if(tableRow.valueType.equalsIgnoreCase("NVAL_NUM")) {
						  valdp = new ValueDisplayProperty();
						  valdp.height = tableRow.height;
						  pdoset.hasValueDisplayProperty = true;
						  
						  RGB contentColor = tableRow.color;
						  if (colorMap.containsKey(contentColor)) {
							  valdp.color = colorMap.get(contentColor);
						  }
						  else {
							  valdp.color = "lightbrown";
						  }
						  
						  if(tableRow.valueText.indexOf("<") >= 0) {
							  String max = tableRow.valueText.substring(tableRow.valueText.indexOf("<")+1);
							  valdp.left = 0.0 - Double.MAX_VALUE;
							  valdp.right = Double.parseDouble(max);
						  }
						  else if(tableRow.valueText.indexOf(">") >= 0) {
							  String min = tableRow.valueText.substring(tableRow.valueText.indexOf(">")+1);
							  valdp.right = Integer.MAX_VALUE;
							  valdp.left = Double.parseDouble(min);
						  }
						  else if(tableRow.valueText.indexOf("between") >= 0) {
							  String min = tableRow.valueText.substring(tableRow.valueText.indexOf("between")+7,
									  tableRow.valueText.indexOf("and"));
							  String max = tableRow.valueText.substring(tableRow.valueText.indexOf("and")+3);
							  valdp.right = Double.parseDouble(max);
							  valdp.left = Double.parseDouble(min);
						  }
						  
						  pdoset.valDisplayProperties.add(valdp);					  
					  }
				  }
			  }  
		  }

		  return rows;
	  }
	  
	  public String getContentXml(){
		  
		 StringBuilder sb = new StringBuilder(100);
		  sb.append("<I2B2Query>\r\n");
		  
		  for (int i=1; i<rowCount; i++){
			  sb.append("<QueryEntry>\r\n");
			  String conceptName = (String) content.get("1/" + i);
			  if (conceptName.equals("Encounter Range Line"))
			  {
				  sb.append("<Concept>\r\n");
				  sb.append("<SpecialConcept>");
				  sb.append("<c_name>" + conceptName + "</c_name>\r\n");
				  sb.append("</SpecialConcept>");
				  sb.append("</Concept>\r\n");
			  }
			  else if (conceptName.equals("Vital Status Line"))
			  {
				  sb.append("<Concept>\r\n");
				  sb.append("<SpecialConcept>");
				  sb.append("<c_name>" + conceptName + "</c_name>\r\n");
				  sb.append("</SpecialConcept>");
				  sb.append("</Concept>\r\n");
			  }
			  else
			  {
				  Object xmlContent = content.get("6/" + i);
				  sb.append((String) xmlContent);
			  }
			  String tmp = ((String)content.get("2/"+i)) + (content.get("3/" + i));
			  String tmp1 = tmp.replaceAll("<", "&lt;");
			  String ModuleValue = tmp1.replaceAll(">", "&gt;");
			  sb.append("\r\n<DisplayName>");
			  sb.append(conceptName);
			  sb.append("</DisplayName>");
			   
			  sb.append("\r\n<ModuleValue>");
			  if(ModuleValue.indexOf("N/A")>=0) {
				  sb.append("</ModuleValue>");
			  } 
			  else {
				  sb.append(ModuleValue);
				  sb.append("\r\n</ModuleValue>");
			  }
			  
			  sb.append("\r\n<Height>");
			  sb.append(content.get("4/" + i));
			  sb.append("\r\n</Height>");
			  
			  sb.append("\r\n<RowNumber>");
			  sb.append(content.get("0/" + i));
			  sb.append("\r\n</RowNumber>");
			  
			  sb.append("\r\n<ConceptColor>");
			  RGB contentColor = (RGB) content.get("5/" + i);
			  if (colorMap.containsKey(contentColor))
				  sb.append(colorMap.get(contentColor));
			  else
				  sb.append("lightbrown");
			  sb.append("\r\n</ConceptColor>");
			  sb.append("\r\n</QueryEntry>");
		  }

		  sb.append("\r\n</I2B2Query>");
		  System.out.println("\n"+sb.toString());

		  return sb.toString();
	  }

	  // Umfang

	  public int getRowCount() {
	    return rowCount;
	  }

	  public int getFixedRowCount() {
	    return 1;
	  }

	  public int getColumnCount() {
	    return 6;
	  }

	  public int getFixedColumnCount() {
	    return 0;
	  }

	  // GroBen

	  public int getColumnWidth(int col) {
	    return colWidths[col];
	  }

	  public int getRowHeight() {
	    return rowHeight;
	  }

	  public boolean isColumnResizable(int col) {
	    return true;
	  }

	  public int getFirstRowHeight() {
	    return 22;
	  }

	  public boolean isRowResizable() {
	    return true;
	  }

	  public int getRowHeightMinimum() {
	    return 18;
	  }

	  public void setColumnWidth(int col, int value) {
	    colWidths[col] = value;
	  }

	  public void setRowHeight(int value) {
	    if (value < 2)
	      value = 2;
	    rowHeight = value;
	  }

	  // Rendering

	  public KTableCellRenderer getCellRenderer(int col, int row) {
		  if ((col==5)&&(row>0))
			  return colorRenderer;
		  else
	    return KTableCellRenderer.defaultRenderer;
	  }
	  
	  private void populateColorMap(){
		  colorMap.put(new RGB(255,255,255), "white");

		  colorMap.put(new RGB(255,250,250), "snow");

		  colorMap.put(new RGB(248,248,255), "ghostwhite");

		  colorMap.put(new RGB(255,255,240), "ivory");

		  colorMap.put(new RGB(245,255,250), "mintcream");

		  colorMap.put(new RGB(240,255,255), "azure");

		  colorMap.put(new RGB(255,250,240), "floralwhite");

		  colorMap.put(new RGB(240,248,255), "aliceblue");

		  colorMap.put(new RGB(255,240,245), "lavenderblush");

		  colorMap.put(new RGB(255,245,238), "seashell");

		  colorMap.put(new RGB(245,245,245), "whitesmoke");

		  colorMap.put(new RGB(240,255,240), "honeydew");

		  colorMap.put(new RGB(255,255,224), "lightyellow");

		  colorMap.put(new RGB(224,255,255), "lightcyan");

		  colorMap.put(new RGB(253,245,230), "oldlace");

		  colorMap.put(new RGB(255,248,220), "cornsilk");

		  colorMap.put(new RGB(250,240,230), "linen");

		  colorMap.put(new RGB(255,250,205), "lemonchiffon");

		  colorMap.put(new RGB(250,250,210), "lightgoldenrodyellow");

		  colorMap.put(new RGB(245,245,220), "beige");

		  colorMap.put(new RGB(230,230,250), "lavender");

		  colorMap.put(new RGB(255,228,225), "mistyrose");

		  colorMap.put(new RGB(255,239,213), "papayawhip");

		  colorMap.put(new RGB(255,245,200), "lightbrown");

		  colorMap.put(new RGB(250,235,215), "antiquewhite");

		  colorMap.put(new RGB(255,235,205), "blanchedalmond");

		  colorMap.put(new RGB(255,228,196), "bisque");

		  colorMap.put(new RGB(255,236,175), "darkbrown");

		  colorMap.put(new RGB(255,228,181), "moccasin");

		  colorMap.put(new RGB(220,220,220), "gainsboro");

		  colorMap.put(new RGB(255,218,185), "peachpuff");

		  colorMap.put(new RGB(175,238,238), "paleturquoise");

		  colorMap.put(new RGB(255,222,173), "navajowhite");

		  colorMap.put(new RGB(255,192,203), "pink");

		  colorMap.put(new RGB(245,222,179), "wheat");

		  colorMap.put(new RGB(238,232,170), "palegoldenrod");

		  colorMap.put(new RGB(211,211,211), "lightgray");

		  colorMap.put(new RGB(211,211,211), "lightgrey");

		  colorMap.put(new RGB(255,182,193), "lightpink");

		  colorMap.put(new RGB(176,224,230), "powderblue");

		  colorMap.put(new RGB(216,191,216), "thistle");

		  colorMap.put(new RGB(173,216,230), "lightblue");

		  colorMap.put(new RGB(240,230,140), "khaki");

		  colorMap.put(new RGB(238,130,238), "violet");

		  colorMap.put(new RGB(221,160,221), "plum");

		  colorMap.put(new RGB(176,196,222), "lightsteelblue");

		  colorMap.put(new RGB(127,255,212), "aquamarine");

		  colorMap.put(new RGB(135,206,250), "lightskyblue");

		  colorMap.put(new RGB(238,221,130), "lightgoldenrod");

		  colorMap.put(new RGB(135,206,235), "skyblue");

		  colorMap.put(new RGB(190,190,190), "gray");

		  colorMap.put(new RGB(152,251,152), "palegreen");

		  colorMap.put(new RGB(218,112,214), "orchid");

		  colorMap.put(new RGB(222,184,135), "burlywood");

		  colorMap.put(new RGB(255,105,180), "hotpink");

		  colorMap.put(new RGB(255,105,180), "severe");

		  colorMap.put(new RGB(255,160,122), "lightsalmon");

		  colorMap.put(new RGB(210,180,140), "tan");

		  colorMap.put(new RGB(255,255,0), "yellow");

		  colorMap.put(new RGB(255,0,255), "magenta");

		  colorMap.put(new RGB(0,255,255), "cyan");

		  colorMap.put(new RGB(233,150,122), "darksalmon");

		  colorMap.put(new RGB(244,164,96), "sandybrown");

		  colorMap.put(new RGB(132,112,255), "lightslateblue");

		  colorMap.put(new RGB(240,128,128), "lightcoral");

		  colorMap.put(new RGB(64,224,208), "turquoise");

		  colorMap.put(new RGB(250,128,114), "salmon");

		  colorMap.put(new RGB(100,149,237), "cornflowerblue");

		  colorMap.put(new RGB(72,209,204), "mediumturquoise");

		  colorMap.put(new RGB(186,85,211), "mediumorchid");

		  colorMap.put(new RGB(189,183,107), "darkkhaki");

		  colorMap.put(new RGB(219,112,147), "palevioletred");

		  colorMap.put(new RGB(147,112,219), "mediumpurple");

		  colorMap.put(new RGB(102,205,170), "mediumaquamarine");

		  colorMap.put(new RGB(188,143,143), "rosybrown");

		  colorMap.put(new RGB(143,188,143), "darkseagreen");

		  colorMap.put(new RGB(255,215,0), "gold");

		  colorMap.put(new RGB(123,104,238), "mediumslateblue");

		  colorMap.put(new RGB(255,127,80), "coral");

		  colorMap.put(new RGB(0,191,255), "deepskyblue");

		  colorMap.put(new RGB(160,32,240), "purple");

		  colorMap.put(new RGB(30,144,255), "dodgerblue");

		  colorMap.put(new RGB(255,99,71), "tomato");

		  colorMap.put(new RGB(255,20,147), "deeppink");

		  colorMap.put(new RGB(255,165,0), "orange");

		  colorMap.put(new RGB(218,165,32), "goldenrod");

		  colorMap.put(new RGB(0,206,209), "darkturquoise");

		  colorMap.put(new RGB(95,158,160), "cadetblue");

		  colorMap.put(new RGB(154,205,50), "yellowgreen");

		  colorMap.put(new RGB(119,136,153), "lightslategray");

		  colorMap.put(new RGB(119,136,153), "lightslategrey");

		  colorMap.put(new RGB(153,50,204), "darkorchid");

		  colorMap.put(new RGB(138,43,226), "blueviolet");

		  colorMap.put(new RGB(0,250,154), "mediumspringgreen");

		  colorMap.put(new RGB(205,133,63), "peru");

		  colorMap.put(new RGB(106,90,205), "slateblue");

		  colorMap.put(new RGB(255,140,0), "darkorange");

		  colorMap.put(new RGB(65,105,225), "royalblue");

		  colorMap.put(new RGB(205,92,92), "indianred");

		  colorMap.put(new RGB(208,32,144), "violetred");

		  colorMap.put(new RGB(112,128,144), "slategray");

		  colorMap.put(new RGB(112,128,144), "slategrey");

		  colorMap.put(new RGB(127,255,0), "chartreuse");

		  colorMap.put(new RGB(0,255,127), "springgreen");

		  colorMap.put(new RGB(70,130,180), "steelblue");

		  colorMap.put(new RGB(32,178,170), "lightseagreen");

		  colorMap.put(new RGB(124,252,0), "lawngreen");

		  colorMap.put(new RGB(148,0,211), "darkviolet");

		  colorMap.put(new RGB(199,21,133), "mediumvioletred");

		  colorMap.put(new RGB(60,179,113), "mediumseagreen");

		  colorMap.put(new RGB(210,105,30), "chocolate");

		  colorMap.put(new RGB(184,134,11), "darkgoldenrod");

		  colorMap.put(new RGB(255,69,0), "orangered");

		  colorMap.put(new RGB(176,48,96), "maroon");

		  colorMap.put(new RGB(105,105,105), "dimgray");

		  colorMap.put(new RGB(105,105,105), "dimgrey");

		  colorMap.put(new RGB(50,205,50), "limegreen");

		  colorMap.put(new RGB(160,82,45), "sienna");

		  colorMap.put(new RGB(107,142,35), "olivedrab");

		  colorMap.put(new RGB(72,61,139), "darkslateblue");

		  colorMap.put(new RGB(46,139,87), "seagreen");

		  colorMap.put(new RGB(255,0,0), "red");

		  colorMap.put(new RGB(0,255,0), "green");

		  colorMap.put(new RGB(0,0,255), "blue");

		  colorMap.put(new RGB(165,42,42), "brown");

		  colorMap.put(new RGB(178,34,34), "firebrick");

		  colorMap.put(new RGB(85,107,47), "darkolivegreen");

		  colorMap.put(new RGB(139,69,19), "saddlebrown");

		  colorMap.put(new RGB(34,139,34), "forestgreen");

		  colorMap.put(new RGB(47,79,79), "darkslategray");

		  colorMap.put(new RGB(47,79,79), "darkslategrey");

		  colorMap.put(new RGB(0,0,205), "mediumblue");

		  colorMap.put(new RGB(25,25,112), "midnightblue");

		  colorMap.put(new RGB(0,0,128), "navy");

		  colorMap.put(new RGB(0,0,128), "navyblue");

		  colorMap.put(new RGB(0,100,0), "darkgreen");

		  colorMap.put(new RGB(0,0,0), "black");		  
	  }
	  
	  public boolean isColorUsable(RGB rgbValue){
		  if (colorMap.containsKey(rgbValue))
			  return true;
		  else
			  return false;
	  }
}

class KTableForModel implements KTableModel {

	  private int[] colWidths;

	  private int rowHeight;

	  private HashMap<String, Object> content;

	  /**
	   * 
	   */
	  public KTableForModel() {
	    colWidths = new int[getColumnCount()];
	    for (int i = 0; i < colWidths.length; i++) {
	      colWidths[i] = 270;
	    }
	    rowHeight = 18;
	    content = new HashMap<String, Object>();
	  }

	  // Inhalte

	  public Object getContentAt(int col, int row) {
	    // System.out.println("col "+col+" row "+row);
	    String erg = (String) content.get(col + "/" + row);
	    if (erg != null)
	      return erg;
	    return col + "/" + row;
	  }

	  /*
	   * overridden from superclass
	   */
	  public KTableCellEditor getCellEditor(int col, int row) {
	    if (col % 2 == 0) {
	      KTableCellEditorCombo e = new KTableCellEditorCombo();
	      e.setItems(new String[] { "First text", "Second text",
	              "third text" });
	      return e;
	    } else
	      return new KTableCellEditorText();
	  }

	  /*
	   * overridden from superclass
	   */
	  public void setContentAt(int col, int row, Object value) {
	    content.put(col + "/" + row, value);
	    //
	  }

	  // Umfang

	  public int getRowCount() {
	    return 5;
	  }

	  public int getFixedRowCount() {
	    return 2;
	  }

	  public int getColumnCount() {
	    return 5;
	  }

	  public int getFixedColumnCount() {
	    return 1;
	  }

	  // GroBen

	  public int getColumnWidth(int col) {
	    return colWidths[col];
	  }

	  public int getRowHeight() {
	    return rowHeight;
	  }

	  public boolean isColumnResizable(int col) {
	    return true;
	  }

	  public int getFirstRowHeight() {
	    return 22;
	  }

	  public boolean isRowResizable() {
	    return true;
	  }

	  public int getRowHeightMinimum() {
	    return 18;
	  }

	  public void setColumnWidth(int col, int value) {
	    colWidths[col] = value;
	  }

	  public void setRowHeight(int value) {
	    if (value < 2)
	      value = 2;
	    rowHeight = value;
	  }

	  // Rendering

	  public KTableCellRenderer getCellRenderer(int col, int row) {
	    return KTableCellRenderer.defaultRenderer;
	  }

	}


/*******************************************************************************
* Copyright (C) 2004 by Friederich Kupzog Elektronik & Software All rights
* reserved. This program and the accompanying materials are made available
* under the terms of the Eclipse Public License v1.0 which accompanies this
* distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
* 
* Contributors: Friederich Kupzog - initial API and implementation
* fkmk@kupzog.de www.kupzog.de/fkmk
******************************************************************************/

abstract class KTableCellEditor {

protected KTableModel m_Model;

protected KTable m_Table;

protected Rectangle m_Rect;

protected int m_Row;

protected int m_Col;

protected Control m_Control;

protected String toolTip;

/**
 * disposes the editor and its components
 */
public void dispose() {
  if (m_Control != null) {
    m_Control.dispose();
    m_Control = null;
  }
}

/**
 * Activates the editor at the given position.
 * 
 * @param row
 * @param col
 * @param rect
 */
public void open(KTable table, int col, int row, Rectangle rect) {
  m_Table = table;
  m_Model = table.getModel();
  m_Rect = rect;
  m_Row = row;
  m_Col = col;
  if (m_Control == null) {
    m_Control = createControl();
    m_Control.setToolTipText(toolTip);
    m_Control.addFocusListener(new FocusAdapter() {
      public void focusLost(FocusEvent arg0) {
        close(true);
      }
    });
  }
  setBounds(m_Rect);
  GC gc = new GC(m_Table);
  m_Table.drawCell(gc, m_Col, m_Row);
  gc.dispose();
}

/**
 * Deactivates the editor.
 * 
 * @param save
 *            If true, the content is saved to the underlying table.
 */
public void close(boolean save) {
  m_Table.m_CellEditor = null;
  // m_Control.setVisible(false);
  GC gc = new GC(m_Table);
  m_Table.drawCell(gc, m_Col, m_Row);
  gc.dispose();
  this.dispose();
}

/**
 * Returns true if the editor has the focus.
 * 
 * @return boolean
 */
public boolean isFocused() {
  if (m_Control == null)
    return false;
  return m_Control.isFocusControl();
}

/**
 * Sets the editor's position and size
 * 
 * @param rect
 */
public void setBounds(Rectangle rect) {
  if (m_Control != null)
    m_Control.setBounds(rect);
}

/*
 * Creates the editor's control. Has to be overwritten by useful editor
 * implementations.
 */
protected abstract Control createControl();

protected void onKeyPressed(KeyEvent e) {
  if ((e.character == '\r') && ((e.stateMask & SWT.SHIFT) == 0)) {
    close(true);
  } else if (e.character == SWT.ESC) {
    close(false);
  } else {
    m_Table.scrollToFocus();
  }
}

protected void onTraverse(TraverseEvent e) {
  close(true);
  // m_Table.tryToOpenEditorAt(m_Col+1, m_Row);
}

/**
 * @param toolTip
 */
public void setToolTipText(String toolTip) {
  this.toolTip = toolTip;
}

}
class KTableCellPickTimeLineColor extends KTableCellEditor {
	  private RGB m_Color;
	  private CCombo m_Combo;
	  private String m_Items[] = new String[] {"Red",
			  "Green",
			  "Brown",
			  "Light Brown",
			  "Blue",
			  "Dark Brown",
			  "Black"
	  	};

	  public void open(KTable table, int row, int col, Rectangle rect) {
	    super.open(table, row, col, rect);
	    //m_Combo.setFocus();
	    //m_Combo.setText((String) m_Model.getContentAt(m_Col, m_Row));
	    /*if (m_Color.equals(new RGB(255, 0, 0)))
	    	m_Combo.setText("Red");
	    else if (m_Color.equals(new RGB(0, 255, 0)))
	    	m_Combo.setText("Green");
	    else if (m_Color.equals(new RGB(150, 75, 0)))
	    	m_Combo.setText("Brown");
	    else if (m_Color.equals(new RGB(205, 133, 63)))
	    	m_Combo.setText("Light Brown");
	    else if (m_Color.equals(new RGB(0, 0, 255)))
	    	m_Combo.setText("Blue");
	    else if (m_Color.equals(new RGB(101, 67, 33)))
	    	m_Combo.setText("Dark Brown");
	    else if (m_Color.equals(new RGB(0, 0, 0)))
	    	m_Combo.setText("Black");*/
	    
	    m_Combo.setBackground(new Color(table.getDisplay(), (RGB) m_Model.getContentAt(m_Col, m_Row)));
	  }

	  public void close(boolean save) {
	    if (save)
	      m_Model.setContentAt(m_Col, m_Row, m_Color);
	    super.close(save);
	    m_Color = null;
	  }

	  protected Control createControl() {
		  m_Combo = new CCombo(m_Table, SWT.READ_ONLY);
		    m_Combo.setVisibleItemCount(7);
		    m_Combo.setEditable(false);
		    m_Combo.setBackground(Display.getCurrent().getSystemColor(
		        SWT.COLOR_LIST_BACKGROUND | SWT.READ_ONLY));
		    if (m_Items != null)
		      m_Combo.setItems(m_Items);
		    m_Combo.addSelectionListener(new SelectionListener(){
		    	public void widgetSelected(SelectionEvent e) {
		    		String colorText = m_Combo.getText();
		    		if (colorText.equals("Red"))
		    			m_Color = new RGB(255, 0, 0);
		    		else if (colorText.equals("Green"))
		    			m_Color = new RGB(0, 255, 0);
		    		else if (colorText.equals("Brown"))
		    			m_Color = new RGB(150, 75, 0);
		    		else if (colorText.equals("Light Brown"))
		    			m_Color = new RGB(205, 133, 63);
		    		else if (colorText.equals("Blue"))
		    			m_Color = new RGB(0, 0, 255);
		    		else if (colorText.equals("Dark Brown"))
		    			m_Color = new RGB(101, 67, 33);
		    		else if (colorText.equals("Black"))
		    			m_Color = new RGB(0, 0, 0);

		    		close(true);
		    		m_Table.redraw();
		    	}
		    	public void widgetDefaultSelected(SelectionEvent e) {
		    		String colorText = m_Combo.getText();
		    		if (colorText.equals("Red"))
		    			m_Color = new RGB(255, 0, 0);
		    		else if (colorText.equals("Green"))
		    			m_Color = new RGB(0, 255, 0);
		    		else if (colorText.equals("Brown"))
		    			m_Color = new RGB(150, 75, 0);
		    		else if (colorText.equals("Light Brown"))
		    			m_Color = new RGB(205, 133, 63);
		    		else if (colorText.equals("Blue"))
		    			m_Color = new RGB(0, 0, 255);
		    		else if (colorText.equals("Dark Brown"))
		    			m_Color = new RGB(101, 67, 33);
		    		else if (colorText.equals("Black"))
		    			m_Color = new RGB(0, 0, 0);

		    		close(true);
		    		m_Table.redraw();
		    	}
		    });
		    m_Combo.addKeyListener(new KeyAdapter() {
		      public void keyPressed(KeyEvent e) {
		        try {
		          onKeyPressed(e);
		        } catch (Exception ex) {
		        }
		      }
		    });
		    return m_Combo;
	  }

	  public void setBounds(Rectangle rect) {
	    super.setBounds(new Rectangle(rect.x, rect.y + 1, rect.width,
	        rect.height));
	  }

	  public void setColor(RGB color) {
	    m_Color = color;
	  }

	}

class KTableCellEditorColor extends KTableCellEditor {
	  private RGB m_Color = new RGB(0, 255, 0);
	  private org.eclipse.swt.widgets.Label m_Label;
	  
	  public void openS(KTable table, int row, int col, Rectangle rect) {
		    super.open(table, row, col, rect);
	  }

	  public void open(KTable table, int row, int col, Rectangle rect) {
	    super.open(table, row, col, rect);
	   // m_Combo.setFocus();
	    //m_Combo.setText((String) m_Model.getContentAt(m_Col, m_Row));
	    
	    
	    //org.eclipse.swt.widgets.ColorDialog dlg = new org.eclipse.swt.widgets.ColorDialog(table.getShell(), SWT.DROP_DOWN);

	    WebColorDialog dlg = new WebColorDialog(m_Table.getShell());
      // Set the selected color in the dialog from
      // user's selected color
	    Object color = m_Table.getModel().getContentAt(5, row);
	    if (color!=null)
	    	dlg.setRGB((RGB) color);
	    else
	    	dlg.setRGB(new RGB(0, 255, 0));
	    	
      // Change the title bar text
      dlg.setText("Choose a Color");

      // Open the dialog and retrieve the selected color
      RGB rgbDlg = dlg.open();
      RGB rgb = new RGB(rgbDlg.red, rgbDlg.green, rgbDlg.blue);
      if (rgb != null) {
      	
      	KTableI2B2Model i2Model = (KTableI2B2Model) m_Table.getModel();
      	if (i2Model.isColorUsable(rgb))
      	{
	            // Dispose the old color, create the
	            // new one, and set into the label
	            m_Color = rgb;
	            try
	            {
	            	Display dplay = m_Table.getDisplay();
	            	Color colBack = new Color(dplay, rgb);
	            	m_Label.setBackground(colBack);	    
	            //colBack.dispose();
	            }
	            catch (Exception e)
	            {
	            	System.out.println(e.getMessage());
	            }
      	}
      	else
      	{
      		MessageBox mBox = new MessageBox(m_Table.getShell(), SWT.ICON_INFORMATION | SWT.OK);
      		mBox.setText("Invalid Color");
      		mBox.setMessage("The color you picked is not supported.  Please choose another color.");
      		mBox.open();
      	}
      }
      close(true);
	  }

	  public void close(boolean save) {
	    if (save)
	      m_Model.setContentAt(m_Col, m_Row, m_Color);
	    super.close(save);
	    m_Color = null;
	  }

	  protected Control createControl() {
		  Object testObj = m_Model.getContentAt(m_Col, m_Row);
		  if (testObj==null)
			  m_Color = new RGB(0, 255, 0);
		  else
			  m_Color = (RGB) testObj;
		  m_Label = new Label(m_Table.getShell(), SWT.NONE);
        m_Label.setBackground(new Color(m_Table.getDisplay(), m_Color));	    
        return m_Label;
	  }

	  public void setBounds(Rectangle rect) {
	    super.setBounds(new Rectangle(rect.x, rect.y + 1, rect.width,
	        rect.height));
	  }

	  public void setColor(RGB rgb) {
	    m_Color = rgb;
	    KTableI2B2Model i2Model = (KTableI2B2Model) m_Table.getModel();
      	if (i2Model.isColorUsable(rgb))
      	{
	            // Dispose the old color, create the
	            // new one, and set into the label
	            m_Color = rgb;
	            try
	            {
	            	Display dplay = m_Table.getDisplay();
	            	Color colBack = new Color(dplay, rgb);
	            	m_Label.setBackground(colBack);	    
	            //colBack.dispose();
	            }
	            catch (Exception e)
	            {
	            	System.out.println(e.getMessage());
	            }
      	}
      	else
      	{
      		MessageBox mBox = new MessageBox(m_Table.getShell(), SWT.ICON_INFORMATION | SWT.OK);
      		mBox.setText("Invalid Color");
      		mBox.setMessage("The color you picked is not supported.  Please choose another color.");
      		mBox.open();
      	}
      	close(true);
	  }
	}

/*******************************************************************************
* Copyright (C) 2004 by Friederich Kupzog Elektronik & Software All rights
* reserved. This program and the accompanying materials are made available
* under the terms of the Eclipse Public License v1.0 which accompanies this
* distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
* 
* Contributors: Friederich Kupzog - initial API and implementation
* fkmk@kupzog.de www.kupzog.de/fkmk
******************************************************************************/

class KTableCellEditorCombo extends KTableCellEditor {
private CCombo m_Combo;

private String m_Items[];

public void open(KTable table, int row, int col, Rectangle rect) {
  super.open(table, row, col, rect);
  m_Combo.setFocus();
  m_Combo.setText((String) m_Model.getContentAt(m_Col, m_Row));
}

public void close(boolean save) {
  if (save)
    m_Model.setContentAt(m_Col, m_Row, m_Combo.getText());
  super.close(save);
  m_Combo = null;
}

protected Control createControl() {
  m_Combo = new CCombo(m_Table, SWT.READ_ONLY);
  m_Combo.setBackground(Display.getCurrent().getSystemColor(
      SWT.COLOR_LIST_BACKGROUND));
  if (m_Items != null) {
  	for(int i=0; i<m_Items.length; i++) {
  		m_Combo.add(m_Items[i]);
  	}
  }
  m_Combo.addKeyListener(new KeyAdapter() {
    public void keyPressed(KeyEvent e) {
      try {
        onKeyPressed(e);
        System.out.println("key pressed");
      } catch (Exception ex) {
      }
    }
  });
  m_Combo.addSelectionListener(new SelectionListener() {
	 public void widgetSelected(SelectionEvent e) {
		 System.out.println("selected: " + m_Combo.getText());
		 int row = m_Table.selectedRow;
		 int col = m_Table.selectedColumn;
		 System.out.println("selected cell: [" + m_Table.selectedColumn+","+m_Table.selectedRow+"]");
		 if(col ==4) {
			 KTableCellEditorColor colorEditor = (KTableCellEditorColor)
			 			m_Table.getModel().getCellEditor(col+1,row);
			 if (colorEditor != null) {
			      Rectangle r = m_Table.getCellRect(col+1, row);
			      colorEditor.openS(m_Table, col+1, row, r);
			 }
			 String text = m_Combo.getText();
			 if(text.equalsIgnoreCase("Tall") || text.equalsIgnoreCase("Low")) {
				 colorEditor.setColor(new RGB(255, 215, 0));
			 }
			 else if(text.equalsIgnoreCase("Very Tall") || text.equalsIgnoreCase("Very Low")) {
				 colorEditor.setColor(new RGB(255,0,0));
			 }
			 else if(text.indexOf("Medium")>=0) {
				 colorEditor.setColor(new RGB(0,255,0));
			 }
		 }
	 }
	 
	 public void widgetDefaultSelected(SelectionEvent e) {
		 
	 }
  });
  /*
   * m_Combo.addTraverseListener(new TraverseListener() { public void
   * keyTraversed(TraverseEvent arg0) { onTraverse(arg0); } });
   */
  return m_Combo;
}

public void setBounds(Rectangle rect) {
  super.setBounds(new Rectangle(rect.x, rect.y + 1, rect.width,
      rect.height - 2));
}

public void setItems(String items[]) {
  m_Items = items;
}

}

class KTableCellEditorComboW extends KTableCellEditor {
	private CCombo m_Combo;

	private String m_Items[];

	public void open(KTable table, int row, int col, Rectangle rect) {
	  super.open(table, row, col, rect);
	  m_Combo.setFocus();
	  m_Combo.setText((String) m_Model.getContentAt(m_Col, m_Row));
	}

	public void close(boolean save) {
	  if (save)
	    m_Model.setContentAt(m_Col, m_Row, m_Combo.getText());
	  super.close(save);
	  m_Combo = null;
	}

	protected Control createControl() {
	  m_Combo = new CCombo(m_Table, SWT.NULL);
	  m_Combo.setBackground(Display.getCurrent().getSystemColor(
	      SWT.COLOR_LIST_BACKGROUND));
	  if (m_Items != null) {
	  	for(int i=0; i<m_Items.length; i++) {
	  		m_Combo.add(m_Items[i]);
	  	}
	  }
	  m_Combo.addKeyListener(new KeyAdapter() {
	    public void keyPressed(KeyEvent e) {
	      try {
	        onKeyPressed(e);
	        System.out.println("key pressed");
	      } catch (Exception ex) {
	      }
	    }
	  });
	  m_Combo.addSelectionListener(new SelectionListener() {
		 public void widgetSelected(SelectionEvent e) {
			 System.out.println("selected: " + m_Combo.getText());
			 int row = m_Table.selectedRow;
			 int col = m_Table.selectedColumn;
			 System.out.println("selected cell: [" + m_Table.selectedColumn+","+m_Table.selectedRow+"]");
			 if(col ==4) {
				 KTableCellEditorColor colorEditor = (KTableCellEditorColor)
				 			m_Table.getModel().getCellEditor(col+1,row);
				 if (colorEditor != null) {
				      Rectangle r = m_Table.getCellRect(col+1, row);
				      colorEditor.openS(m_Table, col+1, row, r);
				 }
				 String text = m_Combo.getText();
				 if(text.equalsIgnoreCase("Tall") || text.equalsIgnoreCase("Low")) {
					 colorEditor.setColor(new RGB(34, 139, 34));
				 }
				 else if(text.equalsIgnoreCase("Very Tall") || text.equalsIgnoreCase("Very Low")) {
					 colorEditor.setColor(new RGB(255,0,0));
				 }
				 else if(text.indexOf("Medium")>=0) {
					 colorEditor.setColor(new RGB(0,255,0));
				 }
			 }
		 }
		 
		 public void widgetDefaultSelected(SelectionEvent e) {
			 
		 }
	  });
	  /*
	   * m_Combo.addTraverseListener(new TraverseListener() { public void
	   * keyTraversed(TraverseEvent arg0) { onTraverse(arg0); } });
	   */
	  return m_Combo;
	}

	public void setBounds(Rectangle rect) {
	  super.setBounds(new Rectangle(rect.x, rect.y + 1, rect.width,
	      rect.height - 2));
	}

	public void setItems(String items[]) {
	  m_Items = items;
	}

}

/*******************************************************************************
* Copyright (C) 2004 by Friederich Kupzog Elektronik & Software All rights
* reserved. This program and the accompanying materials are made available
* under the terms of the Eclipse Public License v1.0 which accompanies this
* distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
* 
* Contributors: Friederich Kupzog - initial API and implementation
* fkmk@kupzog.de www.kupzog.de/fkmk
******************************************************************************/
class KTableCellEditorMultilineText extends KTableCellEditor {
private Text m_Text;

public void open(KTable table, int col, int row, Rectangle rect) {
  super.open(table, col, row, rect);
  m_Text.setText(m_Model.getContentAt(m_Col, m_Row).toString());
  m_Text.selectAll();
  m_Text.setVisible(true);
  m_Text.setFocus();
}

public void close(boolean save) {
  if (save)
    m_Model.setContentAt(m_Col, m_Row, m_Text.getText());
  m_Text = null;
  super.close(save);
}

protected Control createControl() {
  m_Text = new Text(m_Table, SWT.MULTI | SWT.V_SCROLL);
  m_Text.addKeyListener(new KeyAdapter() {
    public void keyPressed(KeyEvent e) {
      try {
        onKeyPressed(e);
      } catch (Exception ex) {
      }
    }
  });
  m_Text.addTraverseListener(new TraverseListener() {
    public void keyTraversed(TraverseEvent arg0) {
      onTraverse(arg0);
    }
  });
  return m_Text;
}

/*
 * overridden from superclass
 */
public void setBounds(Rectangle rect) {
  super.setBounds(new Rectangle(rect.x, rect.y, rect.width, rect.height));
}

}

/*******************************************************************************
* Copyright (C) 2004 by Friederich Kupzog Elektronik & Software All rights
* reserved. This program and the accompanying materials are made available
* under the terms of the Eclipse Public License v1.0 which accompanies this
* distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
* 
* Contributors: Friederich Kupzog - initial API and implementation
* fkmk@kupzog.de www.kupzog.de/fkmk
******************************************************************************/
class KTableCellEditorMultilineWrapText extends KTableCellEditor {
private Text m_Text;

public void open(KTable table, int col, int row, Rectangle rect) {
  super.open(table, col, row, rect);
  m_Text.setText(m_Model.getContentAt(m_Col, m_Row).toString());
  m_Text.selectAll();
  m_Text.setVisible(true);
  m_Text.setFocus();
}

public void close(boolean save) {
  if (save)
    m_Model.setContentAt(m_Col, m_Row, m_Text.getText());
  m_Text = null;
  super.close(save);
}

protected Control createControl() {
  m_Text = new Text(m_Table, SWT.MULTI | SWT.V_SCROLL | SWT.WRAP);
  m_Text.addKeyListener(new KeyAdapter() {
    public void keyPressed(KeyEvent e) {
      try {
        onKeyPressed(e);
      } catch (Exception ex) {
      }
    }
  });
  m_Text.addTraverseListener(new TraverseListener() {
    public void keyTraversed(TraverseEvent arg0) {
      onTraverse(arg0);
    }
  });
  return m_Text;
}

/*
 * overridden from superclass
 */
public void setBounds(Rectangle rect) {
  super.setBounds(new Rectangle(rect.x, rect.y, rect.width, rect.height));
}

}

/*******************************************************************************
* Copyright (C) 2004 by Friederich Kupzog Elektronik & Software All rights
* reserved. This program and the accompanying materials are made available
* under the terms of the Eclipse Public License v1.0 which accompanies this
* distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
* 
* Contributors: Friederich Kupzog - initial API and implementation
* fkmk@kupzog.de www.kupzog.de/fkmk
******************************************************************************/

class KTableCellEditorText extends KTableCellEditor {
private Text m_Text;

public void open(KTable table, int col, int row, Rectangle rect) {
  super.open(table, col, row, rect);
  m_Text.setText(m_Model.getContentAt(m_Col, m_Row).toString());
  m_Text.selectAll();
  m_Text.setVisible(true);
  m_Text.setFocus();
}

public void close(boolean save) {
  if (save)
    m_Model.setContentAt(m_Col, m_Row, m_Text.getText());
  super.close(save);
  m_Text = null;
  // System.out.println("set to null.");
}

protected Control createControl() {
  // System.out.println("Created a new one.");
  m_Text = new Text(m_Table, SWT.NONE);
  m_Text.addKeyListener(new KeyAdapter() {
    public void keyPressed(KeyEvent e) {
      try {
        onKeyPressed(e);
      } catch (Exception ex) {
      }
    }
  });
  m_Text.addTraverseListener(new TraverseListener() {
    public void keyTraversed(TraverseEvent arg0) {
      onTraverse(arg0);
    }
  });
  return m_Text;
}

/*
 * overridden from superclass
 */
public void setBounds(Rectangle rect) {
  super.setBounds(new Rectangle(rect.x, rect.y + (rect.height - 15) / 2
      + 1, rect.width, 15));
}

}

/*******************************************************************************
* Copyright (C) 2004 by Friederich Kupzog Elektronik & Software All rights
* reserved. This program and the accompanying materials are made available
* under the terms of the Eclipse Public License v1.0 which accompanies this
* distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
* 
* Contributors: Friederich Kupzog - initial API and implementation
* fkmk@kupzog.de www.kupzog.de/fkmk
******************************************************************************/
class KTableCellRenderer {

public static KTableCellRenderer defaultRenderer = new KTableCellRenderer();

/**
 * 
 */
protected Display m_Display;

public KTableCellRenderer() {
  m_Display = Display.getCurrent();
}

/**
 * Returns the optimal width of the given cell (used by column resizing)
 * 
 * @param col
 * @param row
 * @param content
 * @param fixed
 * @return int
 */
public int getOptimalWidth(GC gc, int col, int row, Object content,
    boolean fixed) {
  return gc.stringExtent(content.toString()).x + 8;
}

/**
 * Standard implementation for CellRenderer. Draws a cell at the given
 * position. Uses the .getString() method of content to get a String
 * representation to draw.
 * 
 * @param gc
 *            The gc to draw on
 * @param rect
 *            The coordinates and size of the cell (add 1 to width and hight
 *            to include the borders)
 * @param col
 *            The column
 * @param row
 *            The row
 * @param content
 *            The content of the cell (as given by the table model)
 * @param focus
 *            True if the cell is selected
 * @param fixed
 *            True if the cell is fixed (unscrollable header cell)
 * @param clicked
 *            True if the cell is currently clicked (useful e.g. to paint a
 *            pressed button)
 */
public void drawCell(GC gc, Rectangle rect, int col, int row,
    Object content, boolean focus, boolean fixed, boolean clicked) {
  if (fixed) {

    rect.height += 1;
    rect.width += 1;
    gc.setForeground(Display.getCurrent().getSystemColor(
        SWT.COLOR_LIST_FOREGROUND));
    if (clicked) {
      SWTX
          .drawButtonDown(gc, content.toString(),
              SWTX.ALIGN_HORIZONTAL_LEFT
                  | SWTX.ALIGN_VERTICAL_CENTER, null,
              SWTX.ALIGN_HORIZONTAL_RIGHT
                  | SWTX.ALIGN_VERTICAL_CENTER, rect);
    } else {
      SWTX
          .drawButtonUp(gc, content.toString(),
              SWTX.ALIGN_HORIZONTAL_LEFT
                  | SWTX.ALIGN_VERTICAL_CENTER, null,
              SWTX.ALIGN_HORIZONTAL_RIGHT
                  | SWTX.ALIGN_VERTICAL_CENTER, rect);
    }

    return;
  }

  Color textColor;
  Color backColor;
  Color vBorderColor;
  Color hBorderColor;

  if (focus) {
    textColor = m_Display.getSystemColor(SWT.COLOR_LIST_SELECTION_TEXT);
    backColor = (m_Display.getSystemColor(SWT.COLOR_LIST_SELECTION));
    vBorderColor = m_Display.getSystemColor(SWT.COLOR_LIST_SELECTION);
    hBorderColor = m_Display.getSystemColor(SWT.COLOR_LIST_SELECTION);
  } else {
    textColor = m_Display.getSystemColor(SWT.COLOR_LIST_FOREGROUND);
    backColor = m_Display.getSystemColor(SWT.COLOR_LIST_BACKGROUND);
    vBorderColor = m_Display
        .getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
    hBorderColor = m_Display
        .getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
  }

  gc.setForeground(hBorderColor);
  gc.drawLine(rect.x, rect.y + rect.height, rect.x + rect.width, rect.y
      + rect.height);

  gc.setForeground(vBorderColor);
  gc.drawLine(rect.x + rect.width, rect.y, rect.x + rect.width, rect.y
      + rect.height);

  gc.setBackground(backColor);
  gc.setForeground(textColor);

  gc.fillRectangle(rect);

  SWTX.drawTextImage(gc, content.toString(), SWTX.ALIGN_HORIZONTAL_CENTER
      | SWTX.ALIGN_VERTICAL_CENTER, null,
      SWTX.ALIGN_HORIZONTAL_CENTER | SWTX.ALIGN_VERTICAL_CENTER,
      rect.x + 3, rect.y, rect.width - 3, rect.height);

}

}

/*******************************************************************************
* Copyright (C) 2004 by Friederich Kupzog Elektronik & Software All rights
* reserved. This program and the accompanying materials are made available
* under the terms of the Eclipse Public License v1.0 which accompanies this
* distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
* 
* Contributors: Friederich Kupzog - initial API and implementation
* fkmk@kupzog.de www.kupzog.de/fkmk
******************************************************************************/

class KTableCellResizeAdapter implements KTableCellResizeListener {

public void columnResized(int col, int newWidth) {
}

public void rowResized(int newHeight) {
}

}

/*******************************************************************************
* Copyright (C) 2004 by Friederich Kupzog Elektronik & Software All rights
* reserved. This program and the accompanying materials are made available
* under the terms of the Eclipse Public License v1.0 which accompanies this
* distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
* 
* Contributors: Friederich Kupzog - initial API and implementation
* fkmk@kupzog.de www.kupzog.de/fkmk
******************************************************************************/

interface KTableCellResizeListener {

/**
 * Is called when a row is resized.
 */
public void rowResized(int newHeight);

/**
 * Is called when a column is resized.
 */
public void columnResized(int col, int newWidth);

}

/*******************************************************************************
* Copyright (C) 2004 by Friederich Kupzog Elektronik & Software All rights
* reserved. This program and the accompanying materials are made available
* under the terms of the Eclipse Public License v1.0 which accompanies this
* distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
* 
* Contributors: Friederich Kupzog - initial API and implementation
* fkmk@kupzog.de www.kupzog.de/fkmk
******************************************************************************/

class KTableCellSelectionAdapter implements KTableCellSelectionListener {
/**
 * Is called if a non-fixed cell is selected (gets the focus).
 * 
 * @see KTable for an explanation of the term "fixed cells".
 * @param col
 *            the column of the cell
 * @param row
 *            the row of the cell
 * @param statemask
 *            the modifier keys that where pressed when the selection
 *            happened.
 */
public void cellSelected(int col, int row, int statemask) {
}

/**
 * Is called if a fixed cell is selected (is clicked).
 * 
 * @see KTable for an explanation of the term "fixed cells".
 * @param col
 *            the column of the cell
 * @param row
 *            the row of the cell
 * @param statemask
 *            the modifier keys that where pressed when the selection
 *            happened.
 */
public void fixedCellSelected(int col, int row, int statemask) {
}

}

/*******************************************************************************
* Copyright (C) 2004 by Friederich Kupzog Elektronik & Software All rights
* reserved. This program and the accompanying materials are made available
* under the terms of the Eclipse Public License v1.0 which accompanies this
* distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
* 
* Contributors: Friederich Kupzog - initial API and implementation
* fkmk@kupzog.de www.kupzog.de/fkmk
******************************************************************************/

interface KTableCellSelectionListener {

/**
 * Is called if a non-fixed cell is selected (gets the focus).
 * 
 * @see KTable for an explanation of the term "fixed cells".
 * @param col
 *            the column of the cell
 * @param row
 *            the row of the cell
 * @param statemask
 *            the modifier keys that where pressed when the selection
 *            happened.
 */
public void cellSelected(int col, int row, int statemask);

/**
 * Is called if a fixed cell is selected (is clicked).
 * 
 * @see KTable for an explanation of the term "fixed cells".
 * @param col
 *            the column of the cell
 * @param row
 *            the row of the cell
 * @param statemask
 *            the modifier keys that where pressed when the selection
 *            happened.
 */
public void fixedCellSelected(int col, int row, int statemask);

}

/*******************************************************************************
* Copyright (C) 2004 by Friederich Kupzog Elektronik & Software All rights
* reserved. This program and the accompanying materials are made available
* under the terms of the Eclipse Public License v1.0 which accompanies this
* distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
* 
* Contributors: Friederich Kupzog - initial API and implementation
* fkmk@kupzog.de www.kupzog.de/fkmk
******************************************************************************/

/**
* @author kupzog (c) 2004 by Friederich Kupzog Elektronik & Software
* 
* The table model is the most important part of KTable. It provides - content
* information - layout information - rendering information to the KTable.
* 
* Generally speaking, all functions should return their results as quick as
* possible. If the table is slow, check it with KTableModelBasic. It is no
* longer slow, your model should be tuned.
* 
*/

interface KTableModel {

/**
 * This function should return the content at the given position. The
 * content is an Object, that means it can be everything.
 * 
 * The returned Object is handed over to the KTableCellRenderer. You can
 * deciede which renderer is used in getCellRenderer. Usually, the renderer
 * expects the content being of a certain type.
 */
Object getContentAt(int col, int row);

/**
 * A table cell will be "in place editable" if this method returns a valid
 * cell editor for the given cell. For no edit functionalitity return null.
 * 
 * @param col
 * @param row
 * @return KTableCellEditor
 */
KTableCellEditor getCellEditor(int col, int row);

/**
 * If getCellEditor() does return eny editors instead of null, the table
 * will use this method to set the changed cell values.
 * 
 * @param col
 * @param row
 */
void setContentAt(int col, int row, Object value);

/**
 * This function tells the KTable how many rows have to be displayed. KTable
 * counts header rows as normal rows, so the number of header rows has to be
 * added to the number of data rows. The function must at least return the
 * number of fixed rows.
 * 
 * @return int
 */
int getRowCount();

/**
 * This function tells the KTable how many rows form the "column header".
 * These rows are always displayed and not scrolled.
 * 
 * @return int
 */
int getFixedRowCount();

/**
 * This function tells the KTable how many columns have to be displayed. It
 * must at least return the number of fixed Columns.
 */
int getColumnCount();

/**
 * This function tells the KTable how many columns form the "row header".
 * These columns are always displayed and not scrolled.
 * 
 * @return int
 */
int getFixedColumnCount();

/**
 * Each column can have its individual width. The model has to manage these
 * widths and return the values with this function.
 * 
 * @param col
 * @return int
 */
int getColumnWidth(int col);

/**
 * This function should return true if the user should be allowed to resize
 * the given column. (all rows have the same height except the first)
 * 
 * @param col
 * @return boolean
 */
boolean isColumnResizable(int col);

/**
 * Each column can have its individual width. The model has to manage these
 * widths. If the user resizes a column, the model has to keep track of
 * these changes. The model is informed about such a resize by this method.
 * (view updates are managed by the table)
 * 
 * @param col
 * @param value
 */
void setColumnWidth(int col, int value);

/**
 * All rows except the first row have the same height.
 * 
 * @return int
 */
int getRowHeight();

/**
 * Returns the height of the first row, usually the header row. If no header
 * is needed, this function should return the same value as getRowHeight.
 * 
 * @return int
 */
int getFirstRowHeight();

/**
 * This function should return true if the user should be allowed to resize
 * the rows.
 * 
 * @param col
 * @return boolean
 */
boolean isRowResizable();

/**
 * This function should return the minimum height of the rows. It is only
 * needed if the rows are resizable.
 * 
 * @return int
 */
int getRowHeightMinimum();

/**
 * If the user resizes a row, the model has to keep track of these changes.
 * The model is informed about such a resize by this method. (view updates
 * are managed by the table)
 */
void setRowHeight(int value);

/**
 * Returns the cell renderer for the given cell. For a first approach,
 * KTableCellRenderer.defaultRenderer can be returned. Derive
 * KTableCellRenderer to change the tables appearance.
 * 
 * @param col
 * @param row
 * @return KTableCellRenderer
 */
KTableCellRenderer getCellRenderer(int col, int row);
}

class KTableColorCellRenderer extends KTableCellRenderer {

	  /**
	   * 
	   */
	  public KTableColorCellRenderer() {
	  }

	  /*
	   * overridden from superclass
	   */
	  public int getOptimalWidth(GC gc, int col, int row, Object content,
	      boolean fixed) {
	    return 16;
	  }

	  /*
	   * overridden from superclass
	   */
	  public void drawCell(GC gc, Rectangle rect, int col, int row,
	      Object content, boolean focus, boolean fixed, boolean clicked) {
	    // Performance test:
	    /*
	     * gc.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_RED));
	     * gc.fillRectangle(rect);
	     * 
	     * int j=1; for (int i = 0; i < 10000000; i++) { j++; }
	     */
	    Color color = new Color(m_Display, (RGB) content);
	    gc.setBackground(m_Display.getSystemColor(SWT.COLOR_GRAY));
	    rect.height++;
	    rect.width++;
	    gc.fillRectangle(rect);

	    gc.setBackground(color);
	    //if (!focus) {
	      rect.x += 1;
	      rect.y += 1;
	      rect.height -= 2;
	      rect.width -= 2;
	    //}
	    gc.fillRectangle(rect);
	    color.dispose();
	  }

	}







/*******************************************************************************
* Copyright (C) 2004 by Friederich Kupzog Elektronik & Software All rights
* reserved. This program and the accompanying materials are made available
* under the terms of the Eclipse Public License v1.0 which accompanies this
* distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
* 
* Contributors: Friederich Kupzog - initial API and implementation
* fkmk@kupzog.de www.kupzog.de/fkmk
******************************************************************************/

/**
* @author Friederich Kupzog
*/
class KTableModelExample implements KTableModel {

private int[] colWidths;

private int rowHeight;

private HashMap<String, Object> content;

/**
 * 
 */
public KTableModelExample() {
  colWidths = new int[getColumnCount()];
  for (int i = 0; i < colWidths.length; i++) {
    colWidths[i] = 270;
  }
  rowHeight = 18;
  content = new HashMap<String, Object>();
}

// Inhalte

public Object getContentAt(int col, int row) {
  // System.out.println("col "+col+" row "+row);
  String erg = (String) content.get(col + "/" + row);
  if (erg != null)
    return erg;
  return col + "/" + row;
}

/*
 * overridden from superclass
 */
public KTableCellEditor getCellEditor(int col, int row) {
  if (col % 2 == 0) {
    KTableCellEditorCombo e = new KTableCellEditorCombo();
    e
        .setItems(new String[] { "First text", "Second text",
            "third text" });
    return e;
  } else
    return new KTableCellEditorText();
}

/*
 * overridden from superclass
 */
public void setContentAt(int col, int row, Object value) {
  content.put(col + "/" + row, value);
  //
}

// Umfang

public int getRowCount() {
  return 100;
}

public int getFixedRowCount() {
  return 2;
}

public int getColumnCount() {
  return 100;
}

public int getFixedColumnCount() {
  return 1;
}

// GroBen

public int getColumnWidth(int col) {
  return colWidths[col];
}

public int getRowHeight() {
  return rowHeight;
}

public boolean isColumnResizable(int col) {
  return true;
}

public int getFirstRowHeight() {
  return 22;
}

public boolean isRowResizable() {
  return true;
}

public int getRowHeightMinimum() {
  return 18;
}

public void setColumnWidth(int col, int value) {
  colWidths[col] = value;
}

public void setRowHeight(int value) {
  if (value < 2)
    value = 2;
  rowHeight = value;
}

// Rendering

public KTableCellRenderer getCellRenderer(int col, int row) {
  return KTableCellRenderer.defaultRenderer;
}

}

/*******************************************************************************
* Copyright (C) 2004 by Friederich Kupzog Elektronik & Software All rights
* reserved. This program and the accompanying materials are made available
* under the terms of the Eclipse Public License v1.0 which accompanies this
* distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
* 
* Contributors: Friederich Kupzog - initial API and implementation
* fkmk@kupzog.de www.kupzog.de/fkmk
******************************************************************************/

class PaletteExampleModel implements KTableModel {

/*
 * overridden from superclass
 */
public Object getContentAt(int col, int row) {
  return new RGB(col * 16, row * 16, (col + row) * 8);
}

/*
 * overridden from superclass
 */
public KTableCellEditor getCellEditor(int col, int row) {
  return null;
}

/*
 * overridden from superclass
 */
public void setContentAt(int col, int row, Object value) {
}

/*
 * overridden from superclass
 */
public int getRowCount() {
  return 16;
}

/*
 * overridden from superclass
 */
public int getFixedRowCount() {
  return 0;
}

/*
 * overridden from superclass
 */
public int getColumnCount() {
  return 16;
}

/*
 * overridden from superclass
 */
public int getFixedColumnCount() {
  return 0;
}

/*
 * overridden from superclass
 */
public int getColumnWidth(int col) {
  return 10;
}

/*
 * overridden from superclass
 */
public boolean isColumnResizable(int col) {
  return false;
}

/*
 * overridden from superclass
 */
public void setColumnWidth(int col, int value) {
}

/*
 * overridden from superclass
 */
public int getRowHeight() {
  return 10;
}

/*
 * overridden from superclass
 */
public int getFirstRowHeight() {
  return 10;
}

/*
 * overridden from superclass
 */
public boolean isRowResizable() {
  return false;
}

/*
 * overridden from superclass
 */
public int getRowHeightMinimum() {
  return 10;
}

/*
 * overridden from superclass
 */
public void setRowHeight(int value) {
}

private static KTableCellRenderer myRenderer = new PaletteExampleRenderer();

/*
 * overridden from superclass
 */
public KTableCellRenderer getCellRenderer(int col, int row) {
  return myRenderer;
}

}


/*******************************************************************************
* Copyright (C) 2004 by Friederich Kupzog Elektronik & Software All rights
* reserved. This program and the accompanying materials are made available
* under the terms of the Eclipse Public License v1.0 which accompanies this
* distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
* 
* Contributors: Friederich Kupzog - initial API and implementation
* fkmk@kupzog.de www.kupzog.de/fkmk
******************************************************************************/

class PaletteExampleRenderer extends KTableCellRenderer {

/**
 * 
 */
public PaletteExampleRenderer() {
}

/*
 * overridden from superclass
 */
public int getOptimalWidth(GC gc, int col, int row, Object content,
    boolean fixed) {
  return 16;
}

/*
 * overridden from superclass
 */
public void drawCell(GC gc, Rectangle rect, int col, int row,
    Object content, boolean focus, boolean fixed, boolean clicked) {
  // Performance test:
  /*
   * gc.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_RED));
   * gc.fillRectangle(rect);
   * 
   * int j=1; for (int i = 0; i < 10000000; i++) { j++; }
   */
  Color color = new Color(m_Display, (RGB) content);
  gc.setBackground(m_Display.getSystemColor(SWT.COLOR_WHITE));
  rect.height++;
  rect.width++;
  gc.fillRectangle(rect);

  gc.setBackground(color);
  if (!focus) {
    rect.x += 1;
    rect.y += 1;
    rect.height -= 2;
    rect.width -= 2;
  }
  gc.fillRectangle(rect);
  color.dispose();
}

}

/*******************************************************************************
* Copyright (C) 2004 by Friederich Kupzog Elektronik & Software All rights
* reserved. This program and the accompanying materials are made available
* under the terms of the Eclipse Public License v1.0 which accompanies this
* distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
* 
* Contributors: Friederich Kupzog - initial API and implementation
* fkmk@kupzog.de www.kupzog.de/fkmk
******************************************************************************/

class TownExampleModel implements KTableModel {

private int[] colWidths;

private TownExampleContent[] content;

public TownExampleModel() {
  colWidths = new int[getColumnCount()];
  colWidths[0] = 120;
  colWidths[1] = 100;
  colWidths[2] = 180;

  content = new TownExampleContent[3];
  content[0] = new TownExampleContent("Aachen", "Germany");
  content[1] = new TownExampleContent("Cologne", "Germany");
  content[2] = new TownExampleContent("Edinburgh", "Scotland");

}

/*
 * overridden from superclass
 */
public Object getContentAt(int col, int row) {
  if (row == 0) // Header
  {
    if (col == 0)
      return "Town";
    else if (col == 1)
      return "Country";
    else
      return "Notes";
  } else {
    return content[row - 1];
  }
}

/*
 * overridden from superclass
 */
public KTableCellEditor getCellEditor(int col, int row) {
  if (row > 0 && col == 2)
    return new KTableCellEditorMultilineText();
  return null;
}

/*
 * overridden from superclass
 */
public void setContentAt(int col, int row, Object value) {
  content[row - 1].notes = (String) value;
}

/*
 * overridden from superclass
 */
public int getRowCount() {
  return 4;
}

/*
 * overridden from superclass
 */
public int getFixedRowCount() {
  return 1;
}

/*
 * overridden from superclass
 */
public int getColumnCount() {
  return 3;
}

/*
 * overridden from superclass
 */
public int getFixedColumnCount() {
  return 0;
}

/*
 * overridden from superclass
 */
public int getColumnWidth(int col) {
  return colWidths[col];
}

/*
 * overridden from superclass
 */
public boolean isColumnResizable(int col) {
  return (col != 0);
}

/*
 * overridden from superclass
 */
public void setColumnWidth(int col, int value) {
  if (value > 120)
    colWidths[col] = value;
}

/*
 * overridden from superclass
 */
public int getRowHeight() {
  return 140;
}

/*
 * overridden from superclass
 */
public int getFirstRowHeight() {
  return 20;
}

/*
 * overridden from superclass
 */
public boolean isRowResizable() {
  return false;
}

/*
 * overridden from superclass
 */
public int getRowHeightMinimum() {
  return 20;
}

/*
 * overridden from superclass
 */
public void setRowHeight(int value) {
}

/*
 * overridden from superclass
 */
public KTableCellRenderer getCellRenderer(int col, int row) {
  if (row > 0)
    return new TownExampleRenderer();
  return KTableCellRenderer.defaultRenderer;
}

}

/*******************************************************************************
* Copyright (C) 2004 by Friederich Kupzog Elektronik & Software All rights
* reserved. This program and the accompanying materials are made available
* under the terms of the Eclipse Public License v1.0 which accompanies this
* distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
* 
* Contributors: Friederich Kupzog - initial API and implementation
* fkmk@kupzog.de www.kupzog.de/fkmk
******************************************************************************/

class TownExampleRenderer extends KTableCellRenderer {

protected Display m_Display;

public TownExampleRenderer() {
  m_Display = Display.getCurrent();
}

public int getOptimalWidth(GC gc, int col, int row, Object content,
    boolean fixed) {
  return Math.max(gc.stringExtent(content.toString()).x + 8, 120);
}

public void drawCell(GC gc, Rectangle rect, int col, int row,
    Object content, boolean focus, boolean fixed, boolean clicked) {
  Color textColor;
  Color backColor;
  Color ffcc33;
  TownExampleContent myContent = (TownExampleContent) content;

  if (focus) {
    textColor = m_Display.getSystemColor(SWT.COLOR_BLUE);
  } else {
    textColor = m_Display.getSystemColor(SWT.COLOR_LIST_FOREGROUND);
  }
  backColor = (m_Display.getSystemColor(SWT.COLOR_LIST_BACKGROUND));
  ffcc33 = m_Display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);

  gc.setForeground(ffcc33);
  gc.drawLine(rect.x, rect.y + rect.height, rect.x + rect.width, rect.y
      + rect.height);

  gc.setForeground(ffcc33);
  gc.drawLine(rect.x + rect.width, rect.y, rect.x + rect.width, rect.y
      + rect.height);

  if (col == 0) {
    gc.setBackground(m_Display
        .getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
    textColor = m_Display.getSystemColor(SWT.COLOR_LIST_FOREGROUND);
    gc.setForeground(textColor);

    gc.drawImage((myContent.image), rect.x, rect.y);

    rect.y += 120;
    rect.height -= 120;
    gc.fillRectangle(rect);
    gc.drawText((myContent.name), rect.x + 25, rect.y + 2);
  }

  else if (col == 1) {
    gc.setBackground(backColor);
    gc.setForeground(textColor);

    gc.fillRectangle(rect);

    SWTX.drawTextImage(gc, myContent.country,
        SWTX.ALIGN_HORIZONTAL_LEFT | SWTX.ALIGN_VERTICAL_TOP, null,
        SWTX.ALIGN_HORIZONTAL_LEFT | SWTX.ALIGN_VERTICAL_CENTER,
        rect.x + 3, rect.y, rect.width - 3, rect.height);

  }

  else if (col == 2) {
    gc.setBackground(backColor);
    gc.setForeground(textColor);

    gc.fillRectangle(rect);
    Rectangle save = gc.getClipping();
    gc.setClipping(rect);
    gc.drawText((myContent.notes), rect.x + 3, rect.y);
    gc.setClipping(save);

  }

}

}

/*******************************************************************************
* Copyright (C) 2004 by Friederich Kupzog Elektronik & Software All rights
* reserved. This program and the accompanying materials are made available
* under the terms of the Eclipse Public License v1.0 which accompanies this
* distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
* 
* Contributors: Friederich Kupzog - initial API and implementation
* fkmk@kupzog.de www.kupzog.de/fkmk
******************************************************************************/

class TownExampleContent {
public String name;

public Image image;

public String country;

public String notes;

public TownExampleContent(String name, String country) {
  this.name = name;
  this.country = country;
  image = loadImageResource(Display.getCurrent(), "/gfx/" + name + ".gif");
  System.out.println(image);
  notes = "Double click to edit and use \n"
      + "Shift+Enter to start a new line...";
}

public Image loadImageResource(Display d, String name) {
  try {

    Image ret = null;
    Class clazz = this.getClass();
    InputStream is = clazz.getResourceAsStream(name);
    if (is != null) {
      ret = new Image(d, is);
      is.close();
    }
    return ret;
  } catch (Exception e1) {
    return null;
  }
}

/*
 * overridden from superclass
 */
public String toString() {
  return notes;
}

}

/**
* @author Kosta, Friederich Kupzog
*/
class SWTX {
public static final int EVENT_SWTX_BASE = 1000;

public static final int EVENT_TABLE_HEADER = EVENT_SWTX_BASE + 1;

public static final int EVENT_TABLE_HEADER_CLICK = EVENT_SWTX_BASE + 2;

public static final int EVENT_TABLE_HEADER_RESIZE = EVENT_SWTX_BASE + 3;

//
public static final int ALIGN_HORIZONTAL_MASK = 0x0F;

public static final int ALIGN_HORIZONTAL_NONE = 0x00;

public static final int ALIGN_HORIZONTAL_LEFT = 0x01;

public static final int ALIGN_HORIZONTAL_LEFT_LEFT = ALIGN_HORIZONTAL_LEFT;

public static final int ALIGN_HORIZONTAL_LEFT_RIGHT = 0x02;

public static final int ALIGN_HORIZONTAL_LEFT_CENTER = 0x03;

public static final int ALIGN_HORIZONTAL_RIGHT = 0x04;

public static final int ALIGN_HORIZONTAL_RIGHT_RIGHT = ALIGN_HORIZONTAL_RIGHT;

public static final int ALIGN_HORIZONTAL_RIGHT_LEFT = 0x05;

public static final int ALIGN_HORIZONTAL_RIGHT_CENTER = 0x06;

public static final int ALIGN_HORIZONTAL_CENTER = 0x07;

public static final int ALIGN_VERTICAL_MASK = 0xF0;

public static final int ALIGN_VERTICAL_TOP = 0x10;

public static final int ALIGN_VERTICAL_BOTTOM = 0x20;

public static final int ALIGN_VERTICAL_CENTER = 0x30;

//
private static GC m_LastGCFromExtend;

private static Map<String, Point> m_StringExtentCache = new HashMap<String, Point>();

private static synchronized Point getCachedStringExtent(GC gc, String text) {
  if (m_LastGCFromExtend != gc) {
    m_StringExtentCache.clear();
    m_LastGCFromExtend = gc;
  }
  Point p = (Point) m_StringExtentCache.get(text);
  if (p == null) {
    if (text == null)
      return new Point(0, 0);
    p = gc.stringExtent(text);
    m_StringExtentCache.put(text, p);
  }
  return new Point(p.x, p.y);
}

public static int drawTextVerticalAlign(GC gc, String text, int textAlign,
    int x, int y, int w, int h) {
  if (text == null)
    text = "";
  Point textSize = getCachedStringExtent(gc, text);
  {
    boolean addPoint = false;
    while ((text.length() > 0) && (textSize.x >= w)) {
      text = text.substring(0, text.length() - 1);
      textSize = getCachedStringExtent(gc, text + "...");
      addPoint = true;
    }
    if (addPoint)
      text = text + "...";
    textSize = getCachedStringExtent(gc, text);
    if (textSize.x >= w) {
      text = "";
      textSize = getCachedStringExtent(gc, text);
    }
  }
  //
  if ((textAlign & ALIGN_VERTICAL_MASK) == ALIGN_VERTICAL_TOP) {
    gc.drawText(text, x, y);
    gc.fillRectangle(x, y + textSize.y, textSize.x, h - textSize.y);
    return textSize.x;
  }
  if ((textAlign & ALIGN_VERTICAL_MASK) == ALIGN_VERTICAL_BOTTOM) {
    gc.drawText(text, x, y + h - textSize.y);
    gc.fillRectangle(x, y, textSize.x, h - textSize.y);
    return textSize.x;
  }
  if ((textAlign & ALIGN_VERTICAL_MASK) == ALIGN_VERTICAL_CENTER) {
    int yOffset = (h - textSize.y) / 2;
    gc.drawText(text, x, y + yOffset);
    gc.fillRectangle(x, y, textSize.x, yOffset);
    gc.fillRectangle(x, y + yOffset + textSize.y, textSize.x, h
        - (yOffset + textSize.y));
    return textSize.x;
  }
  throw new SWTException(
      "H: "
          + (textAlign & ALIGN_VERTICAL_MASK));
}

public static void drawTransparentImage(GC gc, Image image, int x, int y) {
  if (image == null)
    return;
  Point imageSize = new Point(image.getBounds().width,
      image.getBounds().height);
  Image img = new Image(Display.getCurrent(), imageSize.x, imageSize.y);
  GC gc2 = new GC(img);
  gc2.setBackground(gc.getBackground());
  gc2.fillRectangle(0, 0, imageSize.x, imageSize.y);
  gc2.drawImage(image, 0, 0);
  gc.drawImage(img, x, y);
  gc2.dispose();
  img.dispose();
}

public static void drawImageVerticalAlign(GC gc, Image image,
    int imageAlign, int x, int y, int h) {
  if (image == null)
    return;
  Point imageSize = new Point(image.getBounds().width,
      image.getBounds().height);
  //
  if ((imageAlign & ALIGN_VERTICAL_MASK) == ALIGN_VERTICAL_TOP) {
    drawTransparentImage(gc, image, x, y);
    gc.fillRectangle(x, y + imageSize.y, imageSize.x, h - imageSize.y);
    return;
  }
  if ((imageAlign & ALIGN_VERTICAL_MASK) == ALIGN_VERTICAL_BOTTOM) {
    drawTransparentImage(gc, image, x, y + h - imageSize.y);
    gc.fillRectangle(x, y, imageSize.x, h - imageSize.y);
    return;
  }
  if ((imageAlign & ALIGN_VERTICAL_MASK) == ALIGN_VERTICAL_CENTER) {
    int yOffset = (h - imageSize.y) / 2;
    drawTransparentImage(gc, image, x, y + yOffset);
    gc.fillRectangle(x, y, imageSize.x, yOffset);
    gc.fillRectangle(x, y + yOffset + imageSize.y, imageSize.x, h
        - (yOffset + imageSize.y));
    return;
  }
  throw new SWTException(
      "H: "
          + (imageAlign & ALIGN_VERTICAL_MASK));
}

public static void drawTextImage(GC gc, String text, int textAlign,
    Image image, int imageAlign, int x, int y, int w, int h) {
  Point textSize = getCachedStringExtent(gc, text);
  Point imageSize;
  if (image != null)
    imageSize = new Point(image.getBounds().width,
        image.getBounds().height);
  else
    imageSize = new Point(0, 0);
  //
  /*
   * Rectangle oldClipping = gc.getClipping(); gc.setClipping(x, y, w, h);
   */
  try {
    if ((image == null)
        && ((textAlign & ALIGN_HORIZONTAL_MASK) == ALIGN_HORIZONTAL_CENTER)) {
      Point p = getCachedStringExtent(gc, text);
      int offset = (w - p.x) / 2;
      if (offset > 0) {
        drawTextVerticalAlign(gc, text, textAlign, x + offset, y, w
            - offset, h);
        gc.fillRectangle(x, y, offset, h);
        gc
            .fillRectangle(x + offset + p.x, y, w
                - (offset + p.x), h);
      } else {
        p.x = drawTextVerticalAlign(gc, text, textAlign, x, y, w, h);
        // gc.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_YELLOW));
        gc.fillRectangle(x + p.x, y, w - (p.x), h);
        // offset = (w - p.x) / 2;
        // gc.fillRectangle(x, y, offset, h);
        // gc.fillRectangle(x + offset + p.x, y, w - (offset + p.x),
        // h);
      }
      return;
    }
    if (((text == null) || (text.length() == 0))
        && ((imageAlign & ALIGN_HORIZONTAL_MASK) == ALIGN_HORIZONTAL_CENTER)) {
      int offset = (w - imageSize.x) / 2;
      // System.out.println("w: " + w + " imageSize" + imageSize + "
      // offset: " + offset);
      drawImageVerticalAlign(gc, image, imageAlign, x + offset, y, h);
      gc.fillRectangle(x, y, offset, h);
      gc.fillRectangle(x + offset + imageSize.x, y, w
          - (offset + imageSize.x), h);
      return;
    }
    if ((textAlign & ALIGN_HORIZONTAL_MASK) == ALIGN_HORIZONTAL_LEFT) {
      if ((imageAlign & ALIGN_HORIZONTAL_MASK) == ALIGN_HORIZONTAL_NONE) {
        textSize.x = drawTextVerticalAlign(gc, text, textAlign, x,
            y, w, h);
        gc.fillRectangle(x + textSize.x, y, w - textSize.x, h);
        return;
      }
      if ((imageAlign & ALIGN_HORIZONTAL_MASK) == ALIGN_HORIZONTAL_LEFT) {
        textSize.x = drawTextVerticalAlign(gc, text, textAlign, x
            + imageSize.x, y, w - imageSize.x, h);
        drawImageVerticalAlign(gc, image, imageAlign, x, y, h);
        gc.fillRectangle(x + textSize.x + imageSize.x, y, w
            - (textSize.x + imageSize.x), h);
        return;
      }
      if ((imageAlign & ALIGN_HORIZONTAL_MASK) == ALIGN_HORIZONTAL_RIGHT) {
        textSize.x = drawTextVerticalAlign(gc, text, textAlign, x,
            y, w - imageSize.x, h);
        drawImageVerticalAlign(gc, image, imageAlign, x + w
            - imageSize.x, y, h);
        gc.fillRectangle(x + textSize.x, y, w
            - (textSize.x + imageSize.x), h);
        return;
      }
      if ((imageAlign & ALIGN_HORIZONTAL_MASK) == ALIGN_HORIZONTAL_RIGHT_LEFT) {
        textSize.x = drawTextVerticalAlign(gc, text, textAlign, x,
            y, w - imageSize.x, h);
        drawImageVerticalAlign(gc, image, imageAlign, x
            + textSize.x, y, h);
        gc.fillRectangle(x + textSize.x + imageSize.x, y, w
            - (textSize.x + imageSize.x), h);
        return;
      }
      if ((imageAlign & ALIGN_HORIZONTAL_MASK) == ALIGN_HORIZONTAL_RIGHT_CENTER) {
        textSize.x = drawTextVerticalAlign(gc, text, textAlign, x,
            y, w - imageSize.x, h);
        int xOffset = (w - textSize.x - imageSize.x) / 2;
        drawImageVerticalAlign(gc, image, imageAlign, x
            + textSize.x + xOffset, y, h);
        gc.fillRectangle(x + textSize.x, y, xOffset, h);
        gc.fillRectangle(x + textSize.x + xOffset + imageSize.x, y,
            w - (textSize.x + xOffset + imageSize.x), h);
        return;
      }
      throw new SWTException(
          "H: "
              + (imageAlign & ALIGN_HORIZONTAL_MASK));
    } // text align left
    if ((textAlign & ALIGN_HORIZONTAL_MASK) == ALIGN_HORIZONTAL_RIGHT) {
      if ((imageAlign & ALIGN_HORIZONTAL_MASK) == ALIGN_HORIZONTAL_NONE) {
        textSize.x = drawTextVerticalAlign(gc, text, textAlign, x,
            -1000, w, h);
        drawTextVerticalAlign(gc, text, textAlign, x + w
            - textSize.x, y, w, h);
        gc.fillRectangle(x, y, w - textSize.x, h);
        return;
      }
      if ((imageAlign & ALIGN_HORIZONTAL_MASK) == ALIGN_HORIZONTAL_LEFT) {
        textSize.x = drawTextVerticalAlign(gc, text, textAlign, x,
            -1000, w - imageSize.x, h);
        drawTextVerticalAlign(gc, text, textAlign, x + w
            - textSize.x, y, w - imageSize.x, h);
        drawImageVerticalAlign(gc, image, imageAlign, x, y, h);
        gc.fillRectangle(x + imageSize.x, y, w
            - (textSize.x + imageSize.x), h);
        return;
      }
      if ((imageAlign & ALIGN_HORIZONTAL_MASK) == ALIGN_HORIZONTAL_LEFT_RIGHT) {
        textSize.x = drawTextVerticalAlign(gc, text, textAlign, x,
            -1000, w - imageSize.x, h);
        drawTextVerticalAlign(gc, text, textAlign, x + w
            - textSize.x, y, w - imageSize.x, h);
        drawImageVerticalAlign(gc, image, imageAlign, x + w
            - (textSize.x + imageSize.x), y, h);
        gc.fillRectangle(x, y, w - (textSize.x + imageSize.x), h);
        return;
      }
      if ((imageAlign & ALIGN_HORIZONTAL_MASK) == ALIGN_HORIZONTAL_LEFT_CENTER) {
        textSize.x = drawTextVerticalAlign(gc, text, textAlign, x,
            -1000, w - imageSize.x, h);
        drawTextVerticalAlign(gc, text, textAlign, x + w
            - textSize.x, y, w - imageSize.x, h);
        int xOffset = (w - textSize.x - imageSize.x) / 2;
        drawImageVerticalAlign(gc, image, imageAlign, x + xOffset,
            y, h);
        gc.fillRectangle(x, y, xOffset, h);
        gc.fillRectangle(x + xOffset + imageSize.x, y, w
            - (xOffset + imageSize.x + textSize.x), h);
        return;
      }
      if ((imageAlign & ALIGN_HORIZONTAL_MASK) == ALIGN_HORIZONTAL_RIGHT) {
        textSize.x = drawTextVerticalAlign(gc, text, textAlign, x,
            -1000, w - imageSize.x, h);
        drawTextVerticalAlign(gc, text, textAlign, x + w
            - (textSize.x + imageSize.x), y, w - imageSize.x, h);
        drawImageVerticalAlign(gc, image, imageAlign, x + w
            - imageSize.x, y, h);
        gc.fillRectangle(x, y, w - (textSize.x + imageSize.x), h);
        return;
      }
      throw new SWTException(
          "H: "
              + (imageAlign & ALIGN_HORIZONTAL_MASK));
    } // text align right
    throw new SWTException(
        "H: "
            + (textAlign & ALIGN_HORIZONTAL_MASK));
  } // trye
  finally {
    // gc.setClipping(oldClipping);
  }
}

public static void drawTextImage(GC gc, String text, int textAlign,
    Image image, int imageAlign, Rectangle r) {
  drawTextImage(gc, text, textAlign, image, imageAlign, r.x, r.y,
      r.width, r.height);
}

public static void drawButtonUp(GC gc, String text, int textAlign,
    Image image, int imageAlign, int x, int y, int w, int h,
    Color face, Color shadowHigh, Color shadowNormal, Color shadowDark,
    int leftMargin, int topMargin) {
  Color prevForeground = gc.getForeground();
  Color prevBackground = gc.getBackground();
  try {
    gc.setBackground(face);
    gc.setForeground(shadowHigh);
    gc.drawLine(x, y, x, y + h - 1);
    gc.drawLine(x, y, x + w - 2, y);
    gc.setForeground(shadowDark);
    gc.drawLine(x + w - 1, y, x + w - 1, y + h - 1);
    gc.drawLine(x, y + h - 1, x + w - 1, y + h - 1);
    gc.setForeground(shadowNormal);
    gc.drawLine(x + w - 2, y + 1, x + w - 2, y + h - 2);
    gc.drawLine(x + 1, y + h - 2, x + w - 2, y + h - 2);
    //
    gc.fillRectangle(x + 1, y + 1, leftMargin, h - 3);
    gc.fillRectangle(x + 1, y + 1, w - 3, topMargin);
    gc.setForeground(prevForeground);
    drawTextImage(gc, text, textAlign, image, imageAlign, x + 1
        + leftMargin, y + 1 + topMargin, w - 3 - leftMargin, h - 3
        - topMargin);
  } finally {
    gc.setForeground(prevForeground);
    gc.setBackground(prevBackground);
  }
}

public static void drawButtonUp(GC gc, String text, int textAlign,
    Image image, int imageAlign, int x, int y, int w, int h, Color face) {
  Display display = Display.getCurrent();
  drawButtonUp(gc, text, textAlign, image, imageAlign, x, y, w, h, face,
      display.getSystemColor(SWT.COLOR_WIDGET_HIGHLIGHT_SHADOW),
      display.getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW), display
          .getSystemColor(SWT.COLOR_WIDGET_DARK_SHADOW), 2, 2);
}

public static void drawButtonUp(GC gc, String text, int textAlign,
    Image image, int imageAlign, Rectangle r, int leftMargin,
    int topMargin) {
  Display display = Display.getCurrent();
  drawButtonUp(gc, text, textAlign, image, imageAlign, r.x, r.y, r.width,
      r.height, display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND),
      display.getSystemColor(SWT.COLOR_WIDGET_HIGHLIGHT_SHADOW),
      display.getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW), display
          .getSystemColor(SWT.COLOR_WIDGET_DARK_SHADOW),
      leftMargin, topMargin);
}

public static void drawButtonUp(GC gc, String text, int textAlign,
    Image image, int imageAlign, int x, int y, int w, int h) {
  Display display = Display.getCurrent();
  drawButtonUp(gc, text, textAlign, image, imageAlign, x, y, w, h,
      display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND), display
          .getSystemColor(SWT.COLOR_WIDGET_HIGHLIGHT_SHADOW),
      display.getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW), display
          .getSystemColor(SWT.COLOR_WIDGET_DARK_SHADOW), 2, 2);
}

public static void drawButtonUp(GC gc, String text, int textAlign,
    Image image, int imageAlign, Rectangle r) {
  //Display display = Display.getCurrent();
  drawButtonUp(gc, text, textAlign, image, imageAlign, r.x, r.y, r.width,
      r.height);
}

public static void drawButtonDown(GC gc, String text, int textAlign,
    Image image, int imageAlign, int x, int y, int w, int h,
    Color face, Color shadowNormal, int leftMargin, int topMargin) {
  Color prevForeground = gc.getForeground();
  Color prevBackground = gc.getBackground();
  try {
    gc.setBackground(face);
    gc.setForeground(shadowNormal);
    gc.drawRectangle(x, y, w - 1, h - 1);
    gc.fillRectangle(x + 1, y + 1, 1 + leftMargin, h - 2);
    gc.fillRectangle(x + 1, y + 1, w - 2, topMargin + 1);
    gc.setForeground(prevForeground);
    drawTextImage(gc, text, textAlign, image, imageAlign, x + 2
        + leftMargin, y + 2 + topMargin, w - 3 - leftMargin, h - 3
        - topMargin);
  } finally {
    gc.setForeground(prevForeground);
    gc.setBackground(prevBackground);
  }
}

public static void drawButtonDown(GC gc, String text, int textAlign,
    Image image, int imageAlign, int x, int y, int w, int h) {
  Display display = Display.getCurrent();
  drawButtonDown(gc, text, textAlign, image, imageAlign, x, y, w, h,
      display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND), display
          .getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW), 2, 2);
}

public static void drawButtonDown(GC gc, String text, int textAlign,
    Image image, int imageAlign, Rectangle r) {
  drawButtonDown(gc, text, textAlign, image, imageAlign, r.x, r.y,
      r.width, r.height);
}

public static void drawButtonDown(GC gc, String text, int textAlign,
    Image image, int imageAlign, int x, int y, int w, int h, Color face) {
  Display display = Display.getCurrent();
  drawButtonDown(gc, text, textAlign, image, imageAlign, x, y, w, h,
      face, display.getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW),
      2, 2);
}

public static void drawButtonDeepDown(GC gc, String text, int textAlign,
    Image image, int imageAlign, int x, int y, int w, int h) {
  Display display = Display.getCurrent();
  gc.setForeground(display.getSystemColor(SWT.COLOR_BLACK));
  gc.drawLine(x, y, x + w - 2, y);
  gc.drawLine(x, y, x, y + h - 2);
  gc.setForeground(display.getSystemColor(SWT.COLOR_WHITE));
  gc.drawLine(x + w - 1, y, x + w - 1, y + h - 1);
  gc.drawLine(x, y + h - 1, x + w - 1, y + h - 1);
  gc.setForeground(display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
  gc.drawLine(x + 1, y + h - 2, x + w - 2, y + h - 2);
  gc.drawLine(x + w - 2, y + h - 2, x + w - 2, y + 1);
  //
  gc.setForeground(display.getSystemColor(SWT.COLOR_WIDGET_FOREGROUND));
  gc.setBackground(display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
  gc.fillRectangle(x + 2, y + 2, w - 4, 1);
  gc.fillRectangle(x + 1, y + 2, 2, h - 4);
  //
  gc.setBackground(display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
  drawTextImage(gc, text, textAlign, image, imageAlign, x + 2 + 1,
      y + 2 + 1, w - 4, h - 3 - 1);
}

public static void drawButtonDeepDown(GC gc, String text, int textAlign,
    Image image, int imageAlign, Rectangle r) {
  drawButtonDeepDown(gc, text, textAlign, image, imageAlign, r.x, r.y,
      r.width, r.height);
}

public static void drawFlatButtonUp(GC gc, String text, int textAlign,
    Image image, int imageAlign, int x, int y, int w, int h,
    Color face, Color shadowLight, Color shadowNormal, int leftMargin,
    int topMargin) {
  Color prevForeground = gc.getForeground();
  Color prevBackground = gc.getBackground();
  try {
    gc.setForeground(shadowLight);
    gc.drawLine(x, y, x + w - 1, y);
    gc.drawLine(x, y, x, y + h);
    gc.setForeground(shadowNormal);
    gc.drawLine(x + w, y, x + w, y + h);
    gc.drawLine(x + 1, y + h, x + w, y + h);
    //
    gc.setBackground(face);
    gc.fillRectangle(x + 1, y + 1, leftMargin, h - 1);
    gc.fillRectangle(x + 1, y + 1, w - 1, topMargin);
    //
    gc.setBackground(face);
    gc.setForeground(prevForeground);
    drawTextImage(gc, text, textAlign, image, imageAlign, x + 1
        + leftMargin, y + 1 + topMargin, w - 1 - leftMargin, h - 1
        - topMargin);
  } finally {
    gc.setForeground(prevForeground);
    gc.setBackground(prevBackground);
  }
}

public static void drawShadowImage(GC gc, Image image, int x, int y,
    int alpha) {
  Display display = Display.getCurrent();
  Point imageSize = new Point(image.getBounds().width,
      image.getBounds().height);
  //
  ImageData imgData = new ImageData(imageSize.x, imageSize.y, 24,
      new PaletteData(255, 255, 255));
  imgData.alpha = alpha;
  Image img = new Image(display, imgData);
  GC imgGC = new GC(img);
  imgGC.drawImage(image, 0, 0);
  gc.drawImage(img, x, y);
  imgGC.dispose();
  img.dispose();
}
} 
