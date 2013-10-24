/*
 * @(#) $RCSfile: DataMap3D.java,v $ $Revision: 1.2 $ $Date: 2008/08/19 20:03:32 $ $Name: RELEASE_1_3_1_0001b $
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
public class DataMap3D extends DataMap implements Serializable {
  float dpts[] = null;
  int dptIncr = 3;
  DataMap3D(String name, TableModel tableModel) {
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
    if (e == null) {
      return;
    } 
    if (e.getFirstRow() == TableModelEvent.HEADER_ROW) {
      dpts = null;
      if ( ((TableModel)e.getSource()).getRowCount() > 0) {
        autoMap();
      }
    } else {
      if (dpts != null) {
        int first = e.getFirstRow();
        int last = e.getLastRow();
        int nrow = last - first + 1;
        float tmp[];
        switch (e.getType()) {
        case TableModelEvent.UPDATE:
          getPointTranslation(first, nrow, dpts, first*dptIncr, dptIncr); 
          break;
        case TableModelEvent.INSERT:
          tmp = dpts;
          dpts = new float[tmp.length + nrow * dptIncr];
          if (first > 0) {
            System.arraycopy(tmp, 0, dpts, 0, first*dptIncr) ;
          } 
          if (last < tmp.length/dptIncr-1) {
            int ti = (last+1)*dptIncr;
            int n = tmp.length - first*dptIncr;
            System.arraycopy(tmp, first*dptIncr, dpts, ti, n);
          } 
          getPointTranslation(first, nrow, dpts, first*dptIncr, dptIncr); 
          break;
        case TableModelEvent.DELETE:
          tmp = dpts;
          dpts = new float[tmp.length - nrow * dptIncr];
          if (first > 0) {
            System.arraycopy(tmp, 0, dpts, 0, first*dptIncr) ;
          } 
          if (last < tmp.length/dptIncr-1) {
            int ti = (last+1)*dptIncr;
            int n = tmp.length - (last+1)*dptIncr;
            System.arraycopy(tmp, ti, dpts, first*dptIncr, n) ;
          } 
          break;
        }
      }
    }
    super.tableChanged(e);  // notify Observers
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
