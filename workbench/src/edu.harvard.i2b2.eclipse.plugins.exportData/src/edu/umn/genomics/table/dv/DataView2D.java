/*
 * @(#) $RCSfile: DataView2D.java,v $ $Revision: 1.2 $ $Date: 2008/08/19 20:03:32 $ $Name: RELEASE_1_3_1_0001b $
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
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.util.Hashtable;
import java.util.Observable;
import edu.umn.genomics.table.SetOperator;

/**
 * @author       J Johnson
 * @version $Revision: 1.2 $ $Date: 2008/08/19 20:03:32 $  $Name: RELEASE_1_3_1_0001b $
 * @since        1.0
 */
public class DataView2D extends DataView implements Serializable {
  boolean dbg = false;
  float tpnts[] = new float[0];
  int pntIdx[] = new int[0];
  float xRot = .05f;
  float yRot = .05f;
  float xTrans = .001f;
  float yTrans = .001f;
  float zTrans = .01f;
  DataMap2D lastdm = null;
  Matrix4D mvm = null; // model view matrix
  Matrix4D pjm = null; // projection matrix
                       // viewport
  Hashtable dm2pnts = new Hashtable();
  int incr = 4;
  float near = 4f;
  float far  = 8000f;
  Point startPoint = null;
  Point lastPoint = null;
  int setOp = SetOperator.REPLACE;
  SetOperator setOperator = null;
  int prevSetOp = -1;
  private static int modifierMask =
    InputEvent.BUTTON1_MASK | InputEvent.BUTTON2_MASK |
    InputEvent.BUTTON3_MASK |
    InputEvent.SHIFT_MASK | InputEvent.CTRL_MASK |
    InputEvent.ALT_MASK | InputEvent.META_MASK;


  private MouseAdapter ma = new MouseAdapter() {
    @Override
	public void mousePressed(MouseEvent e) {
      startPoint = e.getPoint();
      lastPoint = e.getPoint();
      if (viewAction == SELECT_RECT) {
        if (setOperator != null) {
          prevSetOp = setOperator.getSetOperator();
          setOperator.setFromInputEventMask(e.getModifiers());
        }
      }
    }
    @Override
	public void mouseReleased(MouseEvent e) {
      if (viewAction == SELECT_RECT) {
        selectRect(startPoint.x, startPoint.y, lastPoint.x, lastPoint.y);
      }
      startPoint = null;
      lastPoint = null;
      if (viewAction == SELECT_RECT) {
        if (setOperator != null) {
          setOperator.setSetOperator(prevSetOp);
        }
      }
      repaint();
    }
  };
  private MouseMotionAdapter mma = new MouseMotionAdapter() {
    @Override
	public void mouseDragged(MouseEvent e) {
      Point p = e.getPoint();
      if (viewAction == NAVIGATE) {
        // Rotate
        int evtMask = e.getModifiers() & modifierMask; 
        if ( (evtMask == InputEvent.BUTTON1_MASK) || 
              evtMask == 0 ) {  // Mac
          float dx = ((p.x - lastPoint.x) * xRot);
          float dy = ((p.y - lastPoint.y) * -yRot);
          float t[] = mvm.getTranslation(null);
          for (int i = 0;i < t.length; i++) {
            t[i] = -t[i];
          }
          mvm.translate(t);
          Matrix4D xm = new Matrix4D();
          Matrix4D ym = new Matrix4D();
          xm.xrot(dy);
          ym.yrot(dx);
          mvm.mult(xm); 
          mvm.mult(ym); 
          for (int i = 0;i < t.length; i++) {
            t[i] = -t[i];
          }
          mvm.translate(t);
          transformPoints(lastdm);
        // zoom: Btn2 or ShiftBtn1
        } else if ( (evtMask == InputEvent.BUTTON2_MASK) || 
             (evtMask == (InputEvent.SHIFT_MASK|InputEvent.BUTTON1_MASK)) ||
             (evtMask == InputEvent.SHIFT_MASK) ) {  // Mac
          if (p.y != lastPoint.y) {
            int h = getHeight(); 
            mvm.translate(0f,0f,((lastPoint.y-p.y)*-zTrans));
            transformPoints(lastdm);
          }
        // translate Btn3 or CtrlBtn1
        } else if ( (evtMask == InputEvent.BUTTON3_MASK) || 
             (evtMask == (InputEvent.CTRL_MASK|InputEvent.BUTTON1_MASK)) ||
             (evtMask == InputEvent.CTRL_MASK) ) {  // Mac
          mvm.translate( ((lastPoint.x-p.x)*-xTrans), 
                         ((lastPoint.y-p.y)*yTrans), 
                          0f);
          transformPoints(lastdm);
        }
      } else if (viewAction == SELECT_RECT) {
        repaint();
      }
      lastPoint = p;
    }
    @Override
	public void mouseMoved(MouseEvent e) {
    }
  };

  class Canvas2D extends JComponent {
    Canvas2D() {
      super();
    }
    @Override
	public void paintComponent(Graphics g) {
      Insets insets = getInsets();
      int vw = (getWidth() - insets.left - insets.right);
      int vh = (getHeight() - insets.top - insets.bottom);
      int vm = vw < vh ? vw : vh;
      if (isOpaque()) {
        g.setColor(getBackground());
        g.fillRect(0,0,getWidth(),getHeight());
        g.setColor(getForeground());
      }
      // draw axes
//System.err.println("Canvas2D.paintComponent " + tpnts.length);
      if (tpnts == null) {
        return;
      }
      // draw points
      for (int j = 0; j < pntIdx.length; j++) {
        int i = pntIdx[j];
        Color c = lastdm.getPointColor(i);
//System.err.print(j + " " + i + " [" ); 
        i *= 4;
//        for (int k = 0; k < 4; k++) {
//System.err.print(tpnts[i+k] + " "); 
//        }
//System.err.println("]"); 
        if (tpnts[i+3] > 0) {
          g.setColor(c);
          int x = (int)tpnts[i];
          int y = (int)tpnts[i+1];
          int f = (int)(tpnts[i+2]*-10f);
          //int f = (int)(vm/10 - (tpnts[i+3]-near));
          //float sf = (near - tpnts[i+2]);
          //int f = (int)(sf * sf);
          int r = f > 1 ? f : 1;
          g.fillOval(x-r, y-r, r*2, r*2);
          g.setColor(c.darker());
          g.drawOval(x-r, y-r, r*2, r*2);
          //g.drawLine(x, y, x, y+1);
        }
      }
//System.err.println(""); 
      if (viewAction == SELECT_RECT && 
          startPoint != null && lastPoint != null) {
        g.setColor(Color.black);
        g.drawLine(startPoint.x,startPoint.y,lastPoint.x,startPoint.y);
        g.drawLine(startPoint.x,startPoint.y,startPoint.x,lastPoint.y);
        g.drawLine(startPoint.x,lastPoint.y,lastPoint.x,lastPoint.y);
        g.drawLine(lastPoint.x,startPoint.y,lastPoint.x,lastPoint.y);
      }
    }
  }
  Canvas2D canvas = null;

  public DataView2D() {
    super();
System.err.println("DataView2D()");
    mvm = new Matrix4D();
    mvm.setIdentity();
    mvm.translate(0,0,-8);
    pjm = new Matrix4D();
    pjm.setFrustum(-1f,1f,-1f,1f,near,far);
    addComponentListener( new ComponentAdapter() {
      @Override
	public void componentResized(ComponentEvent e) {
        transformPoints(lastdm);
      }
    });
    canvas = new Canvas2D();
    canvas.addMouseListener(ma);
    canvas.addMouseMotionListener(mma);
    canvas.setOpaque(true); 
    canvas.setBackground(Color.white);
    add(canvas,BorderLayout.CENTER);
  }

  @Override
public DataMap addDataMap(String name, TableModel tableModel) {
    DataMap2D dm = new DataMap2D(name, tableModel);
    // add entry to hashtable
    DataMap2D prev = (DataMap2D) dataMapName.put(name, dm);
    // check if it already exists
    if (prev != null) {
      System.err.println("remove old dataMap " + name );
    } else {
      System.err.println("add new  dataMap " + name );
    }
    // add to scenegraph
    dm.addObserver(this);
    lastdm = dm;
    transformPoints(dm);
    dataMapList.addElement(dm);
    int index = dataMapList.indexOf(dm);
    dataMapJList.addSelectionInterval(index,index);
    return dm;
  }

  /**
   * Set the SetOperator model.
   * @param setOperator the SetOperator model.
   */
  @Override
public void setSetOperatorModel(SetOperator setOperator) {
    this.setOperator = setOperator;
  }
  /**
   * Return the SetOperator model.
   * @return the SetOperator model.
   */
  @Override
public SetOperator getSetOperatorModel() {
    return setOperator;
  }

  @Override
public void setViewpoint() {
    if (lastdm == null)
      return;
    float xmin, xmax, ymin, ymax, zmin, zmax; 
    float dpnts[] = lastdm.getPoints();
    if (dpnts == null || dpnts.length < 3) {
      return;
    }
    xmin = xmax = dpnts[0];
    ymin = ymax = dpnts[1];
    zmin = zmax = dpnts[2];
    int nv = dpnts.length/3;
    for (int n = 1,i = 3; n < nv; n++) {
      if (dpnts[i] < xmin)
        xmin = dpnts[i];
      else if (dpnts[i] > xmax)
        xmax = dpnts[i];
      i++;
      if (dpnts[i] < ymin)
        ymin = dpnts[i];
      else if (dpnts[i] > ymax)
        ymax = dpnts[i];
      i++;
      if (dpnts[i] < zmin)
        zmin = dpnts[i];
      else if (dpnts[i] > zmax)
        zmax = dpnts[i];
      i++;
    }
    float cx = xmin + (xmax - xmin)/2f;
    float cy = ymin + (ymax - ymin)/2f;
    float cz = zmin + (zmax - zmin)/2f;

    System.err.println("x: " + xmin + " - " + xmax);
    System.err.println("y: " + ymin + " - " + ymax);
    System.err.println("z: " + zmin + " - " + zmax);
    
    double dx = cx - xmin;
    double dy = cy - ymin;
    double dz = cz - zmin;

    double r = Math.sqrt(dx*dx + dy*dy + dz*dz);
    double fov = Math.PI / 4.;
    cz -= (float)(r / Math.tan(fov / 2.0));
    
    System.err.println("eye: " + cx + " " + cy + " " + cz + " " + r);
    mvm.setTranslation(-cx,-cy,cz);
    transformPoints(lastdm);
  }

  /*
    transform points for 2d  screen
    // modelview transform
    // projection transform
    // perspective division
    // viewport tranform
    // sort back to front
  */
  private void transformPoints(DataMap2D dm) {
      if (dm == null) 
        return;
      float dpnts[] = dm.getPoints();
      int nv = dpnts.length/3;
if (dbg) {
System.err.println("data:");
for (int n = 0; n < nv;n++) {
  int i = n * 3;
  System.err.println("" + dpnts[i] + "\t" + dpnts[i+1] + "\t" + dpnts[i+2]);
}
}
      // modelview transform
      tpnts = new float[nv*4];
      mvm.transform(dpnts,3,tpnts,4,0,nv);
if (dbg) {
System.err.println("mv:");
for (int n = 0; n < nv;n++) {
  int i = n * 4;
  System.err.println("" + tpnts[i] + "\t" + tpnts[i+1] + "\t" + tpnts[i+2] + "\t" + tpnts[i+3]);
}
}
      // projection transform
      pjm.transform(tpnts,4,tpnts,4,0,nv);
if (dbg) {
System.err.println("pv:");
for (int n = 0; n < nv;n++) {
  int i = n * 4;
  System.err.println("" + tpnts[i] + "\t" + tpnts[i+1] + "\t" + tpnts[i+2] + "\t" + tpnts[i+3]);
}
}
      // perspective division
      perspectiveDiv(tpnts);
if (dbg) {
System.err.println("dv:");
for (int n = 0; n < nv;n++) {
  int i = n * 4;
  System.err.println("" + tpnts[i] + "\t" + tpnts[i+1] + "\t" + tpnts[i+2] + "\t" + tpnts[i+3]);
}
}
      // viewport tranform
      viewPort(tpnts);
if (dbg) {
System.err.println("sv:");
for (int n = 0; n < nv;n++) {
  int i = n * 4;
  System.err.println("" + tpnts[i] + "\t" + tpnts[i+1] + "\t" + tpnts[i+2] + "\t" + tpnts[i+3]);
}
}
      // x,y,z,w 
      // sort back to front
      if (pntIdx == null || pntIdx.length != tpnts.length/incr) {
        pntIdx = new int[tpnts.length/incr];
      }
      for (int i = 0; i < pntIdx.length; i++) {
        pntIdx[i] = i;
      }
      quickSort(pntIdx, 0, pntIdx.length-1);
    repaint();
  }

  void perspectiveDiv(float v[]) {
    for (int i = 0; i < v.length; i+=4) {
      float w = v[i+3] != 0f ?  v[i+3] : .000001f;
      v[i]   = v[i]/w;
      v[i+1] = v[i+1]/w;
      v[i+2] = v[i+2]/w;
    }
  }

  void viewPort(float v[]) {
    Insets insets = getInsets();
    int vw = (getWidth() - insets.left - insets.right);
    int vh = (getHeight() - insets.top - insets.bottom);
    int vpw = vw / 2;
    int vph = vh / 2;
    for (int i = 0; i < v.length; i+=4) {
      v[i] = (v[i]+1) * vpw;
      v[i+1] = vh - (v[i+1]+1) * vph; // awt is opposite OpenGL
    }
  }

  public  void tableChanged(TableModelEvent e) {
  }
  public void deleteDataMap() {
  }
  public void showDataMap() {
  }
  public void hideDataMap() {
  }
  /* Quick Sort implementation
   */
  private void quickSort(int a[], int left, int right) {
    int leftIndex = left;
    int rightIndex = right;
    int partitionIndex;
    if ( right > left) {
      /* Arbitrarily establishing partition element as the midpoint of
       * the array.
       */
      partitionIndex = ( left + right )  / 2;
      // loop through the array until indices cross
      while ( leftIndex <= rightIndex ) {
        /* find the first element that is greater than or equal to
         * the partitionIndex starting from the leftIndex.
         */
        while ( (leftIndex < right) && 
                   (compare(leftIndex,partitionIndex) < 0) )
               ++leftIndex;
        /* find an element that is smaller than or equal to
         * the partitionIndex starting from the rightIndex.
         */
        while ( ( rightIndex > left ) &&
                   (compare(rightIndex,partitionIndex) > 0) )
          --rightIndex;
            // if the indexes have not crossed, swap
        if ( leftIndex <= rightIndex ) {
          swap(a, leftIndex, rightIndex, incr);
          ++leftIndex;
          --rightIndex;
        }
      }
      /* If the right index has not reached the left side of array
       * must now sort the left partition.
       */
      if ( left < rightIndex ) {
        quickSort( a, left, rightIndex );
      }
      /* If the left index has not reached the right side of array
       * must now sort the right partition.
       */
      if ( leftIndex < right ) {
        quickSort( a, leftIndex, right );
      }
    }
  }
  private void swap(int a[], int i, int j,int incr) {
      int t;
      t = a[i];
      a[i] = a[j];
      a[j] = t;
  }
  private int compare(int idx1, int idx2) {
    int c1 = 2; // offset to z value
    int i1 = idx1 * incr;
    int i2 = idx2 * incr;
    if (tpnts[i1+c1] < tpnts[i2+c1]) 
      return 1;
    if (tpnts[i1+c1] > tpnts[i2+c1]) 
      return -1;
    return 0;
  }
  @Override
public void update(Observable o, Object arg) {
    repaint();
  }
  public void selectRect(int x1, int y1, int x2, int y2) {
    if (x1 > x2) {
      int t = x1;
      x1 = x2;
      x2 = t;
    }    
    if (y1 > y2) {
      int t = y1;
      y1 = y2;
      y2 = t;
    }    
    if (setOperator != null) {
      setOp = setOperator.getSetOperator();
    }
    ListSelectionModel lsm = lastdm.getSelectionModel();
    lsm.setValueIsAdjusting(true); 
    switch (setOp) {
    case SetOperator.DIFFERENCE:
      for (int r = 0, i = 0; i < tpnts.length; r++, i+=4) {
        if (tpnts[i] >= x1 && tpnts[i] <= x2 &&
            tpnts[i+1] >= y1 && tpnts[i+1] <= y2) {
          lsm.removeSelectionInterval(r,r);
        }
      }
      break;
    case SetOperator.INTERSECTION:
      if (lsm.getMinSelectionIndex() < 0)
        break;
      int min = lsm.getMinSelectionIndex();
      int max = lsm.getMaxSelectionIndex();
      for (int r = min, i = min * 4; r <= max; r++, i+=4) {
        if (tpnts[i] < x1 || tpnts[i] > x2 ||
            tpnts[i+1] < y1 || tpnts[i+1] > y2) {
          lsm.removeSelectionInterval(r,r);
        }
      }
      break;
    case SetOperator.XOR:
      if (lsm.getMinSelectionIndex() < 0) {
        for (int r = 0, i = 0; i < tpnts.length; r++, i+=4) {
          if (tpnts[i] >= x1 && tpnts[i] <= x2 &&
            tpnts[i+1] >= y1 && tpnts[i+1] <= y2) {
            lsm.addSelectionInterval(r,r);
          }
        }
        break;
      }
      for (int r = 0, i = 0; i < tpnts.length; r++, i+=4) {
        if (tpnts[i] >= x1 && tpnts[i] <= x2 &&
            tpnts[i+1] >= y1 && tpnts[i+1] <= y2) {
          if (lsm.isSelectedIndex(r)) {
            lsm.removeSelectionInterval(r,r);
          } else {
            lsm.addSelectionInterval(r,r);
          }
        }
      }
      break;
    case SetOperator.BRUSHOVER:
    case SetOperator.REPLACE:
      lsm.clearSelection();
    case SetOperator.UNION:
      for (int r = 0, i = 0; i < tpnts.length; r++, i+=4) {
        if (tpnts[i] >= x1 && tpnts[i] <= x2 &&
            tpnts[i+1] >= y1 && tpnts[i+1] <= y2) {
          lsm.addSelectionInterval(r,r);
        }
      }
      break;
    }
    lsm.setValueIsAdjusting(false); 
  }
}
