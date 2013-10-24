package edu.harvard.i2b2.ontology.ws;
public class StringTest {
	public static void main(String args[]){
		
		String parent = "\\\\testrpdr\\RPDR\\abc";
		
		int end = parent.indexOf("\\", 3);
	
	String key = parent.substring(2, end).trim();
	String path = parent.substring(end).trim();
	int abc = path.indexOf("\\", 2);
	System.out.println(key);
	System.out.println(path);
	System.out.println(path.substring(abc).trim());
		
	}
}