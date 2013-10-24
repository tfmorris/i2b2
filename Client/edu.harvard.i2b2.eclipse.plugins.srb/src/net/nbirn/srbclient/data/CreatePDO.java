package net.nbirn.srbclient.data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.Random;
import java.util.Vector;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.namespace.QName;

import net.nbirn.srbclient.utils.MD5;
import net.nbirn.srbclient.utils.Messages;
import net.nbirn.srbclient.utils.VerifyData;
import net.nbirn.srbclient.utils.VerifyDataPatientDimension;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.ProgressBar;

import edu.harvard.i2b2.common.datavo.pdo.BlobType;
import edu.harvard.i2b2.common.datavo.pdo.EidType;
import edu.harvard.i2b2.common.datavo.pdo.EventSet;
import edu.harvard.i2b2.common.datavo.pdo.EventType;
import edu.harvard.i2b2.common.datavo.pdo.ObservationSet;
import edu.harvard.i2b2.common.datavo.pdo.ObservationType;
import edu.harvard.i2b2.common.datavo.pdo.ObserverSet;
import edu.harvard.i2b2.common.datavo.pdo.ObserverType;
import edu.harvard.i2b2.common.datavo.pdo.PatientIdType;
import edu.harvard.i2b2.common.datavo.pdo.PatientSet;
import edu.harvard.i2b2.common.datavo.pdo.PatientType;
import edu.harvard.i2b2.common.datavo.pdo.PidType;
import edu.harvard.i2b2.common.datavo.pdo.EventType.EventId;
import edu.harvard.i2b2.common.datavo.pdo.PidType.PatientId;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.DTOFactory;
import edu.harvard.i2b2.crc.loader.datavo.loader.query.DataFormatType;
import edu.harvard.i2b2.crc.loader.datavo.loader.query.DataListType;
import edu.harvard.i2b2.crc.loader.datavo.loader.query.FactLoadOptionType;
import edu.harvard.i2b2.crc.loader.datavo.loader.query.InputOptionListType;
import edu.harvard.i2b2.crc.loader.datavo.loader.query.LoadOptionType;
import edu.harvard.i2b2.crc.loader.datavo.loader.query.LoadType;
import edu.harvard.i2b2.crc.loader.datavo.loader.query.OutputOptionListType;
import edu.harvard.i2b2.crc.loader.datavo.loader.query.OutputOptionType;
import edu.harvard.i2b2.crc.loader.datavo.loader.query.PublishDataRequestType;
import edu.harvard.i2b2.crc.loader.datavo.i2b2message.StatusType;
import edu.harvard.i2b2.eclipse.UserInfoBean;
import edu.harvard.i2b2.eclipse.plugins.fr.ws.CrcServiceDriver;
import edu.harvard.i2b2.eclipse.plugins.fr.ws.FrServiceDriver;
import edu.harvard.i2b2.eclipse.plugins.fr.ws.GetPublishDataResponseMessage;
import edu.harvard.i2b2.eclipse.plugins.fr.ws.SendfileResponseMessage;
import edu.harvard.i2b2.fr.datavo.fr.query.SendfileRequestType;
import edu.harvard.i2b2.frclient.security.CryptUtil;

public class CreatePDO {

	private  VerifyData vData = null;
	private 		ProcessThread thread  = null;

	Display display = null;
	public CreatePDO()
	{
	}



	public void process(final Hashtable file, Button startButton, final ProgressBar pbar, final Label uploadStatus, final DirWorker dir, final String key,
			int numberPerBatch, boolean eMRN, boolean eText, boolean eFile, boolean createPatient, boolean createVisit) throws Exception
			{



		//String results = null;

		display = startButton.getDisplay();
		thread = new ProcessThread(file,startButton, pbar,uploadStatus,dir,key,numberPerBatch,eMRN,eText,eFile, createPatient, createVisit);
		thread.start();

		//startButton.setEnabled(true);
		/*
		pbar.setSelection(0);
		createXMLFile(file, key);
		pbar.setSelection(20);

		uploadXMLFile();
		pbar.setSelection(80);

		updatePublishDataRequest();
		pbar.setSelection(100);
		 */
			}
	class ProcessThread extends Thread {

		private boolean done = false;
		private DirWorker theFtp = null;
		private Hashtable file = null;
		private ProgressBar pbar = null;
		private String key = null;


		private CryptUtil crypt;
		private  final Log log = LogFactory.getLog(CreatePDO.class);
		private DTOFactory dtoFactory = new DTOFactory();
		private File f = null;
		private DirWorker dirWorker;
		private boolean encryptMRN= false;
		private boolean encryptText = false;
		private boolean encryptFile = false;
		private Label uploadStatus = null;
		private Button startButton = null;
		private int numberPerBatch = -1;
		private boolean createPatient = false;
		private  boolean createVisit = false;
		private CSVFileReader reader  = null;

		//	private String workingOn = null;

		private int patientCount = 0;

		public ProcessThread(
				final Hashtable file, Button sButton, final ProgressBar pbar, final Label uStatus, final DirWorker dir, final String key,
				final int numPerBatch, boolean eMRN, boolean eText, boolean eFile, boolean cPatient, boolean cVisit)
		{
			if (key != null)
			{
				crypt = new CryptUtil(key);
				encryptMRN= eMRN;
				encryptText = eText;
				encryptFile = eFile;
			}
			numberPerBatch = numPerBatch;

			dirWorker = dir;//display;
			startButton  = sButton;
			uploadStatus = uStatus;
			createPatient = cPatient;
			createVisit = cVisit;

			this.file = file;
			this.pbar = pbar;
			this.key = key;
		}



		public void run()
		{

			try {
				boolean finished = false;
				do {
					display.asyncExec(new Runnable() {
						public void run() {
							if (pbar.isDisposed()) return;
							startButton.setEnabled(false);
							uploadStatus.setText("Creating PDO File");
							// Increment the progress bar
							pbar.setSelection(10);
						}
					});


					//pbar.setSelection(0);
					finished = createXMLFile(file, key);
					display.asyncExec(new Runnable() {
						public void run() {
							if (pbar.isDisposed()) return;
							// Increment the progress bar
							uploadStatus.setText("Calling File Repository Service");

							pbar.setSelection(20);
						}
					});
					//pbar.setSelection(20);

					final String status = uploadXMLFile();
					//pbar.setSelection(80);

					display.asyncExec(new Runnable() {
						public void run() {
							if (pbar.isDisposed()) return;
							// Increment the progress bar
							uploadStatus.setText("Calling CRC UploadService");
							pbar.setSelection(80);
						}
					});
					if (status != null)
						throw new Exception (status);

					updatePublishDataRequest();
					//pbar.setSelection(100);
					display.asyncExec(new Runnable() {
						public void run() {
							if (pbar.isDisposed()) return;
							// Increment the progress bar
							uploadStatus.setText("Finished");
							pbar.setSelection(100);
							startButton.setEnabled(true);
						}
					});
				} while (finished == false); 
			} catch (final Exception e)
			{
				display.asyncExec(new Runnable() {
					public void run() {
						if (pbar.isDisposed()) return;
						// Increment the progress bar
						uploadStatus.setText(e.getMessage());
						pbar.setSelection(100);
						startButton.setEnabled(true);
					}
				});
			}
		}

		/*
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
		*/


		private boolean createXMLFile(Hashtable file, String key) 
		{

			int fileCount = 0;
			BufferedWriter out = null;
			try {

				if (file.containsKey("PDO")) {
					f = (File) file.get("PDO");
					return true;
				}
				JAXBContext jaxbContext = JAXBContext.newInstance(ObservationSet.class);

				// Create temp file.
				//f = File.createTempFile("i2b2FR_PDO", ".tmp");
				int rand = (new Random()).nextInt();
				f = new File("temp");
				f.mkdir();

				f = new File("temp", "i2b2FR_PDO" + rand+ ".tmp");

				//f.mkdir();
				out = new BufferedWriter(new
						FileWriter(f, false)); //"i2b2FR_PDO" + rand+ ".tmp",true));

				// Delete temp file when program exits.
				//f.deleteOnExit();

				Marshaller marshaller = jaxbContext.createMarshaller();
				marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT , true);

				//out = new BufferedWriter(new FileWriter(f));
				out.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n");
				out.write("<ns2:patient_data xmlns:ns2=\"http://www.i2b2.org/xsd/hive/pdo/1.1/\">\n");

				patientCount = 0;
				if (file.containsKey("PTM")) // ((file.containsKey("PTM")) && (workingOn == null) || (file.containsKey("PTM")) && (workingOn.equals("PTM")))    
				{
					//workingOn = "PTM";
					BatchWorker batchworker = (BatchWorker) file.get("PTM");
					if (reader == null) 
					{
						vData = batchworker.getVerifyData();

						reader = new CSVFileReader(batchworker.getFilename(), batchworker.getDelimiter(),
								batchworker.getTextQualifer(), batchworker.getEOLQualifer());						
					}

					Vector<String> csv;

					out.write("<ns2:pid_set>\n"); // xmlns:ns2=\"http://www.i2b2.org/xsd/hive/pdo/1.1/\">\n");
					PidType vType = null;
					int count = 0;

					while ((csv=reader.readFields(batchworker.getStartAt())) != null)
					{

						if (csv.size() < 1)
							continue;

						if (batchworker.getStartAt() <= count++)
						{
							vType = buildPidSet(csv, key);

							StringWriter sw = new StringWriter();

							marshaller.marshal(
									new JAXBElement(  new QName("","pid"),PidType.class, vType),sw);
							String output = sw.toString();
							output = output.substring(output.indexOf('\n',3)+1);
							out.write(output);

							patientCount++;

							sw.close();
							sw = null;
							if (patientCount % numberPerBatch == 0)
							{
								display.asyncExec(new Runnable() {
									public void run() {
										if (pbar.isDisposed()) return;
										// Increment the progress bar
										uploadStatus.setText("Create Patient Map XML, Number of patients done: " + patientCount);
									}
								});

								out.write("</ns2:pid_set>\n");
								out.write("</ns2:patient_data>\n");

								out.flush();
								out.close();

								log.debug(patientCount);								
								return false;
							}
						}
					}
					out.write("</ns2:pid_set>\n");
					reader.close();
					reader = null;
				}
				patientCount = 0;
				if  (file.containsKey("OBS")) //((file.containsKey("OBS")) && (workingOn == null) || (file.containsKey("OBS")) && (workingOn.equals("OBS"))) {
				{
					//	workingOn = "OBS";
					BatchWorker batchworker = (BatchWorker) file.get("OBS");

					if (reader == null) 
					{
						// Each Observation needs a event set associated with it
						vData = batchworker.getVerifyData();
						reader = new CSVFileReader(batchworker.getFilename(), batchworker.getDelimiter(),
								batchworker.getTextQualifer(), batchworker.getEOLQualifer());
					}



					//java.util.List<ObservationSet> oset = new java.util.ArrayList<ObservationSet>();

					Vector<String> csv;

					Hashtable<String, ObservationType> upload = new Hashtable<String, ObservationType> ();
					String text = null;//reader.readLine();

					ObservationType oType = null;
					EventType eType = null;
					EventType.EventId vType = null;
					EidType encounterType =null;

					edu.harvard.i2b2.common.datavo.pdo.EidType.EventId pid = null;
					
					// When saving the file, need to save the observation first and than the events
					StringBuffer processObs = new StringBuffer();
					StringBuffer processEvent = new StringBuffer();
					StringBuffer processEid = new StringBuffer();


					int count = 0;
					processObs.append("<ns2:observation_set>\n"); // xmlns:ns2=\"http://www.i2b2.org/xsd/hive/pdo/1.1/\">\n");
					processEvent.append("<ns2:event_set>\n");
					processEid.append("<ns2:eid_set>\n");
					while ((csv=reader.readFields(batchworker.getStartAt())) != null)
					{
						if (csv.size() < 1)
							continue;

						if (batchworker.getStartAt() <= count++)
						{
							try {
								oType = buildObservationSet(csv, key);

								// Process Observations
								if ((oType.getTvalChar() != null) && (oType.getTvalChar().startsWith("srb:")))
								{
									fileCount++;
									upload.put(csv.elementAt(vData.getDataLocation("FILE")),oType);
								}
								//	ObservationType oType = buildObservationSet(csv);
								StringWriter sw = new StringWriter();
								//marshaller.marshal(otypeSet, sw);//new FileWriter("MikeTest.xml"));

								marshaller.marshal(
										new JAXBElement(  new QName("","observation"),ObservationType.class, oType),sw);

								String output = sw.toString();
								output = output.substring(output.indexOf('\n',3)+1);
								processObs.append(output);


								if (createVisit){
								//Process the Event Id								
								eType = new EventType();

								vType = new EventType.EventId();
								vType.setSource(oType.getEventId().getSource());//oType.getEventId().getSource());
								vType.setValue(oType.getEventId().getValue());

								if ((vData.getDataLocation("UPDA") != -1) && (!csv.elementAt(vData.getDataLocation("UPDA")).equals("")) 
										&& 	(vData.getDate(csv.elementAt(vData.getDataLocation("UPDA"))) != null)) { 
									eType.setUpdateDate(dtoFactory.getXMLGregorianCalendar(
											vData.getDate(csv.elementAt(vData.getDataLocation("UPDA")).trim()).getTime()));
								}

								if ((vData.getDataLocation("DOWN") != -1) && (!csv.elementAt(vData.getDataLocation("DOWN")).equals(""))
										&& 	(vData.getDate(csv.elementAt(vData.getDataLocation("DOWN"))) != null)) { 
									eType.setDownloadDate(dtoFactory.getXMLGregorianCalendar(
											vData.getDate(csv.elementAt(vData.getDataLocation("DOWN")).trim()).getTime()));
								}

								if ((vData.getDataLocation("IMPO") != -1) && (!csv.elementAt(vData.getDataLocation("IMPO")).equals(""))
										&& 	(vData.getDate(csv.elementAt(vData.getDataLocation("IMPO"))) != null)) { 
									eType.setImportDate(dtoFactory.getXMLGregorianCalendar(
											vData.getDate(csv.elementAt(vData.getDataLocation("IMPO")).trim()).getTime()));
								}

								if ((vData.getDataLocation("SOUR") != -1) && (!csv.elementAt(vData.getDataLocation("SOUR")).equals(""))) {
									eType.setSourcesystemCd(csv.elementAt(vData.getDataLocation("SOUR")).trim());
								}	
								
								eType.setEventId(vType);
								eType.setPatientId(oType.getPatientId());
								eType.setStartDate(oType.getStartDate());
								
								sw = new StringWriter();

								marshaller.marshal(
										new JAXBElement(  new QName("","event"),EventType.class, eType),sw);


								output = sw.toString();
								output = output.substring(output.indexOf('\n',3)+1);
								processEvent.append(output);	

								//Now create the EID

								pid = new edu.harvard.i2b2.common.datavo.pdo.EidType.EventId ();

								encounterType = new EidType();

								pid.setSource(oType.getEventId().getSource());//oType.getEventId().getSource());
								pid.setValue(oType.getEventId().getValue());
								pid.setPatientId(oType.getPatientId().getValue());
								pid.setPatientIdSource(oType.getPatientId().getSource());

									if ((vData.getDataLocation("UPDA") != -1) && (!csv.elementAt(vData.getDataLocation("UPDA")).equals("")) 
											&& 	(vData.getDate(csv.elementAt(vData.getDataLocation("UPDA"))) != null)) { 
										pid.setUpdateDate(dtoFactory.getXMLGregorianCalendar(
												vData.getDate(csv.elementAt(vData.getDataLocation("UPDA")).trim()).getTime()));
									}

									if ((vData.getDataLocation("DOWN") != -1) && (!csv.elementAt(vData.getDataLocation("DOWN")).equals(""))
											&& 	(vData.getDate(csv.elementAt(vData.getDataLocation("DOWN"))) != null)) { 
										pid.setDownloadDate(dtoFactory.getXMLGregorianCalendar(
												vData.getDate(csv.elementAt(vData.getDataLocation("DOWN")).trim()).getTime()));
									}

									if ((vData.getDataLocation("IMPO") != -1) && (!csv.elementAt(vData.getDataLocation("IMPO")).equals(""))
											&& 	(vData.getDate(csv.elementAt(vData.getDataLocation("IMPO"))) != null)) { 
										pid.setImportDate(dtoFactory.getXMLGregorianCalendar(
												vData.getDate(csv.elementAt(vData.getDataLocation("IMPO")).trim()).getTime()));
									}

									if ((vData.getDataLocation("SOUR") != -1) && (!csv.elementAt(vData.getDataLocation("SOUR")).equals(""))) {
										pid.setSourcesystemCd(csv.elementAt(vData.getDataLocation("SOUR")).trim());
									}	
									encounterType.setEventId(pid);
									sw = new StringWriter();

									marshaller.marshal(
											new JAXBElement(  new QName("","eid"),EidType.class, encounterType),sw);


									output = sw.toString();
									output = output.substring(output.indexOf('\n',3)+1);
									processEid.append(output);	

								}
								

								//Finished both Observations and Events
								patientCount++;
								if (patientCount % numberPerBatch == 0)
								{
									display.asyncExec(new Runnable() {
										public void run() {
											if (pbar.isDisposed()) return;
											// Increment the progress bar
											uploadStatus.setText("Create Observation XML, Number of patients done: " + patientCount);
										}
									});

									log.debug(patientCount);

									processObs.append("</ns2:observation_set>\n");
									processEvent.append("</ns2:event_set>\n");
									processEid.append("</ns2:eid_set>\n");
									out.write(processObs.toString());
									if (createVisit){
									 out.write(processEvent.toString());
									 out.write(processEid.toString());
									}
									out.write("</ns2:patient_data>\n");
									out.flush();
									out.close();

									return false;
									//out.flush();
								}
								sw.close();
							} catch(Exception ee)
							{
								ee.printStackTrace();
								log.error("Error with line: " + csv);
								break;
							}
						}
					}
					processObs.append("</ns2:observation_set>\n");
					processEvent.append("</ns2:event_set>\n");
					processEid.append("</ns2:eid_set>\n");
					out.write(processObs.toString());
					if (createVisit){
						 out.write(processEvent.toString());
						 out.write(processEid.toString());
						}
					out.flush();
					//out.close();
					reader.close();
					reader = null;
				}
				patientCount = 0;

				// Deal with EID
				if  ((file.containsKey("ETM")))//(((file.containsKey("OBS") ||  (file.containsKey("ETM"))) && (workingOn == null))
					//						|| (file.containsKey("OBS") ||  (file.containsKey("ETM"))) && (workingOn.equals("ETM")))    

				{

					//				workingOn = "ETM";
					BatchWorker batchworker =null;

					if (!file.containsKey("ETM")) 
						batchworker = (BatchWorker) file.get("OBS");
					else
						batchworker = (BatchWorker) file.get("ETM");

					if (reader == null) 
					{
						vData = batchworker.getVerifyData();
						reader = new CSVFileReader(batchworker.getFilename(), batchworker.getDelimiter(), batchworker.getTextQualifer(), batchworker.getEOLQualifer());
					}

					Vector<String> csv;

					out.write("<ns2:eid_set>\n"); // xmlns:ns2=\"http://www.i2b2.org/xsd/hive/pdo/1.1/\">\n");
					EidType vType = null;
					int count = 0;

					while ((csv=reader.readFields(batchworker.getStartAt())) != null)
					{

						if (csv.size() < 1)
						{
							continue;
						}

						if (batchworker.getStartAt() <= count++)
						{
							vType = buildEidSet(csv, key);

							StringWriter sw = new StringWriter();

							marshaller.marshal(
									new JAXBElement(  new QName("","eid"),EidType.class, vType),sw);
							String output = sw.toString();
							output = output.substring(output.indexOf('\n',3)+1);
							out.write(output);

							patientCount++;
							if (patientCount % numberPerBatch == 0)
							{
								display.asyncExec(new Runnable() {
									public void run() {
										if (pbar.isDisposed()) return;
										// Increment the progress bar
										uploadStatus.setText("Create Event Mapping XML, Number of patients done: " + patientCount);
									}
								});

								log.debug(patientCount);
								out.write("</ns2:eid_set>\n");
								out.write("</ns2:patient_data>\n");

								out.flush();
								out.close();
								return false;
							}

							sw.close();
							sw = null;
						}
					}
					out.write("</ns2:eid_set>\n");
					out.flush();
					reader.close();
					reader = null;
				}




				/* Done in Observations Now
		if (file.containsKey("OBS")) 
				{
					// Make a unique encounter based on the observation

					BatchWorker batchworker = (BatchWorker) file.get("OBS");
					vData = batchworker.getVerifyData();

					//BufferedReader reader = new BufferedReader (new FileReader (batchworker.getFilename()));
					reader = new CSVFileReader(batchworker.getFilename(), batchworker.getDelimiter(), batchworker.getTextQualifer(), batchworker.getEOLQualifer());

					int count = 0;
					boolean header = false;
					ObservationType oType = null;
					EventType eType = null;
					EventType.EventId vType = null;
					out.write("<ns2:event_set>\n"); // xmlns:ns2=\"http://www.i2b2.org/xsd/hive/pdo/1.1/\">\n");
					Vector<String> csv = reader.readFields(batchworker.getStartAt());
					while(csv!=null)
					{

						if (csv.size() < 1)
						{
							continue;
						}

						//					if (csv.size() < 1)
						//					{
						//					csv = reader.readFields(batchworker.getStartAt());
						//				continue;
						//		}

						if (batchworker.getStartAt() <= count++)
						{
							try {
								oType = buildObservationSet(csv, key);

								//EventSet vTypeSet = new EventSet();
								//ProviderDimensionType otypeSet = new ProviderDimensionType();
								eType = new EventType();

								vType = new EventType.EventId();
								vType.setSource(oType.getEventId().getSource());//oType.getEventId().getSource());
								vType.setValue(oType.getEventId().getValue());


								eType.setEventId(vType);
								eType.setPatientId(oType.getPatientId());
								eType.setStartDate(oType.getStartDate());
								//vTypeSet.getEvent().add(eType);

								//vset.add(vTypeSet);

								//	ObservationType oType = buildObservationSet(csv);
								StringWriter sw = new StringWriter();
								//marshaller.marshal(vTypeSet, sw);//new FileWriter("MikeTest.xml"));

								marshaller.marshal(
										new JAXBElement(  new QName("","event"),EventType.class, eType),sw);


								String output = sw.toString();
								output = output.substring(output.indexOf('\n',3)+1);
								out.write(output);					

								patientCount++;
								if (patientCount % 1000 == 0)
								{
									out.flush();
								}
								sw.close();
							} catch (Exception ee)
							{
								uploadStatus.setText("Create Event XML, Number of patients done: " + patientCount);

								log.error("Error with line:" + patientCount + " - "  + ee.getMessage());
								break;

							}
							csv = reader.readFields(batchworker.getStartAt());
						}

					}
					out.write("</ns2:event_set>\n");
				}
				 */

				/* TODO After 1.4 release
				if (file.containsKey("PAT")) {
					//TODO FIX

					BufferedReader reader = new BufferedReader (new FileReader ((String) file.get("PAT")));
					java.util.List<PatientSet> vset = new java.util.ArrayList<PatientSet>();
					boolean header = false;

					//ArrayList<ObservationType> upload = new ArrayList<ObservationType>();
					//Hashtable<String, ProviderDimensionType> upload = new Hashtable<String, ProviderDimensionType> ();
					String text =reader.readLine();
					text =reader.readLine();
					vData = new VerifyDataPatientDimension();

					while ((text=reader.readLine()) != null)
					{
						if (text.trim().length() < 1)
							continue;
						String[] csv = text.split(",");
						if (csv.length < 2)
						{
							csv = text.split("\\t");
						}
						if (csv.length < 2)
						{
							continue;
						}

						if (header == false)
						{
							for (int i=0; i < csv.length; i++)				
							{
								vData.setHeaderLocation(csv[i], i);
							}
							header = true;
						} else {

							PatientSet vTypeSet = new PatientSet();
							//ProviderDimensionType otypeSet = new ProviderDimensionType();
							PatientType vType = buildPatientSet(csv, key);

							vTypeSet.getPatient().add(vType);

							vset.add(vTypeSet);


							//	ObservationType oType = buildObservationSet(csv);
							StringWriter sw = new StringWriter();
							marshaller.marshal(vTypeSet, sw);//new FileWriter("MikeTest.xml"));

							String output = sw.toString();
							out.write(output.substring(output.indexOf('>')+1));
							patientCount++;
							if (patientCount % 1000 == 0)
							{
								uploadStatus.setText("Create Patient XML, Number of patients done: " + patientCount);
								out.flush();
							}
							sw.close();
						}
					}
				}
				else if (file.containsKey("PTM")) {
					// Make a unique patient ID from the PTM, otherwise observations will not be laoded
					BufferedReader reader = new BufferedReader (new FileReader ((String) file.get("PTM")));
					java.util.List<PatientSet> vset = new java.util.ArrayList<PatientSet>();

					//ArrayList<ObservationType> upload = new ArrayList<ObservationType>();
					//Hashtable<String, ProviderDimensionType> upload = new Hashtable<String, ProviderDimensionType> ();
					String text =reader.readLine();
					text =reader.readLine();

					while ((text=reader.readLine()) != null)
					{
						if (text.trim().length() < 1)
							continue;
						String[] csv = text.split(",");
						if (csv.length < 2)
						{
							csv = text.split("\\t");
						}
						if (csv.length < 2)
						{
							continue;
						}

						PatientSet vTypeSet = new PatientSet();
						//ProviderDimensionType otypeSet = new ProviderDimensionType();
						PatientType vType = builPatientSet(csv, key);

						vTypeSet.getPatient().add(vType);

						vset.add(vTypeSet);


						//	ObservationType oType = buildObservationSet(csv);
						StringWriter sw = new StringWriter();
						marshaller.marshal(vTypeSet, sw);//new FileWriter("MikeTest.xml"));

						String output = sw.toString();
						out.write(output.substring(output.indexOf('>')+1));
						patientCount++;
						if (patientCount % 1000 == 0)
						{
							out.flush();
						}
						sw.close();
					}

				}


				if (file.containsKey("PRO")) {
					//TODO FIX

					BufferedReader reader = new BufferedReader (new FileReader ((String) file.get("PRO")));
					java.util.List<ObserverSet> pset = new java.util.ArrayList<ObserverSet>();

					//ArrayList<ObservationType> upload = new ArrayList<ObservationType>();
					//Hashtable<String, ProviderDimensionType> upload = new Hashtable<String, ProviderDimensionType> ();
					String text =reader.readLine();
					text =reader.readLine();
					while ((text=reader.readLine()) != null)
					{
						if (text.trim().length() < 1)
							continue;
						String[] csv = text.split(",");
						if (csv.length < 1)
						{
							csv = text.split("\\t");
						}
						if (csv.length < 1)
						{
							continue;
						}

						ObserverSet pTypeSet = new ObserverSet();
						//ProviderDimensionType otypeSet = new ProviderDimensionType();
						ObserverType pType = buildProviderObserverSet(csv, key);

						pTypeSet.getObserver().add(pType);

						pset.add(pTypeSet);


						//	ObservationType oType = buildObservationSet(csv);
						StringWriter sw = new StringWriter();
						marshaller.marshal(pTypeSet, sw);//new FileWriter("MikeTest.xml"));

						String output = sw.toString();
						out.write(output.substring(output.indexOf('>')+1));
						patientCount++;
						if (patientCount % 1000 == 0)
						{
							out.flush();
						}
						sw.close();

					}
				}			
				if (file.containsKey("VIS")) {
					//TODO FIX
					BufferedReader reader = new BufferedReader (new FileReader ((String) file.get("VIS")));
					java.util.List<EventSet> vset = new java.util.ArrayList<EventSet>();

					//ArrayList<ObservationType> upload = new ArrayList<ObservationType>();
					//Hashtable<String, ProviderDimensionType> upload = new Hashtable<String, ProviderDimensionType> ();
					String text =reader.readLine();
					text =reader.readLine();
					while ((text=reader.readLine()) != null)
					{
						if (text.trim().length() < 1)
							continue;
						String[] csv = text.split(",");
						if (csv.length < 2)
						{
							csv = text.split("\\t");
						}
						if (csv.length < 2)
						{
							continue;
						}

						EventSet vTypeSet = new EventSet();
						//ProviderDimensionType otypeSet = new ProviderDimensionType();
						EventType vType = buildEventSet(csv, key);

						vTypeSet.getEvent().add(vType);

						vset.add(vTypeSet);


						//	ObservationType oType = buildObservationSet(csv);
						StringWriter sw = new StringWriter();
						marshaller.marshal(vTypeSet, sw);//new FileWriter("MikeTest.xml"));

						String output = sw.toString();
						out.write(output.substring(output.indexOf('>')+1));
						patientCount++;
						if (patientCount % 1000 == 0)
						{
							out.flush();
						}
						sw.close();

					}
				} 
				 */

				out.write("</ns2:patient_data>\n");

				out.flush();
				out.close();

			} catch (Exception e)
			{
				log.error(e.getMessage());
				e.printStackTrace();
			} finally
			{ 
				try{

					out.flush();
					out.close(); }
				catch (Exception ee) {}
			}
			//return  new int[] {fileCount, patientCount};
			return true;
		}



		public void updatePublishDataRequest()  //final Display theDisplay, final TreeViewer theViewer) 
		{
			try {
				PublishDataRequestType parentType = new PublishDataRequestType();

				InputOptionListType ioType = new InputOptionListType();
				LoadType loadType = new LoadType();
				DataListType dlType = new DataListType();

				loadType.setClearTempLoadTables(false);
				loadType.setCommitFlag(true);
				DataListType.LocationUri uri = new DataListType.LocationUri();
				uri.setValue(UserInfoBean.getInstance().getCellDataParam("FRC", "DestDir")
						+ UserInfoBean.getInstance().getCellDataParam("FRC", "PathSeparator")
						+ UserInfoBean.getInstance().getProjectId()
						+ UserInfoBean.getInstance().getCellDataParam("FRC", "PathSeparator")
						+ f.getName()
				);
				uri.setProtocolName(UserInfoBean.getInstance().getSelectedProjectParam("FRMethod"));
				if ((UserInfoBean.getInstance().getSelectedProjectParam("FRMethod") == null) || (uri.getProtocolName().equals("FR")))
				{
					uri.setProtocolName("FR"); //UserInfoBean.getInstance().getSelectedProjectParam("FRMethod"));
					uri.setValue(f.getName());
				}
				dlType.setLocationUri(uri);
				dlType.setDataFormatType(DataFormatType.PDO);
				dlType.setSourceSystemCd("i2b2");
				dlType.setLoadLabel(UserInfoBean.getInstance().getProjectId() + "-" + f.getName());
				ioType.setDataFile(dlType);

				LoadOptionType loType = new LoadOptionType();
				loType.setEncryptBlob(false);
				loType.setIgnoreBadData(true);

				FactLoadOptionType floType = new FactLoadOptionType();
				floType.setAppendFlag(true);
				loadType.setLoadPidSet(floType);
				loadType.setLoadObservationSet(floType);
				loadType.setLoadEidSet(floType);
				loadType.setLoadEventSet(floType);
				//loadType.setLoadEventidSet(loType);

				OutputOptionListType ooType = new OutputOptionListType();
				ooType.setDetail(true);
				OutputOptionType outputType = new OutputOptionType();
				outputType.setOnlykeys(true);
				ooType.setObservationSet(outputType);
				ooType.setPidSet(outputType);			
				ooType.setEventSet(outputType);
				ooType.setEidSet(outputType);	

				parentType.setInputList(ioType);
				parentType.setLoadList(loadType);
				parentType.setOutputList(ooType);

				GetPublishDataResponseMessage msg = new GetPublishDataResponseMessage();
				StatusType procStatus = null;	
				while(procStatus == null || !procStatus.getType().equals("DONE")){
					String response = CrcServiceDriver.getPublishDataRequest(parentType, "CRC");

					procStatus = msg.processResult(response);
					if (procStatus.getType().equals("ERROR")){		
						/*
					System.setProperty("errorMessage",  procStatus.getValue());				
					theDisplay.syncExec(new Runnable() {
						public void run() {

							MessageBox mBox = new MessageBox(theDisplay.getActiveShell(), SWT.ICON_INFORMATION | SWT.OK);
							mBox.setText("Please Note ...");
							mBox.setMessage("Server reports: " +  System.getProperty("errorMessage"));
							mBox.open();

						}
					});
						 */
						throw new I2B2Exception (procStatus.getValue());
					}			
				}
				/*TODO
			LoadDataResponseType allConcepts = msg.doReadLoad();   	  
			if (allConcepts != null){
				//String fileLocUri = allConcepts..getDataFileLocationUri();
				edu.harvard.i2b2.crc.loader.datavo.loader.query.StatusType status = allConcepts.getStatus();
				//String fileLocUri = allConcepts.getDataFileLocationUri();
				//.getConcept();
				//getChildren().clear();
				//getNodesFromXMLString(concepts);
			}	*/

			} catch (AxisFault e) {
				log.error(e.getMessage());
				//throw new I2B2Exception(  Messages.getString("CreatePDO.AxisFaultError"));
			} catch (Exception e) {
				log.error(e.getMessage());
				//	throw new I2B2Exception(  Messages.getString("CreatePDO.ExceptionError"));
			}

		}


		public String uploadXMLFile() //final Display theDisplay, final TreeViewer theViewer) 
		{
			try {




				SendfileRequestType parentType = new SendfileRequestType();

				InputOptionListType ioType = new InputOptionListType();
				LoadType loadType = new LoadType();
				DataListType dlType = new DataListType();

				DataListType.LocationUri uri = new DataListType.LocationUri();


				edu.harvard.i2b2.fr.datavo.fr.query.File file = new edu.harvard.i2b2.fr.datavo.fr.query.File();

				file.setAlgorithm("MD5");
				file.setHash(MD5.asHex(MD5.getHash(f)));
				file.setName(f.getName());
				GregorianCalendar cal = new GregorianCalendar();
				cal.setTimeInMillis(f.lastModified());
				file.setDate(DatatypeFactory.newInstance().newXMLGregorianCalendar(cal));
				file.setOverwrite("true");
				file.setSize(BigInteger.valueOf(f.length()));
				parentType.setUploadFile(file);


				//parentType..setMax(Integer.parseInt(System.getProperty("OntMax")));
				//parentType.setHiddens(Boolean.parseBoolean(System.getProperty("OntHiddens")));
				//parentType.setSynonyms(Boolean.parseBoolean(System.getProperty("OntSynonyms")));

				//parentType.setParent(this.getData().getKey());		

				SendfileResponseMessage msg = new SendfileResponseMessage();
				StatusType procStatus = null;	
				//String[] attachments = new String[] {f.getAbsolutePath()};
				while(procStatus == null || !procStatus.getType().equals("DONE")){


					String response = FrServiceDriver.getSendfileRequest( new String[]{f.getAbsolutePath()}, parentType, "CRC");

					//return null;
					//TODO FIX

					procStatus = msg.processResult(response);
					if (procStatus.getType().equals("ERROR")){		
						return procStatus.getValue();				
					}	
				}
				/*TODO
			LoadDataResponseType allConcepts = msg.doReadLoad();   	  
			if (allConcepts != null){
				//String fileLocUri = allConcepts..getDataFileLocationUri();
				edu.harvard.i2b2.crc.loader.datavo.loader.query.StatusType status = allConcepts.getStatus();
				//String fileLocUri = allConcepts.getDataFileLocationUri();
				//.getConcept();
				//getChildren().clear();
				//getNodesFromXMLString(concepts);
			}	*/

			} catch (Exception e) {
				log.error(e.getMessage());
				return  Messages.getString("CreatePDO.AxisFaultError");
			}
			return null;

		}

		/*
	private void uploadXMLFile( ) //DirWorker dirWorker)
	//Upload File
	{

		d

		String origDir = dirWorker.getCurrentDir();
		dirWorker.changeDir("/tmp"); //dirWorker.getHomeDirectory());
		dirWorker.createDir("FRPDO");
		dirWorker.changeDir("FRPDO");
		dirWorker.uploadFile(f);
		dirWorker.changeDir(origDir);
	}
		 */

		public PidType buildPidSet(Vector<String> csv, String key) throws ParseException
		{
			PidType patientType = new PidType();


			if ((vData.getDataLocation("PNUM") != -1) && (!csv.elementAt(vData.getDataLocation("PNUM")).equals(""))) {
				if ((key == null) || (encryptMRN == false))
				{
					PidType.PatientMapId pid = new PidType.PatientMapId();
					pid.setValue(csv.elementAt(vData.getDataLocation("PIDE")).trim());
					pid.setSource(csv.elementAt(vData.getDataLocation("PISE")).trim());
					if ((vData.getDataLocation("PISS") != -1))
						pid.setStatus(csv.elementAt(vData.getDataLocation("PISS")).trim());			
					if ((vData.getDataLocation("UPDA") != -1) && (!csv.elementAt(vData.getDataLocation("UPDA")).equals(""))
							&& 	(vData.getDate(csv.elementAt(vData.getDataLocation("UPDA"))) != null)) { 
						pid.setUpdateDate(dtoFactory.getXMLGregorianCalendar(
								vData.getDate(csv.elementAt(vData.getDataLocation("UPDA")).trim()).getTime()));
					}

					if ((vData.getDataLocation("DOWN") != -1) && (!csv.elementAt(vData.getDataLocation("DOWN")).equals(""))
							&& 	(vData.getDate(csv.elementAt(vData.getDataLocation("DOWN"))) != null)) { 
						pid.setDownloadDate(dtoFactory.getXMLGregorianCalendar(
								vData.getDate(csv.elementAt(vData.getDataLocation("DOWN")).trim()).getTime()));
					}

					if ((vData.getDataLocation("IMPO") != -1) && (!csv.elementAt(vData.getDataLocation("IMPO")).equals(""))
							&& 	(vData.getDate(csv.elementAt(vData.getDataLocation("IMPO"))) != null)) { 
						pid.setImportDate(dtoFactory.getXMLGregorianCalendar(
								vData.getDate(csv.elementAt(vData.getDataLocation("IMPO")).trim()).getTime()));
					}

					if ((vData.getDataLocation("SOUR") != -1) && (!csv.elementAt(vData.getDataLocation("SOUR")).equals(""))) { 
						pid.setSourcesystemCd(csv.elementAt(vData.getDataLocation("SOUR")).trim());
					}								
					patientType.getPatientMapId().add(pid);
				}
				else
				{
					PidType.PatientMapId pid = new PidType.PatientMapId();
					pid.setValue(crypt.encryptPatientIde(csv.elementAt(vData.getDataLocation("PIDE")).trim()));
					pid.setSource(csv.elementAt(vData.getDataLocation("PISE")).trim() + "_E");
					if ((vData.getDataLocation("PISS") != -1))
						pid.setStatus(csv.elementAt(vData.getDataLocation("PISS")).trim());		
					if ((vData.getDataLocation("UPDA") != -1) && (!csv.elementAt(vData.getDataLocation("UPDA")).equals(""))
							&& 	(vData.getDate(csv.elementAt(vData.getDataLocation("UPDA"))) != null)) { 
						pid.setUpdateDate(dtoFactory.getXMLGregorianCalendar(
								vData.getDate(csv.elementAt(vData.getDataLocation("UPDA")).trim()).getTime()));
					}

					if ((vData.getDataLocation("DOWN") != -1) && (!csv.elementAt(vData.getDataLocation("DOWN")).equals(""))
							&& 	(vData.getDate(csv.elementAt(vData.getDataLocation("DOWN"))) != null)) { 
						pid.setDownloadDate(dtoFactory.getXMLGregorianCalendar(
								vData.getDate(csv.elementAt(vData.getDataLocation("DOWN")).trim()).getTime()));
					}

					if ((vData.getDataLocation("IMPO") != -1) && (!csv.elementAt(vData.getDataLocation("IMPO")).equals(""))) { 
						pid.setImportDate(dtoFactory.getXMLGregorianCalendar(
								vData.getDate(csv.elementAt(vData.getDataLocation("IMPO")).trim()).getTime()));
					}

					if ((vData.getDataLocation("SOUR") != -1) && (!csv.elementAt(vData.getDataLocation("SOUR")).equals(""))) { 
						pid.setSourcesystemCd(csv.elementAt(vData.getDataLocation("SOUR")).trim());
					}								

					patientType.getPatientMapId().add(pid);
				}
				PatientId pid = new PatientId();
				pid.setValue(csv.elementAt(vData.getDataLocation("PNUM")).trim());
				if ((vData.getDataLocation("PISS") != -1))
					pid.setStatus(csv.elementAt(vData.getDataLocation("PISS")).trim());			

				if ((vData.getDataLocation("UPDA") != -1) && (!csv.elementAt(vData.getDataLocation("UPDA")).equals(""))
						&& 	(vData.getDate(csv.elementAt(vData.getDataLocation("UPDA"))) != null)) { 
					pid.setUpdateDate(dtoFactory.getXMLGregorianCalendar(
							vData.getDate(csv.elementAt(vData.getDataLocation("UPDA")).trim()).getTime()));
				}

				if ((vData.getDataLocation("DOWN") != -1) && (!csv.elementAt(vData.getDataLocation("DOWN")).equals(""))
						&& 	(vData.getDate(csv.elementAt(vData.getDataLocation("DOWN"))) != null)) { 
					pid.setDownloadDate(dtoFactory.getXMLGregorianCalendar(
							vData.getDate(csv.elementAt(vData.getDataLocation("DOWN")).trim()).getTime()));
				}

				if ((vData.getDataLocation("IMPO") != -1) && (!csv.elementAt(vData.getDataLocation("IMPO")).equals(""))) { 
					pid.setImportDate(dtoFactory.getXMLGregorianCalendar(
							vData.getDate(csv.elementAt(vData.getDataLocation("IMPO")).trim()).getTime()));
				}

				if ((vData.getDataLocation("SOUR") != -1) && (!csv.elementAt(vData.getDataLocation("SOUR")).equals(""))) { 
					pid.setSourcesystemCd(csv.elementAt(vData.getDataLocation("SOUR")).trim());
				}					
				pid.setSource("HIVE");
				patientType.setPatientId(pid);

			}		
			else
			{
				if ((key == null) || (encryptMRN == false))
				{
					PatientId pid = new PatientId();
					pid.setValue(csv.elementAt(vData.getDataLocation("PIDE")).trim());
					pid.setSource(csv.elementAt(vData.getDataLocation("PISE")).trim());
					if ((vData.getDataLocation("PISS") != -1))
						pid.setStatus(csv.elementAt(vData.getDataLocation("PISS")).trim());			
					if ((vData.getDataLocation("UPDA") != -1) && (!csv.elementAt(vData.getDataLocation("UPDA")).equals(""))
							&& 	(vData.getDate(csv.elementAt(vData.getDataLocation("UPDA"))) != null)) { 
						pid.setUpdateDate(dtoFactory.getXMLGregorianCalendar(
								vData.getDate(csv.elementAt(vData.getDataLocation("UPDA")).trim()).getTime()));
					}

					if ((vData.getDataLocation("DOWN") != -1) && (!csv.elementAt(vData.getDataLocation("DOWN")).equals(""))
							&& 	(vData.getDate(csv.elementAt(vData.getDataLocation("DOWN"))) != null)) { 
						pid.setDownloadDate(dtoFactory.getXMLGregorianCalendar(
								vData.getDate(csv.elementAt(vData.getDataLocation("DOWN")).trim()).getTime()));
					}

					if ((vData.getDataLocation("IMPO") != -1) && (!csv.elementAt(vData.getDataLocation("IMPO")).equals(""))
							&& 	(vData.getDate(csv.elementAt(vData.getDataLocation("IMPO"))) != null)) { 
						pid.setImportDate(dtoFactory.getXMLGregorianCalendar(
								vData.getDate(csv.elementAt(vData.getDataLocation("IMPO")).trim()).getTime()));
					}

					if ((vData.getDataLocation("SOUR") != -1) && (!csv.elementAt(vData.getDataLocation("SOUR")).equals(""))) { 
						pid.setSourcesystemCd(csv.elementAt(vData.getDataLocation("SOUR")).trim());
					}								
					patientType.setPatientId(pid);
				}
				else
				{
					PatientId pid = new PatientId();
					pid.setValue(crypt.encryptPatientIde(csv.elementAt(vData.getDataLocation("PIDE")).trim()));
					pid.setSource(csv.elementAt(vData.getDataLocation("PISE")).trim() + "_E");
					if ((vData.getDataLocation("PISS") != -1))
						pid.setStatus(csv.elementAt(vData.getDataLocation("PISS")).trim());		
					if ((vData.getDataLocation("UPDA") != -1) && (!csv.elementAt(vData.getDataLocation("UPDA")).equals(""))
							&& 	(vData.getDate(csv.elementAt(vData.getDataLocation("UPDA"))) != null)) { 
						pid.setUpdateDate(dtoFactory.getXMLGregorianCalendar(
								vData.getDate(csv.elementAt(vData.getDataLocation("UPDA")).trim()).getTime()));
					}

					if ((vData.getDataLocation("DOWN") != -1) && (!csv.elementAt(vData.getDataLocation("DOWN")).equals(""))
							&& 	(vData.getDate(csv.elementAt(vData.getDataLocation("DOWN"))) != null)) { 
						pid.setDownloadDate(dtoFactory.getXMLGregorianCalendar(
								vData.getDate(csv.elementAt(vData.getDataLocation("DOWN")).trim()).getTime()));
					}

					if ((vData.getDataLocation("IMPO") != -1) && (!csv.elementAt(vData.getDataLocation("IMPO")).equals(""))) { 
						pid.setImportDate(dtoFactory.getXMLGregorianCalendar(
								vData.getDate(csv.elementAt(vData.getDataLocation("IMPO")).trim()).getTime()));
					}

					if ((vData.getDataLocation("SOUR") != -1) && (!csv.elementAt(vData.getDataLocation("SOUR")).equals(""))) { 
						pid.setSourcesystemCd(csv.elementAt(vData.getDataLocation("SOUR")).trim());
					}								

					patientType.setPatientId(pid);
				}

			}



			return patientType;
		}

		public EidType buildEidSet(Vector<String> csv, String key) throws ParseException
		{
			EidType encounterType = new EidType();
			//Case 1: Encounter Number and Encounter IDE is provided
			if ((vData.getDataLocation("ENUM") != -1) && (!csv.elementAt(vData.getDataLocation("ENUM")).equals(""))) {

				if ((key == null) || (encryptMRN == false))
				{
					edu.harvard.i2b2.common.datavo.pdo.EidType.EventMapId eid = new edu.harvard.i2b2.common.datavo.pdo.EidType.EventMapId ();

					eid.setSource(csv.elementAt(vData.getDataLocation("EISE")).trim());
					eid.setValue(csv.elementAt(vData.getDataLocation("EIDE")).trim());

					if (vData.getDataLocation("PIDE") != -1)
						eid.setPatientId(csv.elementAt(vData.getDataLocation("PIDE")).trim());
					if (vData.getDataLocation("PISE") != -1)
						eid.setPatientIdSource(csv.elementAt(vData.getDataLocation("PISE")).trim());

					if (vData.getDataLocation("EISS") != -1)
						eid.setStatus(csv.elementAt(vData.getDataLocation("EISS")).trim());

					if ((vData.getDataLocation("UPDA") != -1) && (!csv.elementAt(vData.getDataLocation("UPDA")).equals(""))
							&& 	(vData.getDate(csv.elementAt(vData.getDataLocation("UPDA"))) != null)) { 
						eid.setUpdateDate(dtoFactory.getXMLGregorianCalendar(
								vData.getDate(csv.elementAt(vData.getDataLocation("UPDA")).trim()).getTime()));
					}

					if ((vData.getDataLocation("DOWN") != -1) && (!csv.elementAt(vData.getDataLocation("DOWN")).equals(""))
							&& 	(vData.getDate(csv.elementAt(vData.getDataLocation("DOWN"))) != null)) { 
						eid.setDownloadDate(dtoFactory.getXMLGregorianCalendar(
								vData.getDate(csv.elementAt(vData.getDataLocation("DOWN")).trim()).getTime()));
					}

					if ((vData.getDataLocation("IMPO") != -1) && (!csv.elementAt(vData.getDataLocation("IMPO")).equals(""))
							&& 	(vData.getDate(csv.elementAt(vData.getDataLocation("IMPO"))) != null)) { 
						eid.setImportDate(dtoFactory.getXMLGregorianCalendar(
								vData.getDate(csv.elementAt(vData.getDataLocation("IMPO")).trim()).getTime()));
					}

					if ((vData.getDataLocation("SOUR") != -1) && (!csv.elementAt(vData.getDataLocation("SOUR")).equals(""))) { 
						eid.setSourcesystemCd(csv.elementAt(vData.getDataLocation("SOUR")).trim());
					}									
					encounterType.getEventMapId().add(eid);
				}
				else
				{
					edu.harvard.i2b2.common.datavo.pdo.EidType.EventMapId eid = new edu.harvard.i2b2.common.datavo.pdo.EidType.EventMapId ();


					eid.setSource(csv.elementAt(vData.getDataLocation("EISE")).trim() + "_E");
					eid.setValue(csv.elementAt(vData.getDataLocation("EIDE")));

					if (vData.getDataLocation("PIDE") != -1)
						eid.setPatientId(crypt.encryptEncounterIde(csv.elementAt(vData.getDataLocation("PIDE")).trim()  + "_E"));

					if (vData.getDataLocation("PISE") != -1)
						eid.setPatientIdSource(csv.elementAt(vData.getDataLocation("PISE")));

					if (vData.getDataLocation("EISS") != -1)
						eid.setStatus(csv.elementAt(vData.getDataLocation("EISS")).trim());

					if ((vData.getDataLocation("UPDA") != -1) && (!csv.elementAt(vData.getDataLocation("UPDA")).equals(""))
							&& 	(vData.getDate(csv.elementAt(vData.getDataLocation("UPDA"))) != null)) { 
						eid.setUpdateDate(dtoFactory.getXMLGregorianCalendar(
								vData.getDate(csv.elementAt(vData.getDataLocation("UPDA")).trim()).getTime()));
					}

					if ((vData.getDataLocation("DOWN") != -1) && (!csv.elementAt(vData.getDataLocation("DOWN")).equals(""))
							&& 	(vData.getDate(csv.elementAt(vData.getDataLocation("DOWN"))) != null)) { 
						eid.setDownloadDate(dtoFactory.getXMLGregorianCalendar(
								vData.getDate(csv.elementAt(vData.getDataLocation("DOWN")).trim()).getTime()));
					}

					if ((vData.getDataLocation("IMPO") != -1) && (!csv.elementAt(vData.getDataLocation("IMPO")).equals(""))
							&& 	(vData.getDate(csv.elementAt(vData.getDataLocation("IMPO"))) != null)) { 
						eid.setImportDate(dtoFactory.getXMLGregorianCalendar(
								vData.getDate(csv.elementAt(vData.getDataLocation("IMPO")).trim()).getTime()));
					}

					if ((vData.getDataLocation("SOUR") != -1) && (!csv.elementAt(vData.getDataLocation("SOUR")).equals(""))) { 
						eid.setSourcesystemCd(csv.elementAt(vData.getDataLocation("SOUR")).trim());
					}									
					encounterType.getEventMapId().add(eid);
				}
				edu.harvard.i2b2.common.datavo.pdo.EidType.EventId pid = new edu.harvard.i2b2.common.datavo.pdo.EidType.EventId ();
				pid.setSource("HIVE");
				pid.setValue(csv.elementAt(vData.getDataLocation("ENUM")).trim());

				if (vData.getDataLocation("EISS") != -1)
					pid.setStatus(csv.elementAt(vData.getDataLocation("EISS")).trim());

				if ((vData.getDataLocation("UPDA") != -1) && (!csv.elementAt(vData.getDataLocation("UPDA")).equals(""))
						&& 	(vData.getDate(csv.elementAt(vData.getDataLocation("UPDA"))) != null)) { 
					pid.setUpdateDate(dtoFactory.getXMLGregorianCalendar(
							vData.getDate(csv.elementAt(vData.getDataLocation("UPDA")).trim()).getTime()));
				}

				if ((vData.getDataLocation("DOWN") != -1) && (!csv.elementAt(vData.getDataLocation("DOWN")).equals(""))
						&& 	(vData.getDate(csv.elementAt(vData.getDataLocation("DOWN"))) != null)) { 
					pid.setDownloadDate(dtoFactory.getXMLGregorianCalendar(
							vData.getDate(csv.elementAt(vData.getDataLocation("DOWN")).trim()).getTime()));
				}

				if ((vData.getDataLocation("IMPO") != -1) && (!csv.elementAt(vData.getDataLocation("IMPO")).equals(""))
						&& 	(vData.getDate(csv.elementAt(vData.getDataLocation("IMPO"))) != null)) { 
					pid.setImportDate(dtoFactory.getXMLGregorianCalendar(
							vData.getDate(csv.elementAt(vData.getDataLocation("IMPO")).trim()).getTime()));
				}

				if ((vData.getDataLocation("SOUR") != -1) && (!csv.elementAt(vData.getDataLocation("SOUR")).equals(""))) { 
					pid.setSourcesystemCd(csv.elementAt(vData.getDataLocation("SOUR")).trim());
				}								
				encounterType.setEventId(pid);

			} else {
				//Case 2: Encounter Number is not provided
				edu.harvard.i2b2.common.datavo.pdo.EidType.EventId pid = new edu.harvard.i2b2.common.datavo.pdo.EidType.EventId ();

				if ((key == null) || (encryptMRN == false))
				{
					//From a encounter mapping file
					if (vData.getDataLocation("EIDE") != -1)
						pid.setValue(csv.elementAt(vData.getDataLocation("EIDE")).trim());
					if (vData.getDataLocation("EISE") != -1)
						pid.setSource(csv.elementAt(vData.getDataLocation("EISE")).trim());
				
					if (vData.getDataLocation("EISS") != -1)
						pid.setStatus(csv.elementAt(vData.getDataLocation("EISS")).trim());			

					if (vData.getDataLocation("PIDE") != -1)
						pid.setPatientId(csv.elementAt(vData.getDataLocation("PIDE")).trim());	
					if (vData.getDataLocation("PISE") != -1)
						pid.setPatientIdSource(csv.elementAt(vData.getDataLocation("PISE")).trim());	
					if ((vData.getDataLocation("UPDA") != -1) && (!csv.elementAt(vData.getDataLocation("UPDA")).equals("")) 
							&& 	(vData.getDate(csv.elementAt(vData.getDataLocation("UPDA"))) != null)) { 
						pid.setUpdateDate(dtoFactory.getXMLGregorianCalendar(
								vData.getDate(csv.elementAt(vData.getDataLocation("UPDA")).trim()).getTime()));
					}

					if ((vData.getDataLocation("DOWN") != -1) && (!csv.elementAt(vData.getDataLocation("DOWN")).equals(""))
							&& 	(vData.getDate(csv.elementAt(vData.getDataLocation("DOWN"))) != null)) { 
						pid.setDownloadDate(dtoFactory.getXMLGregorianCalendar(
								vData.getDate(csv.elementAt(vData.getDataLocation("DOWN")).trim()).getTime()));
					}

					if ((vData.getDataLocation("IMPO") != -1) && (!csv.elementAt(vData.getDataLocation("IMPO")).equals(""))
							&& 	(vData.getDate(csv.elementAt(vData.getDataLocation("IMPO"))) != null)) { 
						pid.setImportDate(dtoFactory.getXMLGregorianCalendar(
								vData.getDate(csv.elementAt(vData.getDataLocation("IMPO")).trim()).getTime()));
					}

					if ((vData.getDataLocation("SOUR") != -1) && (!csv.elementAt(vData.getDataLocation("SOUR")).equals(""))) {
						pid.setSourcesystemCd(csv.elementAt(vData.getDataLocation("SOUR")).trim());
					}	
					encounterType.setEventId(pid);
				}
				else
				{
					pid.setValue(crypt.encryptPatientIde(csv.elementAt(vData.getDataLocation("EIDE")).trim()));
					pid.setSource(csv.elementAt(vData.getDataLocation("EISE")).trim() + "_E");
					if (vData.getDataLocation("EISS") != -1)
						pid.setStatus(csv.elementAt(vData.getDataLocation("EISS")).trim());			

					pid.setPatientId(csv.elementAt(vData.getDataLocation("PIDE")).trim() + "_E");
					pid.setPatientIdSource(csv.elementAt(vData.getDataLocation("PISE")).trim());	
					if ((vData.getDataLocation("UPDA") != -1) && (!csv.elementAt(vData.getDataLocation("UPDA")).equals(""))
							&& 	(vData.getDate(csv.elementAt(vData.getDataLocation("UPDA"))) != null)) { 
						pid.setUpdateDate(dtoFactory.getXMLGregorianCalendar(
								vData.getDate(csv.elementAt(vData.getDataLocation("UPDA")).trim()).getTime()));
					}

					if ((vData.getDataLocation("DOWN") != -1) && (!csv.elementAt(vData.getDataLocation("DOWN")).equals(""))
							&& 	(vData.getDate(csv.elementAt(vData.getDataLocation("DOWN"))) != null)) { 
						pid.setDownloadDate(dtoFactory.getXMLGregorianCalendar(
								vData.getDate(csv.elementAt(vData.getDataLocation("DOWN")).trim()).getTime()));
					}

					if ((vData.getDataLocation("IMPO") != -1) && (!csv.elementAt(vData.getDataLocation("IMPO")).equals(""))
							&& 	(vData.getDate(csv.elementAt(vData.getDataLocation("IMPO"))) != null)) { 
						pid.setImportDate(dtoFactory.getXMLGregorianCalendar(
								vData.getDate(csv.elementAt(vData.getDataLocation("IMPO")).trim()).getTime()));
					}

					if ((vData.getDataLocation("SOUR") != -1) && (!csv.elementAt(vData.getDataLocation("SOUR")).equals(""))) { 
						pid.setSourcesystemCd(csv.elementAt(vData.getDataLocation("SOUR")).trim());
					}				
				}
				encounterType.setEventId(pid);
			}
			return encounterType;
		}


		/*
		public EidType buildEidSet(Vector<String> csv, String key) throws ParseException
		{
			EidType encounterType = new EidType();


			// Encounter IDE and Encounter Number is provided:

			if (vData.getDataLocation("ENUM") != -1)
			{
				if ((vData.getDataLocation("PTCD") != -1) && (!csv.elementAt(vData.getDataLocation("PTCD")).equals(""))) 
				{
					edu.harvard.i2b2.common.datavo.pdo.EidType.EventId eid = new edu.harvard.i2b2.common.datavo.pdo.EidType.EventId ();
					eid.setSource(csv.elementAt(vData.getDataLocation("ENCD")).trim());
					eid.setValue(csv.elementAt(vData.getDataLocation("ENCO")).trim());
					eid.setPatientId(csv.elementAt(vData.getDataLocation("PATI")).trim());
					eid.setPatientIdSource("HIVE");
					if (vData.getDataLocation("EISS") != -1)
						eid.setStatus(csv.elementAt(vData.getDataLocation("EISS")).trim());

					if ((vData.getDataLocation("UPDA") != -1) && (!csv.elementAt(vData.getDataLocation("UPDA")).equals(""))
							&& 	(vData.getDate(csv.elementAt(vData.getDataLocation("UPDA"))) != null)) { 
						eid.setUpdateDate(dtoFactory.getXMLGregorianCalendar(
								vData.getDate(csv.elementAt(vData.getDataLocation("UPDA")).trim()).getTime()));
					}

					if ((vData.getDataLocation("DOWN") != -1) && (!csv.elementAt(vData.getDataLocation("DOWN")).equals(""))
							&& 	(vData.getDate(csv.elementAt(vData.getDataLocation("DOWN"))) != null)) { 
						eid.setDownloadDate(dtoFactory.getXMLGregorianCalendar(
								vData.getDate(csv.elementAt(vData.getDataLocation("DOWN")).trim()).getTime()));
					}

					if ((vData.getDataLocation("IMPO") != -1) && (!csv.elementAt(vData.getDataLocation("IMPO")).equals(""))
							&& 	(vData.getDate(csv.elementAt(vData.getDataLocation("IMPO"))) != null)) { 
						eid.setImportDate(dtoFactory.getXMLGregorianCalendar(
								vData.getDate(csv.elementAt(vData.getDataLocation("IMPO")).trim()).getTime()));
					}

					if ((vData.getDataLocation("SOUR") != -1) && (!csv.elementAt(vData.getDataLocation("SOUR")).equals(""))) { 
						eid.setSourcesystemCd(csv.elementAt(vData.getDataLocation("SOUR")).trim());
					}				
					encounterType.setEventId(eid);
				}
				else if ((key == null) || (encryptMRN == false))
				{
					edu.harvard.i2b2.common.datavo.pdo.EidType.EventId eid = new edu.harvard.i2b2.common.datavo.pdo.EidType.EventId ();

					eid.setSource(csv.elementAt(vData.getDataLocation("ENCD")).trim());
					eid.setValue(csv.elementAt(vData.getDataLocation("ENCO")).trim());

					eid.setPatientId(csv.elementAt(vData.getDataLocation("PATI")).trim());
					eid.setPatientIdSource(csv.elementAt(vData.getDataLocation("PTCD")).trim());
					//pid.setPatientId(csv.elementAt(vData.getDataLocation("PATI")).trim());	
					//pid.setPatientIdSource(csv.elementAt(vData.getDataLocation("PTCD")).trim());	

					if (vData.getDataLocation("EISS") != -1)
						eid.setStatus(csv.elementAt(vData.getDataLocation("EISS")).trim());

					if ((vData.getDataLocation("UPDA") != -1) && (!csv.elementAt(vData.getDataLocation("UPDA")).equals(""))
							&& 	(vData.getDate(csv.elementAt(vData.getDataLocation("UPDA"))) != null)) { 
						eid.setUpdateDate(dtoFactory.getXMLGregorianCalendar(
								vData.getDate(csv.elementAt(vData.getDataLocation("UPDA")).trim()).getTime()));
					}

					if ((vData.getDataLocation("DOWN") != -1) && (!csv.elementAt(vData.getDataLocation("DOWN")).equals(""))
							&& 	(vData.getDate(csv.elementAt(vData.getDataLocation("DOWN"))) != null)) { 
						eid.setDownloadDate(dtoFactory.getXMLGregorianCalendar(
								vData.getDate(csv.elementAt(vData.getDataLocation("DOWN")).trim()).getTime()));
					}

					if ((vData.getDataLocation("IMPO") != -1) && (!csv.elementAt(vData.getDataLocation("IMPO")).equals(""))
							&& 	(vData.getDate(csv.elementAt(vData.getDataLocation("IMPO"))) != null)) { 
						eid.setImportDate(dtoFactory.getXMLGregorianCalendar(
								vData.getDate(csv.elementAt(vData.getDataLocation("IMPO")).trim()).getTime()));
					}

					if ((vData.getDataLocation("SOUR") != -1) && (!csv.elementAt(vData.getDataLocation("SOUR")).equals(""))) { 
						eid.setSourcesystemCd(csv.elementAt(vData.getDataLocation("SOUR")).trim());
					}									
					encounterType.setEventId(eid);
				}
				else
				{
					edu.harvard.i2b2.common.datavo.pdo.EidType.EventId eid = new edu.harvard.i2b2.common.datavo.pdo.EidType.EventId ();


					eid.setSource(csv.elementAt(vData.getDataLocation("ENCD")).trim() + "_E");
					eid.setValue(csv.elementAt(vData.getDataLocation("ENCO")));

					eid.setPatientId(crypt.encryptEncounterIde(csv.elementAt(vData.getDataLocation("PATI")).trim()  + "_E"));

					eid.setPatientIdSource(csv.elementAt(vData.getDataLocation("PTCD")));

					if (vData.getDataLocation("EISS") != -1)
						eid.setStatus(csv.elementAt(vData.getDataLocation("EISS")).trim());

					if ((vData.getDataLocation("UPDA") != -1) && (!csv.elementAt(vData.getDataLocation("UPDA")).equals(""))
							&& 	(vData.getDate(csv.elementAt(vData.getDataLocation("UPDA"))) != null)) { 
						eid.setUpdateDate(dtoFactory.getXMLGregorianCalendar(
								vData.getDate(csv.elementAt(vData.getDataLocation("UPDA")).trim()).getTime()));
					}

					if ((vData.getDataLocation("DOWN") != -1) && (!csv.elementAt(vData.getDataLocation("DOWN")).equals(""))
							&& 	(vData.getDate(csv.elementAt(vData.getDataLocation("DOWN"))) != null)) { 
						eid.setDownloadDate(dtoFactory.getXMLGregorianCalendar(
								vData.getDate(csv.elementAt(vData.getDataLocation("DOWN")).trim()).getTime()));
					}

					if ((vData.getDataLocation("IMPO") != -1) && (!csv.elementAt(vData.getDataLocation("IMPO")).equals(""))
							&& 	(vData.getDate(csv.elementAt(vData.getDataLocation("IMPO"))) != null)) { 
						eid.setImportDate(dtoFactory.getXMLGregorianCalendar(
								vData.getDate(csv.elementAt(vData.getDataLocation("IMPO")).trim()).getTime()));
					}

					if ((vData.getDataLocation("SOUR") != -1) && (!csv.elementAt(vData.getDataLocation("SOUR")).equals(""))) { 
						eid.setSourcesystemCd(csv.elementAt(vData.getDataLocation("SOUR")).trim());
					}									
					encounterType.setEventId(eid);
				}
				//edu.harvard.i2b2.common.datavo.pdo.EidType.EventId pid = new edu.harvard.i2b2.common.datavo.pdo.EidType.EventId ();
				//pid.setSource(csv.elementAt(vData.getDataLocation("PATI")).trim());
				//pid.setValue(csv.elementAt(vData.getDataLocation("PTCD")).trim());
				//encounterType.setEventId(pid);

				//MM not needed
				//edu.harvard.i2b2.common.datavo.pdo.EidType.EventId pid = new edu.harvard.i2b2.common.datavo.pdo.EidType.EventId ();
				//pid.setSource("HIVE");
				//pid.setValue(csv.elementAt(vData.getDataLocation("ENUM")).trim());
				//encounterType.setEventId(pid);

			} 
			else if (vData.getDataLocation("EIDE") == -1)  // Encounter IDE is not provided
			{
				// Generate a Encounter for a Observation
				if ((vData.getDataLocation("PTCD") != -1) && (!csv.elementAt(vData.getDataLocation("PTCD")).equals(""))) 
				{
					edu.harvard.i2b2.common.datavo.pdo.EidType.EventId eid = new edu.harvard.i2b2.common.datavo.pdo.EidType.EventId ();
					eid.setSource(csv.elementAt(vData.getDataLocation("ENCD")).trim());
					eid.setValue(csv.elementAt(vData.getDataLocation("ENCO")).trim());
					eid.setPatientId(csv.elementAt(vData.getDataLocation("PATI")).trim());
					eid.setPatientIdSource("HIVE");
					if (vData.getDataLocation("EISS") != -1)
						eid.setStatus(csv.elementAt(vData.getDataLocation("EISS")).trim());

					if ((vData.getDataLocation("UPDA") != -1) && (!csv.elementAt(vData.getDataLocation("UPDA")).equals(""))
							&& 	(vData.getDate(csv.elementAt(vData.getDataLocation("UPDA"))) != null)) { 
						eid.setUpdateDate(dtoFactory.getXMLGregorianCalendar(
								vData.getDate(csv.elementAt(vData.getDataLocation("UPDA")).trim()).getTime()));
					}

					if ((vData.getDataLocation("DOWN") != -1) && (!csv.elementAt(vData.getDataLocation("DOWN")).equals(""))
							&& 	(vData.getDate(csv.elementAt(vData.getDataLocation("DOWN"))) != null)) { 
						eid.setDownloadDate(dtoFactory.getXMLGregorianCalendar(
								vData.getDate(csv.elementAt(vData.getDataLocation("DOWN")).trim()).getTime()));
					}

					if ((vData.getDataLocation("IMPO") != -1) && (!csv.elementAt(vData.getDataLocation("IMPO")).equals(""))
							&& 	(vData.getDate(csv.elementAt(vData.getDataLocation("IMPO"))) != null)) { 
						eid.setImportDate(dtoFactory.getXMLGregorianCalendar(
								vData.getDate(csv.elementAt(vData.getDataLocation("IMPO")).trim()).getTime()));
					}

					if ((vData.getDataLocation("SOUR") != -1) && (!csv.elementAt(vData.getDataLocation("SOUR")).equals(""))) { 
						eid.setSourcesystemCd(csv.elementAt(vData.getDataLocation("SOUR")).trim());
					}				
					encounterType.setEventId(eid);
				}
				else if ((key == null) || (encryptMRN == false))
				{
					edu.harvard.i2b2.common.datavo.pdo.EidType.EventId eid = new edu.harvard.i2b2.common.datavo.pdo.EidType.EventId ();

					eid.setSource(csv.elementAt(vData.getDataLocation("ENCD")).trim());
					eid.setValue(csv.elementAt(vData.getDataLocation("ENCO")).trim());

					eid.setPatientId(csv.elementAt(vData.getDataLocation("PATI")).trim());
					eid.setPatientIdSource(csv.elementAt(vData.getDataLocation("PTCD")).trim());
					//pid.setPatientId(csv.elementAt(vData.getDataLocation("PATI")).trim());	
					//pid.setPatientIdSource(csv.elementAt(vData.getDataLocation("PTCD")).trim());	

					if (vData.getDataLocation("EISS") != -1)
						eid.setStatus(csv.elementAt(vData.getDataLocation("EISS")).trim());

					if ((vData.getDataLocation("UPDA") != -1) && (!csv.elementAt(vData.getDataLocation("UPDA")).equals(""))
							&& 	(vData.getDate(csv.elementAt(vData.getDataLocation("UPDA"))) != null)) { 
						eid.setUpdateDate(dtoFactory.getXMLGregorianCalendar(
								vData.getDate(csv.elementAt(vData.getDataLocation("UPDA")).trim()).getTime()));
					}

					if ((vData.getDataLocation("DOWN") != -1) && (!csv.elementAt(vData.getDataLocation("DOWN")).equals(""))
							&& 	(vData.getDate(csv.elementAt(vData.getDataLocation("DOWN"))) != null)) { 
						eid.setDownloadDate(dtoFactory.getXMLGregorianCalendar(
								vData.getDate(csv.elementAt(vData.getDataLocation("DOWN")).trim()).getTime()));
					}

					if ((vData.getDataLocation("IMPO") != -1) && (!csv.elementAt(vData.getDataLocation("IMPO")).equals(""))
							&& 	(vData.getDate(csv.elementAt(vData.getDataLocation("IMPO"))) != null)) { 
						eid.setImportDate(dtoFactory.getXMLGregorianCalendar(
								vData.getDate(csv.elementAt(vData.getDataLocation("IMPO")).trim()).getTime()));
					}

					if ((vData.getDataLocation("SOUR") != -1) && (!csv.elementAt(vData.getDataLocation("SOUR")).equals(""))) { 
						eid.setSourcesystemCd(csv.elementAt(vData.getDataLocation("SOUR")).trim());
					}									
					encounterType.setEventId(eid);
				}
				else
				{
					edu.harvard.i2b2.common.datavo.pdo.EidType.EventId eid = new edu.harvard.i2b2.common.datavo.pdo.EidType.EventId ();


					eid.setSource(csv.elementAt(vData.getDataLocation("ENCD")).trim() + "_E");
					eid.setValue(csv.elementAt(vData.getDataLocation("ENCO")));

					eid.setPatientId(crypt.encryptEncounterIde(csv.elementAt(vData.getDataLocation("PATI")).trim()  + "_E"));

					eid.setPatientIdSource(csv.elementAt(vData.getDataLocation("PTCD")));

					if (vData.getDataLocation("EISS") != -1)
						eid.setStatus(csv.elementAt(vData.getDataLocation("EISS")).trim());

					if ((vData.getDataLocation("UPDA") != -1) && (!csv.elementAt(vData.getDataLocation("UPDA")).equals(""))
							&& 	(vData.getDate(csv.elementAt(vData.getDataLocation("UPDA"))) != null)) { 
						eid.setUpdateDate(dtoFactory.getXMLGregorianCalendar(
								vData.getDate(csv.elementAt(vData.getDataLocation("UPDA")).trim()).getTime()));
					}

					if ((vData.getDataLocation("DOWN") != -1) && (!csv.elementAt(vData.getDataLocation("DOWN")).equals(""))
							&& 	(vData.getDate(csv.elementAt(vData.getDataLocation("DOWN"))) != null)) { 
						eid.setDownloadDate(dtoFactory.getXMLGregorianCalendar(
								vData.getDate(csv.elementAt(vData.getDataLocation("DOWN")).trim()).getTime()));
					}

					if ((vData.getDataLocation("IMPO") != -1) && (!csv.elementAt(vData.getDataLocation("IMPO")).equals(""))
							&& 	(vData.getDate(csv.elementAt(vData.getDataLocation("IMPO"))) != null)) { 
						eid.setImportDate(dtoFactory.getXMLGregorianCalendar(
								vData.getDate(csv.elementAt(vData.getDataLocation("IMPO")).trim()).getTime()));
					}

					if ((vData.getDataLocation("SOUR") != -1) && (!csv.elementAt(vData.getDataLocation("SOUR")).equals(""))) { 
						eid.setSourcesystemCd(csv.elementAt(vData.getDataLocation("SOUR")).trim());
					}									
					encounterType.setEventId(eid);
				}
				//edu.harvard.i2b2.common.datavo.pdo.EidType.EventId pid = new edu.harvard.i2b2.common.datavo.pdo.EidType.EventId ();
				//pid.setSource(csv.elementAt(vData.getDataLocation("PATI")).trim());
				//pid.setValue(csv.elementAt(vData.getDataLocation("PTCD")).trim());
				//encounterType.setEventId(pid);

				//MM not needed
				//edu.harvard.i2b2.common.datavo.pdo.EidType.EventId pid = new edu.harvard.i2b2.common.datavo.pdo.EidType.EventId ();
				//pid.setSource("HIVE");
				//pid.setValue(csv.elementAt(vData.getDataLocation("ENUM")).trim());
				//encounterType.setEventId(pid);

			}
			else {
				edu.harvard.i2b2.common.datavo.pdo.EidType.EventId pid = new edu.harvard.i2b2.common.datavo.pdo.EidType.EventId ();

				if ((key == null) || (encryptMRN == false))
				{
					pid.setValue(csv.elementAt(vData.getDataLocation("EIDE")).trim());
					pid.setSource(csv.elementAt(vData.getDataLocation("EISE")).trim());
					if (vData.getDataLocation("EISS") != -1)
						pid.setStatus(csv.elementAt(vData.getDataLocation("EISS")).trim());			

					if (vData.getDataLocation("PIDE") != -1)
						pid.setPatientId(csv.elementAt(vData.getDataLocation("PIDE")).trim());	
					if (vData.getDataLocation("PISE") != -1)
						pid.setPatientIdSource(csv.elementAt(vData.getDataLocation("PISE")).trim());	
					if ((vData.getDataLocation("UPDA") != -1) && (!csv.elementAt(vData.getDataLocation("UPDA")).equals("")) 
							&& 	(vData.getDate(csv.elementAt(vData.getDataLocation("UPDA"))) != null)) { 
						pid.setUpdateDate(dtoFactory.getXMLGregorianCalendar(
								vData.getDate(csv.elementAt(vData.getDataLocation("UPDA")).trim()).getTime()));
					}

					if ((vData.getDataLocation("DOWN") != -1) && (!csv.elementAt(vData.getDataLocation("DOWN")).equals(""))
							&& 	(vData.getDate(csv.elementAt(vData.getDataLocation("DOWN"))) != null)) { 
						pid.setDownloadDate(dtoFactory.getXMLGregorianCalendar(
								vData.getDate(csv.elementAt(vData.getDataLocation("DOWN")).trim()).getTime()));
					}

					if ((vData.getDataLocation("IMPO") != -1) && (!csv.elementAt(vData.getDataLocation("IMPO")).equals(""))
							&& 	(vData.getDate(csv.elementAt(vData.getDataLocation("IMPO"))) != null)) { 
						pid.setImportDate(dtoFactory.getXMLGregorianCalendar(
								vData.getDate(csv.elementAt(vData.getDataLocation("IMPO")).trim()).getTime()));
					}

					if ((vData.getDataLocation("SOUR") != -1) && (!csv.elementAt(vData.getDataLocation("SOUR")).equals(""))) {
						pid.setSourcesystemCd(csv.elementAt(vData.getDataLocation("SOUR")).trim());
					}									
				}
				else
				{
					pid.setValue(crypt.encryptPatientIde(csv.elementAt(vData.getDataLocation("EIDE")).trim()));
					pid.setSource(csv.elementAt(vData.getDataLocation("EISE")).trim() + "_E");
					if (vData.getDataLocation("EISS") != -1)
						pid.setStatus(csv.elementAt(vData.getDataLocation("EISS")).trim());			

					pid.setPatientId(csv.elementAt(vData.getDataLocation("PIDE")).trim() + "_E");
					pid.setPatientIdSource(csv.elementAt(vData.getDataLocation("PISE")).trim());	
					if ((vData.getDataLocation("UPDA") != -1) && (!csv.elementAt(vData.getDataLocation("UPDA")).equals(""))
							&& 	(vData.getDate(csv.elementAt(vData.getDataLocation("UPDA"))) != null)) { 
						pid.setUpdateDate(dtoFactory.getXMLGregorianCalendar(
								vData.getDate(csv.elementAt(vData.getDataLocation("UPDA")).trim()).getTime()));
					}

					if ((vData.getDataLocation("DOWN") != -1) && (!csv.elementAt(vData.getDataLocation("DOWN")).equals(""))
							&& 	(vData.getDate(csv.elementAt(vData.getDataLocation("DOWN"))) != null)) { 
						pid.setDownloadDate(dtoFactory.getXMLGregorianCalendar(
								vData.getDate(csv.elementAt(vData.getDataLocation("DOWN")).trim()).getTime()));
					}

					if ((vData.getDataLocation("IMPO") != -1) && (!csv.elementAt(vData.getDataLocation("IMPO")).equals(""))
							&& 	(vData.getDate(csv.elementAt(vData.getDataLocation("IMPO"))) != null)) { 
						pid.setImportDate(dtoFactory.getXMLGregorianCalendar(
								vData.getDate(csv.elementAt(vData.getDataLocation("IMPO")).trim()).getTime()));
					}

					if ((vData.getDataLocation("SOUR") != -1) && (!csv.elementAt(vData.getDataLocation("SOUR")).equals(""))) { 
						pid.setSourcesystemCd(csv.elementAt(vData.getDataLocation("SOUR")).trim());
					}				
				}


				encounterType.setEventId(pid);


			}


			return encounterType;
		}
		 */

		public PatientType buildPatientSet(String[] csv, String key) throws ParseException
		{
			PatientType patientType = new PatientType();

			if ((key == null) || (encryptMRN == false))
			{
				PatientIdType pid = new PatientIdType();
				pid.setValue(csv[vData.getDataLocation("PATI")]);
				patientType.setPatientId(pid);//csv[vData.getDataLocation("PATI")]);
			}
			else
			{
				PatientIdType pid = new PatientIdType();
				pid.setValue(crypt.encryptPatientIde(csv[vData.getDataLocation("PATI")]));
				patientType.setPatientId(pid);//crypt.encryptPatientIde(csv[vData.getDataLocation("PATI")]));
			}


			//TODO FIX
			/*
		Date birthDate = vData.getDate(csv[vData.getDataLocation("BIRT")]);
		if (birthDate != null) {
			patientType.set.setBirthDate(dtoFactory.getXMLGregorianCalendar(
					birthDate.getTime()));
		}

		Date deathDate = vData.getDate(csv[vData.getDataLocation("DEAT")]);
		if (deathDate != null) {
			patientType.setDeathDate(dtoFactory.getXMLGregorianCalendar(
					deathDate.getTime()));
		}

		if (! csv[vData.getDataLocation("AGEI")].equals("")) {
			Integer age = Integer.valueOf(csv[vData.getDataLocation("AGEI")]);
			patientType.setAgeInYearsNum((age!=null)?age:null);
		}


		patientType.setSexCd(csv[vData.getDataLocation("SEXC")]);
		patientType.setLanguageCd(csv[vData.getDataLocation("LANG")]);
		patientType.setRaceCd(csv[vData.getDataLocation("RACE")]);
		patientType.setMaritalStatusCd(csv[vData.getDataLocation("MARI")]);
		patientType.setReligionCd(csv[vData.getDataLocation("RELI")]);
		patientType.setZipCd(csv[vData.getDataLocation("ZIPC")]);
		patientType.setStatecityzipPath(csv[vData.getDataLocation("STAT")]);
			 */


			if (!csv[vData.getDataLocation("UPDA")].equals("")) {
				patientType.setUpdateDate(dtoFactory.getXMLGregorianCalendar(
						vData.getDate(csv[vData.getDataLocation("UPDA")]).getTime()));
			}

			if (!csv[vData.getDataLocation("DOWN")].equals("")) {
				patientType.setDownloadDate(dtoFactory.getXMLGregorianCalendar(
						vData.getDate(csv[vData.getDataLocation("DOWN")]).getTime()));
			}

			if (!csv[vData.getDataLocation("IMPO")].equals("")) {
				patientType.setImportDate(dtoFactory.getXMLGregorianCalendar(
						vData.getDate(csv[vData.getDataLocation("IMPO")]).getTime()));
			}

			patientType.setSourcesystemCd(csv[vData.getDataLocation("SOUR")]);

			return patientType;
		}


		public ObservationType buildObservationSet(Vector<String> csv, String key) throws ParseException
		{
			ObservationType observationFactType = new ObservationType();

			if ((vData.getDataLocation("PTCD") == -1) && (csv.elementAt(vData.getDataLocation("PTCD")).equals(""))) 
			{
				PatientIdType pid = new PatientIdType();
				pid.setValue(csv.elementAt(vData.getDataLocation("PATI")).trim());
				pid.setSource("HIVE");
			} 
			else if ( 					
					(vData.getDataLocation("PATI") != -1) && (!csv.elementAt(vData.getDataLocation("PATI")).equals(""))
					||
					((key == null) || (encryptMRN == false)))
			{
				PatientIdType pid = new PatientIdType();
				pid.setValue(
						((vData.getDataLocation("PATI") == -1) && (csv.elementAt(vData.getDataLocation("PATI")).equals("")) ?
								generateMessageId() : csv.elementAt(vData.getDataLocation("PATI")).trim()));
				pid.setSource(csv.elementAt(vData.getDataLocation("PTCD")).trim());

				observationFactType.setPatientId(pid);//.setPatientId(csv[vData.getDataLocation("PATI")]);
			}
			else
			{
				PatientIdType pid = new PatientIdType();
				pid.setSource(csv.elementAt(vData.getDataLocation("PTCD")).trim() + "_E");
				pid.setValue(crypt.encryptPatientIde(csv.elementAt(vData.getDataLocation("PATI")).trim()));
				observationFactType.setPatientId(pid);//(crypt.encryptPatientIde(csv[vData.getDataLocation("PATI")]));
			}

			ObservationType.EventId eventId = new ObservationType.EventId();
			if (
					(vData.getDataLocation("ENCO") != -1) && (!csv.elementAt(vData.getDataLocation("ENCO")).equals(""))
					&&	
					((key == null)  || (encryptMRN == false)))
			{
				eventId.setSource(csv.elementAt(vData.getDataLocation("ENCD")).trim());
				eventId.setValue(
						((vData.getDataLocation("ENCO") == -1) && (csv.elementAt(vData.getDataLocation("ENCO")).equals("")) ?
								generateMessageId() : csv.elementAt(vData.getDataLocation("ENCO")).trim()));
			}
			else if ((vData.getDataLocation("ENCO") != -1) && (!csv.elementAt(vData.getDataLocation("ENCO")).equals("")))
			{
				eventId.setSource(csv.elementAt(vData.getDataLocation("ENCD")).trim() + "_E");
				eventId.setValue(crypt.encryptEncounterIde(csv.elementAt(vData.getDataLocation("ENCO")).trim()));
			} else 
			{
				//Create a encounter mapping 
				eventId.setSource("RND");
				eventId.setValue(generateMessageId());
			}

			observationFactType.setEventId(eventId);
			ObservationType.ConceptCd conceptCd = new ObservationType.ConceptCd();
			conceptCd.setValue(csv.elementAt(vData.getDataLocation("CONC")).trim());
			observationFactType.setConceptCd(conceptCd);


			Date startDate = vData.getDate(csv.elementAt(vData.getDataLocation("STAR")).trim());

			if (startDate != null) {
				observationFactType.setStartDate(dtoFactory.getXMLGregorianCalendar(
						startDate.getTime()));
			}

			if ((vData.getDataLocation("BLOB") != -1) && (!csv.elementAt(vData.getDataLocation("BLOB")).equals(""))) 
			{

				if ((key == null) || (encryptText == false))
				{
					BlobType blob = new BlobType();
					blob.getContent().add(csv.elementAt(vData.getDataLocation("BLOB")));
					observationFactType.setObservationBlob(blob);
				}else
				{
					BlobType blob = new BlobType();
					blob.getContent().add(crypt.encryptNotes(csv.elementAt(vData.getDataLocation("BLOB"))));
					observationFactType.setObservationBlob(blob);

				}
			}
			if ((vData.getDataLocation("PROV") != -1) && (!csv.elementAt(vData.getDataLocation("PROV")).equals(""))) { 
				ObservationType.ObserverCd observerCd = new ObservationType.ObserverCd(); 
				observerCd.setValue(csv.elementAt(vData.getDataLocation("PROV")).trim());
				observationFactType.setObserverCd(observerCd);
			} else 
			{
				ObservationType.ObserverCd observerCd = new ObservationType.ObserverCd(); 
				observerCd.setValue("@");
				observationFactType.setObserverCd(observerCd);
			}

			if ((vData.getDataLocation("MODI") != -1) && (!csv.elementAt(vData.getDataLocation("MODI")).equals(""))) { 
				ObservationType.ModifierCd modifierCd = new ObservationType.ModifierCd();
				modifierCd.setValue(csv.elementAt(vData.getDataLocation("MODI")).trim());
				observationFactType.setModifierCd(modifierCd);
			} else {
				ObservationType.ModifierCd modifierCd = new ObservationType.ModifierCd();
				modifierCd.setValue("@");
				observationFactType.setModifierCd(modifierCd);
			}

			// if (obsFactDetailFlag) {
			if ((vData.getDataLocation("ENDD") != -1) && (!csv.elementAt(vData.getDataLocation("ENDD")).equals(""))) { 

				Date endDate =  vData.getDate(csv.elementAt(vData.getDataLocation("ENDD")).trim());

				if (endDate != null) {
					observationFactType.setEndDate(dtoFactory.getXMLGregorianCalendar(
							endDate.getTime()));
				}
			}

			if ((vData.getDataLocation("VALU") != -1) && (!csv.elementAt(vData.getDataLocation("VALU")).equals(""))) { 
				observationFactType.setValuetypeCd(csv.elementAt(vData.getDataLocation("VALU")).trim());
			}
			//if (!csv.elementAt(vData.getDataLocation("TVAL")].equals(""))
			//	observationFactType.setTvalChar(dirWorker.getURI(csv.elementAt(vData.getDataLocation("TVAL")]));


			if ((vData.getDataLocation("FILE") != -1) && (vData.getDataLocation("TVAL") != -1))
				observationFactType.setTvalChar(dirWorker.getURI(csv.elementAt(vData.getDataLocation("DEST")).trim() + csv.elementAt(vData.getDataLocation("TVAL")).trim()));
			else if ((vData.getDataLocation("TVAL") != -1) && (!csv.elementAt(vData.getDataLocation("TVAL")).equals(""))) 
				observationFactType.setTvalChar(csv.elementAt(vData.getDataLocation("TVAL")).trim());


			if ((vData.getDataLocation("NVAL") != -1) && (!csv.elementAt(vData.getDataLocation("NVAL")).equals(""))) { 
				ObservationType.NvalNum valNum = new ObservationType.NvalNum();
				Double nValNum = Double.valueOf(csv.elementAt(vData.getDataLocation("NVAL")).trim());
				valNum.setValue((nValNum != null)?new BigDecimal(nValNum):null);
				observationFactType.setNvalNum(valNum);
			}

			if ((vData.getDataLocation("VALU") != -1) && (!csv.elementAt(vData.getDataLocation("VALU")).equals(""))) { 
				ObservationType.ValueflagCd valueFlagCd = new ObservationType.ValueflagCd();
				valueFlagCd.setValue(csv.elementAt(vData.getDataLocation("VALU")).trim());
				observationFactType.setValueflagCd(valueFlagCd);
			}

			if ((vData.getDataLocation("QUAN") != -1) && (!csv.elementAt(vData.getDataLocation("QUAN")).equals(""))) { 
				Double qtyNum = Double.valueOf(csv.elementAt(vData.getDataLocation("QUAN")).trim());
				observationFactType.setQuantityNum((qtyNum!=null)?new BigDecimal(qtyNum):null);
			}

			if ((vData.getDataLocation("UNIT") != -1) && (!csv.elementAt(vData.getDataLocation("UNIT")).equals(""))) 
				observationFactType.setUnitsCd(csv.elementAt(vData.getDataLocation("UNIT")).trim());

			if ((vData.getDataLocation("LOCA") != -1) && (!csv.elementAt(vData.getDataLocation("LOCA")).equals(""))) { 
				ObservationType.LocationCd locationCd = new ObservationType.LocationCd();
				locationCd.setValue(csv.elementAt(vData.getDataLocation("LOCA")).trim());
				observationFactType.setLocationCd(locationCd);
			}

			if ((vData.getDataLocation("UPDA") != -1) && (!csv.elementAt(vData.getDataLocation("UPDA")).equals(""))
					&& 	(vData.getDate(csv.elementAt(vData.getDataLocation("UPDA"))) != null)) { 
				observationFactType.setUpdateDate(dtoFactory.getXMLGregorianCalendar(
						vData.getDate(csv.elementAt(vData.getDataLocation("UPDA")).trim()).getTime()));
			}

			if ((vData.getDataLocation("DOWN") != -1) && (!csv.elementAt(vData.getDataLocation("DOWN")).equals(""))
					&& 	(vData.getDate(csv.elementAt(vData.getDataLocation("DOWN"))) != null)) { 
				observationFactType.setDownloadDate(dtoFactory.getXMLGregorianCalendar(
						vData.getDate(csv.elementAt(vData.getDataLocation("DOWN")).trim()).getTime()));
			}

			if ((vData.getDataLocation("IMPO") != -1) && (!csv.elementAt(vData.getDataLocation("IMPO")).equals(""))
					&& 	(vData.getDate(csv.elementAt(vData.getDataLocation("IMPO"))) != null)) { 
				observationFactType.setImportDate(dtoFactory.getXMLGregorianCalendar(
						vData.getDate(csv.elementAt(vData.getDataLocation("IMPO")).trim()).getTime()));
			}

			if ((vData.getDataLocation("SOUR") != -1) && (!csv.elementAt(vData.getDataLocation("SOUR")).equals(""))) { 
				observationFactType.setSourcesystemCd(csv.elementAt(vData.getDataLocation("SOUR")).trim());
			}
			return observationFactType;
		}


		public EventType buildEventSet(String[] csv, String key) throws ParseException
		{
			EventType visitDimensionType = new EventType();

			if ((key == null) || (encryptMRN == false))
			{
				PatientIdType pid = new PatientIdType();
				pid.setValue(csv[vData.getDataLocation("PATI")]);
				visitDimensionType.setPatientId(pid);
			}
			else
			{
				PatientIdType pid = new PatientIdType();
				pid.setValue(crypt.encryptPatientIde(csv[vData.getDataLocation("PATI")]) + "_E");
				visitDimensionType.setPatientId(pid);
			}

			if ((key == null) || (encryptMRN == false))
			{
				EventType.EventId enc = new EventType.EventId();
				enc.setValue(csv[vData.getDataLocation("ENCO")]);
				enc.setSource(csv[vData.getDataLocation("ENCO")]);
				visitDimensionType.setEventId(enc);
			}
			else
			{
				EventType.EventId enc = new EventType.EventId();
				enc.setValue(crypt.encryptEncounterIde(csv[vData.getDataLocation("ENCO")]) + "_E");
				enc.setSource(csv[vData.getDataLocation("ENCO")] + "_E");
				visitDimensionType.setEventId(enc);
			}


			//TODO
			//visitDimensionType.setInoutCd(csv[vData.getDataLocation("INOU")]);
			//visitDimensionType.setLocationCd(csv[vData.getDataLocation("LOCA")]);
			//visitDimensionType.setLocationPath(csv[vData.getDataLocation("PATH")]);


			Date startDate = vData.getDate(csv[vData.getDataLocation("STAR")]);

			if (startDate != null) {
				visitDimensionType.setStartDate(dtoFactory.getXMLGregorianCalendar(
						startDate.getTime()));
			}

			// if (obsFactDetailFlag) {
			Date endDate =  vData.getDate(csv[vData.getDataLocation("ENDD")]);

			if (endDate != null) {
				visitDimensionType.setEndDate(dtoFactory.getXMLGregorianCalendar(
						endDate.getTime()));
			}



			if (!csv[vData.getDataLocation("UPDA")].equals("")) {
				visitDimensionType.setUpdateDate(dtoFactory.getXMLGregorianCalendar(
						vData.getDate(csv[vData.getDataLocation("UPDA")]).getTime()));
			}

			if (!csv[vData.getDataLocation("DOWN")].equals("")) {
				visitDimensionType.setDownloadDate(dtoFactory.getXMLGregorianCalendar(
						vData.getDate(csv[vData.getDataLocation("DOWN")]).getTime()));
			}

			if (!csv[vData.getDataLocation("IMPO")].equals("")) {
				visitDimensionType.setImportDate(dtoFactory.getXMLGregorianCalendar(
						vData.getDate(csv[vData.getDataLocation("IMPO")]).getTime()));
			}

			if (!csv[vData.getDataLocation("SOUR")].equals("")) {
				visitDimensionType.setSourcesystemCd(csv[vData.getDataLocation("SOUR")]);
			}
			return visitDimensionType;
		}


		public ObserverType buildProviderObserverSet(String[] csv, String key) throws ParseException
		{
			ObserverType providerDimensionType = new ObserverType();
			providerDimensionType.setObserverPath(csv[vData.getDataLocation("PATH")]);

			if (!csv[vData.getDataLocation("PROV")].equals("")) {
				providerDimensionType.setObserverCd(csv[vData.getDataLocation("PROV")]);
			}

			if (!csv[vData.getDataLocation("NAME")].equals("")) {
				providerDimensionType.setNameChar(csv[vData.getDataLocation("NAME")]);
			}


			if (!csv[vData.getDataLocation("UPDA")].equals("")) {
				providerDimensionType.setUpdateDate(dtoFactory.getXMLGregorianCalendar(
						vData.getDate(csv[vData.getDataLocation("UPDA")]).getTime()));
			}

			if (!csv[vData.getDataLocation("DOWN")].equals("")) {
				providerDimensionType.setDownloadDate(dtoFactory.getXMLGregorianCalendar(
						vData.getDate(csv[vData.getDataLocation("DOWN")]).getTime()));
			}

			if (!csv[vData.getDataLocation("IMPO")].equals("")) {
				providerDimensionType.setImportDate(dtoFactory.getXMLGregorianCalendar(
						vData.getDate(csv[vData.getDataLocation("IMPO")]).getTime()));
			}

			if (!csv[vData.getDataLocation("SOUR")].equals("")) {
				providerDimensionType.setSourcesystemCd(csv[vData.getDataLocation("SOUR")]);
			}
			return providerDimensionType;
		}	

		protected String generateMessageId() {
			StringWriter strWriter = new StringWriter();
			for(int i=0; i<20; i++) {
				int num = getValidAcsiiValue();
				//System.out.println("Generated number: " + num + " char: "+(char)num);
				strWriter.append((char)num);
			}
			return strWriter.toString();
		}

		private int getValidAcsiiValue() {
			int number = 48;
			while(true) {
				number = 48+(int) Math.round(Math.random() * 74);
				if((number > 47 && number < 58) || (number > 64 && number < 91) 
						|| (number > 96 && number < 123)) {
					break;
				}
			}
			return number;

		}
	}
}
