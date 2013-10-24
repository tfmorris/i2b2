/*
 * @(#) $RCSfile: IndexMapSelection.java,v $ $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
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

package edu.umn.genomics.table;

import java.util.BitSet;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;

/**
 * IndexMapSelection bridges two ListSelectionModels, so that selections in
 * either will be reflected in the other. This adds a listener to each
 * ListSelectionModel and responds to selection events by altering the selection
 * in the other ListSelectionModel. An IndexMap can provide a mapping between
 * the indices of the two ListSelectionModels.
 * 
 * @author J Johnson
 * @version $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 */
public class IndexMapSelection implements CleanUp {
    ListSelectionModel src;
    ListSelectionModel dst;
    ListSelectionModel cur = null;
    IndexMap map;
    // For OneToMany mapping all of the dst indices must be set
    // for the src index to be set.
    boolean allSelected = true;
    ListSelectionListener selListener = new ListSelectionListener() {
	public void valueChanged(ListSelectionEvent e) {
	    if (!e.getValueIsAdjusting() && cur == null) { // prevent recursion
		ListSelectionModel lsm = (ListSelectionModel) e.getSource();
		cur = lsm;
		ListSelectionModel rfl; // reflect changes in other model
		int mn = lsm.getMinSelectionIndex();
		int mx = lsm.getMaxSelectionIndex();
		if (lsm == src) {
		    rfl = dst;
		} else {
		    rfl = src;
		}
		int min = 0;
		int max = map == null ? lsm.getMaxSelectionIndex()
			: lsm == src ? map.getSrcSize() : map.getDstSize();
		BitSet bidx = null;
		if (map != null && !map.isOneToOne()) {
		    bidx = new BitSet(max + 1);
		}
		rfl.setValueIsAdjusting(true);
		if (map == null) {
		    rfl.clearSelection();
		}

		for (int i = min; i <= max; i++) {
		    if (map == null) {
			if (lsm.isSelectedIndex(i)) {
			    rfl.addSelectionInterval(i, i);
			} else {
			    rfl.removeSelectionInterval(i, i);
			}
		    } else if (map.isOneToOne()) {
			int j = lsm == src ? map.getDst(i) : map.getSrc(i);
			if (j < 0)
			    continue;
			if (lsm.isSelectedIndex(i)) {
			    rfl.addSelectionInterval(j, j);
			} else {
			    rfl.removeSelectionInterval(j, j);
			}
		    } else {
			// How to deal with OneToMany mapping?
			// All dst indices selected -> select src index
			// Any dst indices selected -> select src index
			//
			if (!bidx.get(i)) {
			    bidx.set(i); // Flag this index as visited
			    // Get the set of destination indices
			    int di[] = lsm == src ? map.getDsts(i) : map
				    .getSrcs(i);
			    if (di != null) {
				for (int j = 0; j < di.length; j++) {
				    if (di[j] < 0)
					continue;
				    // Get the set of source indices for each
				    // destination index
				    int si[] = lsm == dst ? map.getDsts(di[j])
					    : map.getSrcs(di[j]);
				    if (si != null) {
					boolean allSet = true;
					boolean anySet = false;
					for (int k = 0; k < si.length; k++) {
					    bidx.set(si[k]); // Flag this index
					    // as visited
					    allSet &= lsm
						    .isSelectedIndex(si[k]);
					    anySet |= lsm
						    .isSelectedIndex(si[k]);
					}
					if (allSet || (!allSelected && anySet)) {
					    rfl.addSelectionInterval(di[j],
						    di[j]);
					} else {
					    rfl.removeSelectionInterval(di[j],
						    di[j]);
					}
				    }
				}
			    }
			}
		    }
		}
		rfl.setValueIsAdjusting(false);
		cur = null;
	    }
	}
    };

    /**
     * Link two ListSelectionModels so that selections in either will be
     * reflected in the other. If a map is given, it provides the mapping of
     * indices between the two ListSelectionModels.
     * 
     * @param model1
     *            One of the ListSelectionModels to link together.
     * @param model2
     *            The other ListSelectionModels to link together.
     * @param map
     *            If not null, this provides the mapping of indices between the
     *            two ListSelectionModels.
     */
    public IndexMapSelection(ListSelectionModel model1,
	    ListSelectionModel model2, IndexMap map) {
	this(model1, model2, map, true);
    }

    /**
     * Link two ListSelectionModels so that selections in either will be
     * reflected in the other. If a map is given, it provides the mapping of
     * indices between the two ListSelectionModels.
     * 
     * @param model1
     *            One of the ListSelectionModels to link together.
     * @param model2
     *            The other ListSelectionModels to link together.
     * @param map
     *            If not null, this provides the mapping of indices between the
     *            two ListSelectionModels.
     * @param allSelected
     *            Sets the selection behavior for a OneToManyIndexMap.
     * @see #setAllSelected
     */
    public IndexMapSelection(ListSelectionModel model1,
	    ListSelectionModel model2, IndexMap map, boolean allSelected) {
	this.src = model1;
	this.dst = model2;
	this.map = map;
	this.allSelected = allSelected;
	if (src.getMinSelectionIndex() >= 0) {
	    selListener
		    .valueChanged(new ListSelectionEvent(src, src
			    .getMinSelectionIndex(),
			    src.getMaxSelectionIndex(), false));
	}
	src.addListSelectionListener(selListener);
	dst.addListSelectionListener(selListener);
    }

    /**
     * Set the selection behavior for a OneToManyIndexMap.
     * 
     * @param allSelected
     *            This only applies to OneToManyIndexMaps, if true all
     *            destination indices must be selected in order for the source
     *            index to be selected, if false then the source index is
     *            selected if any of its associated destination indices are
     *            selected.
     */
    public void setAllSelected(boolean allSelected) {
	this.allSelected = allSelected;
	if (map != null && !map.isOneToOne()) {
	    selListener
		    .valueChanged(new ListSelectionEvent(src, src
			    .getMinSelectionIndex(),
			    src.getMaxSelectionIndex(), false));
	}
    }

    /**
     * Get the selection behavior for a OneToManyIndexMap.
     * 
     * @see #setAllSelected
     */
    public boolean getAllSelected() {
	return allSelected;
    }

    @Override
    public void finalize() throws Throwable {
	cleanUp();
	super.finalize();
    }

    public void cleanUp() {
	if (src != null) {
	    src.removeListSelectionListener(selListener);
	    src = null;
	}
	if (dst != null) {
	    dst.removeListSelectionListener(selListener);
	    dst = null;
	}
    }
}
