/*
 * @(#) $RCSfile: DataView.java,v $ $Revision: 1.2 $ $Date: 2008/08/19 20:03:32 $ $Name: RELEASE_1_3_1_0001b $
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
import java.awt.image.BufferedImage;
import javax.swing.*;
import javax.swing.table.*;
import java.util.Hashtable;
import java.util.Vector;
import java.util.Observer;
import java.util.Observable;
import edu.umn.genomics.table.SetOperator;

/**
 * @author       J Johnson
 * @version $Revision: 1.2 $ $Date: 2008/08/19 20:03:32 $  $Name: RELEASE_1_3_1_0001b $
 * @since        1.0
 */
public class DataView extends JPanel implements Serializable, Observer {
  public final static int NAVIGATE = 0;
  public final static int SELECT_RECT = 1;
  protected int viewAction = NAVIGATE;
  protected boolean inertia = true;
  protected SetOperator setOperator = null;

  /** Extend the Observable class so we can overide the setChanged() method. */
  class Observed extends Observable {
    @Override
	public void setChanged() {
      super.setChanged();
    }
  }
  /** Observers to notify. */
  protected Observed observable = new Observed();
  protected Hashtable dataMapName = new Hashtable(); // name
  protected Hashtable dataMap = new Hashtable(); // data to vector of branchgroups
  /** The list of all DataMaps. */
  protected DefaultListModel dataMapList = new DefaultListModel();
  /** JList */
  protected JList dataMapJList = new JList(dataMapList);
  protected boolean displayAxes = true;

  /** Constructor for benefit of JPython scripting. */
  public DataView() {
    super();
    dataMapJList.setSelectionMode(
        ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    dataMapJList.setCellRenderer(
     new DefaultListCellRenderer() {
       @Override
	public Component getListCellRendererComponent(
         JList list,
         Object ds,            // value to display
         int index,               // cell index
         boolean isSelected,      // is the cell selected
         boolean cellHasFocus)    // the list and the cell have the focus
       {
         setText(((DataMap)ds).getName());
         setBackground(isSelected ? Color.gray : Color.white);
         setForeground(isSelected ? Color.white : Color.black);
         return this;
       }
      }
    );
    setLayout(new BorderLayout());
    add(makeToolBar(),BorderLayout.NORTH);
  }
  public DataMap addDataMap(String name, TableModel tableModel) {
    // check if it already exists
    // add entry to hashtable
    // make branchgroups
    // add to scenegraph
    return null;
  }
  public DataMap addDataMap(String name, TableModel tableModel, 
                            ListSelectionModel lsm) {
    return null;
  }
  public void deleteDataMap(DataMap dataMap) {
    // remove from scenegraph
    // remove entry from hashtable
  }
  public void showDataMap(DataMap dataMap, boolean flag) {
  }
  
  public void showAxes(boolean show) {
    displayAxes = show;
  }

  public void setViewAction(int viewAction) {
    this.viewAction = viewAction;
  }
  public int getViewAction() {
    return viewAction;
  }
  /**
   * Set the SetOperator model.
   * @param setOperator the SetOperator model.
   */
  public void setSetOperatorModel(SetOperator setOperator) {
    this.setOperator = setOperator;
  }
  /**
   * Return the SetOperator model.
   * @return the SetOperator model.
   */
  public SetOperator getSetOperatorModel() {
    return setOperator;
  }


  protected JComponent makeToolBar() {
  if (false) { // start of new menulook
   // ToolBar action buttons
     // view all
     // mode: navaigate, selection 
   // dataset
     // list
       // multiple selection
     // delete
     // show/hide
     // properties editor
   // property menu both per dataset and as a whole
     // show axes
     // inertial rotation
     // navigation speed << < + > >> 
     // point scale << < + > >>
     // point rep point line sphere, cube, cone, cylinder 
     // mapping x,y,z, rgb, hsb
       // colIdx, offset, scale
     // axis scaling x,y,z
     // color fg, bg, select
     // selection as: color, size, shape
     // non selection as: color, size, shape, hidden
    /*
     JCheckBox showAxes;
     JComboBox representation;
     JComboBox selected;
     RowVertexMap
       public int getRowIndexAt(int coordinateIndex);
       public int getCoordinateIndex(int rowIndex);
       public double[] getCoordinateAt(int rowIndex, double coordinate[]);
     CoordinateMapping
       x
       y
       z
       xyz
     ColorMapping
       h/r
       s/g
       b/b
       hsb/rgb 
     TransparencyMapping
     TextureMapping
     ShapeMapping
       shape map 
     SelectionMapping
    */
    JMenu properties = new JMenu();
    JMenuItem mi;

    mi = properties.add(new JCheckBoxMenuItem("Show Axes",true));
    mi.addItemListener(new ItemListener() {
          public void itemStateChanged(ItemEvent e) {
            showAxes(e.getStateChange() == ItemEvent.SELECTED);
          }
    });
    properties.add(mi);

    mi = properties.add(new JCheckBoxMenuItem("Inertia",inertia));
    mi.addItemListener(new ItemListener() {
          public void itemStateChanged(ItemEvent e) {
            inertia = e.getStateChange() == ItemEvent.SELECTED;
          }
    });
    properties.add(mi);

    JToolBar tb = new JToolBar();
    tb.add(properties);
    return tb;
  } // end of new menu look
    JPanel bP = new JPanel();
    JPanel tbP = new JPanel();
    JPanel bbP = new JPanel();

      JCheckBox sa = new JCheckBox("Show Axes",true);
      sa.setAlignmentX(0f);
      sa.addItemListener(new ItemListener() {
          public void itemStateChanged(ItemEvent e) {
            showAxes(e.getStateChange() == ItemEvent.SELECTED);
          }
      });
      tbP.add(sa);

      JButton va = new JButton("View All");
      va.setAlignmentX(0f);
      va.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            setViewpoint();
          }
      });
      tbP.add(va);

      JButton mb = new JButton("Map Columns");
      mb.setAlignmentX(0f);
      mb.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            Object obj[] = dataMapJList.getSelectedValues();
            for (int i = 0; i < obj.length; i++) {
              if (obj[i] instanceof DataMap) {
                DataMap ds = (DataMap)obj[i];
                ds.openMapPanel();
              }
            }
          }
      });
      tbP.add(mb);

      ButtonGroup viewOpGroup = new ButtonGroup();
      JRadioButton navb = new JRadioButton("Navigate", viewAction == DataView.NAVIGATE);
      navb.setAlignmentX(0f);
      navb.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            setViewAction(DataView.NAVIGATE);
          }
        }
      );
      viewOpGroup.add(navb);
      tbP.add(navb);
      JRadioButton selb = new JRadioButton("Select", viewAction == DataView.SELECT_RECT);
      selb.setAlignmentX(0f);
      selb.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            setViewAction(DataView.SELECT_RECT);
          }
        }
      );
      viewOpGroup.add(selb);
      tbP.add(selb);

      final JTextField scaleField = new JTextField("1.",4);
      scaleField.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            String s = e.getActionCommand();
            try {
              double factor = Double.parseDouble(s);
              Object obj[] = dataMapJList.getSelectedValues();
              for (int i = 0; i < obj.length; i++) {
                if (obj[i] instanceof DataMap) {
                  DataMap ds = (DataMap)obj[i];
                  ds.scale(factor);
                }
              }
            } catch (Exception ex) {
              System.err.println(ex);
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
                  DataMap ds = (DataMap)obj[i];
                  ds.scale(factor);
                }
              }
            } catch (Exception ex) {
              System.err.println(ex);
            }
          }
      });
      sbb.setAlignmentX(0f);
      scaleField.setAlignmentX(0f);
      bbP.add(sbb);
      bbP.add(scaleField);

      ActionListener repListener = new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            String rep = e.getActionCommand();
            Object obj[] = dataMapJList.getSelectedValues();
            for (int i = 0; i < obj.length; i++) {
              if (obj[i] instanceof DataMap) {
                DataMap ds = (DataMap)obj[i];
                ds.setDataRepresentation(rep);
              }
            }
          }
      };

      //Vector rv = Glyph3D.getGlyphTypes();        // temp
      Vector rv =  new Vector();
      rv.addElement("sphere");
      rv.addElement("cube");
      rv.addElement("cylinder");
      rv.addElement("cone");
      rv.addElement("points");           // temp
      String dataReps[] = new String[rv.size()];  // temp
      rv.copyInto(dataReps);                      // temp

      ButtonGroup repGroup = new ButtonGroup();
      for (int i = 0; i < dataReps.length; i++) {
        JRadioButton rb = new JRadioButton(dataReps[i], i==dataReps.length-1);
        rb.setAlignmentX(0f);
        rb.addActionListener(repListener);
        repGroup.add(rb);
        bbP.add(rb);
      }

      bP.setLayout(new GridLayout(0,1));
      tbP.setAlignmentX(0f);
      bbP.setAlignmentX(0f);
      bP.setAlignmentX(0f);
      bP.add(tbP);
      bP.add(bbP);

      dataMapJList.setVisibleRowCount(2);
      JPanel ap = new JPanel();
      ap.setLayout(new BoxLayout(ap, BoxLayout.X_AXIS));
      //ap.setAlignmentX(0f);
      ap.add(bP);
      //ap.add(dataMapJList);

    JToolBar tb = new JToolBar();
    tb.add(ap);
    return tb;
  }

  public void setViewpoint() {
  }

  public void update(Observable o, Object arg) {
    if (o instanceof DataMap)
      System.err.println("Observable " + ((DataMap)o).getName());
    System.err.println("Observable " );
  }

  /** Override for grabImage.
   */
  @Override
public void paint(Graphics g) {
    paintComponent(g);
    paintBorder(g);
    paintChildren(g);
  }

  public void grabImage(Observer observer) {
    observable.addObserver(observer);
    BufferedImage img = new BufferedImage(getSize().width,getSize().height, BufferedImage.TYPE_INT_RGB); 
    paint(img.getGraphics());
    observable.setChanged();
    observable.notifyObservers(img);
    /*
      Image img = RepaintManager.currentManager(this).getOffscreenBuffer(this,getSize().width,getSize().height);
    */
  }

  /**
   * Get the component displaying the table data in this view.
   * @return the component displaying the table data.
   */
  public Component getCanvas() {
    return this;
  }
}
