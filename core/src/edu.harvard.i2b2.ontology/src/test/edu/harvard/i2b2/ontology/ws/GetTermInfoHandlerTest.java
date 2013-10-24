package edu.harvard.i2b2.ontology.ws;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.ontology.datavo.vdo.GetTermInfoType;
import edu.harvard.i2b2.ontology.delegate.GetTermInfoHandler;

public class GetTermInfoHandlerTest {
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
		                "<ns2:get_term_info max=\"300\" hiddens=\"false\" synonyms=\"false\" type=\"core\" blob=\"true\"> " +
		                    "<self>\\\\rpdr\\RPDR\\Labtests\\LAB\\(LLB16) Chemistry\\(LLB21) General Chemistries\\CA</self> " + 
		                "</ns2:get_term_info>  " +
		    "</message_body> " +
		"</ns3:request>";
//        "<parent>\\\\testrpdr\\RPDR\\HealthHistory\\PHY\\Health Maintenance\\Mammogram\\Mammogram - Deferred</parent> " +		
		}
	
	
	public static void main(String args[]){
		
	//	String parent = "\\\\testrpdr\\RPDR\\abc";
		String requestVdo = getRequestText();

		try {
			GetTermInfoDataMessage termInfoDataMsg = new GetTermInfoDataMessage(requestVdo);	

			GetTermInfoType type = termInfoDataMsg.getTermInfoType();
			
			GetTermInfoHandler handler = new GetTermInfoHandler(termInfoDataMsg);
			
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
