package edu.harvard.i2b2.common.util;

//import javax.ejb.EJBLocalHome;
import javax.jms.Queue;
import javax.jms.QueueConnectionFactory;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;


//import org.springframework.beans.factory.BeanFactory;
//import org.springframework.beans.factory.access.SingletonBeanFactoryLocator;
//import org.springframework.beans.factory.access.BeanFactoryLocator;
//import org.springframework.beans.factory.access.BeanFactoryReference;
//import org.springframework.context.support.FileSystemXmlApplicationContext;
//import org.springframework.beans.factory.ListableBeanFactory;
//import org.springframework.beans.propertyeditors.PropertiesEditor;
//import org.springframework.beans.factory.config.PropertiesFactoryBean;
//import org.springframework.core.io.ClassPathResource;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.io.IOException;

import edu.harvard.i2b2.common.exception.I2B2Exception;
/**
 * ServiceLocator class to get reference to resource like EJB, JMS, Datasource etc.
 * This is singleton class and caches resources.  
 * @author rk903
 */
public class ServiceLocator {

	//datasource bean name
	public static String DATASOURCE_BEAN_NAME = "dataSource"; 

	//default client property file name
	public static final String CLIENT_PROPERTY_CONFIG_LOCATION = "client.properties";
	
//	private static BeanFactory beanFactory = null ;
	
	private static Properties clientProperties = null;

	//to make this class singleton
	private static ServiceLocator thisInstance;
	
    private InitialContext ic;

    //used to hold references to EJBHomes/JMS Resources for re-use
    private Map cache; 

    
    static {
      try {
    	  thisInstance = new ServiceLocator();
      } catch(ServiceLocatorException se) {
        System.err.println(se);
        se.printStackTrace(System.err);
      }
    }

    private ServiceLocator() throws ServiceLocatorException  {
      try {
        ic = new InitialContext();
        cache = Collections.synchronizedMap(new HashMap());
      } catch (NamingException ne) {
            throw new ServiceLocatorException(ne);
      } catch (Exception e) {
            throw new ServiceLocatorException(e);
       }
    }
    
    
    public static ServiceLocator getInstance() {
        return thisInstance;
    }
    
	
	
//	public static BeanFactory getSpringFactory()  { 
//		if (beanFactory == null) {
//			 BeanFactoryLocator beanFactoryLoc = SingletonBeanFactoryLocator.getInstance();
//			 BeanFactoryReference beanFactoryRef = beanFactoryLoc.useBeanFactory("edu.harvard.i2b2Core2");
//			 beanFactory = beanFactoryRef.getFactory();
//			
//		}
//		return beanFactory;
//	}
//	
//	public static DataSource getDataSource() { 
//		if (beanFactory == null)	{
//			getSpringFactory();
//		}
//		DataSource ds = (DataSource)ServiceLocator.getSpringFactory().getBean(DATASOURCE_BEAN_NAME);
//		return ds;
//	}
//	
	/**
	 * Function to get client's property context.
	 * @return PropertyUtil
	 */
	public static Properties getClientProperty() throws I2B2Exception {
		if (clientProperties == null) { 
//			clientProperties = getProperties(CLIENT_PROPERTY_CONFIG_LOCATION);
		}
		return clientProperties;
	}
	
	
	/**
	 * Return Properties object, for input property file.
	 * @param propFileName
	 * @return java.util.Properties
	 * @throws I2B2Exception
	 */
//	public static Properties getProperties(String propFileName) throws I2B2Exception { 
//		PropertiesFactoryBean pfb = new PropertiesFactoryBean();
//		pfb.setLocation(new ClassPathResource(propFileName));
//		Properties props = null;
//		try {
//			 pfb.afterPropertiesSet();
//			 props = (Properties) pfb.getObject();
//		} catch (IOException ioEx) { 
//			throw new I2B2Exception("IOException " + ioEx.getMessage(),ioEx);
//		}
//		return props;
//	}
	
	/**
     * @return the factory for the factory to get queue connections from
     */
    public  QueueConnectionFactory getQueueConnectionFactory(String qConnFactoryName) 
                                                 throws ServiceLocatorException {
      QueueConnectionFactory factory = null;
      try {
        if (cache.containsKey(qConnFactoryName)) {
            factory = (QueueConnectionFactory) cache.get(qConnFactoryName);
        } else {
            factory = (QueueConnectionFactory) ic.lookup(qConnFactoryName);
            cache.put(qConnFactoryName, factory);
        }
      } catch (NamingException ne) {
        throw new ServiceLocatorException(ne);
      } catch (Exception e) {
            throw new ServiceLocatorException(e);
      }
      return factory;
    }


    /**
     * @return the Queue Destination to send messages to
     */
    public  Queue getQueue(String queueName) throws ServiceLocatorException {
      Queue queue = null;
      try {
        if (cache.containsKey(queueName)) {
            queue = (Queue) cache.get(queueName);
        } else {
            queue =(Queue)ic.lookup(queueName);
            cache.put(queueName, queue);
        }
      } catch (NamingException ne) {
            throw new ServiceLocatorException(ne);
      } catch (Exception e) {
            throw new ServiceLocatorException(e);
      }

      return queue;
    }
    
    /**
     * will get the ejb Local home factory. If this ejb home factory has already been
     * clients need to cast to the type of EJBHome they desire
     *
     * @return the EJB Home corresponding to the homeName
     */
//    public EJBLocalHome getLocalHome(String jndiHomeName) throws ServiceLocatorException {
//      EJBLocalHome home = null;
//      try { 
//        if (cache.containsKey(jndiHomeName)) {
//            home = (EJBLocalHome) cache.get(jndiHomeName);
//        } else {         
//            home = (EJBLocalHome) ic.lookup(jndiHomeName);
//            cache.put(jndiHomeName, home);
//        }
//       } catch (NamingException ne) {
//            throw new ServiceLocatorException(ne);
//       } catch (Exception e) {
//            throw new ServiceLocatorException(e);
//       }
//       return home;
//    }
    
	
}