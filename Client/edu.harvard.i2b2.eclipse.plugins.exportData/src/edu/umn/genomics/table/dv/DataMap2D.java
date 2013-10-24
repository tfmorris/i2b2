/*
 * @(#) $RCSfile: DataMap2D.java,v $ $Revision: 1.2 $ $Date: 2008/08/19 20:03:32 $ $Name: RELEASE_1_3_1_0001b $
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
import javax.swing.event.*;
import javax.swing.table.*;

/**
 * @author       J Johnson
 * @version $Revision: 1.2 $ $Date: 2008/08/19 20:03:32 $  $Name: RELEASE_1_3_1_0001b $
 * @since        1.0
 */
public class DataMap2D extends DataMap implements Serializable {
  float dpts[] = null;
  int dptIncr = 3;
  DataMap2D(String name, TableModel tableModel) {
    super(name, tableModel);
  }
  public float[] getPoints() {
    if (dpts == null) {
      int nrow = tableModel.getRowCount();
      dpts = new float[nrow * dptIncr];
      getPointTranslation(0, nrow, dpts, 0, dptIncr); 
    }
    return dpts;
  }
  /**
   * Called when the column to axis mapping has been changed.
   */
  @Override
protected void reMapAxes() {
    dpts = null;
  }

  @Override
public void tableChanged(TableModelEvent e) {
  }
  public void setPositionSource(String column[]) {
  }
  public void setPositionSource(int column[]) {
  }
  public void setColorSource(int column) {
  }
  public void setTransparencySource(int column) {
  }
  public void setScaleSource(int column) {
  }
  public void setScaleSource(int column[]) {
  }
  public void setColorMap() {
  }
  public void points() {
  }
  public void glyph() {
  }
}
