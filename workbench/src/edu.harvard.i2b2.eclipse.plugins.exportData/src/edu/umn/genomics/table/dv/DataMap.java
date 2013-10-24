/*
 * @(#) $RCSfile: DataMap.java,v $ $Revision: 1.2 $ $Date: 2008/08/19 20:03:32 $ $Name: RELEASE_1_3_1_0001b $
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
import javax.swing.ListSelectionModel;
import javax.swing.JTable;
import javax.swing.JFrame;
import javax.swing.DefaultCellEditor;
import java.util.Observable;
import java.util.Observer;
import java.math.*;
import edu.umn.genomics.graph.util.ColorMap;
import edu.umn.genomics.table.CleanUp;

/* Some mapping to consider for the table:
 *   points - one point per table row
 *   polyline - connecting the points
 *   area - shaded area under (over) a polyline
 *   lines - one point per table row
 *   bars 
 *   stacked bars 
 *   glyphs
 *   text
 *   surface - 3D 
 *   volume - 3D
*/

/**
 * This class maps columns from a TableModel to displayable attributes.
 * It implements TableModelListener so that it can change the displayed 
 * shapes when the data is changed.
 * This is a base class that should be extended for particular displays 
 * such as a 2D or 3D mapping.
 * @author       James E Johnson
 * @version $Revision: 1.2 $ $Date: 2008/08/19 20:03:32 $  $Name: RELEASE_1_3_1_0001b $ 
 * @since dv1.0
 */
public class DataMap extends Observable 
                     implements Serializable,TableModelListener,ListSelectionListener, CleanUp {
  protected String name;
  protected TableModel tableModel;
  protected ListSelectionModel lsm = null;
  protected JTable  jtable;
  protected JFrame  jframe;
  protected boolean show;
  protected boolean showLabels;
  protected int labelIndex = -1;
  protected int translationIndices[] = {-1,-1,-1};
  protected int colormapIndex = -1;
  //int scaleIndices[] = null;
  protected Color selectColor = Color.cyan;
  protected ColorMap colorMap = new ColorMap();
  protected static final NumberField numberField = new NumberField(0., 20);
  static DefaultCellEditor doubleEditor = 
            new DefaultCellEditor(numberField) {
                //Override DefaultCellEditor's getCellEditorValue method
                //to return an Integer, not a String:
                @Override
				public Object getCellEditorValue() {
                    return new Double(numberField.getValue());
                }
            };
 
  /** Set the number of dimensions for translation.
   * @param dimension The number of axes for translation.
   */
  protected void setTranslationDimension(int dimension) {
    if (dimension < 0 || dimension == translationIndices.length) 
      return;
    int[] oldTrans = translationIndices;
    translationIndices = new int[dimension];
    for (int i = 0; i < translationIndices.length; i++)
      translationIndices[i] = -1;
    System.arraycopy(oldTrans, 0, translationIndices, 0, 
                     translationIndices.length < oldTrans.length ? 
                     translationIndices.length : 
                     oldTrans.length);
    
  }

  /** Create a mapping object for a TableModel.
   * @param name A name to identify this data mapping.
   * @param tableModel The table to map to displayable attributes.
   */
  public DataMap(String name, TableModel tableModel) {
    this(name, tableModel, null);
  }
  /** Create a mapping object for a TableModel.
   * @param name A name to identify this data mapping.
   * @param tableModel The table to map to displayable attributes.
   * @param lsm The ListSelectionModel for selecting rows of the table.
   */
  public DataMap(String name, TableModel tableModel, ListSelectionModel lsm) {
    this.name = name;
    this.tableModel = tableModel;
    this.lsm = lsm;
    tableModel.addTableModelListener(this);
    autoMap();
  }

  /** Return the TableModel used by this mapping.
   * @return the TableModel used by this mapping.
   */
  public TableModel getTableModel() {
    return tableModel;
  }

  /**  Open a window containing a JTable view of the TableModel.
   */
  public void openTable() {
    if (jtable != null) {
      jframe.setVisible(true);
    } else {
      jtable = new JTable(tableModel);
      int columnCnt = tableModel.getColumnCount();
      for (int i = 0; i < columnCnt; i++) {
        try {
          Object colObj = tableModel.getValueAt(0,i);
          if (colObj instanceof Double || colObj instanceof Integer) {
            TableColumn numberColumn = jtable.getColumnModel().getColumn(i);
            numberColumn.setCellEditor(doubleEditor);
          }
        } catch (Exception e) {
          System.err.println("column " + i + " Unknown Class" + e);
        }
      }
      jframe = new JFrame(name);
      jframe.getContentPane().add(jtable);
      jframe.pack();
      jframe.setVisible(true);
    }
  }

  /**  Open a window containing a mapping panel.
   */
  public void openMapPanel() {
    int columnCnt = tableModel.getColumnCount();
    String columnNames[] = new String[columnCnt];
    for (int i = 0; i < columnCnt; i++) {
      columnNames[i] = tableModel.getColumnName(i);
    }

    JMenuItem mi;
      
    JMenu xMenu = new JMenu("X Axis");
    xMenu.setMnemonic('x');
    ButtonGroup xBG = new ButtonGroup();
    ActionListener xListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int c = Integer.parseInt(e.getActionCommand());
        translationIndices[0] = c;
        reMapAxes();
      }
    };
    for (int i = 0; i < columnCnt; i++) {
      mi = xMenu.add( new JRadioButtonMenuItem(columnNames[i],i==0));
      mi.setActionCommand("" + i);
      xBG.add(mi);
      mi.addActionListener(xListener);
    }

      
    JMenu yMenu = new JMenu("Y Axis");
    yMenu.setMnemonic('Y');
    ButtonGroup yBG = new ButtonGroup();
    ActionListener yListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int c = Integer.parseInt(e.getActionCommand());
        translationIndices[1] = c;
        reMapAxes();
      }
    };
    for (int i = 0; i < columnCnt; i++) {
      mi = yMenu.add( new JRadioButtonMenuItem(columnNames[i],i==1));
      mi.setActionCommand("" + i);
      yBG.add(mi);
      mi.addActionListener(yListener);
    }
      
    JMenu zMenu = new JMenu("Z Axis");
    zMenu.setMnemonic('z');
    ButtonGroup zBG = new ButtonGroup();
    ActionListener zListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int c = Integer.parseInt(e.getActionCommand());
        translationIndices[2] = c;
        reMapAxes();
      }
    };
    for (int i = 0; i < columnCnt; i++) {
      mi = zMenu.add( new JRadioButtonMenuItem(columnNames[i],i==2));
      mi.setActionCommand("" + i);
      zBG.add(mi);
      mi.addActionListener(zListener);
    }
    JMenuBar mp = new JMenuBar();
    mp.add(xMenu); 
    mp.add(yMenu); 
    mp.add(zMenu); 
    JFrame frame = new JFrame("Map");
    frame.addWindowListener(new WindowAdapter() {
      @Override
	public void windowClosing(WindowEvent e) {
        e.getWindow().dispose();
      }
    });
    frame.getContentPane().add(mp);
    frame.pack();
    frame.setVisible(true);
  }


  /** Remove any frames associated with this object.
   */
  @Override
protected void finalize() {
    if (jframe != null) {
      jframe.dispose();
    }
    tableModel.removeTableModelListener(this);
  }

  /** Attempt a default mapping of TableColumns to displayable attributes.
   * The first three numerically valued columns are assigned to x,y,z axes.
   * A fourth numerically valued column is assigned to a color value.
   * The first String valued column is used as a label for this data point.
   */
  public void autoMap() {
    int rowCnt = tableModel.getRowCount();
    if (rowCnt < 1) {
      return;
    }
    int columnCnt = tableModel.getColumnCount();
    int stringCnt = 0;
    int numberCnt = 0;
    // Count column class types

    for (int i = 0; i < columnCnt; i++) {
      try {
        Object colObj = tableModel.getValueAt(0,i);
        if (colObj instanceof String) { 
          if (stringCnt == 0) // Assign the first String type as label
            labelIndex = i;
          stringCnt++;
        //} else if (colObj instanceof Double || colObj instanceof Integer || 
        //       colObj instanceof BigDecimal || colObj instanceof BigInteger) {
        } else if (colObj instanceof Number) {
          // assign first 3 numbers as translation
          if (numberCnt < translationIndices.length) 
            translationIndices[numberCnt] = i;
          else if (numberCnt == translationIndices.length)
            colormapIndex = i;
          numberCnt++;
        } else {
          System.err.println("column " + i + " " + colObj.getClass().getName());
          System.err.println(tableModel.getColumnClass(i).getName());
        }
      } catch (Exception e) {
        System.err.println("column " + i + " Unknown Class" + e);
      }
    }

    //System.err.println("labelIndex " + labelIndex);
    //System.err.print("translationIndices[");
    //for(int i = 0; i < translationIndices.length; i++) {
       //System.err.print(" " + translationIndices[i]);
    //}
    //System.err.println(" ]");
  }


  /** Get the translation for a datapoint.
   * @param rowIndex the row in the table.
   * @return An array containing the the translation for a datapoint.
   */
  public double[] getPointTranslation(int rowIndex) {
    double trans[] = new double[translationIndices.length];
    int ncols = tableModel.getColumnCount();
    for (int i = 0; i < translationIndices.length; i++) { 
      if (translationIndices[i] >= 0 && translationIndices[i] < ncols) {
        try {
          Object colObj = tableModel.getValueAt(rowIndex,translationIndices[i]);
          
          if (colObj instanceof Double || colObj instanceof Integer) {
            trans[i] = ((Double)colObj).doubleValue();
          } else if (colObj instanceof BigDecimal) {
            trans[i] = ((BigDecimal)colObj).doubleValue();
          } else if (colObj instanceof BigInteger) {
            trans[i] = ((BigInteger)colObj).doubleValue();
          } else if (colObj instanceof String) { 
            trans[i] = Double.parseDouble((String)colObj);
          }
        } catch (Exception e) {
          System.err.println(e);
          System.err.println(tableModel.getValueAt(
                     rowIndex,translationIndices[i]));
        }
      }
    }
    return trans;
  }

  /** Get the translation for a datapoint.
   * @param rowIndex the row in the table.
   * @return An array containing the the translation for a datapoint.
   */
  public float[] getPointTranslation(int rowIndex, int rowCount, 
                                     float points[], 
                                     int pointOffset, int pointIncrement) {
    if (points == null) {
      points = new float[rowCount * pointIncrement];
    }
    int ncols = tableModel.getColumnCount();
    int rmax = rowIndex + rowCount;
    if (rmax > tableModel.getRowCount()) {
      rmax = tableModel.getRowCount();
    }
    
    if (pointOffset + pointIncrement*(rmax-rowIndex) > points.length) {
      rmax = rowIndex + (points.length - pointOffset) / pointIncrement;
    }
    for (int r = rowIndex,p=pointOffset; r < rmax; r++,p+=pointIncrement) {
      for (int i = 0; i < translationIndices.length; i++) {
        if (translationIndices[i] >= 0 && translationIndices[i] < ncols) {
          try {
            Object colObj = tableModel.getValueAt(r,translationIndices[i]);
            if (colObj instanceof Number) {
              points[p+i] = ((Number)colObj).floatValue();
            } else if (colObj instanceof String) { 
              points[p+i] = (new Float((String)colObj)).floatValue();
            }
          } catch (Exception e) {
            System.err.println(e);
            System.err.println(tableModel.getValueAt(
                       r,translationIndices[i]));
          }
        }
      }
    }
    return points;
  }

  /** Get the color for a datapoint.
   * @param rowIndex the row in the table.
   * @return the color in which to represent the datapoint.
   */
  public Color getPointColor(int rowIndex) {
    Color color = Color.black;
    if (tableModel != null && rowIndex < tableModel.getRowCount()) {
      if (lsm != null && lsm.isSelectedIndex(rowIndex)) {
        return selectColor;
      }
      if (colormapIndex >= 0 && colorMap != null) {
        try {
          double dval = Double.NaN;
          Object colObj = tableModel.getValueAt(rowIndex,colormapIndex);
          if (colObj instanceof Number) {
            dval = ((Number)colObj).doubleValue();
          } else if (colObj instanceof BigDecimal) {
            dval = ((BigDecimal)colObj).doubleValue();
          } else if (colObj instanceof BigInteger) {
            dval = ((BigInteger)colObj).doubleValue();
          } else if (colObj instanceof String) {
            dval = Double.parseDouble((String)colObj);
          }
          if (dval != Double.NaN)
            color = colorMap.getColor(dval);
        } catch (Exception e) {
          System.err.println(e);
          System.err.println(tableModel.getValueAt(
                     rowIndex,colormapIndex));
        }
      }
    }
    return color;
  }

  public float[] getPointColor(int rowIndex, int rowCount, float colors[], 
    int colorOffset, int colorIncrement) {
    if (colors == null) {
      colors = new float[rowCount * colorIncrement];
    }
    int ncols = tableModel.getColumnCount();
    int rmax = rowIndex + rowCount;
    if (rmax > tableModel.getRowCount()) {
      rmax = tableModel.getRowCount();
    }
    if (colorOffset + colorIncrement*(rmax-rowIndex) > colors.length) {
      rmax = rowIndex + (colors.length - colorOffset) / colorIncrement;
    }
    float ca[] = new float[4];
    for (int r = rowIndex,p=colorOffset; r < rmax; r++,p+=colorIncrement) {
      getPointColor(r).getComponents(ca);
      for (int i = 0; i < colorIncrement; i++) {
        colors[p + i] = ca[i];
      }
    }
    return colors;
  }

  /**
   * Called when the column to axis mapping has been changed.
   */
  protected void reMapAxes() {
  }

  /** The callabck method for a TableModelListener, extending classes should 
   * overide this method to handle table changes.
   * @param e describes changes to the TableModel.
   */
  public void tableChanged(TableModelEvent e) {
    setChanged();
    notifyObservers(tableModel);
  }
  /**
   * Sets the row selection model for this table to newModel and registers
   * with for listener notifications from the new selection model.
   * @param newModel the new selection model
   */
  public void setSelectionModel(ListSelectionModel newModel) {
    if (newModel != null && newModel != lsm) {
      if (lsm != null) {
        lsm.removeListSelectionListener(this);
      }
      lsm = newModel;
      lsm.addListSelectionListener(this);
      if (lsm.getMinSelectionIndex() >= 0) {
        valueChanged(new ListSelectionEvent(lsm, lsm.getMinSelectionIndex(),
                                            lsm.getMaxSelectionIndex(), false));
      }
    }
  }
  /**
   * Returns the ListSelectionModel that is used to maintain row
   * selection state.
   * @return the object that provides row selection state.
   */
  public ListSelectionModel getSelectionModel() {
    return lsm;
  }

  /**
   * Called whenever the value of the selection changes.
   * @param e the event that characterizes the change in selection.
   */
  public void valueChanged(ListSelectionEvent e) {
    if (e.getValueIsAdjusting()) {
      return;
    }
/*
    int min = e.getFirstIndex();
    int max = e.getLastIndex();
    for (int i = min; i < max; i++) {
      selectDataPoint(i,lsm.isSelectedIndex(i));
    }
*/
    setChanged();
    notifyObservers(lsm);
  }
  /** 
   * Called when selection status of a datapoint changes.  SubClasses 
   * need to override this method. 
   * @param i the index of that data point.
   * @param isSelected The selection status of the data point.
   */
  public void selectDataPoint(int i,boolean isSelected) {
  }
  /** Return the index for column given its name.
   * @param columnName The name of the table column.
   * @return The index of the column, or -1 if not found.
   */
  public int getColumnIndex(String columnName) {
    int ncols = tableModel.getColumnCount();
    for (int i = 0; i < ncols; i++) {
      if (tableModel.getColumnName(i).equals(columnName))
        return(i);
    } 
    return -1;
  }
  /** Return an array of indices associated with column given its name.
   * If a name isn't found, the index is set to -1.
   * @param columnName  An array of table column names.
   * @return The table column indices for the given names.
   */
  public int[] getColumnIndex(String columnName[]) {
    int idx[] = new int[columnName.length];
    for (int i = 0; i < columnName.length; i++) {
      idx[i] = getColumnIndex(columnName[i]);
    }
    return idx;
  }

  /** Return the name associated with this set of data.
   * @return the associated with this set of data.
   */
  public String getName() {
    return name;
  }

  /** Dispose of this data.
   */
  public void cleanUp() {
    notifyObservers();
    tableModel.removeTableModelListener(this);
    lsm.removeListSelectionListener(this);
  }

  @Override
public void deleteObserver(Observer o) {
    super.deleteObserver(o);
    if (countObservers() < 1) {
      if (tableModel != null)
        tableModel.removeTableModelListener(this);
      if (lsm != null)
        lsm.removeListSelectionListener(this);
    }
  }
  @Override
public void addObserver(Observer o) {
    if (countObservers() < 1) {
      if (tableModel != null)
        tableModel.addTableModelListener(this);
      if (lsm != null)
        lsm.addListSelectionListener(this);
    }
    super.addObserver(o);
  }

  public void setDataRepresentation(String rep) {
  }
  public void scale(double scaleFactor) {
  }
/*
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
*/
}
