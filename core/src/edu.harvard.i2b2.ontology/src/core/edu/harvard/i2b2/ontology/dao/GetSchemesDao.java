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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.ontology.datavo.vdo.ConceptType;
import edu.harvard.i2b2.ontology.datavo.vdo.GetReturnType;
import edu.harvard.i2b2.ontology.util.OntologyUtil;
import edu.harvard.i2b2.ontology.datavo.pm.ProjectType;

public class GetSchemesDao extends JdbcDaoSupport {
	
    private static Log log = LogFactory.getLog(GetSchemesDao.class);
//	final static String CORE = "c_hlevel, c_fullname, c_name, c_synonym_cd, c_visualattributes, c_totalnum, c_basecode, c_facttablecolumn, c_tablename, c_columnname, c_columndatatype, c_operator, c_dimcode, c_tooltip";
//	final static String ALL = DEFAULT + ", update_date, download_date, import_date, sourcesystem_cd, valuetype_cd";
	final static String DEFAULT = " key, name ";
//	final static String BLOB = ", c_metadataxml, c_comment ";
	
	public List findSchemes(final GetReturnType returnType) throws DataAccessException{
		DataSource ds = null;
		try {
			ds = OntologyUtil.getInstance().getDataSource();
		} catch (I2B2Exception e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} 
		SimpleJdbcTemplate jt = new SimpleJdbcTemplate(ds);
		
		// find return parameters
		String parameters = DEFAULT;		
//		if (returnType.getType().equals("core")){
//			parameters = CORE;
//		}
/*		else if (childrenType.getType().equals("all")){
			parameters = ALL;
		}
		if(childrenType.isBlob() == true)
			parameters = parameters + BLOB;*/
		
		// First step is get metadata schema name from properties file.
		String metadataSchema = "";
		try {
			metadataSchema = OntologyUtil.getInstance().getMetaDataSchemaName();
		} catch (I2B2Exception e1) {
			log.error(e1.getMessage());
		}
		
		String schemesSql = "select distinct " + parameters  + " from " + metadataSchema + "schemes ";
 
		ParameterizedRowMapper<ConceptType> mapper = new ParameterizedRowMapper<ConceptType>() {
	        public ConceptType mapRow(ResultSet rs, int rowNum) throws SQLException {
	            ConceptType scheme = new ConceptType();
	            //TODO fix this for all/+blob

		        scheme.setKey(rs.getString("key")); 
	            scheme.setName(rs.getString("name"));
	            if(returnType.getType().equals("core")) {
//	            	scheme.setBasecode(rs.getString("c_basecode"));
//	            	child.setLevel(rs.getInt("c_hlevel"));
//	            	child.setSynonymCd(rs.getString("c_synonym_cd"));
//	            	child.setVisualattributes(rs.getString("c_visualattributes"));
//	            	child.setTotalnum(rs.getInt("c_totalnum"));
//	            	child.setFacttablecolumn(rs.getString("c_facttablecolumn" ));
//	            	child.setTablename(rs.getString("c_tablename")); 
//	            	child.setColumnname(rs.getString("c_columnname")); 
//	            	child.setColumndatatype(rs.getString("c_columndatatype")); 
//	            	child.setOperator(rs.getString("c_operator")); 
//	            	child.setDimcode(rs.getString("c_dimcode")); 
//	            	child.setComment(rs.getString("c_comment")); 
//	            	child.setTooltip(rs.getString("c_tooltip"));
	            }
//	            if(childrenType.isBlob() == true)
//					try {
//						child.setMetadataxml(JDBCUtil.getClobString(rs.getClob("c_metadataxml")));
//					} catch (IOException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
	            return scheme;
	        }
	    };

		
	    // TODO loop for all projects/roles
		List queryResult = null;
		try {
		    queryResult = jt.query(schemesSql, mapper);
		} catch (DataAccessException e) {
			log.error(e.getMessage());
			throw e;
		}
		log.debug("result size = " + queryResult.size());
		
		
		
		return queryResult;

	}
	



}
