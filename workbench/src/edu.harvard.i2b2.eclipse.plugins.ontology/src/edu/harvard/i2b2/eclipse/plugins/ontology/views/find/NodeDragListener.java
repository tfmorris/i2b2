/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors:
 * 		Lori Phillips
 */
package edu.harvard.i2b2.eclipse.plugins.ontology.views.find;

import java.io.StringWriter;
import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.dnd.*;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.eclipse.plugins.ontology.util.OntologyJAXBUtil;
import edu.harvard.i2b2.eclipse.plugins.ontology.views.find.NodeDragListener;
import edu.harvard.i2b2.eclipse.plugins.ontology.views.find.TreeNode;
import edu.harvard.i2b2.ontclient.datavo.dnd.DndType;
import edu.harvard.i2b2.ontclient.datavo.vdo.ConceptType;
import edu.harvard.i2b2.ontclient.datavo.vdo.ConceptsType;

final class NodeDragListener implements DragSourceListener
{
	private Log log = LogFactory.getLog(NodeDragListener.class.getName());
	private TreeViewer viewer;
	private IStructuredSelection selectionOnDrag = null;

	NodeDragListener(TreeViewer viewer)
	{
		this.viewer = viewer;
	}
	
	public void dragStart(DragSourceEvent event) 
	{
		selectionOnDrag = (IStructuredSelection)this.viewer.getSelection();
		event.doit = true;		
	}

	// this is the jaxb based dnd XML
	public void dragSetData(DragSourceEvent event) 
	{
		StringWriter strWriter = null;
		ConceptsType concepts = new ConceptsType();
		Iterator iterator = selectionOnDrag.iterator();
		while(iterator.hasNext())
		{
			TreeNode node = (TreeNode) iterator.next();
			ConceptType data = node.getData();
			concepts.getConcept().add(data);
		}	
			
		try {
			strWriter = new StringWriter();
			DndType dnd = new DndType();
			edu.harvard.i2b2.ontclient.datavo.vdo.ObjectFactory vdoOf = new edu.harvard.i2b2.ontclient.datavo.vdo.ObjectFactory();
			dnd.getAny().add( vdoOf.createConcepts(concepts));

			edu.harvard.i2b2.ontclient.datavo.dnd.ObjectFactory of = new edu.harvard.i2b2.ontclient.datavo.dnd.ObjectFactory();
			OntologyJAXBUtil.getJAXBUtil().marshaller(of.createPluginDragDrop(dnd), strWriter);
			
			
		} catch (JAXBUtilException e) {
			log.error("Error marshalling Ont drag text");
		} 


		event.data = strWriter.toString();
	}
	
	public void dragFinished(DragSourceEvent event) {
		selectionOnDrag = null;
	}
}