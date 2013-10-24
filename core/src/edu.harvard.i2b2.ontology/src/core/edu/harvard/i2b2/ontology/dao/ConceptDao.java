/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors:
 * 		Lori Phillips
 */
package edu.harvard.i2b2.ontology.dao;

import java.io.IOException;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.DOMOutputter;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.db.JDBCUtil;
import edu.harvard.i2b2.common.util.jaxb.DTOFactory;
import edu.harvard.i2b2.ontology.datavo.pm.ProjectType;
import edu.harvard.i2b2.ontology.datavo.vdo.ConceptType;
import edu.harvard.i2b2.ontology.datavo.vdo.GetChildrenType;
import edu.harvard.i2b2.ontology.datavo.vdo.GetReturnType;
import edu.harvard.i2b2.ontology.datavo.vdo.GetTermInfoType;
import edu.harvard.i2b2.ontology.datavo.vdo.VocabRequestType;
import edu.harvard.i2b2.ontology.datavo.vdo.XmlValueType;
import edu.harvard.i2b2.ontology.delegate.RequestHandler;
import edu.harvard.i2b2.ontology.ejb.DBInfoType;
import edu.harvard.i2b2.ontology.ejb.NodeType;
import edu.harvard.i2b2.ontology.util.OntologyUtil;
import edu.harvard.i2b2.ontology.util.StringUtil;

public class ConceptDao extends JdbcDaoSupport {
	
    private static Log log = LogFactory.getLog(ConceptDao.class);
    final static String CAT_CORE = " c_hlevel, c_fullname, c_name, c_synonym_cd, c_visualattributes, c_totalnum, c_basecode, c_facttablecolumn, c_dimtablename, c_columnname, c_columndatatype, c_operator, c_dimcode, c_tooltip ";
    final static String CAT_DEFAULT = " c_fullname, c_name ";

    final static String DEFAULT = " c_hlevel, c_fullname, c_name, c_synonym_cd, c_visualattributes, c_totalnum, c_basecode, c_facttablecolumn, c_tablename, c_columnname, c_columndatatype, c_operator, c_dimcode, c_tooltip ";
    final static String CORE = DEFAULT;
    final static String ALL = ", update_date, download_date, import_date, sourcesystem_cd, valuetype_cd,";
	final static String BLOB = ", c_metadataxml, c_comment ";

    final static String NAME_DEFAULT = " c_name ";
    
    private SimpleJdbcTemplate jt;
    
	private void setDataSource(String dataSource) {
		DataSource ds = null;
		try {
			ds = OntologyUtil.getInstance().getDataSource(dataSource);
		} catch (I2B2Exception e2) {
			log.error(e2.getMessage());;
		} 
		this.jt = new SimpleJdbcTemplate(ds);
	}
	
	private String getMetadataSchema() throws I2B2Exception{

		return OntologyUtil.getInstance().getMetaDataSchemaName();
	}
	
	public List findRootCategories(final GetReturnType returnType, final ProjectType projectInfo, final DBInfoType dbInfo) throws I2B2Exception, I2B2DAOException{
				
		// find return parameters
		String parameters = CAT_DEFAULT;		
		if (returnType.getType().equals("core")){
			parameters = CAT_CORE;
		}
/*		else if (returnType.getType().equals("all")){
			parameters = ALL;
		}
*/
		String metadataSchema = dbInfo.getDb_fullSchema();
		setDataSource(dbInfo.getDb_dataSource());
		
//		 First step is to call PM to see what roles user belongs to.
		
		if (projectInfo.getRole().size() == 0)
		{
			log.error("no role found for this user in project: " + projectInfo.getName());
			I2B2Exception e = new I2B2Exception("No role found for user");
			throw e;
		}
		
		Boolean protectedAccess = false;
		Iterator it = projectInfo.getRole().iterator();
		while (it.hasNext()){
			 String role = (String) it.next();
			 if(role.toLowerCase().equals("protected_access")) {
				 protectedAccess = true;
				 break;
			 }
		}

		ParameterizedRowMapper<ConceptType> mapper = new ParameterizedRowMapper<ConceptType>() {
	        public ConceptType mapRow(ResultSet rs, int rowNum) throws SQLException {
	            ConceptType child = new ConceptType();
	            //TODO fix this for all
		        child.setKey("\\\\" + rs.getString("c_table_cd")+ rs.getString("c_fullname")); 
	            child.setName(rs.getString("c_name"));
	            if(returnType.getType().equals("core")) {
	            	child.setBasecode(rs.getString("c_basecode"));
	            	child.setLevel(rs.getInt("c_hlevel"));
	            	child.setSynonymCd(rs.getString("c_synonym_cd"));
	            	child.setVisualattributes(rs.getString("c_visualattributes"));
	            	child.setTotalnum(rs.getInt("c_totalnum"));
	            	child.setFacttablecolumn(rs.getString("c_facttablecolumn" ));
	            	child.setTablename(rs.getString("c_dimtablename")); 
	            	child.setColumnname(rs.getString("c_columnname")); 
	            	child.setColumndatatype(rs.getString("c_columndatatype")); 
	            	child.setOperator(rs.getString("c_operator")); 
	            	child.setDimcode(rs.getString("c_dimcode")); 
	            	child.setTooltip(rs.getString("c_tooltip"));
	            }
	            return child;
	        }
	    };
	    
		List queryResult = null;

		if (!protectedAccess){
			String tablesSql = "select distinct(c_table_cd), " + parameters + " from " +  metadataSchema +  "table_access where c_protected_access = ?";

			try {
				queryResult = jt.query(tablesSql, mapper, "N");
			} catch (DataAccessException e) {
				log.error(e.getMessage());
				throw new I2B2DAOException("Database error");
			}
		}
		else{
			String tablesSql = "select distinct(c_table_cd), " + parameters + " from " +  metadataSchema +  "table_access";

			try {
				queryResult = jt.query(tablesSql, mapper);
			} catch (DataAccessException e) {
				log.error(e.getMessage());
				throw new I2B2DAOException("Database Error");
			}
		}
		log.debug("result size = " + queryResult.size());
		
		if (returnType.isBlob() == true && queryResult != null){
			Iterator itr = queryResult.iterator();
			while (itr.hasNext()){
				ConceptType child = (ConceptType) itr.next();
				String clobSql = "select c_metadataxml, c_comment from "+  metadataSchema +  "table_access where c_table_cd = ?";
				ParameterizedRowMapper<ConceptType> map = new ParameterizedRowMapper<ConceptType>() {
			        public ConceptType mapRow(ResultSet rs, int rowNum) throws SQLException {
			        	ConceptType concept = new ConceptType();
//			        	ResultSetMetaData rsmd = rs.getMetaData();
//			        	rsmd.get
			        	if(rs.getClob("c_metadataxml") == null){
			        		concept.setMetadataxml(null);
			        	}else {
			        		String c_xml = null;
			        		try {
			        			c_xml = JDBCUtil.getClobString(rs.getClob("c_metadataxml"));
			        		} catch (IOException e1) {
			        			log.error(e1.getMessage());
			        			concept.setMetadataxml(null);
			        		}
			        		if ((c_xml!=null)&&(c_xml.trim().length()>0)&&(!c_xml.equals("(null)")))
			        		{
			        			SAXBuilder parser = new SAXBuilder();
			        			java.io.StringReader xmlStringReader = new java.io.StringReader(c_xml);
			        			Element rootElement = null;
			        			try {
			        				org.jdom.Document metadataDoc = parser.build(xmlStringReader);
			        				org.jdom.output.DOMOutputter out = new DOMOutputter(); 
			        				Document doc = out.output(metadataDoc);
			        				rootElement = doc.getDocumentElement();
			        			} catch (JDOMException e) {
			        				log.error(e.getMessage());
			        				concept.setMetadataxml(null);
			        			} catch (IOException e) {
			        				log.error(e.getMessage());
			        				concept.setMetadataxml(null);
			        			}
			        			if(rootElement != null) {
			        				XmlValueType xml = new XmlValueType();									
			        				xml.getAny().add(rootElement);								
			        				concept.setMetadataxml(xml);
			        			}
			        		}else {
			        			concept.setMetadataxml(null);
			        		}
			        	}	

			        	if(rs.getClob("c_comment") == null){
			        		concept.setComment(null);
			        	}else {
			        		try {
								concept.setComment(JDBCUtil.getClobString(rs.getClob("c_comment")));
							} catch (IOException e) {
								log.error(e.getMessage());
								concept.setComment(null);
							}
			        	}	

			        	return concept;
			        }
				};
				List clobResult = null;
				try {
					clobResult = jt.query(clobSql, map, StringUtil.getTableCd(child.getKey()));
				} catch (DataAccessException e) {
					log.error(e.getMessage());
					throw new I2B2DAOException("Database Error");
				}
				if(clobResult != null)  {
					child.setMetadataxml(((ConceptType)(clobResult.get(0))).getMetadataxml());
					child.setComment(((ConceptType)(clobResult.get(0))).getComment());
				}
				else {
					child.setMetadataxml(null);
					child.setComment(null);
				}
					
			}
		}
		return queryResult;
	}
	
	public List findChildrenByParent(final GetChildrenType childrenType, ProjectType projectInfo, DBInfoType dbInfo) throws I2B2DAOException, I2B2Exception{

		// find return parameters
		String parameters = DEFAULT;		
		if (childrenType.getType().equals("core")){
			parameters = CORE;
		}
		else if (childrenType.getType().equals("all")){
			parameters = CORE + ALL;
		}
		if(childrenType.isBlob() == true)
			parameters = parameters + BLOB;
				
		String metadataSchema = dbInfo.getDb_fullSchema();
		setDataSource(dbInfo.getDb_dataSource());
		
		if (projectInfo.getRole().size() == 0)
		{
			log.error("no role found for this user in project: " + projectInfo.getName());
			I2B2Exception e = new I2B2Exception("No role found for user");
			throw e;
		}
		
		Boolean protectedAccess = false;
		Iterator it = projectInfo.getRole().iterator();
		while (it.hasNext()){
			 String role = (String) it.next();
			 if(role.toLowerCase().equals("protected_access")) {
				 protectedAccess = true;
				 break;
			 }
		}
		
		ParameterizedRowMapper<String> map = new ParameterizedRowMapper<String>() {
	        public String mapRow(ResultSet rs, int rowNum) throws SQLException {
	            String name = (rs.getString("c_table_name"));
	            return name;
	        }
		};
		
		//extract table code
		String tableCd = StringUtil.getTableCd(childrenType.getParent());
		String tableName=null;
		if (!protectedAccess){
			String tableSql = "select distinct(c_table_name) from " + metadataSchema + "table_access where c_table_cd = ? and c_protected_access = ? ";
	//		log.info("getChildren " + tableSql);
			try {
				tableName = jt.queryForObject(tableSql, map, tableCd, "N");	    
			} catch (DataAccessException e) {
				log.error(e.getMessage());
				throw new I2B2DAOException("Database Error");
			}
		}else {
			String tableSql = "select distinct(c_table_name) from " + metadataSchema + "table_access where c_table_cd = ?";
			try {
				tableName = jt.queryForObject(tableSql, map, tableCd);	    
			} catch (DataAccessException e) {
				log.error(e.getMessage());
				throw new I2B2DAOException("Database Error");
			}
		}

		String path = StringUtil.getPath(childrenType.getParent());
		String searchPath = path + "%";

// Lookup to get chlevel + 1 ---  dont allow synonyms so we only get one result back
				
		String levelSql = "select c_hlevel from " + metadataSchema+tableName  + " where c_fullname = ?  and c_synonym_cd = 'N'";

	    int level = 0;
		try {
			level = jt.queryForInt(levelSql, path);
		} catch (DataAccessException e1) {
			// should only get 1 result back  (path == c_fullname which should be unique)
			log.error(e1.getMessage());
			throw new I2B2DAOException("Database Error");
		}

		String hidden = "";
		if(childrenType.isHiddens() == false)
			hidden = " and c_visualattributes not like '_H%'";
	
		String synonym = "";
		if(childrenType.isSynonyms() == false)
			synonym = " and c_synonym_cd = 'N'";
		
		String sql = "select " + parameters +" from " + metadataSchema+tableName  + " where c_fullname like ? and c_hlevel = ? "; 
		sql = sql + hidden + synonym + " order by c_name ";
 
		//log.info(sql + " " + path + " " + level);
		
		ParameterizedRowMapper<ConceptType> mapper = getMapper(new NodeType(childrenType));
		
		List queryResult = null;
		try {
			queryResult = jt.query(sql, mapper, searchPath, (level + 1) );
		} catch (DataAccessException e) {
			log.error(e.getMessage());
			throw new I2B2DAOException("Database Error");
		}
		log.debug("result size = " + queryResult.size());
		
		
		return queryResult;
		// tested statement with aqua data studio   verified output from above against this. 
		// select  c_fullname, c_name, c_synonym_cd, c_visualattributes  from metadata.testrpdr 
		// where c_fullname like '\RPDR\Diagnoses\Circulatory system (390-459)\Arterial vascular disease (440-447)\(446) Polyarteritis nodosa and al%' 
		// and c_hlevel = 5  and c_visualattributes not like '_H%' and c_synonym_cd = 'N'
		
		// verified both with and without hiddens and synonyms.
		
		// clob test   level = 4
		//   <parent>\\testrpdr\RPDR\HealthHistory\PHY\Health Maintenance\Mammogram\Mammogram - Deferred</parent> 
	}

	public List findByFullname(final GetTermInfoType termInfoType, ProjectType projectInfo, DBInfoType dbInfo) throws DataAccessException, I2B2Exception{

		// find return parameters
		String parameters = DEFAULT;		
		if (termInfoType.getType().equals("core")){
			parameters = CORE;
		}
		else if (termInfoType.getType().equals("all")){
			parameters = CORE + ALL;
		}
		if(termInfoType.isBlob() == true)
			parameters = parameters + BLOB;
				
		String metadataSchema = dbInfo.getDb_fullSchema();
		setDataSource(dbInfo.getDb_dataSource());
		
		if (projectInfo.getRole().size() == 0)
		{
			log.error("no role found for this user in project: " + projectInfo.getName());
			I2B2DAOException e = new I2B2DAOException("No role found for user");
			throw e;
		}
		
		Boolean protectedAccess = false;
		Iterator it = projectInfo.getRole().iterator();
		while (it.hasNext()){
			 String role = (String) it.next();
			 if(role.toLowerCase().equals("protected_access")) {
				 protectedAccess = true;
				 break;
			 }
		}
		
		//tableCd to table name conversion
		ParameterizedRowMapper<String> map = new ParameterizedRowMapper<String>() {
	        public String mapRow(ResultSet rs, int rowNum) throws SQLException {
	            String name = (rs.getString("c_table_name"));
	            return name;
	        }
		};
		
		//extract table code
		String tableCd = StringUtil.getTableCd(termInfoType.getSelf());
		String tableName=null;
		if (!protectedAccess){
			String tableSql = "select distinct(c_table_name) from " + metadataSchema + "table_access where c_table_cd = ? and c_protected_access = ? ";
			try {
				tableName = jt.queryForObject(tableSql, map, tableCd, "N");	    
			} catch (DataAccessException e) {
				log.error(e.getMessage());
				throw e;
			}
		}else {
			String tableSql = "select distinct(c_table_name) from " + metadataSchema + "table_access where c_table_cd = ?";
			try {
				tableName = jt.queryForObject(tableSql, map, tableCd);	    
			} catch (DataAccessException e) {
				log.error(e.getMessage());
				throw e;
			}
		}

		String path = StringUtil.getPath(termInfoType.getSelf());
		String searchPath = path;


		String hidden = "";
		if(termInfoType.isHiddens() == false)
			hidden = " and c_visualattributes not like '_H%'";
	
		String synonym = "";
		if(termInfoType.isSynonyms() == false)
			synonym = " and c_synonym_cd = 'N'";
		
		String sql = "select " + parameters +" from " + metadataSchema+tableName  + " where c_fullname like ?  "; 
		sql = sql + hidden + synonym + " order by c_name ";
 
		//log.info(sql + " " + path + " " + level);
		
		ParameterizedRowMapper<ConceptType> mapper = getMapper(new NodeType(termInfoType));

		List queryResult = null;
		try {
			queryResult = jt.query(sql, mapper, searchPath );
		} catch (DataAccessException e) {
			log.error(e.getMessage());
			throw e;
		}
		log.debug("result size = " + queryResult.size());
		
		
		return queryResult;

	}
	
	public List findNameInfo(final VocabRequestType vocabType, ProjectType projectInfo, DBInfoType dbInfo) throws DataAccessException, I2B2Exception{

		// find return parameters
		String parameters = NAME_DEFAULT;		
		if (vocabType.getType().equals("core")){
			parameters = CORE;
		}
		else if (vocabType.getType().equals("all")){
			parameters = CORE + ALL;
		}
		if(vocabType.isBlob() == true)
			parameters = parameters + BLOB;
				
		String metadataSchema = dbInfo.getDb_fullSchema();
		setDataSource(dbInfo.getDb_dataSource());
		
	//	log.info(metadataSchema);
		
		if (projectInfo.getRole().size() == 0)
		{
			log.error("no role found for this user in project: " + projectInfo.getName());
			I2B2Exception e = new I2B2Exception("No role found for user");
			throw e;
		}
		
		Boolean protectedAccess = false;
		Iterator it = projectInfo.getRole().iterator();
		while (it.hasNext()){
			 String role = (String) it.next();
			 if(role.toLowerCase().equals("protected_access")) {
				 protectedAccess = true;
				 break;
			 }
		}
		
		//tableCd to table name conversion
		ParameterizedRowMapper<String> map = new ParameterizedRowMapper<String>() {
	        public String mapRow(ResultSet rs, int rowNum) throws SQLException {
	            String name = (rs.getString("c_table_name"));
	            return name;
	        }
		};
		
		//extract table code
		String tableCd = vocabType.getCategory();
		String tableName=null;
		if (!protectedAccess){
			String tableSql = "select distinct(c_table_name) from " + metadataSchema + "table_access where c_table_cd = ? and c_protected_access = ? ";
			try {
				tableName = jt.queryForObject(tableSql, map, tableCd, "N");	    
			} catch (DataAccessException e) {
				log.error(e.getMessage());
				throw e;
			}
		}else {
			String tableSql = "select distinct(c_table_name) from " + metadataSchema + "table_access where c_table_cd = ?";
			try {
				tableName = jt.queryForObject(tableSql, map, tableCd);	    
			} catch (DataAccessException e) {
				log.error(e.getMessage());
				throw e;
			}
		}

		 String nameInfoSql = null;
		    String compareName = null;
			
		    if(vocabType.getMatchStr().getStrategy().equals("exact")) {
		    	nameInfoSql = "select " + parameters  + " from " + metadataSchema+tableName + " where upper(c_name) = ?  ";
		    	compareName = vocabType.getMatchStr().getValue().toUpperCase();
		    }
		    
		    else if(vocabType.getMatchStr().getStrategy().equals("left")){
		    	nameInfoSql = "select " + parameters  + " from " + metadataSchema+tableName +" where upper(c_name) like ?  ";
		    	compareName = vocabType.getMatchStr().getValue().toUpperCase() + "%";
		    }
		    
		    else if(vocabType.getMatchStr().getStrategy().equals("right")) {
		    	nameInfoSql = "select " + parameters  + " from " + metadataSchema+tableName +" where upper(c_name) like ?  ";
		    	compareName =  "%" + vocabType.getMatchStr().getValue().toUpperCase();
		    }
		    
		    else if(vocabType.getMatchStr().getStrategy().equals("contains")) {
		    	nameInfoSql = "select " + parameters  + " from " + metadataSchema+tableName +" where upper(c_name) like ?  ";
		    	compareName =  "%" + vocabType.getMatchStr().getValue().toUpperCase() + "%";
		    }
		    


		String hidden = "";
		if(vocabType.isHiddens() == false)
			hidden = " and c_visualattributes not like '_H%'";
	
		String synonym = "";
		if(vocabType.isSynonyms() == false)
			synonym = " and c_synonym_cd = 'N'";
		
		nameInfoSql = nameInfoSql + hidden + synonym + " order by c_name ";
	    
	//	log.info(nameInfoSql + " " +compareName);
		
		ParameterizedRowMapper<ConceptType> mapper = getMapper(new NodeType(vocabType));

		List queryResult = null;
		try {
			queryResult = jt.query(nameInfoSql, mapper, compareName );
		} catch (DataAccessException e) {
			log.error(e.getMessage());
			throw e;
		}
		log.debug("result size = " + queryResult.size());
		
		
		return queryResult;

	}
	
	public List findCodeInfo(final VocabRequestType vocabType, ProjectType projectInfo, DBInfoType dbInfo) throws DataAccessException, I2B2Exception{

		// find return parameters
		String parameters = NAME_DEFAULT;		
		if (vocabType.getType().equals("core")){
			parameters = CORE;
		}
		else if (vocabType.getType().equals("all")){
			parameters = CORE + ALL;
		}
		if(vocabType.isBlob() == true)
			parameters = parameters + BLOB;
				
		String metadataSchema = dbInfo.getDb_fullSchema();
		setDataSource(dbInfo.getDb_dataSource());
		
//		log.info(metadataSchema);
		
		if (projectInfo.getRole().size() == 0)
		{
			log.error("no role found for this user in project: " + projectInfo.getName());
			I2B2Exception e = new I2B2Exception("No role found for user");
			throw e;
		}
		
		Boolean protectedAccess = false;
		Iterator it = projectInfo.getRole().iterator();
		while (it.hasNext()){
			 String role = (String) it.next();
			 if(role.toLowerCase().equals("protected_access")) {
				 protectedAccess = true;
				 break;
			 }
		}
		
		ParameterizedRowMapper<String> map = new ParameterizedRowMapper<String>() {
	        public String mapRow(ResultSet rs, int rowNum) throws SQLException {
	           String name =  rs.getString("c_table_name");
	           return name;
	        }
		};
		
		//no table code provided so check all tables user has access to
		List tableNames=null;
		if (!protectedAccess){
			String tableSql = "select distinct(c_table_name) from " + metadataSchema + "table_access where c_protected_access = ? ";
//			log.info(tableSql);
			try {
				tableNames = jt.query(tableSql, map, "N");	    
			} catch (DataAccessException e) {
				log.error(e.getMessage());
				throw e;
			}
		}else {
			String tableSql = "select distinct(c_table_name) from " + metadataSchema + "table_access ";
			try {
				tableNames = jt.query(tableSql, map);	    
			} catch (DataAccessException e) {
				log.error(e.getMessage());
				throw e;
			}
		}

		String hidden = "";
		if(vocabType.isHiddens() == false)
			hidden = " and c_visualattributes not like '_H%'";
	
		String synonym = "";
		if(vocabType.isSynonyms() == false)
			synonym = " and c_synonym_cd = 'N'";
		
		
		 String whereClause = null;
			
		    if(vocabType.getMatchStr().getStrategy().equals("exact")) {
		    	whereClause = " where upper(c_basecode) = '" + vocabType.getMatchStr().getValue().toUpperCase()+ "'";
		    }
		    
		    else if(vocabType.getMatchStr().getStrategy().equals("left")){
		    	whereClause = " where upper(c_basecode) like '" + vocabType.getMatchStr().getValue().toUpperCase() + "%'";
		    }
		    
		    else if(vocabType.getMatchStr().getStrategy().equals("right")) {
		    	whereClause = " where upper(c_basecode) like " + "'%" + vocabType.getMatchStr().getValue().toUpperCase() + "'";
		    }
		    
		    else if(vocabType.getMatchStr().getStrategy().equals("contains")) {
		    	whereClause = " where upper(c_basecode) like " +  "'%" + vocabType.getMatchStr().getValue().toUpperCase() + "%'";
		    }
		    


		log.debug(vocabType.getMatchStr().getStrategy() + whereClause);
		
		String codeInfoSql = null;
		if(tableNames != null){
			Iterator itTn = tableNames.iterator();
			String table = (String)itTn.next();
			String tableCdSql = ", (select distinct(c_table_cd) from "+ metadataSchema + "TABLE_ACCESS where c_table_name = '"+  table+ "') as tableCd"; 
			String basecode = " '" + vocabType.getMatchStr().getValue() + "' ";
			codeInfoSql = "select " + parameters + tableCdSql + " from " + metadataSchema + table + whereClause	+ hidden + synonym;;
			while(itTn.hasNext()){		
				table = (String)itTn.next();
				tableCdSql = ", (select distinct(c_table_cd) from "+ metadataSchema + "TABLE_ACCESS where c_table_name = '"+  table + "') as tableCd"; 
				codeInfoSql = codeInfoSql +  " union all (select "+ parameters + tableCdSql + " from " + metadataSchema + table + whereClause
				+ hidden + synonym + ")";
			}
			codeInfoSql = codeInfoSql + " order by c_name ";
		}
		else
			return null;
	    
//		log.info(codeInfoSql);
		
		ParameterizedRowMapper<ConceptType> mapper = getMapper(new NodeType(vocabType));

		List queryResult = null;
		try {
			queryResult = jt.query(codeInfoSql, mapper);
		} catch (DataAccessException e) {
			log.error(e.getMessage());
			throw e;
		}
		log.debug("result size = " + queryResult.size());
		
		
		return queryResult;

	}
	
	
	private ParameterizedRowMapper<ConceptType> getMapper(final NodeType node){

		ParameterizedRowMapper<ConceptType> mapper = new ParameterizedRowMapper<ConceptType>() {
        public ConceptType mapRow(ResultSet rs, int rowNum) throws SQLException {
            ConceptType child = new ConceptType();	          
            child.setName(rs.getString("c_name"));
            if(!(node.getType().equals("default"))) {
            child.setBasecode(rs.getString("c_basecode"));
            child.setLevel(rs.getInt("c_hlevel"));
            // cover get Code Info case where we dont know the vocabType.category apriori
            if(node.getNode() == null){
            	if(rs.getString("tableCd") != null)
            		node.setNode(rs.getString("tableCd"));
            }
            child.setKey("\\\\" + node.getNode() + rs.getString("c_fullname"));  
            child.setSynonymCd(rs.getString("c_synonym_cd"));
            child.setVisualattributes(rs.getString("c_visualattributes"));
            child.setTotalnum(rs.getInt("c_totalnum"));
            child.setFacttablecolumn(rs.getString("c_facttablecolumn" ));
            child.setTablename(rs.getString("c_tablename")); 
            child.setColumnname(rs.getString("c_columnname")); 
            child.setColumndatatype(rs.getString("c_columndatatype")); 
            child.setOperator(rs.getString("c_operator")); 
            child.setDimcode(rs.getString("c_dimcode")); 
            child.setTooltip(rs.getString("c_tooltip"));
            }
            if(node.isBlob() == true){
				try {
					if(rs.getClob("c_comment") == null)
						child.setComment(null);
					else
						child.setComment(JDBCUtil.getClobString(rs.getClob("c_comment")));
				} catch (IOException e) {
        			log.error(e.getMessage());
        			child.setComment(null);
				} 

				if(rs.getClob("c_metadataxml") == null){
					child.setMetadataxml(null);
				}else {
					String c_xml = null;
					try {
						c_xml = JDBCUtil.getClobString(rs.getClob("c_metadataxml"));
					} catch (IOException e) {
						log.error(e.getMessage());
            			child.setMetadataxml(null);
					}
					if ((c_xml!=null)&&(c_xml.trim().length()>0)&&(!c_xml.equals("(null)")))
					{
						SAXBuilder parser = new SAXBuilder();
						java.io.StringReader xmlStringReader = new java.io.StringReader(c_xml);
						Element rootElement = null;
						try {
							org.jdom.Document metadataDoc = parser.build(xmlStringReader);
							org.jdom.output.DOMOutputter out = new DOMOutputter(); 
							Document doc = out.output(metadataDoc);
							rootElement = doc.getDocumentElement();
						} catch (JDOMException e) {
							log.error(e.getMessage());
	            			child.setMetadataxml(null);
            			} catch (IOException e1) {
	            			log.error(e1.getMessage());
	            			child.setMetadataxml(null);
						}
            			if (rootElement != null) {
            				XmlValueType xml = new XmlValueType();
            				xml.getAny().add(rootElement);
            				child.setMetadataxml(xml);
            			}
					}else {
						child.setMetadataxml(null);
					}
				}	

            }
			if((node.getType().equals("all"))){
				DTOFactory factory = new DTOFactory();
				// make sure date isnt null before converting to XMLGregorianCalendar
				Date date = rs.getDate("update_date");
				if (date == null)
					child.setUpdateDate(null);
				else 
					child.setUpdateDate(factory.getXMLGregorianCalendar(date.getTime())); 

				date = rs.getDate("download_date");
				if (date == null)
					child.setDownloadDate(null);
				else 
					child.setDownloadDate(factory.getXMLGregorianCalendar(date.getTime())); 

				date = rs.getDate("import_date");
				if (date == null)
					child.setImportDate(null);
				else 
					child.setImportDate(factory.getXMLGregorianCalendar(date.getTime())); 

	            child.setSourcesystemCd(rs.getString("sourcesystem_cd"));
	            child.setValuetypeCd(rs.getString("valuetype_cd"));
			}
            return child;
        }
    };
    return mapper;
	}
	
}
