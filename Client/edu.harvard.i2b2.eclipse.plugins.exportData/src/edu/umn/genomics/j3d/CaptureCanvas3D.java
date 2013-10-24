/*
 * @(#) $RCSfile: CaptureCanvas3D.java,v $ $Revision: 1.4 $ $Date: 2008/10/30 19:12:08 $ $Name: RELEASE_1_3_1_0001b $
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

package edu.umn.genomics.j3d; //DataViewer

import javax.media.j3d.Canvas3D;
import javax.media.j3d.ImageComponent;
import javax.media.j3d.Raster;
import javax.media.j3d.ImageComponent2D;
import java.awt.image.BufferedImage;
import java.awt.Dimension;
import java.util.Observable;
import java.util.Observer;

/**
 * This class extends the Canvas3D class to capture the rendered image.
 * 
 * @author James E Johnson
 * @version $Revision: 1.4 $ $Date: 2008/10/30 19:12:08 $ $Name: RELEASE_1_3_1_0001b $
 * @since dv1.0
 */
public class CaptureCanvas3D extends Canvas3D {
    /** Extend the Observable class so we can overide the setChanged() method. */
    class Observed extends Observable {
	@Override
	public void setChanged() {
	    super.setChanged();
	}
    }

    /** Observers to notify. */
    private Observed observable = new Observed();
    /** Flag to capture an image. */
    private boolean captureImage = false;

    /**
     * Create the CaptureCanvas3D.
     * 
     * @param graphicsConfiguration
     */
    public CaptureCanvas3D(java.awt.GraphicsConfiguration graphicsConfiguration) {
	super(graphicsConfiguration);
    }

    /**
     * This routine is called by the Java 3D rendering loop after completing all
     * rendering to the canvas. It reads the raster image and notifies
     * registered observers.
     */
    @Override
    public void postSwap() {
	if (captureImage) {
	    Dimension d = getSize();
	    Raster raster = new Raster();
	    raster.setSize(d);
	    raster.setImage(new ImageComponent2D(ImageComponent.FORMAT_RGB,
		    new BufferedImage(d.width, d.height,
			    BufferedImage.TYPE_INT_RGB)));
	    getGraphicsContext3D().readRaster(raster);
	    // Now strip out the image info
	    if (captureImage) {
		observable.setChanged();
		observable.notifyObservers(raster.getImage().getImage());
		observable.deleteObservers();
		captureImage = false;
	    }
	}
    }

    /**
     * Request a captured image of the Canvas. This Object will call the
     * observer's update method with a BufferImage as the argument.
     * 
     * @param observer
     *            The object to notify when the image is available.
     */
    public void captureImage(Observer observer) {
	observable.addObserver(observer);
	captureImage = true;
	paint(getGraphics()); // repaint so we can capture the image in
	// postSwap.
    }
}
