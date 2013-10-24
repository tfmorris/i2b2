package net.nbirn.srbclient.data;

import java.io.StringWriter;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import javax.xml.bind.JAXBElement;

import net.nbirn.srbclient.utils.Messages;
import net.nbirn.srbclient.utils.VerifyDataEventMapping;
import net.nbirn.srbclient.utils.VerifyDataObservationFact;
import net.nbirn.srbclient.utils.VerifyDataPatientMapping;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import edu.harvard.i2b2.common.datavo.pdo.BlobType;
import edu.harvard.i2b2.common.datavo.pdo.EidSet;
import edu.harvard.i2b2.common.datavo.pdo.EidType;
import edu.harvard.i2b2.common.datavo.pdo.EventSet;
import edu.harvard.i2b2.common.datavo.pdo.EventType;
import edu.harvard.i2b2.common.datavo.pdo.ObservationSet;
import edu.harvard.i2b2.common.datavo.pdo.ObservationType;
import edu.harvard.i2b2.common.datavo.pdo.PatientDataType;
import edu.harvard.i2b2.common.datavo.pdo.PatientIdType;
import edu.harvard.i2b2.common.datavo.pdo.PidSet;
import edu.harvard.i2b2.common.datavo.pdo.PidType;
import edu.harvard.i2b2.common.datavo.pdo.EidType.EventMapId;
import edu.harvard.i2b2.common.datavo.pdo.ObservationType.EventId;
import edu.harvard.i2b2.common.datavo.pdo.ObservationType.ModifierCd;
import edu.harvard.i2b2.common.datavo.pdo.ObservationType.ObserverCd;
import edu.harvard.i2b2.common.datavo.pdo.PidType.PatientId;
import edu.harvard.i2b2.common.datavo.pdo.PidType.PatientMapId;
import edu.harvard.i2b2.common.util.jaxb.JAXBUnWrapHelper;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtil;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.fr.datavo.dnd.DndType;
import edu.harvard.i2b2.eclipse.plugins.fr.ws.FRJAXBUtil;
import edu.harvard.i2b2.frclient.security.CryptUtil;

public class XMLtoPDO {


	private Log log = LogFactory.getLog(XMLtoPDO.class.getName());
	private PatientDataType patientDataType= null;

	public XMLtoPDO(){

	}
	public PatientDataType getPDO()
	{
		return patientDataType;
	}
	public void setPDO(String xmlstr)
	{
		if (xmlstr == null)
		{
			patientDataType = null;
			return;

		}
		String note = null;
		try {
			JAXBUtil jaxbUtil = FRJAXBUtil.getJAXBUtil();
			JAXBElement jaxbElement = jaxbUtil.unMashallFromString(xmlstr);
			DndType dndType = (DndType)jaxbElement.getValue();

			if(dndType == null)
			{
				log.info("dndType is null");
			} else {
				patientDataType = (PatientDataType) new JAXBUnWrapHelper().getObjectByClass(dndType.getAny(),
						PatientDataType.class);

				if(patientDataType == null)
					log.info("patientDataType is null");
			}
			//note = (String) patientDataType.getObservationSet().get(0).getObservation().get(0).getObservationBlob().getContent().get(0);
		} catch (Exception ex) {
			ex.printStackTrace();
			log.error("Error marshalling Explorer drag text");
		}

	}
	public boolean updateTableObservationSet(Table table, List<ObservationSet> list)
	{
		if (list == null || list.size() == 0) 
			return false;

		table.clearAll();
		table.removeAll();

		VerifyDataObservationFact vdata = new VerifyDataObservationFact();

		for(;table.getColumns().length>0;)
		{
			table.getColumns()[0].dispose();	
		}		

		for (int i = 1; i < vdata.getColumns().length-3; i++) {
			TableColumn column = new TableColumn(table, SWT.CENTER);
			column.setText(vdata.getColumns()[i]);
			column.setAlignment(SWT.LEFT);
			column.setWidth(100);
			switch (i){
			case 1:
				column.setText(Messages.getString("ClientFolderView.Step1ObserFactTable4"));
				break;
			case 2:
				column.setText(Messages.getString("ClientFolderView.Step1ObserFactTable3"));
				break;
			case 3:
				column.setText(Messages.getString("ClientFolderView.Step1ObserFactTable2"));
				break;
			default:
				column.setText(Messages.getString("ClientFolderView.Step1ObserFactTable" + i));
			break;			
			}

			//column.pack();
		}	

		for (ObservationSet oset:list)
		{
			for (ObservationType observation:oset.getObservation())
			{
				TableItem abc = new TableItem(table,SWT.NONE);

				//				abc.setText(observation.getEventId().getSource());
				if (observation.getEventId().getSource() == null)
				{
					EventId event = observation.getEventId();
					event.setSource("@");
					observation.setEventId(event);
				}
				abc.setText(0,observation.getEventId().getValue());

				if (observation.getPatientId().getSource() == null)
				{
					PatientIdType event = observation.getPatientId();
					event.setSource("@");
					observation.setPatientId(event);
				}

				abc.setText(1,observation.getPatientId().getSource());

				abc.setText(2,observation.getPatientId().getValue());
				abc.setText(3,observation.getConceptCd().getValue());
				if (observation.getObserverCd() == null)
				{
					ObserverCd observer = new ObserverCd();
					observer.setValue("@");
					observation.setObserverCd(observer);
				}
				abc.setText(4,observation.getObserverCd().getValue());
				abc.setText(5,observation.getStartDate().toXMLFormat());
				if (observation.getModifierCd() == null)
				{
					ModifierCd observer = new ModifierCd();
					observer.setValue("@");
					observation.setModifierCd(observer);
				}
				abc.setText(6,observation.getModifierCd().getValue());
				if (observation.getValuetypeCd() != null)
					abc.setText(7,observation.getValuetypeCd());
				if (observation.getTvalChar() != null)
					abc.setText(8,observation.getTvalChar());
				if (observation.getNvalNum() != null)
					abc.setText(9,observation.getNvalNum().getValue().toPlainString());
				if (observation.getValueflagCd() != null)
					abc.setText(10,observation.getValueflagCd().getValue());
				if (observation.getQuantityNum() != null)
					abc.setText(11,observation.getQuantityNum().toPlainString());
				if (observation.getUnitsCd() != null)
					abc.setText(12,observation.getUnitsCd());
				if (observation.getEndDate() != null)
					abc.setText(13,observation.getEndDate().toXMLFormat());
				if (observation.getLocationCd() != null)
					abc.setText(14,observation.getLocationCd().getValue());
				if (observation.getConfidenceNum() != null)
					abc.setText(15,observation.getConfidenceNum().toPlainString());
				if (observation.getObservationBlob() != null)
					abc.setText(16,observation.getObservationBlob().toString());
				if (observation.getUpdateDate() != null)
					abc.setText(17,observation.getUpdateDate().toXMLFormat());
				if (observation.getDownloadDate() != null)
					abc.setText(18,observation.getDownloadDate().toXMLFormat());
				if (observation.getImportDate() != null)
					abc.setText(19,observation.getImportDate().toXMLFormat());
				if (observation.getSourcesystemCd() == null)
					observation.setSourcesystemCd("@");

				abc.setText(20,observation.getSourcesystemCd());
			}
		}
		return true;
	}


	public boolean updateTableEidSet(Table table,EidSet list)
	{
		if (list == null) 
			return false;

		table.clearAll();
		table.removeAll();

		VerifyDataEventMapping vdata = new VerifyDataEventMapping();

		for(;table.getColumns().length>0;)
		{
			table.getColumns()[0].dispose();	
		}		

		for (int i = 1; i < vdata.getColumns().length-2; i++) {
			TableColumn column = new TableColumn(table, SWT.CENTER);
			column.setText(vdata.getColumns()[i]);
			column.setAlignment(SWT.LEFT);
			column.setWidth(100);
			column.setText(Messages.getString("ClientFolderView.Step1VisitTable" + i));
			//column.pack();
		}	

		//	for (PidSet pidset:list)
		//	{
		for (EidType pid:list.getEid())
		{
			for (EventMapId pmap:pid.getEventMapId())
			{

				TableItem abc = new TableItem(table,SWT.NONE);
				abc.setText(0,pmap.getValue());   //MRN NUMBER
				abc.setText(1,pmap.getSource());   //MRN NUMBER
				abc.setText(2,pid.getEventId().getValue());   //MRN NUMBER
				if (pmap.getStatus() != null)
					abc.setText(3,pmap.getStatus());  
				if (pmap.getUpdateDate() != null)
					abc.setText(4,pmap.getUpdateDate().toXMLFormat());
				if (pmap.getDownloadDate() != null)
					abc.setText(5,pmap.getDownloadDate().toXMLFormat());
				if (pmap.getImportDate() != null)
					abc.setText(6,pmap.getImportDate().toXMLFormat());
				if (pmap.getSourcesystemCd() != null)
					abc.setText(7,pmap.getSourcesystemCd());
			}
		}
		//}
		return true;
	}


	public boolean updateTablePidSet(Table table, PidSet list)
	{
		if (list == null) 
			return false;

		table.clearAll();
		table.removeAll();

		VerifyDataPatientMapping vdata = new VerifyDataPatientMapping();

		for(;table.getColumns().length>0;)
		{
			table.getColumns()[0].dispose();	
		}		

		for (int i = 1; i < vdata.getColumns().length-2; i++) {
			TableColumn column = new TableColumn(table, SWT.CENTER);
			column.setText(vdata.getColumns()[i]);
			column.setAlignment(SWT.LEFT);
			column.setWidth(100);
			column.setText(Messages.getString("ClientFolderView.Step1VisitTable" + i));
			//column.pack();
		}	

		//	for (PidSet pidset:list)
		//	{
		for (PidType pid:list.getPid())
		{
			for (PatientMapId pmap:pid.getPatientMapId())
			{

				TableItem abc = new TableItem(table,SWT.NONE);
				abc.setText(0,pmap.getValue());   //MRN NUMBER
				abc.setText(1,pmap.getSource());   //MRN NUMBER
				abc.setText(2,pid.getPatientId().getValue());   //MRN NUMBER
				if (pmap.getStatus() != null)
					abc.setText(3,pmap.getStatus());  
				if (pmap.getUpdateDate() != null)
					abc.setText(4,pmap.getUpdateDate().toXMLFormat());
				if (pmap.getDownloadDate() != null)
					abc.setText(5,pmap.getDownloadDate().toXMLFormat());
				if (pmap.getImportDate() != null)
					abc.setText(6,pmap.getImportDate().toXMLFormat());
				if (pmap.getSourcesystemCd() != null)
					abc.setText(7,pmap.getSourcesystemCd());
			}
		}
		//}
		return true;
	}

	public void doEncrypt(CryptUtil crypt,  boolean encryptMRN, boolean encryptText)
	{
		for (ObservationSet oset:patientDataType.getObservationSet())
		{
			for (ObservationType observation:oset.getObservation())
			{
				if (encryptMRN && !observation.getPatientId().getSource().toUpperCase().equals("HIVE"))
				{
					PatientIdType ptype = observation.getPatientId();
					ptype.setSource(ptype.getSource() + "_E");
					ptype.setValue(crypt.encryptPatientIde(ptype.getValue()));
					observation.setPatientId(ptype);
				}
				if (encryptMRN && !observation.getEventId().getSource().toUpperCase().equals("HIVE"))
				{
					EventId eid = observation.getEventId();
					eid.setSource(eid.getSource() + "_E");
					eid.setValue(crypt.encryptEncounterIde(eid.getValue()));
					observation.setEventId(eid);
				}
				if (encryptText)
				{
					BlobType blob = observation.getObservationBlob();
					//blob.getContent().e
				}
			}			
		}
	}


	private String generateMessageId() {
		StringWriter strWriter = new StringWriter();
		for (int i = 0; i < 20; i++) {
			int num = getValidAcsiiValue();
			// System.out.println("Generated number: " + num + " char:
			// "+(char)num);
			strWriter.append((char) num);
		}
		return strWriter.toString();
	}


	private int getValidAcsiiValue() {
		int number = 48;
		while (true) {
			number = 48 + (int) Math.round(Math.random() * 74);
			if ((number > 47 && number < 58) || (number > 64 && number < 91)
					|| (number > 96 && number < 123)) {
				break;
			}
		}
		return number;
	}

	public void createVisit()
	{
		EventSet events = new EventSet();
		for (ObservationSet oset:patientDataType.getObservationSet())
		{
			for (ObservationType observation:oset.getObservation())
			{
				if (observation.getEventId() == null)
				{
					EventType eventType = new EventType();

					eventType.setPatientId(observation.getPatientId());
					edu.harvard.i2b2.common.datavo.pdo.EventType.EventId eventid = new edu.harvard.i2b2.common.datavo.pdo.EventType.EventId();
					eventid.setSource("RND");
					eventid.setValue(generateMessageId());
					eventType.setEventId(eventid);
					eventType.setStartDate(observation.getStartDate());
					if (observation.getEndDate() != null)
						eventType.setEndDate(observation.getEndDate());
					else
						eventType.setEndDate(observation.getStartDate());
					events.getEvent().add(eventType);
					//Update observation with new data
					EventId obsEventId = new EventId();
					obsEventId.setSource(eventid.getSource());
					obsEventId.setValue(eventid.getValue());
					observation.setEventId(obsEventId);
				}
			}
		}

		patientDataType.setEventSet(events);
	}

	public void createPatient()
	{
		Hashtable patients = new Hashtable();
		for (ObservationSet oset:patientDataType.getObservationSet())
		{
			for (ObservationType observation:oset.getObservation())
			{
				if (!observation.getPatientId().getSource().toUpperCase().equals("HIVE"))
					patients.put(observation.getPatientId().getSource() + observation.getPatientId().getValue(), observation.getPatientId());
			}
		}
		Enumeration e = patients.elements();

		PidSet pidset = new PidSet();
		while( e. hasMoreElements() ){

			PidType pid = new PidType();
			pid.setPatientId((PatientId) e.nextElement());
			pidset.getPid().add(pid);
		}
		patientDataType.setPidSet(pidset);
	}


	public String toString( final String key, boolean encryptMRN, boolean encryptText, boolean createPatient, boolean createVisit) throws Exception
	{
		CryptUtil crypt;

		StringWriter strWriter = null;
		try {

			if (key != null && (encryptMRN || encryptText))
			{
				crypt = new CryptUtil(key);
				doEncrypt(crypt, encryptMRN, encryptText);
			}

			if (createPatient)
				createPatient();

			if (createVisit)
				createVisit();

			strWriter = new StringWriter();

			edu.harvard.i2b2.common.datavo.pdo.ObjectFactory pdoOf = new  edu.harvard.i2b2.common.datavo.pdo.ObjectFactory();
			FRJAXBUtil.getJAXBUtil().marshaller(pdoOf.createPatientData(patientDataType), strWriter);

		} catch (JAXBUtilException e) {
			log.error("Error marshalling PDO  text");
		} 

		return  strWriter.toString(); 
	}

}
