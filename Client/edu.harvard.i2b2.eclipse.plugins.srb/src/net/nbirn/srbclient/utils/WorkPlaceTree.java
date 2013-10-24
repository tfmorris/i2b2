package net.nbirn.srbclient.utils;

import java.io.File;

import javax.swing.filechooser.FileSystemView;

import net.nbirn.srbclient.data.ClientDirWorker;
import net.nbirn.srbclient.plugin.PluginPlugin;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.events.TreeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;


public class WorkPlaceTree extends Composite implements TreeListener,SelectionListener{

	private Tree tree;
	
	public WorkPlaceTree(Composite parent, int style) {
		super(parent,SWT.CENTER);
		tree = new Tree(this,SWT.BORDER);
		tree.addTreeListener(this);
		tree.addSelectionListener(this);
		FileSystemView fileSystemView = ClientDirWorker.getClientDir().getFileSystemView();
		
		File[] desktop = fileSystemView.getRoots();
		File[] roots = desktop[0].listFiles();
		
		
		
		
		for ( int i = 0 ; i < roots.length ; i++)
		{
			if (roots[i].isDirectory())
			{
				
					File[] list = roots[i].listFiles();
					for ( int y = 0 ; y < list.length ; y++)
					{
						if ( fileSystemView.isDrive(list[y]) )
						{
							TreeItem item = new TreeItem(tree,SWT.NONE);
							item.setText(list[y].toString());
							File[] ThisList = list[y].listFiles();
							if ( ThisList != null )
							{
								for ( int z = 0 ; z < ThisList.length ; z++)
								{
									if ( ThisList[z].isDirectory() )
									{
										addDirItem(item,ThisList[z]);
									}
								}
							}
						}
						
					}
				
			}
		}
		
		
		
		
	}
	private void addDirItem(TreeItem parent,File f)
	{
		TreeItem[] list = parent.getItems();
		if ( list != null )
			for ( int i = 0 ; i < list.length ; i++ )
			{
				if ( list[i].getText().equals(f.getName()))
					return;
			}
		TreeItem item = new TreeItem(parent,SWT.NONE);
		item.setText(f.getName());		
	}
	public void treeCollapsed(TreeEvent e) {
		
	}
	public String getPath(TreeItem item)
	{
		String path = item.getText();
		while ( ( item = item.getParentItem()) != null)
		{
			path = item.getText() + "\\" + path;
		}
		return path;
	}
	public void treeExpanded(TreeEvent e) {
		TreeItem item = (TreeItem)e.item;
		String path = getPath(item);
		TreeItem[] childs = item.getItems();
		if ( childs != null )
		{
			for ( int i = 0 ; i < childs.length ; i++ )
			{
				if ( childs[i].getItems() != null )
				{
					File f = new File(path+"\\"+childs[i].getText());
					addNewItems(childs[i],f);
				}
			}
		}
	}
	/**
	 *  The Tree new Items adds in addition if it sublists gives  
	 * @param parent
	 * @param file
	 */
	private void addNewItems(final TreeItem parent,final File f)
	{
		PluginPlugin.getDefault().getWorkbench().getDisplay().asyncExec(new Runnable(){

			public void run() {
				if ( f.isDirectory())
				{
					File[] list = f.listFiles();
					if ( list != null )
					{
						for ( int y = 0; y < list.length ; y++)
						{
							if ( list[y].isDirectory())
								addDirItem(parent,list[y]);
						}
					}
				}
				
			}});
	}
	public void widgetSelected(SelectionEvent e) {
		ClientDirWorker.getClientDir().setActDirectory(new File(getPath((TreeItem)e.item)));
		
	}
	public void widgetDefaultSelected(SelectionEvent e) {
		ClientDirWorker.getClientDir().setActDirectory(new File(getPath((TreeItem)e.item)));
		
	}

}
