package edu.harvard.i2b2.eclipse.plugins.ontology.views.edit;

import java.util.ArrayList;

import edu.harvard.i2b2.eclipse.UserInfoBean;

public class Roles {
	private static Roles thisInstance;

	   static {
           thisInstance = new Roles();
   }
   
   public static Roles getInstance() {
       return thisInstance;
   }

   public boolean isRoleValid(){
 
	   ArrayList<String> roles = (ArrayList<String>) UserInfoBean.getInstance().getProjectRoles();
	   for(String param :roles) {
		   // Bug 728; enable feature for role = editor only
			//   if(param.equalsIgnoreCase("manager")) 
			//	   return true;
			 //  if(param.equalsIgnoreCase("admin")) 
			//	   return true;
			   if(param.equalsIgnoreCase("editor")) 
				   return true;
	    }
	   return false;
   }
	
}
