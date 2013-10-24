package edu.harvard.i2b2.ontology.ws;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.ontology.datavo.vdo.VocabRequestType;
import edu.harvard.i2b2.ontology.delegate.GetCodeInfoHandler;

public class GetCodeInfoHandlerTest {
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
		                "<ns2:get_code_info category=\"i2b2\" max=\"300\" hiddens=\"false\" synonyms=\"true\" type=\"default\" blob=\"false\"> " +
		                    "<match_str strategy=\"exact\">2015</match_str>" + 
		                "</ns2:get_code_info>  " +
		    "</message_body> " +
		"</ns3:request>";
			
		}
	
//test case --no synonym	
//   "<message_body> " +
//   "<ns2:get_code_info category=\"i2b2\" max=\"300\" hiddens=\"false\" synonyms=\"true\" type=\"default\" blob=\"false\"> " +
//       "<match_str strategy=\"exact\">LCS-I2B2:pulheight</match_str>" + 
//   "</ns2:get_code_info>  " +
//"</message_body> " +
//   "<ns2:get_code_info category=\"rpdr\" max=\"300\" hiddens=\"false\" synonyms=\"true\" type=\"default\" blob=\"false\"> " +
	
	public static void main(String args[]){
		
		String requestVdo = getRequestText();

		try {
			GetCodeInfoDataMessage codeInfoDataMsg = new GetCodeInfoDataMessage(requestVdo);	

			VocabRequestType type = codeInfoDataMsg.getVocabRequestType();
			
			GetCodeInfoHandler handler = new GetCodeInfoHandler(codeInfoDataMsg);
			
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
