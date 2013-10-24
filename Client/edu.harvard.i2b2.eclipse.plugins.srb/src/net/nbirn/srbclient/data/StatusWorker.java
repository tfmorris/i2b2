package net.nbirn.srbclient.data;


import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import edu.harvard.i2b2.crc.loader.datavo.loader.query.GetUploadInfoRequestType;
import edu.harvard.i2b2.crc.loader.datavo.loader.query.LoadDataListResponseType;
import edu.harvard.i2b2.crc.loader.datavo.loader.query.LoadDataResponseType;
import edu.harvard.i2b2.eclipse.UserInfoBean;
import edu.harvard.i2b2.eclipse.plugins.fr.ws.CrcServiceDriver;
import edu.harvard.i2b2.eclipse.plugins.fr.ws.GetPublishDataResponseMessage;
import edu.harvard.i2b2.crc.loader.datavo.i2b2message.StatusType;


public class StatusWorker {
	private  final Log log = LogFactory.getLog(StatusWorker.class);
	//private final Display display;

	public StatusWorker() 
	{
	}



	public void updateLoadStatusDataRequest(final Display display, final Table table) //final Display theDisplay, final TreeViewer theViewer) 
	{
		try {
			GetUploadInfoRequestType parentType = new GetUploadInfoRequestType();

			parentType.setUserId(UserInfoBean.getInstance().getUserName());

			GetPublishDataResponseMessage msg = new GetPublishDataResponseMessage();
			StatusType procStatus = null;	
			while(procStatus == null || !procStatus.getType().equals("DONE")){
				String response = CrcServiceDriver.getLoadDataStatusRequest(parentType, "CRC");

				procStatus = msg.processResult(response);
				if (procStatus.getType().equals("ERROR")){		
					System.setProperty("errorMessage",  procStatus.getValue());				
					display.syncExec(new Runnable() {
						public void run() {

							MessageBox mBox = new MessageBox(table.getShell(), SWT.ICON_INFORMATION | SWT.OK);
							mBox.setText("Please Note ...");
							mBox.setMessage("Server reports: " +  System.getProperty("errorMessage"));
							mBox.open();

						}
					});
					return;
				}			
			}
			LoadDataListResponseType loadDataListResponse = msg.doReadLoad();  

			if (loadDataListResponse != null){
				table.removeAll();
				//String fileLocUri = allConcepts..getDataFileLocationUri();
				for (LoadDataResponseType data : loadDataListResponse.getLoadDataResponse())
				{
					log.debug(data.getLoadStatus());
					final TableItem abc = new TableItem(table,SWT.NONE);
					abc.setText(0, data.getUploadId());
					java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat();
					abc.setText(1, sdf.format(data.getStartDate().toGregorianCalendar().getTime()));
					abc.setText(2, data.getLoadStatus());
					if (data.getObservationSet() != null) {
						abc.setText(3, (data.getObservationSet().getTotalRecord() == null? "-1": Integer.toString(data.getObservationSet().getTotalRecord())));//	System.currentTimeMillis());
						abc.setText(4, (data.getObservationSet().getInsertedRecord() == null? "-1": Integer.toString(data.getObservationSet().getInsertedRecord())));//	System.currentTimeMillis());
					}
					if (data.getEventidSet() != null) {
						abc.setText(5, (data.getEventidSet().getTotalRecord() == null? "-1": Integer.toString(data.getEventidSet().getTotalRecord())));//	System.currentTimeMillis());
						abc.setText(6, (data.getEventidSet().getInsertedRecord() == null? "-1": Integer.toString(data.getEventidSet().getInsertedRecord())));//	System.currentTimeMillis());
					}
					if (data.getPidSet() != null) {
						abc.setText(7, (data.getPidSet().getTotalRecord() == null? "-1": Integer.toString(data.getPidSet().getTotalRecord())));//	System.currentTimeMillis());
						abc.setText(8, (data.getPidSet().getInsertedRecord() == null? "-1": Integer.toString(data.getPidSet().getInsertedRecord())));//	System.currentTimeMillis());
					}

					if (data.getEndDate() == null)
					{
						abc.setText(9, "");
					}
					else {
					
					abc.setText(9, sdf.format(data.getEndDate().toGregorianCalendar().getTime()));
					}

				}
				//edu.harvard.i2b2.crc.loader.datavo.loader.query.StatusType status = allConcepts.getStatus();
				//String fileLocUri = allConcepts.getDataFileLocationUri();
				//.getConcept();
				//getChildren().clear();
				//getNodesFromXMLString(concepts);
			}	

		} catch (AxisFault e) {
			log.error(e.getMessage());
			display.syncExec(new Runnable() {
				public void run() {
					// e.getMessage() == Incoming message input stream is null  -- for the case of connection down.

					MessageBox mBox = new MessageBox(table.getShell(), SWT.ICON_INFORMATION | SWT.OK);
					mBox.setText("Please Note ...");
					mBox.setMessage("Unable to make a connection to the remote server\n" +  
					"This is often a network error, please try again");
					mBox.open();

				}
			});
		} catch (Exception e) {
			log.error(e.getMessage());
			display.syncExec(new Runnable() {
				public void run() {
					// e.getMessage() == Incoming message input stream is null  -- for the case of connection down.
					MessageBox mBox = new MessageBox(table.getShell(), SWT.ICON_INFORMATION | SWT.OK);
					mBox.setText("Please Note ...");
					mBox.setMessage("Error message delivered from the remote server\n" +  
					"You may wish to retry your last action");
					mBox.open();
				}
			});			
		}

	}


	public void process(final  Display display, final Table table) throws Exception
	{
		Runnable longJob = new Runnable() {
			boolean done = false;
			public void run() {
				Thread thread = new Thread(new Runnable() {
					public void run() {

						display.syncExec(new Runnable() {
							public void run() {
								updateLoadStatusDataRequest(display, table);

							}
						});
						done = true;
						display.wake();
					}
				});
				thread.start();
				while (!done ) { //&& !display..isDisposed()) {
					if (!display.readAndDispatch())
						display.sleep();
				}
			}
		};

	}

}
