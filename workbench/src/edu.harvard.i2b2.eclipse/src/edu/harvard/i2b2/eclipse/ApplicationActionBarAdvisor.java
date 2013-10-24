/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors:
 *     Lori Phillips - initial API and implementation
 *     Mike Mendis
 */

package edu.harvard.i2b2.eclipse;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ContributionItemFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;

/**
 * An action bar advisor is responsible for creating, adding, and disposing of
 * the actions added to a workbench window. Each window will be populated with
 * new actions.
 */
public class ApplicationActionBarAdvisor extends ActionBarAdvisor {

	// Actions - important to allocate these only in makeActions, and then use
	// them
	// in the fill methods. This ensures that the actions aren't recreated
	// when fillActionBars is called with FILL_PROXY.
	private IWorkbenchAction exitAction;
	private IWorkbenchAction helpAction;
	private IWorkbenchAction aboutAction;
	private IWorkbenchAction propertiesAction;
	private IContributionItem views;
	private IContributionItem perspectives;
	
	
	public ApplicationActionBarAdvisor(IActionBarConfigurer configurer) {
		super(configurer);
	}

	@Override
	protected void makeActions(final IWorkbenchWindow window) {
		// Creates the actions and registers them.
		// Registering is needed to ensure that key bindings work.
		// The corresponding commands keybindings are defined in the plugin.xml
		// file.
		// Registering also provides automatic disposal of the actions when
		// the window is closed.

		exitAction = ActionFactory.QUIT.create(window);
		register(exitAction);
		
		helpAction = ActionFactory.HELP_CONTENTS.create(window);
		register(helpAction);
		
		propertiesAction = ActionFactory.PROPERTIES.create(window);
		register(propertiesAction);
		
		aboutAction = ActionFactory.ABOUT.create(window);
		register(aboutAction);
		
		views = ContributionItemFactory.VIEWS_SHORTLIST.create(window);
		
		perspectives = ContributionItemFactory.PERSPECTIVES_SHORTLIST.create(window);
		
	}

	@Override
	protected void fillMenuBar(IMenuManager menuBar) {
		// Creates the menu bar and binds actions 

		MenuManager fileMenu = new MenuManager("&File",
				IWorkbenchActionConstants.M_FILE);
		menuBar.add(fileMenu);
		fileMenu.add(exitAction);
		
		MenuManager perspectivesMenu = new MenuManager("Open Perspectives", "perspectives");
		perspectivesMenu.add(perspectives);

		MenuManager viewsMenu = new MenuManager("Show View", "views");
		viewsMenu.add(views);
		
		MenuManager windowMenu = new MenuManager("&Window","window");	
		windowMenu.add(perspectivesMenu);
		windowMenu.add(viewsMenu);
		windowMenu.add(propertiesAction);
		menuBar.add(windowMenu);

		MenuManager helpMenu = new MenuManager("&Help", "help");
		helpMenu.add(helpAction);	
		helpMenu.add(aboutAction);	
		menuBar.add(helpMenu);
	}

}
