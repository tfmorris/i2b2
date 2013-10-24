/*
 * @(#) $RCSfile: SavePDF.java,v $ $Revision: 1.3 $ $Date: 2008/10/30 20:56:40 $ $Name: RELEASE_1_3_1_0001b $
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
import javax.swing.*;
import java.io.*;
import java.io.IOException;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

/**
 * Produce a Portable Document File from a Java Component, using the Lowagie
 * iText PDF package: http://itext.sourceforge.net/
 * http://www.lowagie.com/iText/index.html
 * 
 * @author J Johnson
 * @version $Revision: 1.3 $ $Date: 2008/10/30 20:56:40 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 */
public class SavePDF {
    /**
     * Saves the Component to a Portable Document File, PDF, with the file
     * location selected using the JFileChooser.
     * 
     * @param c
     *            the Component to save as a PDF
     */
    public static boolean savePDF(Component c) throws IOException {
	System.out.println("");
	final int w = c.getWidth() > 0 ? c.getWidth() : 1;
	final int h = c.getHeight() > 0 ? c.getHeight() : 1;
	final Dimension dim = c.getPreferredSize();

	JFileChooser chooser = new JFileChooser();
	JPanel ap = new JPanel();
	ap.setLayout(new BoxLayout(ap, BoxLayout.Y_AXIS));
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

	ap.add(sp);

	chooser.setAccessory(ap);
	// ImageFilter filter = new ImageFilter(fmt);
	// chooser.setFileFilter(filter);
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

	    // step 1: creation of a document-object
	    Document document = new Document();

	    try {
		// step 2:
		// we create a writer that listens to the document
		// and directs a PDF-stream to a file
		PdfWriter writer = PdfWriter.getInstance(document,
			new FileOutputStream(file));

		// step 3: we open the document
		document.open();

		// step 4: we grab the ContentByte and do some stuff with it

		// we create a fontMapper and read all the fonts in the font
		// directory
		DefaultFontMapper mapper = new DefaultFontMapper();

		// mapper.insertDirectory("c:\\winnt\\fonts");

		com.lowagie.text.Rectangle pgSize = document.getPageSize();
		// we create a template and a Graphics2D object that corresponds
		// with it
		PdfContentByte cb = writer.getDirectContent();
		PdfTemplate tp = cb.createTemplate(iw, ih);
		tp.setWidth(iw);
		tp.setHeight(ih);
		Graphics2D g2 = tp.createGraphics(iw, ih, mapper);
		g2.setStroke(new BasicStroke(.1f));
		// cb.setLineWidth(.1f);
		// cb.stroke();

		c.paintAll(g2);

		g2.dispose();

		// cb.addTemplate(tp, 0, 0);
		float sfx = pgSize.width() / iw;
		float sfy = pgSize.height() / ih;
		// preserve the aspect ratio
		float sf = Math.min(sfx, sfy);
		cb.addTemplate(tp, sf, 0f, 0f, sf, 0f, 0f);

	    } catch (DocumentException de) {
		System.err.println(de.getMessage());
		de.printStackTrace();
	    } catch (IOException ioe) {
		System.err.println(ioe.getMessage());
		ioe.printStackTrace();
	    } catch (Exception ex) {
		System.err.println(ex.getMessage());
		ex.printStackTrace();
	    }

	    // step 5: we close the document
	    document.close();

	    if (iw != w || ih != h) {
		c.setSize(w, h);
	    }

	}
	return true;
    }

}
