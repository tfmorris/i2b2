package edu.harvard.i2b2.eclipse.plugins.ontology.util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class MessageUtil {
    //to make this class singleton
	
    private static  final int MAX_STACK_SIZE = 28;
	
    private static MessageUtil thisInstance;
    private String navRequest;
    private String navResponse;
    private String findRequest;
    private String findResponse;
    private List<StackData> xmlStack = new ArrayList<StackData>();
    
    static {
            thisInstance = new MessageUtil();
    }
    
    public static MessageUtil getInstance() {
        return thisInstance;
    }

	public String getFindRequest() {
		return findRequest;
	}

	public void setFindRequest(String findRequest) {
		this.findRequest = findRequest;
		checkXmlStackSize();
		StackData stackData = new StackData();
		stackData.setMessage(findRequest);
		stackData.setName("Sent" + getTimestamp());
		xmlStack.add( stackData );
	}

	public String getFindResponse() {
		return findResponse;
	}

	public void setFindResponse(String findResponse) {
		this.findResponse = findResponse;
		checkXmlStackSize();
		StackData stackData = new StackData();
		stackData.setMessage(findResponse);
		stackData.setName("Received" + getTimestamp());
		xmlStack.add( stackData );
	}

	public String getNavRequest() {
		return navRequest;
	}

	public void setNavRequest(String navRequest) {
		this.navRequest = navRequest;
		checkXmlStackSize();
//if(xmlStack.size() == 11){
//	xmlStack.remove(0);
//    xmlStack = xmlStack.subList(1,10);
//}
		StackData stackData = new StackData();
		stackData.setMessage(navRequest);
		
		stackData.setName("Sent" + getTimestamp());
		xmlStack.add( stackData );
		
//		System.out.println(xmlStack.size() + " last " + xmlStack.get(xmlStack.size()-1).getName() );
	}

	public String getNavResponse() {
		return navResponse;
	}
	
	public List<StackData> getXmlStack(){
		return xmlStack;
	}

	public int getXmlStackSize(){
		return xmlStack.size();
	}
	
	public void setNavResponse(String navResponse) {
		this.navResponse = navResponse;
		checkXmlStackSize();
//if(xmlStack.size() == 11){
//	xmlStack.remove(0);
//	xmlStack = xmlStack.subList(1,10);
//}
		StackData stackData = new StackData();
		stackData.setMessage(navResponse);
		stackData.setName("Received" + getTimestamp());
		xmlStack.add( stackData );
		
//		System.out.println(xmlStack.size() + " last " + xmlStack.get(xmlStack.size()-1).getName() );
	}

	private String getTimestamp(){
		Calendar cldr = Calendar.getInstance(Locale.getDefault());
		
	//	Calendar cldr = Calendar.getInstance(TimeZone
	//			.getTimeZone("America/New_York"));
		String atTimestamp = "@"
				+ addZero(cldr.get(Calendar.HOUR_OF_DAY)) + ":"
				+ addZero(cldr.get(Calendar.MINUTE)) + ":"
				+ addZero(cldr.get(Calendar.SECOND));
		
		return atTimestamp;
	}
		
	private String addZero(int number) {
		String result = new Integer(number).toString();
		if (number < 10 && number >= 0) {
			result = "0" + result;
		}
		return result;
	}
	
	private void checkXmlStackSize(){
		if(xmlStack.size() == MAX_STACK_SIZE) {
			xmlStack.remove(0);
		    xmlStack = xmlStack.subList(1,MAX_STACK_SIZE-1);
		}
	}
	
}
