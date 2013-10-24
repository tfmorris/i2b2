/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *     Rajesh Kuttan
 */
package edu.harvard.i2b2.crc.dao.pdo;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.crc.dao.pdo.input.FactRelatedQueryHandler;
import edu.harvard.i2b2.crc.dao.pdo.output.ConceptFactRelated;
import edu.harvard.i2b2.crc.dao.pdo.output.ObservationFactFactRelated;
import edu.harvard.i2b2.crc.dao.pdo.output.PatientFactRelated;
import edu.harvard.i2b2.crc.dao.pdo.output.ProviderFactRelated;
import edu.harvard.i2b2.crc.dao.pdo.output.VisitFactRelated;
import edu.harvard.i2b2.crc.datavo.pdo.PatientDataType;
import edu.harvard.i2b2.crc.datavo.pdo.query.EventListType;
import edu.harvard.i2b2.crc.datavo.pdo.query.FilterListType;
import edu.harvard.i2b2.crc.datavo.pdo.query.InputOptionListType;
import edu.harvard.i2b2.crc.datavo.pdo.query.OutputOptionListType;
import edu.harvard.i2b2.crc.datavo.pdo.query.PatientListType;
import edu.harvard.i2b2.crc.datavo.pdo.ConceptSet;
import edu.harvard.i2b2.crc.datavo.pdo.EventSet;
import edu.harvard.i2b2.crc.datavo.pdo.ObservationSet;
import edu.harvard.i2b2.crc.datavo.pdo.ObserverSet;
import edu.harvard.i2b2.crc.datavo.pdo.PatientSet;
//import edu.harvard.i2b2.crc.datavo.tablepdo.PatientSet;


import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * <b>Main class for PDO queries.<b>
 * 
 * <p> The is the main class to handle pdo query's. 
 * It reads input,filter and output option list from pdo request and 
 * delegate to following class to build individual PDO sections.  
 * 
 *  Observation Fact = {@link FactRelatedQueryHandler}
 *  PatientSet = {@list PatientSection}
 *  ObservationSet ={@list ObservationSection}
 *  ObserverSet = {@list ObserverSection}
 *  ConceptSet = {@list ConceptSection}
 *  
 * <p>Sample PDO request sections:
 * <p> Input list: 
 *  		<b><input_list>
 *               <patient_list max="10" min="0">
 *                    <patient_set_coll_id>184</patient_set_coll_id>
 *                </patient_list>
 *           </input_list>
 *           </b>
 *           
 * <p> Filter list:          
 *           <b><filter_list>
 *               <concept_list>
 *                    <concept_path filter_name="Bakers'asthma">\i2b2\Diagnoses\</concept_path>
 *                </concept_list>
 *           </filter_list>
 *			 </b>
 *
 *<p>Output Option List: 
 *			 <b>	
 *           <ouput_option>
 *               <observation_set blob="false" onlykeys="false"/>
 *               <patient_set select="using_input_list" onlykeys="false"/>
 *           </ouput_option>
 *			 </b>
 * $Id: PdoQueryHandler.java,v 1.8 2007/08/31 14:40:23 rk903 Exp $
 * @author rkuttan
 * @see FactRelatedQueryHandler
 * @see PatientFactRelated
 * @see ProviderFactRelated
 * @see VisitFactRelated
 * @see ObservationFactFactRelated
 */
public class PdoQueryHandler {
	/** logger **/
	protected final Log log = LogFactory.getLog(getClass());
	
	/**Table pdo type value used internally **/
    public static final String TABLE_PDO_TYPE = "TABLE_PDO_TYPE";
    /**Plain pdo type value used internally **/
    public static final String PLAIN_PDO_TYPE = "PLAIN_PDO_TYPE";
    /**PDO request input list **/
    private InputOptionListType inputList = null;
    /**PDO request filter list **/
    private FilterListType filterList = null;
    /**PDO output option list **/
    private OutputOptionListType outputOptionList = null;
    /**Observation helper to build observation section in pdo **/
    private VisitFactRelated visitFactRelated = null;
    /**Observer helper class to build Observer section in pdo**/
    private ProviderFactRelated providerFactRelated = null;
    /**Patient helper class to build patient section in pdo**/
    private PatientFactRelated patientFactRelated = null;
    /**Concept helper class to  build concept section in pdo**/
    private ConceptFactRelated conceptFactRelated = null;
    /**Observation fact helper class to build observationfact **/
    private ObservationFactFactRelated obsFactFactRelated = null;
    /**instance variable for pdo type **/
    private String pdoType = null;
    /**instance variable to hold plain pdo **/
    private PatientDataType plainPdoType = null;
    /**instance variable to hold table pdo **/
    private PatientDataType tablePdoType = null;

    /**
     * Parameter constructor to initialize helper classes  
     * @param pdoType
     * @param inputList
     * @param filterList
     * @param outputOptionList
     * @throws I2B2Exception
     */
    public PdoQueryHandler(String pdoType, InputOptionListType inputList,
        FilterListType filterList, OutputOptionListType outputOptionList)
        throws I2B2DAOException {
        if (pdoType == null) {
            throw new I2B2DAOException("Input pdoType should not be null");
        }

        if (!(pdoType.equalsIgnoreCase(PLAIN_PDO_TYPE) ||
                pdoType.equalsIgnoreCase(TABLE_PDO_TYPE))) {
            throw new I2B2DAOException("Invalid pdoType : " + pdoType);
        }
        
        if (outputOptionList == null) { 
        	throw new I2B2DAOException("Input output option lisr should not be null");
        }

        this.pdoType = pdoType;
        this.inputList = inputList;
        this.filterList = filterList;
        this.outputOptionList = outputOptionList;

        visitFactRelated = new VisitFactRelated(outputOptionList.getEventSet());
        providerFactRelated = new ProviderFactRelated(outputOptionList.getObserverSetUsingFilterList());
        patientFactRelated = new PatientFactRelated(outputOptionList.getPatientSet());
        conceptFactRelated = new ConceptFactRelated(outputOptionList.getConceptSetUsingFilterList());
        obsFactFactRelated = new ObservationFactFactRelated(outputOptionList.getObservationSet());
    }

    /**
     * Method to find if input list is patient set 
     * @return boolean
     */
    public boolean isGetPDOFromPatientSet() {
        if (inputList.getPatientList() != null) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Method to find if input list is visit set
     * @return boolean
     */
    public boolean isGetPDOFromVisitSet() {
        if (inputList.getEventList() != null) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns TablePDO  
     * @return TablePatientDataType
     */
    public PatientDataType getTablePdo() {
        return tablePdoType;
    }

    /**
     * Returns PlainPDO 
     * @return PatientDataType
     */
    public PatientDataType getPlainPdo() {
        return plainPdoType;
    }

    /**
     * 
     * @throws Exception
     */
    public void processPDORequest() throws Exception {
        tablePdoType = new PatientDataType();
        plainPdoType = new PatientDataType();

        // check if obsrvation_fact tag present
        boolean obsFactSelected = obsFactFactRelated.isSelected();

        // check if provider or concept present
        boolean providerSelected = providerFactRelated.isSelected();
        boolean conceptSelected = conceptFactRelated.isSelected();

        // check if patient present
        boolean patientSelected = patientFactRelated.isSelected();

        // check if visit present
        boolean visitSelected = visitFactRelated.isSelected();

        boolean patientFromFact = patientFactRelated.isFactRelated();
        boolean visitFromFact = visitFactRelated.isFactRelated();

        FactRelatedQueryHandler factRelatedQry = null;

        // PatientDataType patientDataType = new PatientDataType();

        //check if this is a fact related query
        if (obsFactSelected || providerSelected || conceptSelected ||
                patientFromFact || visitFromFact) {
            factRelatedQry = new FactRelatedQueryHandler(inputList, filterList,
                    outputOptionList);

            // execute query
            if (pdoType.equalsIgnoreCase(TABLE_PDO_TYPE)) {
                List <ObservationSet> tableObservationSet = factRelatedQry.getTablePdoObservationFact();
                if (obsFactSelected) { 
                	tablePdoType.getObservationSet()
                    .addAll(tableObservationSet);
                }
            } else {
                List<ObservationSet> plainPdoObservationSet = factRelatedQry.getPdoObservationFact();
                
                if (obsFactSelected) { 
                	plainPdoType.getObservationSet()
                    .addAll(plainPdoObservationSet);
                }
            }
        }

        

        //check if observer section is specified in outputoption
        if (providerSelected) {
            ProviderSection providerSection = new ProviderSection(pdoType,
                    factRelatedQry);
            providerSection.generateSet();

            if (pdoType.equalsIgnoreCase(TABLE_PDO_TYPE)) {
                tablePdoType.setObserverSet(providerSection.getTableProviderSet());
            } else {
                plainPdoType.setObserverSet(providerSection.getPlainProviderSet());
            }
        }
        
        //check if concept section is specified in outputoption 
        if (conceptSelected) {
            ConceptSection cs = new ConceptSection(pdoType, factRelatedQry);
            cs.generateSet();

            if (pdoType.equalsIgnoreCase(TABLE_PDO_TYPE)) {
                tablePdoType.setConceptSet(cs.getTableConceptSet());
            } else {
                plainPdoType.setConceptSet(cs.getPlainConceptSet());
            }
        }

        //check if patient section is specified in outputoption
        if (patientSelected) {
            PatientSection ps = new PatientSection(pdoType, factRelatedQry,
                    patientFromFact, isGetPDOFromVisitSet(),
                    isGetPDOFromPatientSet());
            ps.generateSet();

            if (pdoType.equalsIgnoreCase(TABLE_PDO_TYPE)) {
                tablePdoType.setPatientSet(ps.getTablePatientSet());
            } else {
                plainPdoType.setPatientSet(ps.getPlainPatientSet());
            }
        }

        //check if observation section is specified in outputoption
        if (visitSelected) {
            VisitSection vs = new VisitSection(pdoType, factRelatedQry,
                    visitFromFact, isGetPDOFromVisitSet(),
                    isGetPDOFromPatientSet());
            vs.generateSet();

            if (pdoType.equalsIgnoreCase(TABLE_PDO_TYPE)) {
                tablePdoType.setEventSet(vs.getTableEventSet());
            } else {
                plainPdoType.setEventSet(vs.getPlainVisitSet());
            }
        }
    }

    /**
     *   
     */
    private class ProviderSection {
        FactRelatedQueryHandler factRelatedQry = null;
        ObserverSet providerDimensionSet = null;
        ObserverSet observerSet = null;
        String pType = null;

        public ProviderSection(String pType,
            FactRelatedQueryHandler factRelatedQry) {
            this.factRelatedQry = factRelatedQry;
            this.pType = pType;
        }

        /**
         * 
         * @throws I2B2Exception
         */
        public void generateSet()  throws I2B2Exception {
            //check if provider selected
            PdoQueryProviderDao providerDao = new PdoQueryProviderDao();
            TablePdoQueryProviderDao tableProviderDao = new TablePdoQueryProviderDao();
            List<String> providerFactList = factRelatedQry.getProviderFactList();
            boolean detailFlag = providerFactRelated.isSelectDetail();
            boolean blobFlag = providerFactRelated.isSelectBlob();
            boolean statusFlag = providerFactRelated.isSelectStatus();

            if (pType.equalsIgnoreCase(TABLE_PDO_TYPE)) {
                observerSet = tableProviderDao.getProviderById(providerFactList,
                        detailFlag, blobFlag, statusFlag);
            } else {
                providerDimensionSet = providerDao.getProviderById(providerFactList,
                        detailFlag, blobFlag, statusFlag);
            }
        }

        public ObserverSet getTableProviderSet() {
            return observerSet;
        }

        public ObserverSet getPlainProviderSet() {
            return providerDimensionSet;
        }
    }

    private class ConceptSection {
        FactRelatedQueryHandler factRelatedQry = null;
        String pType = null;
        ConceptSet conceptSet = null;
        ConceptSet conceptDimensionSet = null;

        public ConceptSection(String pType,
            FactRelatedQueryHandler factRelatedQry) {
            this.factRelatedQry = factRelatedQry;
            this.pType = pType;
        }

        public void generateSet() throws I2B2Exception {
            //			check if concept selected
            List<String> conceptFactList = factRelatedQry.getConceptFactList();
            PdoQueryConceptDao conceptDao = new PdoQueryConceptDao();
            TablePdoQueryConceptDao tableConceptDao = new TablePdoQueryConceptDao();
            boolean detailFlag = conceptFactRelated.isSelectDetail();
            boolean blobFlag = conceptFactRelated.isSelectBlob();
            boolean statusFlag = conceptFactRelated.isSelectStatus();

            if (pType.equalsIgnoreCase(TABLE_PDO_TYPE)) {
                conceptSet = tableConceptDao.getConceptByConceptCd(conceptFactList,
                        detailFlag, blobFlag, statusFlag);
            } else {
                conceptDimensionSet = conceptDao.getConceptByConceptCd(conceptFactList,
                        detailFlag, blobFlag, statusFlag);
            }
        }

        public ConceptSet getTableConceptSet() {
            return conceptSet;
        }

        public ConceptSet getPlainConceptSet() {
            return conceptDimensionSet;
        }
    }

    private class PatientSection {
        FactRelatedQueryHandler factRelatedQry = null;
        boolean patientFromFact = false;
        boolean fromVisitSet = false;
        boolean fromPatientSet = false;
        PatientSet patientDimensionSet = null;
        PatientSet patientSet = null;
        String pType = null;

        public PatientSection(String pType,
            FactRelatedQueryHandler factRelatedQry, boolean patientFromFact,
            boolean fromVisitSet, boolean fromPatientSet) {
            this.factRelatedQry = factRelatedQry;
            this.patientFromFact = patientFromFact;
            this.fromVisitSet = fromVisitSet;
            this.fromPatientSet = fromPatientSet;
            this.pType = pType;
        }

        public void generateSet() throws Exception {
            PdoQueryPatientDao pdoQueryPatientDao = new PdoQueryPatientDao();
            TablePdoQueryPatientDao tablePdoQueryPatientDao = new TablePdoQueryPatientDao();

            //			check if patient dimension is in output option
            boolean detailFlag = patientFactRelated.isSelectDetail();

            //			check if patient dimension is in output option
            boolean blobFlag = patientFactRelated.isSelectBlob();

            //			check if patient dimension is in output option
            boolean statusFlag = patientFactRelated.isSelectStatus();

            if (patientFromFact) {
                List<String> patientFactList = factRelatedQry.getPatientFactList();
                System.out.println("Patient fact list size" +
                    patientFactList.size());

                if (pType.equalsIgnoreCase(TABLE_PDO_TYPE)) {
                    patientSet = tablePdoQueryPatientDao.getPatientByPatientNum(patientFactList,
                            detailFlag, blobFlag, statusFlag);
                } else {
                    patientDimensionSet = pdoQueryPatientDao.getPatientByPatientNum(patientFactList,
                            detailFlag, blobFlag, statusFlag);
                }
            } else {
                // if visit list get patient list from the visit list and pass
                // it to patient dimention
                // if patient list do direct query from patient dimension
                if (fromPatientSet) {
                    PatientListType patientListType = inputList.getPatientList();

                    if (pType.equalsIgnoreCase(TABLE_PDO_TYPE)) {
                        patientSet = tablePdoQueryPatientDao.getPatientFromPatientSet(patientListType,
                                detailFlag, blobFlag, statusFlag);
                    } else {
                        patientDimensionSet = pdoQueryPatientDao.getPatientFromPatientSet(patientListType,
                                detailFlag, blobFlag, statusFlag);
                    }
                } else if (fromVisitSet) {
                    EventListType visitListType = inputList.getEventList();

                    if (pType.equalsIgnoreCase(TABLE_PDO_TYPE)) {
                        patientSet = tablePdoQueryPatientDao.getPatientFromVisitSet(visitListType,
                                detailFlag, blobFlag, statusFlag);
                    } else {
                        patientDimensionSet = pdoQueryPatientDao.getPatientFromVisitSet(visitListType,
                                detailFlag, blobFlag, statusFlag);
                    }
                }
            }
        }

        public PatientSet getTablePatientSet() {
            return patientSet;
        }

        public PatientSet getPlainPatientSet() {
            return patientDimensionSet;
        }
    }

    private class VisitSection {
        boolean visitFromFact = false;
        boolean fromVisitSet = false;
        boolean fromPatientSet = false;
        FactRelatedQueryHandler factRelatedQry = null;
        EventSet visitDimensionSet = null;
        EventSet eventSet = null;
        String pType = null;

        public VisitSection(String pType,
            FactRelatedQueryHandler factRelatedQry, boolean visitFromFact,
            boolean fromVisitSet, boolean fromPatientSet) {
            this.factRelatedQry = factRelatedQry;
            this.visitFromFact = visitFromFact;
            this.fromVisitSet = fromVisitSet;
            this.fromPatientSet = fromPatientSet;
            this.pType = pType;
        }

        public void generateSet() throws Exception {
            PdoQueryVisitDao pdoQueryVisitDao = new PdoQueryVisitDao();
            TablePdoQueryVisitDao tablePdoQueryVisitDao = new TablePdoQueryVisitDao();

            //check if visit is in output option
            boolean detailFlag = visitFactRelated.isSelectDetail();

            //check if visit is in output option
            boolean blobFlag = visitFactRelated.isSelectBlob();

            //check if visit is in output option
            boolean statusFlag = visitFactRelated.isSelectStatus();

            if (visitFromFact) {
                List<String> visitFactList = factRelatedQry.getVisitFactList();

                // get patient from visit
                if (pType.equalsIgnoreCase(TABLE_PDO_TYPE)) {
                    eventSet = tablePdoQueryVisitDao.getVisitsByEncounterNum(visitFactList,
                            detailFlag, blobFlag, statusFlag);
                } else {
                    visitDimensionSet = pdoQueryVisitDao.getVisitsByEncounterNum(visitFactList,
                            detailFlag, blobFlag, statusFlag);
                }
            } else {
                // check if input is visit or patient list
                if (fromVisitSet) {
                    EventListType visitListType = inputList.getEventList();

                    if (pType.equalsIgnoreCase(TABLE_PDO_TYPE)) {
                        eventSet = tablePdoQueryVisitDao.getVisitDimensionSetFromVisitList(visitListType,
                                detailFlag, blobFlag, statusFlag);
                    } else {
                        visitDimensionSet = pdoQueryVisitDao.getVisitDimensionSetFromVisitList(visitListType,
                                detailFlag, blobFlag, statusFlag);
                    }
                } else if (fromPatientSet) {
                    PatientListType patientListType = inputList.getPatientList();

                    if (pType.equalsIgnoreCase(TABLE_PDO_TYPE)) {
                        eventSet = tablePdoQueryVisitDao.getVisitDimensionSetFromPatientList(patientListType,
                                detailFlag, blobFlag, statusFlag);
                    } else {
                        visitDimensionSet = pdoQueryVisitDao.getVisitDimensionSetFromPatientList(patientListType,
                                detailFlag, blobFlag, statusFlag);
                    }
                }
            }
        }

        public EventSet getTableEventSet() {
            return eventSet;
        }

        public EventSet getPlainVisitSet() {
            return visitDimensionSet;
        }
    }
}
