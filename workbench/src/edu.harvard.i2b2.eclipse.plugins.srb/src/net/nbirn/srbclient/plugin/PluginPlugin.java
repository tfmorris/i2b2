package net.nbirn.srbclient.plugin;


import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;


/**
 * The main plugin class to be used in the desktop.
 */
public class PluginPlugin extends AbstractUIPlugin {

	//The shared instance.
	private static PluginPlugin plugin;

	
	/**
	 * The constructor.
	 */
	public PluginPlugin() {
		plugin = this;
	}

	/**
	 * This method is called upon plug-in activation
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		
		initFtpUtils();
		
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		plugin = null;
	}

	/**
	 * Returns the shared instance.
	 */
	public static PluginPlugin getDefault() {
		return plugin;
	}
	private void initFtpUtils()
	{
		
	}
	
	
	
	
	

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path.
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return AbstractUIPlugin.imageDescriptorFromPlugin("net.nbirn.srbclient.plugin", path);
	}
}
