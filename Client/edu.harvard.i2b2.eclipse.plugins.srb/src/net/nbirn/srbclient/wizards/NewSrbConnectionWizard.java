package net.nbirn.srbclient.wizards;

import net.nbirn.srbclient.plugin.PluginPlugin;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IWorkbench;


public class NewSrbConnectionWizard extends Wizard{

	public NewSrbConnectionWizard()
	{
		setWindowTitle("New SRP Connection...");
	}
	public void init(IWorkbench workbench)
	{
	}
	@Override
	public boolean performFinish() {
		((NewSrbConnectionWizardPageAdress)getPage("ServerAddressAndPort")).saveToWorkSpace(PluginPlugin.getDefault().getPreferenceStore());
		this.dispose();
		return true;
	}

	@Override
	public void addPages() {
		NewSrbConnectionWizardPageAdress srbWizard = new NewSrbConnectionWizardPageAdress("ServerAddressAndPort");
		srbWizard.retrieveFromWorkSpace(PluginPlugin.getDefault().getPreferenceStore());
		
		addPage(srbWizard); 
	}
	@Override
	public boolean canFinish() {
		return super.canFinish();
	}
	
	
}
