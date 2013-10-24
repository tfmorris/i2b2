/*
 * Created on Jul 26, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package edu.harvard.i2b2.common.util;

import java.text.SimpleDateFormat;
//import java.util.Date;

/**
 * @author mem61
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Constant {
    public final static String I2B2_XSL  = "Download_Date";

    
    public final static String UPDATE_DATE_NAME  = "Update_Date";
    public final static String DOWNLOAD_DATE_NAME  = "Download_Date";
    public final static String IMPORT_DATE_NAME = "Import_Date";
    public final static String OBSERVATION_BLOB = "Observation_Blob";
    public final static String SOURCE_SYSTEM_NAME = "Sourcesystem_Cd";
    public final static String CONCEPT_PREDEID = "predeid_Cd";
    public final static String CONCEPT_PULMONARY_HEIGHT = "pulheight";
    public final static String CONCEPT_PULMONARY_WEIGHT = "pulweight";
    public final static String CONCEPT_PULMONARY_FEV1_OBS = "pulfev1obs";
    public final static String CONCEPT_PULMONARY_FVC_OBS = "pulfvcobs";
    public final static String CONCEPT_PULMONARY_FEV1_PRED = "pulfev1pred";
    public final static String CONCEPT_PULMONARY_FVC_PRED = "pulfvcpred";
    public final static String CONCEPT_POSTDEID = "postdeid_Cd";
    
    /**
     *  Description of the Field
     */
    public final static String DEFAULT_DATE_FORMAT = "dd.MM.yyyy";
    /**
     *  Description of the Field
     */
    public final static String DEFAULT_TIME_FORMAT = "HH:mm:ss";
    /**
     *  Description of the Field
     */
    public final static String DEFAULT_TIMESTAMP_FORMAT = "MM/dd/yyyy HH:mm:ss";

    /**
     *  Description of the Field
     */
    public final static SimpleDateFormat dateFormat = new SimpleDateFormat(DEFAULT_DATE_FORMAT);
    /**
     *  Description of the Field
     */
    public final static SimpleDateFormat timeFormat = new SimpleDateFormat(DEFAULT_TIME_FORMAT);
    /**
     *  Description of the Field
     */
    public final static SimpleDateFormat timestampFormat = new SimpleDateFormat(DEFAULT_TIMESTAMP_FORMAT);

}
