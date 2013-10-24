package edu.harvard.i2b2.ontology.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.w3c.dom.Element;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.xml.XMLUtil;
import edu.harvard.i2b2.ontology.datavo.pm.ProjectType;
import edu.harvard.i2b2.ontology.datavo.vdo.ConceptType;
import edu.harvard.i2b2.ontology.datavo.vdo.ModifyChildType;
import edu.harvard.i2b2.ontology.datavo.vdo.XmlValueType;
import edu.harvard.i2b2.ontology.datavo.vdo.DeleteChildType;
import edu.harvard.i2b2.ontology.ejb.DBInfoType;
import edu.harvard.i2b2.ontology.util.OntologyUtil;
import edu.harvard.i2b2.ontology.util.StringUtil;


public class ConceptPersistDao extends JdbcDaoSupport {

	private static Log log = LogFactory.getLog(ConceptPersistDao.class);
	
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
	
		public int addNode(final ConceptType addChildType, ProjectType projectInfo, DBInfoType dbInfo) throws I2B2DAOException, I2B2Exception{

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
				 if(role.toUpperCase().equals("DATA_PROT")) {
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
			String tableCd = StringUtil.getTableCd(addChildType.getKey());
			// table code to table name conversion
			String tableName=null;
			if (!protectedAccess){
				String tableSql = "select distinct(c_table_name) from " + metadataSchema + "table_access where c_table_cd = ? and c_protected_access = ? ";
				try {
					tableName = jt.queryForObject(tableSql, map, tableCd, "N");	    
				} catch (DataAccessException e) {
					log.error(tableSql + tableCd);
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
			
		int numRowsAdded = -1;
		try {
			Date today = Calendar.getInstance().getTime();
			String xml = null;
			XmlValueType metadataXml=addChildType.getMetadataxml();
			if (metadataXml != null) {
				String addSql = "insert into " + metadataSchema+tableName  + 
				"(c_hlevel, c_fullname, c_name, c_synonym_cd, c_visualattributes, c_basecode, c_metadataxml, c_facttablecolumn, c_tablename, c_columnname, c_columndatatype, c_operator, c_dimcode, c_comment, c_tooltip, import_date, update_date, download_date, sourcesystem_cd, valuetype_cd) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				log.info(addSql);

				Element element = metadataXml.getAny().get(0);
				if(element != null)
					xml = XMLUtil.convertDOMElementToString(element);
				numRowsAdded = jt.update(addSql, 
						addChildType.getLevel(), StringUtil.getPath(addChildType.getKey()),addChildType.getName(), addChildType.getSynonymCd(), 
						addChildType.getVisualattributes(), addChildType.getBasecode(), xml, addChildType.getFacttablecolumn() ,addChildType.getTablename() ,
						addChildType.getColumnname() , addChildType.getColumndatatype() ,addChildType.getOperator() ,addChildType.getDimcode() ,addChildType.getComment() ,
						addChildType.getTooltip(),today,  today,today, addChildType.getSourcesystemCd() ,addChildType.getValuetypeCd());
			}		
			else {
				String addSql = "insert into " + metadataSchema+tableName  + 
				"(c_hlevel, c_fullname, c_name, c_synonym_cd, c_visualattributes, c_basecode, c_facttablecolumn, c_tablename, c_columnname, c_columndatatype, c_operator, c_dimcode, c_comment, c_tooltip, import_date, update_date, download_date,sourcesystem_cd, valuetype_cd) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				log.info(addSql);
				numRowsAdded = jt.update(addSql, 
						addChildType.getLevel(), StringUtil.getPath(addChildType.getKey()),addChildType.getName(), addChildType.getSynonymCd(), 
						addChildType.getVisualattributes(), addChildType.getBasecode(), addChildType.getFacttablecolumn() ,addChildType.getTablename() ,
						addChildType.getColumnname() , addChildType.getColumndatatype() ,addChildType.getOperator() ,addChildType.getDimcode() ,addChildType.getComment() ,
						addChildType.getTooltip(), today, today,today, addChildType.getSourcesystemCd() ,addChildType.getValuetypeCd());
			}
		} catch (DataAccessException e) {
			log.error("Dao addChild failed");
			log.error(e.getMessage());
			throw new I2B2DAOException("Data access error " , e);
		}

		log.debug("Number of rows added: " + numRowsAdded);

		return numRowsAdded;

		}
		
		public int deleteNode(final DeleteChildType deleteChildType, ProjectType projectInfo, DBInfoType dbInfo) throws DataAccessException, I2B2Exception{
			String metadataSchema = dbInfo.getDb_fullSchema();
			String serverType = dbInfo.getDb_serverType();
			setDataSource(dbInfo.getDb_dataSource());
			
				
			if (projectInfo.getRole().size() == 0)
			{
				log.error("no role found for this user in project: " + projectInfo.getName());
				I2B2Exception e = new I2B2Exception("No role found for user");
				throw e;
			}
			
			Boolean protectedAccess = false;
			Iterator<String> it = projectInfo.getRole().iterator();
			while (it.hasNext()){
				 String role = (String) it.next();
				 if(role.toUpperCase().equals("DATA_PROT")) {
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
			String tableCd = StringUtil.getTableCd(deleteChildType.getKey());
			// table code to table name conversion
			String tableName=null;
			if (!protectedAccess){
				String tableSql = "select distinct(c_table_name) from " + metadataSchema + "table_access where c_table_cd = ? and c_protected_access = ? ";
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
			//Mark node for deletion  --- change visAttrib to Hidden
		
/*			String updateSql = " update " + metadataSchema+tableName  + " set update_date = ?, c_visualattributes = ?  where c_fullname = ? and c_basecode = ?";
			String updateChildrenSql = null;
			if(deleteChildType.isIncludeChildren()){
				if(serverType.equals("ORACLE"))
					updateChildrenSql = " update " + metadataSchema+tableName  + " set update_date = ?, " +
						"c_visualattributes = concat(substr(c_visualattributes,1,1) ,'HE' ) where c_fullname like ? ";
				else 
					updateChildrenSql = " update " + metadataSchema+tableName  + " set update_date = ?, " +
					"c_visualattributes = substring(c_visualattributes,1,1) + 'H' + substring(c_visualattributes,3,1) where c_fullname like ? ";
			}
	
			String deleteSql = " delete from " + metadataSchema+tableName  + " where c_fullname = ? and c_name = ? and c_synonym_cd = ? and c_basecode = ?";
			String deleteChildrenSql =  " delete from " + metadataSchema+tableName  + " where c_fullname like ? and c_visualattributes like '%E'";
			int numRowsDeleted = -1;
			try {
		//		log.info(sql + " " + w_index);

				numRowsDeleted = jt.update(updateSql,Calendar.getInstance().getTime(), deleteChildType.getVisualattribute(), StringUtil.getPath(deleteChildType.getKey()),
							deleteChildType.getBasecode());
				if(updateChildrenSql != null)
					numRowsDeleted += jt.update(updateChildrenSql, Calendar.getInstance().getTime(),StringUtil.getPath(deleteChildType.getKey())+"%");
				
 */

			String deleteChildrenSql = null;
			String deleteSql = " delete from " + metadataSchema+tableName  + " where c_fullname = ? and c_basecode = ?";
			if(deleteChildType.isIncludeChildren()){	
				deleteChildrenSql =  " delete from " + metadataSchema+tableName  + " where c_fullname like ? and c_visualattributes like '%E'";
			}
			int numRowsDeleted = -1;
			try{	
				numRowsDeleted = jt.update(deleteSql, StringUtil.getPath(deleteChildType.getKey()), deleteChildType.getBasecode());
				if(deleteChildrenSql != null)
					numRowsDeleted += jt.update(deleteChildrenSql, StringUtil.getPath(deleteChildType.getKey())+"%");
			} catch (DataAccessException e) {
				log.error("Dao deleteChild failed");
				log.error(e.getMessage());
				throw e;
			}
			log.debug("Number of rows deleted " + numRowsDeleted);
			return numRowsDeleted;

		}
		
		public int modifyNode(final ModifyChildType modifyChildType, ProjectType projectInfo, DBInfoType dbInfo) throws DataAccessException, I2B2Exception{
			String metadataSchema = dbInfo.getDb_fullSchema();
	//		String serverType = dbInfo.getDb_serverType();
			setDataSource(dbInfo.getDb_dataSource());
			
			Date today = Calendar.getInstance().getTime();	
			if (projectInfo.getRole().size() == 0)
			{
				log.error("no role found for this user in project: " + projectInfo.getName());
				I2B2Exception e = new I2B2Exception("No role found for user");
				throw e;
			}
			
			Boolean protectedAccess = false;
			Iterator<String> it = projectInfo.getRole().iterator();
			while (it.hasNext()){
				 String role = (String) it.next();
				 if(role.toUpperCase().equals("DATA_PROT")) {
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
			String tableCd = StringUtil.getTableCd(modifyChildType.getSelf().getKey());
			// table code to table name conversion
			String tableName=null;
			if (!protectedAccess){
				String tableSql = "select distinct(c_table_name) from " + metadataSchema + "table_access where c_table_cd = ? and c_protected_access = ? ";
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

			log.info("path: " + StringUtil.getPath(modifyChildType.getSelf().getKey()));

	
			String updateSql = " update " + metadataSchema+tableName  + " set update_date = ?, c_visualattributes = ?, c_tooltip = ?, c_name = ?, c_basecode = ?, valuetype_cd = ?, " +
					" c_tablename = ?, c_columnname = ?, c_facttablecolumn = ?, c_operator = ?, c_columndatatype = ?, c_metadataxml = ? where c_fullname = ? and c_synonym_cd = 'N'";

	//		log.info(updateSql);
			
		int numRowsModified= -1;
			try {

				String xml = "";
				XmlValueType metadataXml=modifyChildType.getSelf().getMetadataxml();
				if (metadataXml != null){
					Element element = metadataXml.getAny().get(0);
					if(element != null)
						xml = XMLUtil.convertDOMElementToString(element);
				}
				
				numRowsModified = jt.update(updateSql,today, modifyChildType.getSelf().getVisualattributes(), modifyChildType.getSelf().getTooltip(),
							modifyChildType.getSelf().getName(), modifyChildType.getSelf().getBasecode(), modifyChildType.getSelf().getValuetypeCd(), 
							modifyChildType.getSelf().getTablename(), modifyChildType.getSelf().getColumnname(),  modifyChildType.getSelf().getFacttablecolumn(),  modifyChildType.getSelf().getOperator(),  
							modifyChildType.getSelf().getColumndatatype(), xml, StringUtil.getPath(modifyChildType.getSelf().getKey()));
				
			
			//	log.debug("1.Number of rows modified " + numRowsModified);
				
				if(modifyChildType.isInclSynonyms()){
					// apply the modification to the synonyms as well.
						
					String updateSynonymsSql = " update " + metadataSchema+tableName  + " set update_date = ?, c_visualattributes = ?, c_tooltip = ?,c_basecode = ?, valuetype_cd = ?, " +
					" c_tablename = ?, c_columnname = ?, c_facttablecolumn = ?, c_operator = ?, c_columndatatype = ?, c_metadataxml = ? where c_fullname = ? and c_synonym_cd = 'Y'";

			//		log.info(updateSynonymsSql);
					
					numRowsModified += jt.update(updateSynonymsSql,today, modifyChildType.getSelf().getVisualattributes(), modifyChildType.getSelf().getTooltip(),
						modifyChildType.getSelf().getBasecode(), modifyChildType.getSelf().getValuetypeCd(), 
						modifyChildType.getSelf().getTablename(), modifyChildType.getSelf().getColumnname(),  modifyChildType.getSelf().getFacttablecolumn(),  modifyChildType.getSelf().getOperator(),  
						modifyChildType.getSelf().getColumndatatype(), xml, StringUtil.getPath(modifyChildType.getSelf().getKey()));
			//		log.debug("2. Number of rows modified " + numRowsModified);
				}
				
				else{  // else we are not including synonyms ; 
					// this is the case where we modified the synonyms list so we dont include them
					//  in the general modify case; we delete them; the client then sends addChild for
					//   each of them
					String deleteSynonymsSql = "delete from "+ metadataSchema+tableName  + " where c_fullname = ? and c_synonym_cd = 'Y'";
				//	log.info(deleteSynonymsSql);
					
					int numRowsDeleted = jt.update(deleteSynonymsSql, StringUtil.getPath(modifyChildType.getSelf().getKey()));
					
			//		log.debug("Number of rows deleted " + numRowsDeleted);
				}
				
			} catch (DataAccessException e) {
				log.error("Dao modifyChild failed");
				log.error(e.getMessage());
				throw e;
			}
			
			log.debug("Number of rows modified " + numRowsModified);
			return numRowsModified;

		}
		public int dirtyCandidate(final ModifyChildType modifyChildType, ProjectType projectInfo, DBInfoType dbInfo) throws DataAccessException, I2B2Exception{
			String metadataSchema = dbInfo.getDb_fullSchema();
	//		String serverType = dbInfo.getDb_serverType();
			setDataSource(dbInfo.getDb_dataSource());
			
				
			if (projectInfo.getRole().size() == 0)
			{
				log.error("no role found for this user in project: " + projectInfo.getName());
				I2B2Exception e = new I2B2Exception("No role found for user");
				throw e;
			}
			
			Boolean protectedAccess = false;
			Iterator<String> it = projectInfo.getRole().iterator();
			while (it.hasNext()){
				 String role = (String) it.next();
				 if(role.toUpperCase().equals("DATA_PROT")) {
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
			String tableCd = StringUtil.getTableCd(modifyChildType.getSelf().getKey());
			// table code to table name conversion
			String tableName=null;
			if (!protectedAccess){
				String tableSql = "select distinct(c_table_name) from " + metadataSchema + "table_access where c_table_cd = ? and c_protected_access = ? ";
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

			String countSql = "select count(*) from " + metadataSchema+tableName  + " where c_name = ? and c_basecode = ? and c_fullname = ? and c_visualattributes = ?";

			log.info(countSql);
			
		int count= -1;
			try {
				
				count = jt.queryForInt(countSql,modifyChildType.getSelf().getName(), modifyChildType.getSelf().getBasecode(),
						StringUtil.getPath(modifyChildType.getSelf().getKey()), modifyChildType.getSelf().getVisualattributes());
			
				
			} catch (DataAccessException e) {
				log.error("Dao modifyChild failed");
				log.error(e.getMessage());
				throw e;
			}
			
			log.debug("Dirty candidate check yielded " + count + " entries");
			return count;
		}
}
