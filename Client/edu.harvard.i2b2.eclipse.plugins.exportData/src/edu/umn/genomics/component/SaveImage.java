/*
 * @(#) $RCSfile: SaveImage.java,v $ $Revision: 1.3 $ $Date: 2008/10/30 20:56:40 $ $Name: RELEASE_1_3_1_0001b $
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

package edu.umn.genomics.component;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.imageio.*;
import java.io.*;
import javax.swing.*;
import java.util.*;

//import edu.umn.genomics.j3d.CaptureCanvas3D;

/**
 * SaveImage generates an image of a component, which may be encoded and written
 * to a file.
 * 
 * @author J Johnson
 * @version $Revision: 1.3 $ $Date: 2008/10/30 20:56:40 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 */
public class SaveImage {
    // special handling for Java3D Canvas3D.
    static boolean j3Davailable = false;
    static {
	try { // Use java3D if it seeems to be available
	    Class.forName("javax.media.j3d.Canvas3D");
	    j3Davailable = true;
	} catch (ClassNotFoundException e) {
	    j3Davailable = false;
	}
    }

    /**
     * Gets an image of the given component.
     * 
     * @param c
     *            The component for which to generate an image.
     * @return the image of the component.
     */
    public static BufferedImage getImg(Component c) {
	int w = c.getWidth();
	int h = c.getHeight();
	BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
	Graphics2D g = img.createGraphics();
	c.paintAll(g);
	g.dispose();
	return img;
    }

    /**
     * Gets an image of the given component.
     * 
     * @param c
     *            The component for which to generate an image.
     * @return true if the image of the component was successfully written,
     */
    public static boolean saveImage(Component c) throws IOException {
	String imgFmt = "png";
	final int w = c.getWidth() > 0 ? c.getWidth() : 1;
	final int h = c.getHeight() > 0 ? c.getHeight() : 1;
	final Dimension dim = c.getPreferredSize();

	String fmt[] = ImageIO.getWriterFormatNames();
	TreeSet set = new TreeSet();
	for (int i = 0; i < fmt.length; i++) {
	    set.add(fmt[i].toLowerCase());
	}
	fmt = new String[set.size()];
	fmt = (String[]) set.toArray(fmt);
	JFileChooser chooser = new JFileChooser();
	JPanel ap = new JPanel();
	ap.setLayout(new BoxLayout(ap, BoxLayout.Y_AXIS));
	JPanel fp = new JPanel(new GridLayout(0, 1));
	fp.setBorder(BorderFactory.createTitledBorder(BorderFactory
		.createEtchedBorder(), "Image Format"));
	final ButtonGroup bg = new ButtonGroup();
	for (int i = 0; i < fmt.length; i++) {
	    JRadioButton btn = new JRadioButton(fmt[i]);
	    btn.setActionCommand(fmt[i]);
	    btn.setSelected(i == 0);
	    bg.add(btn);
	    fp.add(btn);
	}

	JPanel sp = new JPanel(new GridLayout(0, 1));
	sp.setBorder(BorderFactory.createTitledBorder(BorderFactory
		.createEtchedBorder(), "Image Size"));
	final JTextField iwtf = new JTextField("" + w, 4);
	iwtf.setBorder(BorderFactory.createTitledBorder(BorderFactory
		.createEtchedBorder(), "width"));
	final JTextField ihtf = new JTextField("" + h, 4);
	ihtf.setBorder(BorderFactory.createTitledBorder(BorderFactory
		.createEmptyBorder(), "height"));
	JButton curSzBtn = new JButton("As Viewed: " + w + "x" + h);
	curSzBtn.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		iwtf.setText("" + w);
		ihtf.setText("" + h);
	    }
	});
	sp.add(curSzBtn);
	if (dim != null && dim.getWidth() > 0 && dim.getHeight() > 0) {
	    JButton prefSzBtn = new JButton("As Preferred: " + dim.width + "x"
		    + dim.height);
	    prefSzBtn.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    iwtf.setText("" + dim.width);
		    ihtf.setText("" + dim.height);
		}
	    });
	    sp.add(prefSzBtn);
	}
	sp.add(iwtf);
	sp.add(ihtf);

	ap.add(fp);
	ap.add(sp);

	chooser.setAccessory(ap);
	ImageFilter filter = new ImageFilter(fmt);
	chooser.setFileFilter(filter);
	int returnVal = chooser.showSaveDialog(c);
	boolean status = false;
	if (returnVal == JFileChooser.APPROVE_OPTION) {
	    final File file = chooser.getSelectedFile();
	    int iw = w;
	    int ih = h;
	    try {
		iw = Integer.parseInt(iwtf.getText());
		ih = Integer.parseInt(ihtf.getText());
	    } catch (Exception ex) {
	    }
	    iw = iw > 0 ? iw : w;
	    ih = ih > 0 ? ih : h;
	    if (iw != w || ih != h) {
		c.setSize(iw, ih);
	    }
	    BufferedImage img = null;
	    if (j3Davailable && isCaptureCanvas3D(c)) {
		final Component comp = c;
		Observer observer = new Observer() {
		    final ButtonGroup fbg = bg;
		    final File ffile = file;

		    public void update(Observable o, Object arg) {
			if (arg instanceof BufferedImage) {
			    try {
				ImageIO.write((BufferedImage) arg, fbg
					.getSelection().getActionCommand(),
					ffile);
			    } catch (IOException ioex) {
				JOptionPane.showMessageDialog(comp, ioex
					.toString(), "Save Image",
					JOptionPane.ERROR_MESSAGE);
			    }
			}
		    }
		};
		// cc.captureImage(observer);
		Class[] paramClass = new Class[1];
		paramClass[0] = java.util.Observer.class;
		Object[] args = new Object[1];
		args[0] = observer;
		try {
		    Class.forName("edu.umn.genomics.j3d.CaptureCanvas3D")
			    .getMethod("captureImage", paramClass).invoke(c,
				    args);
		} catch (Exception ex) {
		    JOptionPane.showMessageDialog(comp, ex.toString(),
			    "Save Image", JOptionPane.ERROR_MESSAGE);
		}
	    } else {
		img = new BufferedImage(iw, ih, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = img.createGraphics();
		c.paintAll(g);
		g.dispose();
		if (iw != w || ih != h) {
		    c.setSize(w, h);
		}
		status = ImageIO.write(img, bg.getSelection()
			.getActionCommand(), file);
	    }
	}
	return status;
    }

    public static boolean isCaptureCanvas3D(Component c) {
	boolean val = false;
	try {
	    val = Class.forName("edu.umn.genomics.j3d.CaptureCanvas3D")
		    .isInstance(c);
	} catch (Exception ex) {
	}
	return val;
    }

}

/**
 * A file filter that filters out all files except those ending with the given
 * extensions.
 */
class ImageFilter extends javax.swing.filechooser.FileFilter {
    String[] exts = {};

    ImageFilter(String[] exts) {
	this.exts = exts;
    }

    // Accept all directories and all gif, jpg, or tiff files.
    @Override
    public boolean accept(File f) {
	if (f.isDirectory()) {
	    return true;
	}
	String ext = null;
	String s = f.getName();
	if (s != null) {
	    int i = s.lastIndexOf('.');
	    if (i > 0 && i < s.length() - 1) {
		ext = s.substring(i + 1).toLowerCase();
	    }
	}
	for (int i = 0; i < exts.length; i++) {
	    if (exts[i].equals(ext)) {
		return true;
	    }
	}
	return false;
    }

    // The description of this filter
    @Override
    public String getDescription() {
	return "Just Images";
    }
};
