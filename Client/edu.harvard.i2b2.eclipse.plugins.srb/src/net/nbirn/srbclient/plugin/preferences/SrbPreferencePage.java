package net.nbirn.srbclient.plugin.preferences;

import net.nbirn.srbclient.plugin.PluginPlugin;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;


/**
 * This class represents a preference page that
 * is contributed to the Preferences dialog. By 
 * subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows
 * us to create a page that is small and knows how to 
 * save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They
 * are stored in the preference store that belongs to
 * the main plug-in class. That way, preferences can
 * be accessed directly via the preference store.
 */

public class SrbPreferencePage
	extends FieldEditorPreferencePage
	implements IWorkbenchPreferencePage {

	public SrbPreferencePage() {
		super(GRID);
		setPreferenceStore(PluginPlugin.getDefault().getPreferenceStore());
		setDescription("Attitudes to the SRP Client Plugin");
	}
	
	/**
	 * Creates the field editors. Field editors are abstractions of
	 * the common GUI blocks needed to manipulate various types
	 * of preferences. Each field editor knows how to save and
	 * restore itself.
	 */
	@Override
	public void createFieldEditors() {
		addField(new StringFieldEditor(PreferenceConstants.SRB_SERVER_ADDRESS,"Srb Address",getFieldEditorParent()));
		addField(new StringFieldEditor(PreferenceConstants.SRB_SERVER_ACCOUNT_NAME,"Srb Account",getFieldEditorParent()));
		addField(new StringFieldEditor(PreferenceConstants.SRB_SERVER_PASSWORD,"Account Password",getFieldEditorParent()));
		addField(new StringFieldEditor(PreferenceConstants.SRB_SERVER_PORT,"Srb Port",getFieldEditorParent()));
		addField(new StringFieldEditor(PreferenceConstants.SRB_SERVER_DEFAULTSTORAGERESOURCE,"Srb Default Resource",getFieldEditorParent()));
		addField(new StringFieldEditor(PreferenceConstants.SRB_SERVER_HOMEDIRECTORY,"Srb Home Directory",getFieldEditorParent()));
		addField(new StringFieldEditor(PreferenceConstants.SRB_SERVER_MDASDOMAINHOME,"Srb MDAS Domain Home",getFieldEditorParent()));
	}
	public void init(IWorkbench workbench) {
	}
	
}