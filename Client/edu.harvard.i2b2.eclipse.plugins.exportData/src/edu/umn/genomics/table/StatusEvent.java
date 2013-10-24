/*
 * @(#) $RCSfile: StatusEvent.java,v $ $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
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

import java.util.EventObject;

/**
 * An event indicating a change in status, which maybe due to an Exception.
 * 
 * @author J Johnson
 * @version $Revision: 1.3 $ $Date: 2008/10/31 17:43:22 $ $Name: RELEASE_1_3_1_0001b $
 * @since 1.0
 * @see edu.umn.genomics.table.StatusListener
 */
public class StatusEvent extends EventObject {
    String msg = null;
    Exception ex = null;

    /*
     * to the given value.
     * 
     * @param source The source of this event.
     * 
     * @param message a status message (may be null).
     * 
     * @param exception an exception that prompted this event (may be null).
     */
    public StatusEvent(Object source, String message, Exception exception) {
	super(source);
	this.msg = message;
	this.ex = exception;
    }

    /**
     * Return the status message.
     * 
     * @return the status message.
     */
    public String getStatus() {
	return msg != null ? msg : ex != null ? ex.toString() : "";
    }

    /**
     * Return the exception associated with this status.
     * 
     * @return the exception associated with this status.
     */
    public Exception getException() {
	return ex;
    }
}
