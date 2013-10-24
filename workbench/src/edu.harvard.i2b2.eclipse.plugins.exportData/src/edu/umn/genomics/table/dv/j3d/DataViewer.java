/*
 * @(#) $RCSfile: DataViewer.java,v $ $Revision: 1.4 $ $Date: 2008/11/05 19:32:04 $ $Name: RELEASE_1_3_1_0001b $
 *
 * Center for Computational Genomics and Bioinformatics
 * Academic Health Center, University of Minnesota
 * Copyright (c) 2000-2002. The Regents of the University of Minnesota  
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * see: http://www.gnu.org/copyleft/gpl.html
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 */

package edu.umn.genomics.table.dv.j3d; //DataViewer

import java.io.Serializable;
import java.awt.*;
import java.io.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Vector;
import edu.umn.genomics.table.FileTableModel;
import edu.umn.genomics.table.dv.*;
import edu.umn.genomics.component.SaveImage;

/**
 * Application Class for data viewing.
 * 
 * @author J Johnson
 * @version $Revision: 1.4 $ $Date: 2008/11/05 19:32:04 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 */
public class DataViewer extends JPanel implements Serializable {
    /** This variable is static so that the previous directory is remembered */
    static FileDialog FileBox = null;
    /** The viewing panel */
    DataView dv;
    /** The list of all DataMaps. */
    DefaultListModel dataMapList = new DefaultListModel();
    /** JList */
    JList dataMapJList = new JList(dataMapList);
    /** The controls panel */
    Controls ctrls;
    DSCtrls dsCtrls;

    DataViewer() {
	if (!true) { // Temporary: force use of the 2D panel
	    dv = new DataView2D();
	    System.err.println("using DataView2D");
	} else {
	    try { // Use java3D if it seeems to be available
		Class.forName("javax.media.j3d.Canvas3D");
		dv = new DataViewJ3D();
		System.err.println("using DataViewJ3D");
		JPopupMenu.setDefaultLightWeightPopupEnabled(false);
	    } catch (ClassNotFoundException e) {
		dv = new DataView2D();
		System.err.println("using DataView2D");
	    }
	}

	if (true) {
	    dataMapJList
		    .setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	    dataMapJList.setCellRenderer(new DefaultListCellRenderer() {

		@Override
		public Component getListCellRendererComponent(JList list,
			Object ds, // value to display
			int index, // cell index
			boolean isSelected, // is the cell selected
			boolean cellHasFocus) // the list and the cell have the
		// focus
		{
		    setText(((DataMap) ds).getName());
		    setBackground(isSelected ? Color.gray : Color.white);
		    setForeground(isSelected ? Color.white : Color.black);
		    return this;
		}
	    });
	}
	ctrls = new Controls();
	/*
	 * GridBagLayout gbl = new GridBagLayout(); setLayout(gbl);
	 * GridBagConstraints c = new GridBagConstraints(); c.gridwidth =
	 * GridBagConstraints.REMAINDER; c.weightx = 1.0; c.fill =
	 * GridBagConstraints.HORIZONTAL; gbl.setConstraints(ctrls, c);
	 * add(ctrls); c.gridheight = GridBagConstraints.REMAINDER; c.weighty =
	 * 1.0; c.fill = GridBagConstraints.BOTH; gbl.setConstraints(dv, c);
	 * add(dv);
	 */
	setLayout(new BorderLayout());
	add(ctrls, BorderLayout.NORTH);
	add(dv, BorderLayout.CENTER);
    }

    /**
     * Recursively search parents to find the Frame component.
     * 
     * @return the frame in which this panel resides.
     */
    private Frame getFrame() {
	Component c = this;
	while (!(c instanceof Frame))
	    c = c.getParent();
	return (Frame) c;
    }

    /**
     * Import a data set into this view.
     * 
     * @param source
     *            The data source location (file pathname or URL).
     */
    public void importData(String source) throws IOException {
	FileTableModel tm = new FileTableModel(source);
	DataMap ds = dv.addDataMap(source, tm);
	dataMapList.addElement(ds);
	// ctrls.addDataMap(ds);
	// ds.openTable();
    }

    /** Load a file selected by the user */
    private void importDataDialog() {
	// get file name from file dialog
	Frame parent = this.getFrame();
	if (FileBox == null)
	    FileBox = new FileDialog(parent);
	FileBox.setMode(FileDialog.LOAD);
	FileBox.setVisible(true);

	// make sure file exists
	String file = FileBox.getFile();
	if (file == null)
	    return;
	String directory = FileBox.getDirectory();
	if (directory == null)
	    return;
	File f = new File(directory, file);
	if (!f.exists()) {
	    JOptionPane.showMessageDialog(parent, file + " does not exist",
		    "Cannot load file", JOptionPane.ERROR_MESSAGE);
	    return;
	}

	// load file
	// String filename = "file:/" + f.getAbsolutePath();
	String filename = f.getAbsolutePath();
	try {
	    importData(filename);
	} catch (IOException ioex) {
	    JOptionPane.showMessageDialog(parent, file + " " + ioex,
		    "Cannot load file", JOptionPane.ERROR_MESSAGE);
	    return;
	}
    }

    /**
     * Control panel.
     */
    class Controls extends JPanel {
	JMenu fileMenu;
	JMenu dataMapMenu;

	public void addDataMap(DataMap ds) {
	    JMenuItem mi = dataMapMenu.add(new JCheckBoxMenuItem(ds.getName()));

	    System.err.println("adding to menu: " + ds.getName());
	}

	Controls() {
	    JMenuItem mi;
	    JToolBar tb = new JToolBar();
	    JMenuBar mb = new JMenuBar();

	    fileMenu = new JMenu("File");
	    mi = fileMenu.add(new JMenuItem("import data..."));
	    mi.setMnemonic('i');
	    mi.getAccessibleContext().setAccessibleDescription("importData");
	    mi.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    importDataDialog();
		}
	    });

	    mi = fileMenu.add(new JMenuItem("Exit"));
	    mi.setMnemonic('x');
	    mi.getAccessibleContext().setAccessibleDescription("Exit");
	    mi.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    System.exit(0);
		}
	    });

	    mb.add(fileMenu);

	    dataMapMenu = new JMenu("DataMaps");
	    /*
	     * mb.add(dataMapMenu);
	     */

	    // javasoft recommends adding a ToolBar as the single object
	    // in a component using BorderLayout.
	    mb.setMinimumSize(new Dimension(400, 20));

	    JButton dataCtrl = new JButton("Data Maps");
	    dataCtrl.setMnemonic('D');
	    dataCtrl.getAccessibleContext().setAccessibleDescription(
		    "Data Maps");
	    dataCtrl.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    if (dsCtrls == null) {
			dsCtrls = new DSCtrls("DataMaps");
		    }
		    dsCtrls.setVisible(true);
		}
	    });
	    mb.add(dataCtrl);

	    JButton snap = new JButton("Snap Image");
	    snap.setMnemonic('I');
	    snap.getAccessibleContext().setAccessibleDescription("snap image");
	    snap.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    try {
			SaveImage.saveImage(dv.getCanvas());
		    } catch (Exception ex) {
			JOptionPane.showMessageDialog(
				JOptionPane.getFrameForComponent((Component) e
					.getSource()), ex, "Cannot save image",
				JOptionPane.ERROR_MESSAGE);
		    }
		}
	    });
	    mb.add(snap);
	    tb.add(mb);
	    tb.setSize(400, 40);
	    setBorder(BorderFactory.createEtchedBorder());
	    setLayout(new BorderLayout());
	    add(tb);
	}
    }

    class DSCtrls extends JFrame {
	DSCtrls(String title) {
	    super(title);
	    JMenuBar ap = new JMenuBar();
	    JPanel lp = new JPanel();
	    JPanel panel = new JPanel(new BorderLayout());

	    JScrollPane scrollPane = new JScrollPane(dataMapJList);
	    scrollPane.setAlignmentX(LEFT_ALIGNMENT);
	    scrollPane.setAlignmentY(TOP_ALIGNMENT);

	    panel.add("North", ap);
	    panel.add("Center", lp);
	    lp.add(scrollPane);

	    JButton cb = new JButton("Close");
	    cb.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    setVisible(false);
		}
	    });
	    ap.add(cb);

	    JButton db = new JButton("Destroy");
	    db.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    Object obj[] = dataMapJList.getSelectedValues();
		    for (int i = 0; i < obj.length; i++) {
			if (obj[i] instanceof DataMap) {
			    DataMap ds = (DataMap) obj[i];
			    ds.cleanUp();
			    dv.deleteDataMap(ds);
			}
			dataMapList.removeElement(obj[i]);
		    }
		}
	    });
	    ap.add(db);

	    JButton sb = new JButton("Show");
	    sb.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    Object obj[] = dataMapJList.getSelectedValues();
		    for (int i = 0; i < obj.length; i++) {
			if (obj[i] instanceof DataMap) {
			    DataMap ds = (DataMap) obj[i];
			    dv.showDataMap(ds, true);
			}
		    }
		}
	    });
	    ap.add(sb);

	    JButton hb = new JButton("Hide");
	    hb.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    Object obj[] = dataMapJList.getSelectedValues();
		    for (int i = 0; i < obj.length; i++) {
			if (obj[i] instanceof DataMap) {
			    DataMap ds = (DataMap) obj[i];
			    dv.showDataMap(ds, false);
			}
		    }
		}
	    });
	    ap.add(hb);

	    if (true) {
		JMenu repMenu = new JMenu("Representation");
		JMenuItem mi;
		ButtonGroup repGroup = new ButtonGroup();
		ActionListener repListener = new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
			String rep = e.getActionCommand();
			Object obj[] = dataMapJList.getSelectedValues();
			for (int i = 0; i < obj.length; i++) {
			    if (obj[i] instanceof DataMap) {
				DataMap ds = (DataMap) obj[i];
				ds.setDataRepresentation(rep);
			    }
			}
		    }
		};

		Vector rv = GlyphJ3D.getGlyphTypes(); // temp
		rv.add("points"); // temp
		String dataReps[] = new String[rv.size()]; // temp
		rv.copyInto(dataReps); // temp
		for (int i = 0; i < dataReps.length; i++) {
		    mi = repMenu.add(new JRadioButtonMenuItem(dataReps[i],
			    i == dataReps.length - 1));
		    mi.addActionListener(repListener);
		    repGroup.add(mi);
		}
		ap.add(repMenu);
	    }
	    if (true) {
		final JTextField scaleField = new JTextField("1.", 6);
		scaleField.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
			String s = e.getActionCommand();
			try {
			    double factor = Double.parseDouble(s);
			    Object obj[] = dataMapJList.getSelectedValues();
			    for (int i = 0; i < obj.length; i++) {
				if (obj[i] instanceof DataMap) {
				    DataMap ds = (DataMap) obj[i];
				    ds.scale(factor);
				}
			    }
			} catch (Exception ex) {
			}
		    }
		});

		JButton sbb = new JButton("Scale By:");
		sbb.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
			String s = scaleField.getText();
			try {
			    double factor = Double.parseDouble(s);
			    Object obj[] = dataMapJList.getSelectedValues();
			    for (int i = 0; i < obj.length; i++) {
				if (obj[i] instanceof DataMap) {
				    DataMap ds = (DataMap) obj[i];
				    ds.scale(factor);
				}
			    }
			} catch (Exception ex) {
			}
		    }
		});
		ap.add(sbb);
		ap.add(scaleField);
	    }

	    JButton tb = new JButton("Table");
	    tb.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    Object obj[] = dataMapJList.getSelectedValues();
		    for (int i = 0; i < obj.length; i++) {
			if (obj[i] instanceof DataMap) {
			    DataMap ds = (DataMap) obj[i];
			    ds.openTable();
			}
		    }
		}
	    });
	    ap.add(tb);

	    getContentPane().add(panel);
	    // dsCtrls.addWindowListener(new WindowAdapter() {
	    // public void windowClosing(WindowEvent e) {setVisible(false);}
	    // });
	    pack();
	    setVisible(false);
	}
    }

    public static void main(String args[]) {
	String usage = "usage: \n\tjava DataViewer [datafile]";
	JFrame frame = new JFrame("DataViewer");
	frame.addWindowListener(new WindowAdapter() {
	    @Override
	    public void windowClosing(WindowEvent e) {
		System.exit(0);
	    }
	});
	DataViewer dv = new DataViewer();
	frame.getContentPane().add(dv);
	frame.pack();
	Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	Dimension d = frame.getSize();
	d.width = (int) (.8 * screenSize.width);
	d.height = (int) (.8 * screenSize.height);
	frame.setSize(d);
	frame.setLocation(100, 100);
	frame.show();
	for (int i = 0; i < args.length; i++) {
	    try {
		dv.importData(args[i]);
	    } catch (IOException ioex) {
		System.err.println(args[i] + " :  " + ioex);
	    }
	}
    }
}
