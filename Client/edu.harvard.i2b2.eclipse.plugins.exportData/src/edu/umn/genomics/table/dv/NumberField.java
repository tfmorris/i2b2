/*
 * @(#) $RCSfile: NumberField.java,v $ $Revision: 1.2 $ $Date: 2008/08/19 20:03:32 $ $Name: RELEASE_1_3_1_0001b $
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


package edu.umn.genomics.table.dv;  //DataViewer

import java.io.Serializable;
import javax.swing.*; 
import javax.swing.text.*; 

import java.awt.Toolkit;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

/**
 * @author       J Johnson
 * @version $Revision: 1.2 $ $Date: 2008/08/19 20:03:32 $  $Name: RELEASE_1_3_1_0001b $
 * @since        1.0
 */
public class NumberField extends JTextField implements Serializable {
  
    private Toolkit toolkit;
    private NumberFormat numberFormatter;

    public NumberField(double value, int columns) {
        super(columns);
        toolkit = Toolkit.getDefaultToolkit();
        numberFormatter = NumberFormat.getNumberInstance(Locale.US);
        //numberFormatter.setParseIntegerOnly(false);
        setValue(value);
    }

    public double getValue() {
        double retVal = 0.;
        try {
            retVal = numberFormatter.parse(getText()).doubleValue();
        } catch (ParseException e) {
            // This should never happen because insertString allows
            // only properly formatted data to get in the field.
            toolkit.beep();
        }
        return retVal;
    }

    public void setValue(double value) {
        setText(numberFormatter.format(value));
    }

    @Override
	protected Document createDefaultModel() {
        return new DecimalDocument();
    }

    protected class DecimalDocument extends PlainDocument {
        static final String dchars = "-.Ee+";

        @Override
		public void insertString(int offs, String str, AttributeSet a) 
            throws BadLocationException {

            char[] source = str.toCharArray();
            char[] result = new char[source.length];
            int j = 0;

            for (int i = 0; i < result.length; i++) {
                if (Character.isDigit(source[i]) || 
                    dchars.indexOf(source[i]) >=0) { 
                    result[j++] = source[i];
                } else {
                    toolkit.beep();
                    //System.err.println("insertString: " + source[i]);
                }
            }
            super.insertString(offs, new String(result, 0, j), a);
        }
    }

}
