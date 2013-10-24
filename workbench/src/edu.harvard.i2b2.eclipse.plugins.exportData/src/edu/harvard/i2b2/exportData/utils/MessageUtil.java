package edu.harvard.i2b2.exportData.utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MessageUtil {
    // to make this class singleton

    private static final int MAX_STACK_SIZE = 28;

    private static MessageUtil thisInstance;
    private String request;
    private String response;
    private List<StackData> xmlStack = new ArrayList<StackData>();

    static {
	thisInstance = new MessageUtil();
    }

    public static MessageUtil getInstance() {
	return thisInstance;
    }

    public String getRequest() {
	return request;
    }

    public void setRequest(String request) {
	this.request = request;
	checkXmlStackSize();
	StackData stackData = new StackData();
	stackData.setMessage(request);
	stackData.setName("Sent" + getTimestamp());
	xmlStack.add(stackData);
    }

    public String getResponse() {
	return response;
    }

    public void setResponse(String response) {
	this.response = response;
	checkXmlStackSize();
	StackData stackData = new StackData();
	stackData.setMessage(response);
	stackData.setName("Received" + getTimestamp());
	xmlStack.add(stackData);
    }

    public List<StackData> getXmlStack() {
	return xmlStack;
    }

    public int getXmlStackSize() {
	return xmlStack.size();
    }

    public String getTimestamp() {
	Calendar cldr = Calendar.getInstance(Locale.getDefault());
	String atTimestamp = "@" + addZero(cldr.get(Calendar.HOUR_OF_DAY))
		+ ":" + addZero(cldr.get(Calendar.MINUTE)) + ":"
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

    private void checkXmlStackSize() {
	if (xmlStack.size() == MAX_STACK_SIZE) {
	    xmlStack.remove(0);
	    xmlStack = xmlStack.subList(1, MAX_STACK_SIZE - 1);
	}
    }

}