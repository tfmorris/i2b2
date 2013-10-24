package edu.harvard.i2b2.ontology.ws;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.ontology.datavo.vdo.GetChildrenType;
import edu.harvard.i2b2.ontology.delegate.GetChildrenHandler;
import edu.harvard.i2b2.ontology.ws.GetChildrenDataMessage;

public class GetChildrenHandlerTest {
	
/*	private static String getRequestText() {
		StringBuffer queryStr = new StringBuffer();
		try {
			// Modify to point to your sample response 
			DataInputStream dataStream = new DataInputStream(new FileInputStream("OntGetChildrenRequest.xml"));
			while(dataStream.available()>0) {
				queryStr.append(dataStream.readLine() + "\n");
			}
			// Log query string
			System.out.println("queryStr " + queryStr);
		} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
				// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		return queryStr.toString();
	}*/
	
	
	private static String getRequestText() {
	     return 
			"<ns3:request xmlns:ns3=\"http://www.i2b2.org/xsd/hive/msg/\" xmlns:ns2=\"http://www.i2b2.org/xsd/cell/ont/v2/\"> " +
		    "<message_header> " +
		        "<i2b2_version_compatible>0.30</i2b2_version_compatible> " +
		        "<sending_facility> " +
		            "<facility_name>LCS</facility_name> " +
		        "</sending_facility> " +
		    "</message_header> " +
		    "<request_header> " +
		        "<result_waittime_ms>120000</result_waittime_ms> " +
		    "</request_header> " +
		    "<message_body> " +
		                "<ns2:get_children max=\"300\" hiddens=\"false\" synonyms=\"false\" type=\"default\" blob=\"true\"> " +
		                    "<parent>\\\\rpdr\\RPDR\\Labtests\\LAB\\(LLB16) Chemistry\\(LLB21) General Chemistries\\CA</parent> " + 
		                "</ns2:get_children>  " +
		    "</message_body> " +
		"</ns3:request>";
 //        "<parent>\\\\testrpdr\\RPDR\\HealthHistory\\PHY\\Health Maintenance\\Mammogram\\Mammogram - Deferred</parent> " +		
		}
	
	
	public static void main(String args[]){
		
	//	String parent = "\\\\testrpdr\\RPDR\\abc";
		String requestVdo = getRequestText();

		try {
			GetChildrenDataMessage childrenDataMsg = new GetChildrenDataMessage(requestVdo);	

			GetChildrenType type = childrenDataMsg.getChildrenType();
			
			GetChildrenHandler handler = new GetChildrenHandler(childrenDataMsg);
			
			String output = handler.execute();
			
			System.out.println(output);
			
		} catch (I2B2Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (JAXBUtilException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
	

	
	
	
