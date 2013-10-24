/*
 * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
 * @version $Id: StringRangeValidator.java,v 1.1 2007/08/24 17:24:58 mem61 Exp $
 */
package org.gridlab.gridsphere.provider.portletui.validator;

public class StringRangeValidator extends BaseValidator {

    private int min = 0;
    private int max = 30;
    private String value = null;

    public void setMinLength(int min) {
        this.min = min;
    }

    public int getMinLength() {
        return min;
    }

    public void setMaxLength(int max) {
        this.max = max;
    }

    public int getMaxLength() {
        return max;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public boolean isValid() {
        /*
        BaseTag input = (BaseTag)getParent();
        String name = input.getName();
        */
        return true;
    }

}
