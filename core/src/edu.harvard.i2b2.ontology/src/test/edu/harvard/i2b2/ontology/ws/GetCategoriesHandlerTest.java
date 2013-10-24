package edu.harvard.i2b2.ontology.ws;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.ontology.datavo.vdo.GetReturnType;
import edu.harvard.i2b2.ontology.delegate.GetCategoriesHandler;

public class GetCategoriesHandlerTest {
	private static String getRequestText() {
	     return 
	     "<ns2:request xmlns:ns4=\"http://www.i2b2.org/xsd/hive/plugin/\" xmlns:ns3=\"http://www.i2b2.org/xsd/cell/ont/v2/\" xmlns:ns2=\"http://www.i2b2.org/xsd/hive/msg/\">" +
	     "<message_header>" +
	         "<i2b2_version_compatible>1.0</i2b2_version_compatible>" +
	         "<hl7_version_compatible>2.4</hl7_version_compatible>" +
	         "<sending_application>" +
	             "<application_name>i2b2 Ontology</application_name>" +
	             "<application_version>1.0</application_version>" +
	         "</sending_application>" +
	         "<sending_facility>" +
	             "<facility_name>i2b2 Hive</facility_name>" +
	         "</sending_facility>" +
	         "<receiving_application>" +
	             "<application_name>Ontology Cell</application_name>" +
	             "<application_version>1.0</application_version>" +
	         "</receiving_application>" +
	         "<receiving_facility>" +
	             "<facility_name>i2b2 Hive</facility_name>" +
	         "</receiving_facility>" +
	         "<datetime_of_message>2007-04-27T13:01:36.611-04:00</datetime_of_message>" +
	         "<security>" +
	             "<domain>demo</domain>" +
	             "<username>demo</username>" +
	             "<password>demouser</password>" +
	         "</security>" +
	         "<message_control_id>" +
	             "<message_num>W682mwsdqkj57yINyOur</message_num>" +
	             "<instance_num>0</instance_num>" +
	         "</message_control_id>" +
	         "<processing_id>" +
	             "<processing_id>P</processing_id>" +
	             "<processing_mode>I</processing_mode>" +
	         "</processing_id>" +
	         "<accept_acknowledgement_type>AL</accept_acknowledgement_type>" +
	         "<application_acknowledgement_type>AL</application_acknowledgement_type>" +
	         "<country_code>US</country_code>" +
	     "</message_header>" +
	     "<request_header>" +
	         "<result_waittime_ms>120000</result_waittime_ms>" +
	     "</request_header>" +
	     "<message_body>" +
	         "<ns3:get_categories type=\"core\"/>" +
	     "</message_body>" +
	 "</ns2:request>";
		
		}
	
	
	public static void main(String args[]){
		
	//	String parent = "\\\\testrpdr\\RPDR\\abc";
		String requestVdo = getRequestText();

		try {
			GetCategoriesDataMessage categoriesDataMsg = new GetCategoriesDataMessage(requestVdo);	

			
			GetCategoriesHandler handler = new GetCategoriesHandler(categoriesDataMsg);
			
			String output = handler.execute();
			
			System.out.println(output);
			
		} catch (I2B2Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
