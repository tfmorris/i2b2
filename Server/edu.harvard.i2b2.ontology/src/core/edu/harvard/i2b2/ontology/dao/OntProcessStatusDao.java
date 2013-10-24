package edu.harvard.i2b2.ontology.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.util.jaxb.DTOFactory;

import edu.harvard.i2b2.ontology.datavo.pm.ProjectType;

import edu.harvard.i2b2.ontology.datavo.vdo.DirtyValueType;
import edu.harvard.i2b2.ontology.datavo.vdo.GetReturnType;
import edu.harvard.i2b2.ontology.datavo.vdo.OntologyProcessStatusType;
import edu.harvard.i2b2.ontology.ejb.DBInfoType;

/**
 * Class to access table_access table.
 * 
 * @author rkuttan
 * 
 */
public class OntProcessStatusDao extends JdbcDaoSupport {

	private static Log log = LogFactory.getLog(OntProcessStatusDao.class);

	private SimpleJdbcTemplate jt = null;
	private DataSource dataSource = null;
	private DBInfoType dbInfoType = null;
	private ProjectType projectType = null;

	public void setDataSourceObject(DataSource dataSource) {
		this.jt = new SimpleJdbcTemplate(dataSource);
	}

	public OntProcessStatusDao(DataSource dataSource, ProjectType projectType,
			DBInfoType dbInfo) {
		this.dataSource = dataSource;
		this.projectType = projectType;
		this.dbInfoType = dbInfo;
		this.jt = new SimpleJdbcTemplate(dataSource);
	}

	public OntologyProcessStatusType createOntologyProcessStatus(
			OntologyProcessStatusType ontProcessStatusType, String userId)
			throws I2B2DAOException {
		int numRowsAdded = 0;
		try {
			Date today = Calendar.getInstance().getTime();

			String addSql = "insert into "
					+ this.dbInfoType.getDb_fullSchema()
					+ "ONT_PROCESS_STATUS"
					+ "(process_id, process_type_cd, process_step_cd, start_date,  process_status_cd, changedby_char, message,entry_date,status_cd) values (?,?,?,?,?,?,?,?,?)";
			int processId = 0;
			if (this.dbInfoType.getDb_serverType().equals("ORACLE")) {
				log.info(addSql);
				processId = jt.queryForInt("select "
						+ this.dbInfoType.getDb_fullSchema()
						+ "ONT_SQ_PS_PRID.nextval from dual");
				ontProcessStatusType.setProcessId(String.valueOf(processId));
				numRowsAdded = jt.update(addSql, ontProcessStatusType
						.getProcessId(), ontProcessStatusType
						.getProcessTypeCd(), ontProcessStatusType
						.getProcessStepCd(), today, "PROCESSING", userId,
						ontProcessStatusType.getMessage(), today, "C");
			} else if (this.dbInfoType.getDb_serverType().equals("SQLSERVER")) {
				addSql = "insert into "
						+ this.dbInfoType.getDb_fullSchema()
						+ "ONT_PROCESS_STATUS"
						+ "(process_type_cd, process_step_cd, start_date,  process_status_cd, changedby_char, message,entry_date,status_cd) values (?,?,?,?,?,?,?,?)";
				numRowsAdded = jt.update(addSql, ontProcessStatusType
						.getProcessTypeCd(), ontProcessStatusType
						.getProcessStepCd(), today, "PROCESSING", userId,
						ontProcessStatusType.getMessage(), today, "C");
				processId = jt.queryForInt("SELECT @@IDENTITY");

			}
			ontProcessStatusType.setProcessId(String.valueOf(processId));
			System.out.println("Rows added [" + numRowsAdded + "]");
			return ontProcessStatusType;
		} catch (DataAccessException e) {
			e.printStackTrace();
			log.error("Dao ontProcessStatus failed");
			log.error(e.getMessage());
			throw new I2B2DAOException("Data access error ", e);
		}
	}

	public OntologyProcessStatusType findById(int processId) {
		String sql = "select * from " + dbInfoType.getDb_fullSchema()
				+ "ONT_PROCESS_STATUS where process_id = ?";
		OntologyProcessStatusType ontProcessStatusType = jt.queryForObject(sql,
				getParameterizedRowMapper(), processId);
		return ontProcessStatusType;
	}

	public int updateStatus(int processId, Date endDate, String processStateCd,
			String statusCd) {
		Date today = Calendar.getInstance().getTime();
		String sql = "update " + dbInfoType.getDb_fullSchema()
				+ "ONT_PROCESS_STATUS set ";
		if (endDate != null) {
			sql += "end_date = ?,";
		}
		sql += "process_step_cd = ?,process_status_cd = ?,change_date = ?,status_cd = ? where process_id = ? ";
		int recordCount = 0;
		if (endDate != null) {
			recordCount = jt.update(sql, endDate, processStateCd, statusCd,
					today, "U", processId);
		} else {
			recordCount = jt.update(sql, processStateCd, statusCd, today, "U",
					processId);
		}

		return recordCount;
	}

	public int updateStatusMessage(int processId, String message) {
		Date today = Calendar.getInstance().getTime();
		String sql = "update " + dbInfoType.getDb_fullSchema()
				+ "ONT_PROCESS_STATUS set  message = ? where process_id = ? ";
		int recordCount = jt.update(sql, message, processId);
		return recordCount;
	}

	public int updateCRCUploadId(int processId, String uploadId) {
		Date today = Calendar.getInstance().getTime();
		String sql = "update "
				+ dbInfoType.getDb_fullSchema()
				+ "ONT_PROCESS_STATUS set  crc_upload_id = ? where process_id = ? ";
		int recordCount = jt.update(sql, uploadId, processId);
		return recordCount;
	}

	private ParameterizedRowMapper<OntologyProcessStatusType> getParameterizedRowMapper() {

		ParameterizedRowMapper<OntologyProcessStatusType> map = new ParameterizedRowMapper<OntologyProcessStatusType>() {
			DTOFactory factory = new DTOFactory();
			Date startDate = null, endDate = null;

			public OntologyProcessStatusType mapRow(ResultSet rs, int rowNum)
					throws SQLException {
				OntologyProcessStatusType processStatusType = new OntologyProcessStatusType();
				processStatusType.setProcessId(rs.getString("process_id"));
				startDate = rs.getDate("start_date");
				if (startDate != null) {
					processStatusType.setStartDate(factory
							.getXMLGregorianCalendar(startDate.getTime()));
				}
				endDate = rs.getDate("end_date");
				if (endDate != null) {
					processStatusType.setEndDate(factory
							.getXMLGregorianCalendar(endDate.getTime()));
				}
				processStatusType.setProcessStepCd(rs
						.getString("process_step_cd"));
				processStatusType.setProcessStatusCd(rs
						.getString("process_status_cd"));
				processStatusType.setCrcUploadId(rs.getString("crc_upload_id"));
				processStatusType.setMessage(rs.getString("message"));
				return processStatusType;
			}
		};
		return map;
	}
	public DirtyValueType getDirtyState(GetReturnType returnType, DBInfoType dbInfo) {
		DirtyValueType response;
		int count = getDeleteEditCount(returnType, dbInfo);
		log.debug("Dirty process delete/edit after sync count = " + count);
		if(count > 0){
			response = DirtyValueType.DELETE_EDIT;
		}
		else {
			count = getAddCount(returnType, dbInfo);
			log.debug("Dirty process add after update count = " + count);
			if(count > 0){
				response = DirtyValueType.ADD;
			}
			else {
				response = DirtyValueType.NONE;

			}
		}
		log.debug(response.value());
		return response;
	}

	private int getDeleteEditCount(GetReturnType returnType, DBInfoType dbInfo){
		String startDateSql = "select start_date from " + dbInfoType.getDb_fullSchema()
		+ "ONT_PROCESS_STATUS where process_type_cd = ? " +
				" and status_cd <> 'ERROR' order by start_date desc";
	

		ParameterizedRowMapper<java.sql.Timestamp> mapper = new ParameterizedRowMapper<java.sql.Timestamp>() {
	        public java.sql.Timestamp mapRow(ResultSet rs, int rowNum) throws SQLException {
	        	java.sql.Timestamp startDate = rs.getTimestamp("start_date");
	        	return startDate;
	        }
	    };
		
		
		List<java.sql.Timestamp> queryResult = null;
		try{
			queryResult = jt.query(startDateSql,mapper,"ONT_SYNCALL_CRC_CONCEPT");
		}catch (DataAccessException e) {
			log.error(e.getMessage());
			throw e;
		}
		
		int count = -1;
		String sql = null;
		if(queryResult.isEmpty()){
			sql = "select count(*) from " + dbInfoType.getDb_fullSchema()
			+ "ONT_PROCESS_STATUS where (process_type_cd = ? or process_type_cd = ?)" ;
			count = jt.queryForInt(sql, "ONT_EDIT_CONCEPT", "ONT_DELETE_CONCEPT");
			
		}else{
		
			java.util.Date date2 = new java.util.Date(queryResult.get(0).getTime());
			if (dbInfoType.getDb_serverType().equalsIgnoreCase("ORACLE")){ 
				SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");	  			
				String sqlFormatedStartDate = dateFormat.format(date2.getTime());
				sql = "select count(*) from " + dbInfoType.getDb_fullSchema()
				+ "ONT_PROCESS_STATUS where (process_type_cd = ? or process_type_cd = ?)"+
						"and start_date > to_date('" + sqlFormatedStartDate +  "', 'DD-MM-YYYY HH24:MI:SS') ";
			}
			else if(dbInfoType.getDb_serverType().equalsIgnoreCase("SQLSERVER")){ 
				SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

				String sqlFormatedStartDate = dateFormat.format(date2.getTime());
				sql = "select count(*) from " + dbInfoType.getDb_fullSchema()
				+ "ONT_PROCESS_STATUS where (process_type_cd = ? or process_type_cd = ?)"+
				"and start_date >  '" + sqlFormatedStartDate +  "' ";
			}

			if(sql != null)
				count = jt.queryForInt(sql, "ONT_EDIT_CONCEPT", "ONT_DELETE_CONCEPT");
		}
		return count;
	}
	
	private int getAddCount(GetReturnType returnType, DBInfoType dbInfo){
		
		// get last startDate of all syncs and updates
		String startDateSql = "select start_date from " + dbInfoType.getDb_fullSchema()
		+ "ONT_PROCESS_STATUS where (process_type_cd = ? or process_type_cd = ?)" +
				" and status_cd <> 'ERROR' order by start_date desc";
	

		ParameterizedRowMapper<java.sql.Timestamp> mapper = new ParameterizedRowMapper<java.sql.Timestamp>() {
	        public java.sql.Timestamp mapRow(ResultSet rs, int rowNum) throws SQLException {
	        	java.sql.Timestamp startDate = rs.getTimestamp("start_date");
	        	return startDate;
	        }
	    };
		
		
		List<java.sql.Timestamp> queryResult = null;
		try{
			queryResult = jt.query(startDateSql,mapper,"ONT_UPDATE_CRC_CONCEPT", "ONT_SYNCALL_CRC_CONCEPT");
		}catch (DataAccessException e) {
			log.error(e.getMessage());
			throw e;
		}
		
		int count = -1;
		String sql = null;
		if(queryResult.isEmpty()){  // no updates or syncs so look for # of adds in general
				sql = "select count(*) from " + dbInfoType.getDb_fullSchema()
				+ "ONT_PROCESS_STATUS where process_type_cd = ? ";
				count = jt.queryForInt(sql, "ONT_ADD_CONCEPT");
		
		}
		if(count == -1) {  // this means we havent found anything yet so 
			// look for adds after startDate....
			java.util.Date date2 = new java.util.Date(queryResult.get(0).getTime());
			if (dbInfoType.getDb_serverType().equalsIgnoreCase("ORACLE")){ 
				SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");	  			
				String sqlFormatedStartDate = dateFormat.format(date2.getTime());
				sql = "select count(*) from " + dbInfoType.getDb_fullSchema()
				+ "ONT_PROCESS_STATUS where process_type_cd = ? and start_date > " +
				" to_date('" + sqlFormatedStartDate +  "', 'DD-MM-YYYY HH24:MI:SS') ";
			}
			else if(dbInfoType.getDb_serverType().equalsIgnoreCase("SQLSERVER")){ 
				SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

				String sqlFormatedStartDate = dateFormat.format(date2.getTime());
				sql = "select count(*) from " + dbInfoType.getDb_fullSchema()
				+ "ONT_PROCESS_STATUS where process_type_cd = ? and start_date > " +
				"'" + sqlFormatedStartDate +  "' ";
			}

			if(sql != null)
				count = jt.queryForInt(sql, "ONT_ADD_CONCEPT");
		}
	
		return count;
	}
	
	public int createOntologyProcessType(
			String ontProcessType, String userId)
			throws I2B2DAOException {
		int numRowsAdded = 0;
		try {
			Date today = Calendar.getInstance().getTime();

			String addSql = "insert into "
					+ this.dbInfoType.getDb_fullSchema()
					+ "ONT_PROCESS_STATUS"
					+ "(process_id, process_type_cd, start_date, changedby_char, process_status_cd, status_cd, end_date, entry_date ) values (?,?,?,?,?,?,?,?)";
			int processId = 0;
			if (this.dbInfoType.getDb_serverType().equals("ORACLE")) {
				log.info(addSql);
				processId = jt.queryForInt("select "
						+ this.dbInfoType.getDb_fullSchema()
						+ "ONT_SQ_PS_PRID.nextval from dual");
			
				numRowsAdded = jt.update(addSql, String.valueOf(processId), ontProcessType, 
						 today, userId, "COMPLETED", "C", today, today);
			} else if (this.dbInfoType.getDb_serverType().equals("SQLSERVER")) {
				addSql = "insert into "
						+ this.dbInfoType.getDb_fullSchema()
						+ "ONT_PROCESS_STATUS"
						+ "(process_type_cd,  start_date, changedby_char, process_status_cd, status_cd, end_date, entry_date) values (?,?,?,?,?,?,?)";
				numRowsAdded = jt.update(addSql, ontProcessType,  today, userId, "COMPLETED", "C", today, today);
						
				processId = jt.queryForInt("SELECT @@IDENTITY");

			}
			
			System.out.println("Rows added [" + numRowsAdded + "]");
			return numRowsAdded;
		} catch (DataAccessException e) {
	//		e.printStackTrace();
			log.error("Dao ontProcessStatus failed");
	//		log.error(e.getMessage());
			throw new I2B2DAOException("Data access error ", e);
		}
	}
	
	
}

