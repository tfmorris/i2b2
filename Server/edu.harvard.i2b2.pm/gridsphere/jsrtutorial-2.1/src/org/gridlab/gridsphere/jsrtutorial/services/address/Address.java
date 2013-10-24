package org.gridlab.gridsphere.jsrtutorial.services.address;

/*
 * @author <a href="mailto:oliver.wehrens@aei.mpg.de">Oliver Wehrens</a>
 * @version $Id: Address.java,v 1.3 2007/08/14 16:47:36 mem61 Exp $
 */

public class Address {

    // every persistent object needs an identifier
    private String oid = null;

    private String firstname = new String();
    private String lastname = new String();

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }
}
