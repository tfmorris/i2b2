/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 * 
 *     Wensong Pan
 *     
 */
/**
 * Class: TableFactory
 * 
 * A utility class for generating table files based on the PDO.
 * 
 */

package edu.harvard.i2b2.exportData.dataModel;

import java.io.IOException;
import java.io.RandomAccessFile;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import edu.harvard.i2b2.common.datavo.pdo.EventSet;
import edu.harvard.i2b2.common.datavo.pdo.EventType;
import edu.harvard.i2b2.common.datavo.pdo.ObservationSet;
import edu.harvard.i2b2.common.datavo.pdo.PatientSet;
import edu.harvard.i2b2.common.datavo.pdo.ObservationType;
import edu.harvard.i2b2.common.datavo.pdo.PatientType;
import edu.harvard.i2b2.crcxmljaxb.datavo.i2b2message.StatusType;
import edu.harvard.i2b2.exportData.data.EventData;
import edu.harvard.i2b2.exportData.data.PDOItem;
import edu.harvard.i2b2.exportData.data.PDOResponseMessageFactory;
import edu.harvard.i2b2.exportData.data.PatientDemographics;

public class TableFactory {

    public static String writeTableFiles(String result,
	    ArrayList<TimelineRow> tlrows, boolean useName) {

	StringBuilder resultString = new StringBuilder();
	RandomAccessFile resultFile = null;

	try {
	    PDOResponseMessageFactory pdoresponsefactory = new PDOResponseMessageFactory();
	    final StatusType statusType = pdoresponsefactory
		    .getStatusFromResponseXML(result);

	    if (statusType.getType().equalsIgnoreCase("Error")) {
		java.awt.EventQueue.invokeLater(new Runnable() {
		    public void run() {
			JOptionPane.showMessageDialog(null,
				"Error returned from server:\n",// +statusType.
				// getValue(),
				"Error", JOptionPane.INFORMATION_MESSAGE);
		    }
		});

		return "Error";
	    }

	    PatientSet patientDimensionSet = pdoresponsefactory
		    .getPatientSetFromResponseXML(result);
	    String tableFile = System.getProperty("user.dir") + '/'
		    + "i2b2tmptablefiles/patienttable.txt";
	    resultFile = new RandomAccessFile(tableFile, "rw");

	    if (patientDimensionSet != null) {
		// get table columns
		// PatientType patientType0 =
		// patientDimensionSet.getPatient().get(0);
		append(
			resultFile,
			"Patient ID Source|Patient ID|Birth Date|Death Date|Race|Gender|Age In Years|Death Recorded\n");

		System.out.println("Total patient: "
			+ patientDimensionSet.getPatient().size());
		for (int i = 0; i < patientDimensionSet.getPatient().size(); i++) {
		    PatientType patientType = patientDimensionSet.getPatient()
			    .get(i);

		    PatientDemographics pdemo = new PatientDemographics();
		    pdemo.setParamData(patientType.getParam());

		    System.out.println("Patient id: "
			    + patientType.getPatientId() + "|" + pdemo.race()
			    + "|" + pdemo.gender() + "|" + pdemo.age() + "|"
			    + pdemo.vitalStatus());

		    // resultString.append(patientType.getPatientIdSource() +
		    // "|" + patientType.getPatientId() + "|"
		    // + pdemo.birthDate() + "|" + pdemo.race() + "|" +
		    // pdemo.gender() + "|"
		    // + pdemo.age() + "|" + pdemo.vitalStatus() + "\n");

		    append(resultFile, patientType.getPatientId().getSource()
			    + "|" + patientType.getPatientId().getValue() + "|"
			    + pdemo.birthDate() + "|" + pdemo.deathDate() + "|"
			    + pdemo.race() + "|" + pdemo.gender() + "|"
			    + pdemo.age() + "|" + pdemo.vitalStatus() + "\n");
		    // resultString.toString());
		}
	    }

	    resultFile.close();

	    List<ObservationSet> factSets = pdoresponsefactory
		    .getFactSetsFromResponseXML(result);
	    ObservationSet observationFactSet = null;
	    String path = null;

	    for (int i = 0; i < tlrows.size(); i++) {
		TimelineRow row = tlrows.get(i);

		int total = 0;
		resultString = new StringBuilder();

		// loop thru all the pdo sets for this row here
		for (int s = 0; s < row.pdoItems.size(); s++) {
		    PDOItem pset = row.pdoItems.get(s);
		    observationFactSet = null;

		    for (int m = 0; m < factSets.size(); m++) {
			ObservationSet tmpFactSet = factSets.get(m);
			if (tmpFactSet.getPanelName().equalsIgnoreCase(
				pset.fullPath)) {
			    observationFactSet = tmpFactSet;
			    path = observationFactSet.getPanelName();
			    break;
			}
		    }

		    if (observationFactSet == null) {
			continue;
		    }

		    for (int k = 0; k < observationFactSet.getObservation()
			    .size(); k++) {
			ObservationType obsFactType = observationFactSet
				.getObservation().get(k);

			String sStart_date = " ";
			if (obsFactType.getStartDate() != null) {
			    sStart_date = obsFactType.getStartDate().getYear()
				    + "-"
				    + getZeroString(obsFactType.getStartDate()
					    .getMonth())
				    + "-"
				    + getZeroString(obsFactType.getStartDate()
					    .getDay())
				    + " "
				    + getZeroString(obsFactType.getStartDate()
					    .getHour())
				    + ":"
				    + getZeroString(obsFactType.getStartDate()
					    .getMinute())
				    + ":"
				    + getZeroString(obsFactType.getStartDate()
					    .getSecond());
			}

			String sEnd_date = " ";
			if (obsFactType.getEndDate() != null) {
			    sEnd_date = obsFactType.getEndDate().getYear()
				    + "-"
				    + getZeroString(obsFactType.getEndDate()
					    .getMonth())
				    + "-"
				    + getZeroString(obsFactType.getEndDate()
					    .getDay())
				    + " "
				    + getZeroString(obsFactType.getEndDate()
					    .getHour())
				    + ":"
				    + getZeroString(obsFactType.getEndDate()
					    .getMinute())
				    + ":"
				    + getZeroString(obsFactType.getEndDate()
					    .getSecond());
			}

			if (useName) {
			    resultString
				    .append(obsFactType.getPatientId()
					    .getSource()
					    + "|"
					    + obsFactType.getPatientId()
						    .getValue()
					    + "|"
					    + obsFactType.getConceptCd()
						    .getName()
					    + "|"
					    + sStart_date
					    + "|"
					    + sEnd_date
					    + "|"
					    + ((obsFactType.getModifierCd()
						    .getName() == null) ? " "
						    : obsFactType
							    .getModifierCd()
							    .getName())
					    + "|"
					    + ((obsFactType.getValuetypeCd() == null || obsFactType
						    .getValuetypeCd().equals(
							    "@")) ? " "
						    : obsFactType
							    .getValuetypeCd())
					    + "|"
					    + ((obsFactType.getTvalChar() == null) ? " "
						    : obsFactType.getTvalChar())
					    + "|"
					    + ((obsFactType.getNvalNum()
						    .getValue() == null) ? " "
						    : obsFactType.getNvalNum()
							    .getValue())
					    + "|"
					    + ((obsFactType.getValueflagCd()
						    .getName() == null || obsFactType
						    .getValueflagCd().getName()
						    .equals("@")) ? " "
						    : obsFactType
							    .getValueflagCd()
							    .getName())
					    + "|"
					    + ((obsFactType.getUnitsCd() == null || obsFactType
						    .getUnitsCd().equals("@")) ? " "
						    : obsFactType.getUnitsCd())
					    + "|"
					    + ((obsFactType.getQuantityNum() == null) ? " "
						    : obsFactType
							    .getQuantityNum()
							    .doubleValue())
					    + "|"
					    + ((obsFactType.getLocationCd() == null) ? " "
						    : obsFactType
							    .getLocationCd()
							    .getName())
					    + "|"
					    + obsFactType.getObserverCd()
						    .getSoruce()
					    + "|"
					    + obsFactType.getEventId()
						    .getSource()
					    + "|"
					    + obsFactType.getEventId()
						    .getValue()
					    + "|"
					    + path
					    + "\n");
			} else {
			    resultString
				    .append(obsFactType.getPatientId()
					    .getSource()
					    + "|"
					    + obsFactType.getPatientId()
						    .getValue()
					    + "|"
					    + obsFactType.getConceptCd()
						    .getValue()
					    + "|"
					    + sStart_date
					    + "|"
					    + sEnd_date
					    + "|"
					    + ((obsFactType.getModifierCd()
						    .getValue() == null) ? " "
						    : obsFactType
							    .getModifierCd()
							    .getValue())
					    + "|"
					    + ((obsFactType.getValuetypeCd() == null) ? " "
						    : obsFactType
							    .getValuetypeCd())
					    + "|"
					    + ((obsFactType.getTvalChar() == null) ? " "
						    : obsFactType.getTvalChar())
					    + "|"
					    + ((obsFactType.getNvalNum()
						    .getValue() == null) ? " "
						    : obsFactType.getNvalNum()
							    .getValue())
					    + "|"
					    + ((obsFactType.getValueflagCd()
						    .getValue() == null) ? " "
						    : obsFactType
							    .getValueflagCd()
							    .getValue())
					    + "|"
					    + ((obsFactType.getUnitsCd() == null) ? " "
						    : obsFactType.getUnitsCd())
					    + "|"
					    + ((obsFactType.getQuantityNum() == null) ? " "
						    : obsFactType
							    .getQuantityNum()
							    .doubleValue())
					    + "|"
					    + ((obsFactType.getLocationCd() == null) ? " "
						    : obsFactType
							    .getLocationCd()
							    .getName())
					    + "|"
					    + obsFactType.getObserverCd()
						    .getValue()
					    + "|"
					    + obsFactType.getEventId()
						    .getValue()
					    + "|"
					    + obsFactType.getEventId()
						    .getValue()
					    + "|"
					    + path
					    + "\n");
			}
			total++;
		    }
		}

		tableFile = System.getProperty("user.dir") + '/'
			+ "i2b2tmptablefiles/facttable_" + i + ".txt";
		resultFile = new RandomAccessFile(tableFile, "rw");

		if (useName) {
		    append(
			    resultFile,
			    "Patient id Source|Patient id|Concept Name|Start Date|"
				    + "End Date|Modifier Name|Value Type Code|Text Value|Numeric Value|"
				    + "Value Flag Name|Units Code|Quantity|Location Name|Observer Name|"
				    + "Event id Source|Event id|Path\n");
		} else {
		    append(
			    resultFile,
			    "Patient id source|Patient id|Concept Code|Start Date|"
				    + "End Date|Modifier Code|Value Type Code|Text Value|Numeric Value|"
				    + "Value Flag Code|Units Code|Quantity|Location Code|Observer id|"
				    + "Event id source|Event id|Path\n");
		}

		append(resultFile, resultString.toString());
		resultFile.close();
	    }

	    EventSet evntSet = pdoresponsefactory
		    .getEventSetFromResponseXML(result);
	    if (evntSet != null) {
		System.out.println("Total event: " + evntSet.getEvent().size());

		resultString = new StringBuilder();
		for (int j = 0; j < evntSet.getEvent().size(); j++) {
		    EventType eventType = evntSet.getEvent().get(j);

		    EventData event = new EventData();
		    event.setParamData(eventType.getParam());
		    event.eventID(eventType.getEventId().getValue());
		    event.patientID(eventType.getPatientId().getValue());

		    String sStart_date = " ";
		    if (eventType.getStartDate() != null) {
			sStart_date = eventType.getStartDate().getYear()
				+ "-"
				+ getZeroString(eventType.getStartDate()
					.getMonth())
				+ "-"
				+ getZeroString(eventType.getStartDate()
					.getDay())
				+ " "
				+ getZeroString(eventType.getStartDate()
					.getHour())
				+ ":"
				+ getZeroString(eventType.getStartDate()
					.getMinute())
				+ ":"
				+ getZeroString(eventType.getStartDate()
					.getSecond());
		    }

		    String sEnd_date = " ";
		    if (eventType.getEndDate() != null) {
			sEnd_date = eventType.getEndDate().getYear()
				+ "-"
				+ getZeroString(eventType.getEndDate()
					.getMonth())
				+ "-"
				+ getZeroString(eventType.getEndDate().getDay())
				+ " "
				+ getZeroString(eventType.getEndDate()
					.getHour())
				+ ":"
				+ getZeroString(eventType.getEndDate()
					.getMinute())
				+ ":"
				+ getZeroString(eventType.getEndDate()
					.getSecond());
		    }

		    // System.out.println("Event: " + event.eventID() + "|"
		    // + event.patientID() + "|" + event.admissionStatus()
		    // + "|" + event.site() + "|" + sStart_date + "|" +
		    // sEnd_date);

		    resultString.append(eventType.getPatientId().getSource()
			    + "|" + event.patientID() + "|"
			    + event.admissionStatus() + "|" + event.site()
			    + "|" + sStart_date + "|" + sEnd_date + "|"
			    + eventType.getEventId().getSource() + "|"
			    + event.eventID() + "\n");
		}
	    }

	    tableFile = System.getProperty("user.dir") + '/'
		    + "i2b2tmptablefiles/eventtable.txt";
	    resultFile = new RandomAccessFile(tableFile, "rw");
	    append(
		    resultFile,
		    "Patient ID Source|Patient ID|Admission Status|Site|Start Date|End Date|Event Source|Event ID\n");
	    append(resultFile, resultString.toString());
	    resultFile.close();

	    return resultString.toString();
	} catch (Exception e) {
	    e.printStackTrace();
	    try {
		if (resultFile != null) {
		    resultFile.close();
		}
	    } catch (Exception e1) {
	    }
	    return null;
	}
    }

    private static String getZeroString(int i) {
	return ((i < 10) ? "0" + i : "" + i);
    }

    public static void append(RandomAccessFile file, String outString)
	    throws IOException {
	try {
	    file.seek(file.length());
	    file.writeBytes(outString);
	} catch (IOException e) {
	    throw new IOException("trouble writing to random access file.");
	}
	return;
    }

}
