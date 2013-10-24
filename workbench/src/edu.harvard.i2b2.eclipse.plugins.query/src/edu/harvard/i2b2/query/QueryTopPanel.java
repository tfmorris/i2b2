/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *     Wensong Pan
 */

package edu.harvard.i2b2.query;

/*
 * QueryTopPanel.java
 *
 * Created on August 2, 2006, 9:04 AM
 */

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Cursor;
import java.util.ArrayList;

import javax.swing.*;
import javax.xml.bind.JAXBElement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.part.ViewPart;

import edu.harvard.i2b2.common.util.jaxb.JAXBUtil;
import edu.harvard.i2b2.common.util.jaxb.JAXBUnWrapHelper;
import edu.harvard.i2b2.query.datavo.QueryJAXBUtil;
import edu.harvard.i2b2.crcxmljaxb.datavo.i2b2message.*;
import edu.harvard.i2b2.crcxmljaxb.datavo.psm.query.MasterInstanceResultResponseType;
import edu.harvard.i2b2.crcxmljaxb.datavo.psm.query.QueryResultInstanceType;
import edu.harvard.i2b2.eclipse.ICommonMethod;
import edu.harvard.i2b2.eclipse.UserInfoBean;

/**
 *
 * @author  wp066
 */
public class QueryTopPanel extends javax.swing.JPanel {
	private static final Log log = LogFactory.getLog(QueryTopPanel.class);
    private QueryTopPanelModel dataModel;
    private String response = null;
    public QueryPanel parentPanel;
    
    private int max_child = 1000;
    public void max_child(int i) {max_child = i;}
    public int max_child() {return max_child;}
    
    private Thread queryThread = null;
    private boolean firsttime = true; //for the start up bug on windows 2000
    
    /** Creates new form QueryTopPanel */
    public QueryTopPanel(QueryPanel parent) {
    	parentPanel = parent;
    	dataModel = parent.dataModel();
        initComponents();
        
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentMoved(java.awt.event.ComponentEvent evt) {
                //formComponentMoved(evt);
            }
            public void componentResized(java.awt.event.ComponentEvent evt) {
                //formComponentResized(evt);
            	//System.out.println("waiting panel resizing ...");
            	int width = (int)(getParent().getWidth());
            	int height = (int)(getParent().getHeight());
            	if(width < 5 || height < 5) {
            		return;
            	}
            	
            	resizePanels(width, height);
            	log.info("width: "+width+", height: "+height);
            	
            	if(firsttime) {
            		firsttime = false;
            		resizePanels(width, height+3);
            		log.info("second width: "+width+", height: "+(height+3));
            	}
            }
        });
    }
    
    public JButton getRunQueryButton() {
    	return jRunQueryButton;
    }
    
    public JButton getDeleteButton() {
    	return jDeleteButton;
    }
    
    /** This method is called from within the constructor to
     *  initialize the form.
     */
    private void initComponents() {
    	jNameLabel = new javax.swing.JLabel();
        jNameTextField = new javax.swing.JTextField();
    	jDeleteButton = new javax.swing.JButton();
        jScrollPane1 = new QueryConceptTreePanel("Group 1", this);
        jRunQueryButton = new javax.swing.JButton();
        jCancelButton = new javax.swing.JButton();
        jClearGroupsButton = new javax.swing.JButton();
        jScrollPane2 = new QueryConceptTreePanel("Group 2", this);
        jScrollPane3 = new QueryConceptTreePanel("Group 3", this);
        jScrollPane4 = new javax.swing.JScrollPane();
        jPanel1 = new javax.swing.JPanel();
        //jVisitComboBox = new javax.swing.JComboBox();
        jAndOrLabel1 = new javax.swing.JLabel();
        jAndOrLabel2 = new javax.swing.JLabel();
        jMorePanelsButton = new javax.swing.JButton();
        //jSlider1 = new javax.swing.JSlider();
        //jLabel1 = new javax.swing.JLabel();
        //jLabel2 = new javax.swing.JLabel();

        setLayout(null);

        //jScrollPane4.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        jPanel1.setLayout(null);

        jPanel1.add(jScrollPane1);
        jScrollPane1.setBounds(0, 0, 180, 200);

        jPanel1.add(jScrollPane2);
        jScrollPane2.setBounds(185, 0, 180, 200);

        jPanel1.add(jScrollPane3);
        jScrollPane3.setBounds(370, 0, 180, 200);

        //jAndOrLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jAndOrLabel1.setText("and");
        //jAndOrLabel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        //jPanel1.add(jAndOrLabel1);
        //jAndOrLabel1.setBounds(190, 90, 30, 18);

        //jAndOrLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        //jAndOrLabel2.setText("and");
        //jAndOrLabel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        //jPanel1.add(jAndOrLabel2);
        //jAndOrLabel2.setBounds(410, 90, 30, 18);

        jNameLabel.setText("Query Name: ");
        jNameLabel.setBounds(8, 10, 70, 23);
        add(jNameLabel);
        
        jNameTextField.setText("");
        jNameTextField.setBounds(95, 10, 400, 20);
        jNameTextField.setEditable(false);
        add(jNameTextField);
        
        jClearGroupsButton.setText("Reset Groups");
        jClearGroupsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	jClearGroupsButtonActionPerformed(evt);
            }
        });
        jClearGroupsButton.setBounds(600, 10, 100, 20);
        add(jClearGroupsButton);
        
        
        jPanel1.setPreferredSize(new Dimension(550, 150));
        jScrollPane4.setViewportView(jPanel1);
        add(jScrollPane4);
        jScrollPane4.setBounds(20, 35, 635, 220);
        
        jCancelButton.setText("Cancel");
        jCancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	jCancelButtonActionPerformed(evt);
            }
        });
        add(jCancelButton);
        jCancelButton.setBounds(20, 255, 70, 23);
        
        jRunQueryButton.setText("Run Query");
        jRunQueryButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	jRunQueryButtonActionPerformed(evt);
            }
        });
        add(jRunQueryButton);
        jRunQueryButton.setBounds(90, 255, 625, 23);
        
        jMorePanelsButton.setText("<html><center>Add<br>"
                + "<left>Group");
        jMorePanelsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMorePanelsButtonActionPerformed(evt);
            }
        });
        add(jMorePanelsButton);
        jMorePanelsButton.setBounds(655, 35, 60, 220);

        /*jDeleteButton.setText("Delete");
        jDeleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jDeleteButtonActionPerformed(evt);
            }
        });
        
        jVisitComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Groups don't have to occur in the same visit", "Groups must all occur in the same visit" }));
        add(jVisitComboBox);
        jVisitComboBox.setBounds(20, 40, 240, 22);

        //add(jDeleteButton);
        //jDeleteButton.setBounds(20, 10, 65, 23);

        add(jScrollPane1);
        jScrollPane1.setBounds(20, 70, 170, 320);

        jRunQueryButton.setText("Run Query");
        jRunQueryButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	jRunQueryButtonActionPerformed(evt);
            }
        });

        add(jRunQueryButton);
        jRunQueryButton.setBounds(20, 10, 87, 23);

        jCancelButton.setText("Remove All");
        jCancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	jRemoveAllButtonActionPerformed(evt);
            }
        });
        
        add(jCancelButton);
        jCancelButton.setBounds(115, 10, 90, 23);

        //jScrollPane4.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        jPanel1.setLayout(null);
        jScrollPane4.getHorizontalScrollBar().setUnitIncrement(20);
        //jPanel1.setVisible(false);

        //jScrollPane1.setToolTipText("scrollpane 1");
        jPanel1.add(jScrollPane1);
        jScrollPane1.setBounds(0, 0, 170, 350);

        jPanel1.add(jScrollPane2);
        jScrollPane2.setBounds(210, 0, 170, 350);*/

        jAndOrLabel1.setBackground(new java.awt.Color(255, 255, 255));
        jAndOrLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jAndOrLabel1.setText("and");
        jAndOrLabel1.setToolTipText("Click to change the relationship");
        jAndOrLabel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jAndOrLabel1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jAndOrLabel1MouseClicked(evt);
            }
        });

        //jPanel1.add(jAndOrLabel1);

        jAndOrLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jAndOrLabel2.setText("and");
        jAndOrLabel2.setToolTipText("Click to change the relationship");
        jAndOrLabel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jAndOrLabel2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jAndOrLabel2MouseClicked(evt);
            }
        });

        //jPanel1.add(jAndOrLabel2);

        /*jPanel1.add(jScrollPane3);
        jScrollPane3.setBounds(420, 0, 170, 350);

        jScrollPane4.setViewportView(jPanel1);

        add(jScrollPane4);
        jScrollPane4.setBounds(20, 70, 594, 370);

        jMorePanelsButton.setText("Add Panel");
        jMorePanelsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMorePanelsButtonActionPerformed(evt);
            }
        });

        add(jMorePanelsButton);
        jMorePanelsButton.setBounds(215, 10, 90, 23);*/

        //jTree1.addTreeWillExpandListener(this);
        //jTree1.addTreeExpansionListener(this);
        //jScrollPane1.setViewportView(new QueryConceptTreePanel("Group 1"));
        //jScrollPane1.setToolTipText("Double click on a folder to view the items inside");
        
        //jTree2.addTreeExpansionListener(this);
        //jScrollPane2.setViewportView(new QueryConceptTreePanel("Group 2"));
        
        //jTree3.addTreeExpansionListener(this);
        //treepanel = new QueryConceptTreePanel("", this);
        //jScrollPane3.setViewportView(new QueryConceptTreePanel("Group 3"));
        
        //jSlider1.setMajorTickSpacing(20);
        /*jSlider1.setPaintTicks(true);
        jSlider1.setValue(0);
        jSlider1.setMinorTickSpacing(10);
        jSlider1.setToolTipText("Slider on left is more Sensitive Query, " +
        		"on right is more Specific");
        add(jSlider1);
        jSlider1.setBounds(380, 40, 140, 18);

        //jLabel1.setFont(new java.awt.Font("Tahoma", 1, 11));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel1.setText("Sensitivity <");
        add(jLabel1);
        jLabel1.setBounds(290, 40, 80, 20);

        //jLabel2.setFont(new java.awt.Font("Tahoma", 1, 11));
        jLabel2.setText("> Specificity");
        add(jLabel2);
        jLabel2.setBounds(525, 40, 70, 20);*/

        dataModel.addPanel(jScrollPane1, null, 0);
        dataModel.addPanel(jScrollPane2, jAndOrLabel1, 0);
        dataModel.addPanel(jScrollPane3, jAndOrLabel2, 555);
    }
    
   /* private void jDeleteButtonActionPerformed(java.awt.event.ActionEvent evt) {
    	DefaultMutableTreeNode node = null;
        TreePath parentPath = jTree1.getSelectionPath();

        if (parentPath == null) {
            //There's no selection.
        	return;
        } else {
            node = (DefaultMutableTreeNode)
                         (parentPath.getLastPathComponent());
            System.out.println("Remove node: "+
            		((QueryTreeNodeData)node.getUserObject()).tooltip());
            treeModel.removeNodeFromParent(node);        
        }
    }*/
    
    protected static ImageIcon createImageIcon(String path) {
        java.net.URL imgURL = QueryTopPanel.class.getResource(path);
        return new ImageIcon(imgURL);
    }
    
    @SuppressWarnings("deprecation")
	private void jCancelButtonActionPerformed(java.awt.event.ActionEvent evt) {
    	//System.out.println("Cancel action");
    	if(queryThread != null) {
	    	queryThread.stop();
	    	queryThread = null;
	    	//setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		    jRunQueryButton.setIcon(null);
	    	jRunQueryButton.setText("Run Query");
	    	//jRunQueryButton.setBackground(defaultcolor);
    	}
    }

    private void jRunQueryButtonActionPerformed(java.awt.event.ActionEvent evt) {
    	//System.out.println("value set on the slider: "+ jSlider1.getValue());
    	if(dataModel.isAllPanelEmpty()) {
    		JOptionPane.showMessageDialog(this, "All groups are empty.");
    		return;
    	}
    	
    	String queryNametmp = jNameTextField.getText();
    	//if(queryNametmp.equals("") || queryNametmp == null) {
    		queryNametmp = dataModel.getTmpQueryName();
    	//}
    	Object selectedValue = JOptionPane.showInputDialog(this, 
    			"Please supply a name for this query: ", "Query Name Dialog",
    			JOptionPane.PLAIN_MESSAGE, null,
    			null, queryNametmp);
    	
    	if(selectedValue == null) {
    		return;
    	}
    	else {
    		queryNametmp = (String) selectedValue;
    	}
    	
    	dataModel.queryName(queryNametmp);
    	final String queryName = queryNametmp;
    	//System.out.println("Provided query name: " + queryName);
    	
    	ImageIcon buttonIcon = createImageIcon("indicator_18.gif");
    	this.jRunQueryButton.setIcon(buttonIcon);
    	this.jRunQueryButton.setText("         Running ......");
    	final Color defaultcolor = jRunQueryButton.getBackground();
    	 
    	dataModel.specificity(0);//jSlider1.getValue());
    	final String xmlStr = dataModel.wirteQueryXML();
    	parentPanel.lastRequestMessage(xmlStr);
    	parentPanel.setPatientCount("");
    	parentPanel.setRequestText(xmlStr);
    	parentPanel.setResponseText("Waiting for response ...");
    	//System.out.println("Query request: "+xmlStr);
    	jNameTextField.setText(queryName);
    	
    	queryThread = new Thread() {
			public void run() {	     
				//setCursor(new Cursor(Cursor.WAIT_CURSOR));
				response = QueryRequestClient.sendQueryRequestREST(xmlStr);
				parentPanel.lastResponseMessage(response);
				if(response != null) {
					//response = response.substring(response.indexOf("<ns2:response"), response.indexOf("</i2b2:response>"));
					parentPanel.setResponseText(response);
					JAXBUtil jaxbUtil = QueryJAXBUtil.getJAXBUtil();
			        
			        try {
			        	JAXBElement jaxbElement = jaxbUtil.unMashallFromString(response);
			        	ResponseMessageType messageType = (ResponseMessageType)jaxbElement.getValue();
				        BodyType bt = messageType.getMessageBody();
				        MasterInstanceResultResponseType masterInstanceResultResponseType = 
							(MasterInstanceResultResponseType) new JAXBUnWrapHelper().getObjectByClass(bt.getAny(),MasterInstanceResultResponseType.class);
				        String queryId = null;
			        	//ResponseMessageType messageType = jaxbUtil.unMashallResponseMessageTypeFromString(response);
			        	StatusType statusType = 
			        		messageType.getResponseHeader().getResultStatus().getStatus();
			        	String status = statusType.getType();
			        	queryId = new Integer(masterInstanceResultResponseType.getQueryMaster().getQueryMasterId()).
			        		toString();//messageType.getResponseHeader().getInfo().getValue();
			        	//System.out.println("Get query id: "+queryId);
			        				        	
			        	QueryMasterData nameNode = new QueryMasterData();
				    	nameNode.name(queryName);
				    	nameNode.visualAttribute("CA");
				    	nameNode.userId(UserInfoBean.getInstance().getUserName());
				    	nameNode.tooltip("A query run by "+nameNode.userId());
				    	nameNode.id(queryId);
				    	//nameNode.xmlContent(xmlStr);
			        	
				    	String count = "";		        	
				    	if(status.equalsIgnoreCase("DONE")) {
				    		String refId=null;
				    		try {
				    			edu.harvard.i2b2.crcxmljaxb.datavo.psm.query.StatusType cellStatusType 
				    				= masterInstanceResultResponseType.getStatus();
				    			if(cellStatusType.getCondition().get(0).getValue().equalsIgnoreCase("RUNNING")) {
				    				JOptionPane.showMessageDialog(parentPanel, 
						    				 "Query is still running, you may check its status later \n" +
						    				 "in the previous queries view by right clicking on a node\n" +
						    				 "then selecting refresh all.");
				    				jRunQueryButton.setIcon(null);
							    	jRunQueryButton.setText("Run Query");
						        	return;
				    			}
				    			else if(cellStatusType.getCondition().get(0).getValue().equalsIgnoreCase("ERROR")) {
				    				JOptionPane.showMessageDialog(parentPanel, 
				    						"Error message delivered from the remote server, " +
				    				 		"you may wish to retry your last action");
				    				jRunQueryButton.setIcon(null);
							    	jRunQueryButton.setText("Run Query");
						        	return;
				    			}
				    			
					    	  	QueryResultInstanceType queryResultInstanceType = 
					        		masterInstanceResultResponseType.getQueryResultInstance().get(0);
					        	refId = new Integer(queryResultInstanceType.getResultInstanceId()).toString();
					        	//System.out.println("Set Ref id: "+ refId);
					    		count = new Integer(queryResultInstanceType.getSetSize()).toString();
						        parentPanel.setPatientCount(count);
				    		}
				    		catch(Exception e) {
					        	e.printStackTrace();
					        	JOptionPane.showMessageDialog(parentPanel, 
			    				"Response delivered from the remote server could not be understood,\n" +
			    				"you may wish to retry your last action.");
					        	
					        	jRunQueryButton.setIcon(null);
						    	jRunQueryButton.setText("Run Query");
					        	return;
					        }
				    		
				    		IWorkbenchPage page = ((QueryPanelInvestigator)parentPanel).parentview.getViewSite().getPage();
				    		ViewPart previousqueryview = (ViewPart) page.findView("edu.harvard.i2b2.eclipse.plugins.previousquery.views.PreviousQueryView");
					    	((ICommonMethod)previousqueryview).doSomething(nameNode.name()+" ["+dataModel.getDayString()+"]"+
					    			"#i2b2seperater#"+nameNode.id());
					        
					        ArrayList<String> nodeXmls = new ArrayList<String>();
					    	for(int i=0; i<dataModel.getCurrentPanelCount(); i++) {
					    		ArrayList<QueryConceptTreeNodeData> nodelist = dataModel.getTreePanel(i).getItems();
					    		for(int j=0; j<nodelist.size(); j++) {
					    			QueryConceptTreeNodeData nodedata = nodelist.get(j);
					    			String termStatus = nodedata.setXmlContent();
					    			if(termStatus.equalsIgnoreCase("error")) {
					    				JOptionPane.showMessageDialog(parentPanel, 
							    				"Response delivered from the remote server could not be understood,\n" +
							    				"you may wish to retry your last action.");
					    				jRunQueryButton.setIcon(null);
								    	jRunQueryButton.setText("Run Query");
					    				return;
					    			}
					    			nodeXmls.add(nodedata.xmlContent());
					    		}
					    	}
					    					    	
					    	ViewPart explorerview = (ViewPart) page.findView("edu.harvard.i2b2.eclipse.plugins.explorer.views.ExplorerView");
					    	String str1 = ""+count;
					    	String str2 = "-"+refId;
					    	((ICommonMethod)explorerview).doSomething(str1+str2);
					    	((ICommonMethod)explorerview).doSomething(nodeXmls);					    	
				    	}
				    	else {
				    		JOptionPane.showMessageDialog(parentPanel, 
				    				 "Error message delivered from the remote server, " +
				    				 "you may wish to retry your last action");
				    		
				    		jRunQueryButton.setIcon(null);
					    	jRunQueryButton.setText("Run Query");
				        	return;
				    	}
				    }
			        catch(Exception e) {
			        	e.printStackTrace();
			        	JOptionPane.showMessageDialog(parentPanel, 
			    				"Response delivered from the remote server could not be understood,\n" +
			    				"you may wish to retry your last action.");
	    				jRunQueryButton.setIcon(null);
				    	jRunQueryButton.setText("Run Query");
	    				return;
			        }
				}
			    //setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			    jRunQueryButton.setIcon(null);
		    	jRunQueryButton.setText("Run Query");
		    	//jRunQueryButton.setBackground(defaultcolor);
			}
      	};	
    	
    	try {    	
	    	queryThread.start();
	    }
	    catch (Exception e) {
	    	e.printStackTrace();
	    	parentPanel.setResponseText(e.getMessage());	    	
	    }	   
    }
    
    private void jClearGroupsButtonActionPerformed(java.awt.event.ActionEvent evt) {
    	reset();   	
    }
    
    public void reset() {
    	java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
		    	jNameTextField.setText("");
		    	
		    	dataModel.clearConceptTrees();
		    	dataModel.removeAdditionalPanels();
		    	dataModel.lastLabelPosition(555);
		    	
		    	jPanel1.setPreferredSize(new Dimension(555, 150));
		    	jScrollPane4.setViewportView(jPanel1);
			}
    	});
    }
    
    private void jAndOrLabel2MouseClicked(java.awt.event.MouseEvent evt) {
    	if(jAndOrLabel2.getText().equalsIgnoreCase("and")) {
    		jAndOrLabel2.setText("or");
    	}
    	else if(jAndOrLabel2.getText().equalsIgnoreCase("or")) {
    		jAndOrLabel2.setText("and");
    	}
    }
    
    private void jAndOrLabelMouseClicked(java.awt.event.MouseEvent evt) {
    	JLabel label = (JLabel) evt.getSource();
    	if(label.getText().equalsIgnoreCase("and")) {
    		label.setText("or");
    	}
    	else if(label.getText().equalsIgnoreCase("or")) {
    		label.setText("and");
    	}
    }

    private void jAndOrLabel1MouseClicked(java.awt.event.MouseEvent evt) {
    	if(jAndOrLabel1.getText().equalsIgnoreCase("and")) {
    		jAndOrLabel1.setText("or");
    	}
    	else if(jAndOrLabel1.getText().equalsIgnoreCase("or")) {
    		jAndOrLabel1.setText("and");
    	}
    }

    private void jMorePanelsButtonActionPerformed(java.awt.event.ActionEvent evt) {
    	if(dataModel.hasEmptyPanels()) {
    		JOptionPane.showMessageDialog(this, "Please use an existing empty panel before adding a new one.");
    		return;
    	}
    	int rightmostPosition = dataModel.lastLabelPosition();
    	JLabel label = new JLabel();
    	label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    	label.setText("and");
    	label.setToolTipText("Click to change the relationship");
    	label.setBorder(javax.swing.BorderFactory.createEtchedBorder());
    	label.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jAndOrLabelMouseClicked(evt);
            }
        });
    	
    	//jPanel1.add(label);
    	//label.setBounds(rightmostPosition, 90, 30, 18);
        
    	QueryConceptTreePanel panel = new QueryConceptTreePanel("Group "+(dataModel.getCurrentPanelCount()+1), this);
    	jPanel1.add(panel);
    	panel.setBounds(rightmostPosition+5, 0, 180, getParent().getHeight()-100);
    	jPanel1.setPreferredSize(new Dimension(rightmostPosition+5+181, getHeight()-100));
    	jScrollPane4.setViewportView(jPanel1);
    	
    	dataModel.addPanel(panel, label, rightmostPosition+5+180);
    	
    	/*System.out.println(jScrollPane4.getViewport().getExtentSize().width+":"+
    			jScrollPane4.getViewport().getExtentSize().height);
    	System.out.println(jScrollPane4.getHorizontalScrollBar().getVisibleRect().width+":"
    			+jScrollPane4.getHorizontalScrollBar().getVisibleRect().height);
    	System.out.println(jScrollPane4.getHorizontalScrollBar().getVisibleAmount());
    	System.out.println(jScrollPane4.getHorizontalScrollBar().getValue());*/
    	jScrollPane4.getHorizontalScrollBar().setValue(jScrollPane4.getHorizontalScrollBar().getMaximum());
    	jScrollPane4.getHorizontalScrollBar().setUnitIncrement(40);
    	//this.jScrollPane4.removeAll();
    	//this.jScrollPane4.setViewportView(jPanel1);
    	//revalidate();
    	//jScrollPane3.setBounds(420, 0, 170, 300);
    	//jScrollPane4.setBounds(20, 35, 335, 220);
    	resizePanels(getParent().getWidth(), getParent().getHeight());
    }
    
    private void resizePanels(int width, int height) {
    	jScrollPane4.setBounds(5, 35, width-70, height-65);
    	//jPanel1.setPreferredSize(new Dimension(dataModel.lastLabelPosition(), height-85));
    	jScrollPane4.setViewportView(jPanel1);
    	
    	jRunQueryButton.setBounds(75, height-25, width-80, 23);
    	jMorePanelsButton.setBounds(width-65, 35, 60, height-65);
    	jNameTextField.setBounds(80, 10, width-195, 20);
    	jClearGroupsButton.setBounds(width-105, 8, 100, 23);
    	jCancelButton.setBounds(5, height-25, 70, 23);
    	jNameLabel.setBounds(11, 8, 70, 23);
    	
    	for(int i=0; i<dataModel.getCurrentPanelCount(); i++) {
			QueryConceptTreePanel panel = dataModel.getTreePanel(i);
			panel.setBounds((i*180)+(i*5), 0, 180, height-90);
			panel.invalidate();
		}
    }
    
    public void addPanel() {
    	int rightmostPosition = dataModel.lastLabelPosition();
    	JLabel label = new JLabel();
    	label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    	label.setText("and");
    	label.setToolTipText("Click to change the relationship");
    	label.setBorder(javax.swing.BorderFactory.createEtchedBorder());
    	label.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jAndOrLabelMouseClicked(evt);
            }
        });
    	
    	//jPanel1.add(label);
    	//label.setBounds(rightmostPosition, 90, 30, 18);
        
    	QueryConceptTreePanel panel = new QueryConceptTreePanel("Group "+(dataModel.getCurrentPanelCount()+1), this);
    	jPanel1.add(panel);
    	panel.setBounds(rightmostPosition+5, 0, 180, getParent().getHeight()-100);
    	jPanel1.setPreferredSize(new Dimension(rightmostPosition+5+181, 150));
    	jScrollPane4.setViewportView(jPanel1);
    	
    	dataModel.addPanel(panel, null, rightmostPosition+5+180);
    
    	jScrollPane4.getHorizontalScrollBar().setValue(jScrollPane4.getHorizontalScrollBar().getMaximum());
    	jScrollPane4.getHorizontalScrollBar().setUnitIncrement(40);
    }
    
    public void setQueryName(String str) {
    	jNameTextField.setText(str);
    }
	
	//Variables declaration 
	private javax.swing.JLabel jAndOrLabel1;
    private javax.swing.JLabel jAndOrLabel2;
    private javax.swing.JLabel jNameLabel;
    private javax.swing.JTextField jNameTextField;
    private javax.swing.JButton jDeleteButton;
    private javax.swing.JButton jMorePanelsButton;
    private javax.swing.JButton jClearGroupsButton;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JButton jCancelButton;
    private javax.swing.JButton jRunQueryButton;
    private QueryConceptTreePanel jScrollPane1;
    private QueryConceptTreePanel jScrollPane2;
    private QueryConceptTreePanel jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    //private javax.swing.JSlider jSlider1;
    //private javax.swing.JComboBox jVisitComboBox;
    
    public javax.swing.JTree jTree1;
    public javax.swing.JTree jTree2;
    public javax.swing.JTree jTree3;
    public QueryConceptTreePanel treepanel;
    // End of variables declaration
}