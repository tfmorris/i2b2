package net.nbirn.srbclient.plugin.views;


import net.nbirn.srbclient.data.ClientDirWorker;
import net.nbirn.srbclient.data.DirWorker;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;

import com.enterprisedt.net.ftp.FTPProgressMonitor;

public class ProgressMonitor implements IProgressMonitor,FTPProgressMonitor
{
    private static final Log log = LogFactory.getLog(ProgressMonitor.class);

	private DirWorker dirWorker = null;
	private ProgressBar bar;
	private Label text;
	private Shell parent;
	private boolean isCanceled = false;

	public ProgressMonitor(ProgressBar bar,Label text, Shell parent)//,Shell parent)
	{
		this.bar = bar;
		this.text = text;
		this.parent = parent;
	}
	public void beginTask(final String name, final int totalWork) {
		parent.getDisplay().asyncExec(new Runnable(){
		//PluginPlugin.getDefault().getWorkbench().getDisplay().(new Runnable(){
		//new Thread(new Runnable(){
			public void run() {
				log.debug("start");
				text.setText(name);
				bar.setMinimum(0);
				bar.setMaximum(totalWork);
			}});		
	}

	public void done() {
		//PluginPlugin.getDefault().getWorkbench().getDisplay().asyncExec(new Runnable(){
		parent.getDisplay().asyncExec(new Runnable(){
			public void run() {
				log.debug("Finished");
				text.setText("Finished");
			//	parent.close();
			}});		
	}

	public void internalWorked(double work) {
		
		
	}

	public boolean isCanceled() {
		
		return isCanceled;
	}

	public void setCanceled(boolean value) {
		isCanceled = value;
		//PluginPlugin.getDefault().getWorkbench().getDisplay().asyncExec(new Runnable(){
		parent.getDisplay().asyncExec(new Runnable(){
			public void run() {
				text.setText("Cancel");
				dirWorker.getFtpDirWorker().stop();
				ClientDirWorker.getClientDir().stop();
			}});		
		
	}

	public void setTaskName(final String name) {
		//PluginPlugin.getDefault().getWorkbench().getDisplay().asyncExec(new Runnable(){
		parent.getDisplay().asyncExec(new Runnable(){
			public void run() {
				//parent.setText(name);
			}});	
		
		
	}

	public void subTask(String name) {
		
		
	}

	public void worked(final int work) {
		//PluginPlugin.getDefault().getWorkbench().getDisplay().asyncExec(new Runnable(){
		parent.getDisplay().asyncExec(new Runnable(){
			public void run() {
				log.debug(work);
				bar.setSelection(work);
			}});
		
		
	}
	public void bytesTransferred(long arg0) {
		worked((int) arg0);
	}
	
}
