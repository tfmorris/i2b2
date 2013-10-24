/**
 *  ORCA Systems GmbH This code was written by ORCA Systems GmbH by ORCA Systems
 *  GmbH (http://www.orcasys.ch). The JWarp core framework and the JWarp
 *  Persistence framework are published by ORCA Systems GmbH
 *  (http://www.orcasys.ch) under the GNU General Public Licence (GPL).
 */
package edu.harvard.i2b2.common.util;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;

import org.jdom.*;
import java.io.*;

//import com.sun.rsasign.m;

/**
 * Description of the Class
 * 
 * @author atr
 * @created 15. November 2002
 * @since 14. Dezember 2001
 */
public class SQLResultConvertor {

	/**
	 * Description of the Field
	 */
	String dateFormat = "dd.MM.yyyy";

	/**
	 * Description of the Field
	 */
	String timeFormat = "HH:mm:ss";

	/**
	 * Description of the Field
	 */
	String timestampFormat = "dd.MM.yyyy HH:mm:ss";

	/**
	 * Constructor for the SQLResultConvertor object
	 */
	public SQLResultConvertor() {
	}

	/**
	 * Gets the dateFormat attribute of the SQLResultConvertor object
	 * 
	 * @return The dateFormat value
	 */
	public String getDateFormat() {
		return dateFormat;
	}

	/**
	 * Gets the timeFormat attribute of the SQLResultConvertor object
	 * 
	 * @return The timeFormat value
	 */
	public String getTimeFormat() {
		return timeFormat;
	}

	/**
	 * Gets the timestampFormat attribute of the SQLResultConvertor object
	 * 
	 * @return The timestampFormat value
	 */
	public String getTimestampFormat() {
		return timestampFormat;
	}

	/**
	 * Sets the dateFormat attribute of the SQLResultConvertor object
	 * 
	 * @param formatString
	 *            The new dateFormat value
	 */
	public void setDateFormat(String formatString) {
		SimpleDateFormat df = new SimpleDateFormat(formatString);
		dateFormat = formatString;
	}

	/**
	 * Sets the timeFormat attribute of the SQLResultConvertor object
	 * 
	 * @param formatString
	 *            The new timeFormat value
	 */
	public void setTimeFormat(String formatString) {
		SimpleDateFormat df = new SimpleDateFormat(formatString);
		timeFormat = formatString;
	}

	/**
	 * Sets the timestampFormat attribute of the SQLResultConvertor object
	 * 
	 * @param formatString
	 *            The new timestampFormat value
	 */
	public void setTimestampFormat(String formatString) {
		SimpleDateFormat df = new SimpleDateFormat(formatString);
		timestampFormat = formatString;
	}

	/**
	 * Description of the Method
	 * 
	 * @param sql
	 *            Description of the Parameter
	 * @param connection
	 *            Description of the Parameter
	 * @return Description of the Return Value
	 * @exception SQLException
	 *                Description of the Exception
	 */
	public Element convertToElement(String sql, Connection connection,
			String tablename, boolean withMetaData) throws SQLException {
		return convertToElement(sql, connection, java.lang.Integer.MAX_VALUE,
				tablename, withMetaData);
	}

	/**
	 * Description of the Method
	 * 
	 * @param sql
	 *            Description of the Parameter
	 * @param connection
	 *            Description of the Parameter
	 * @param maxCount
	 *            Description of the Parameter
	 * @return Description of the Return Value
	 * @exception SQLException
	 *                Description of the Exception
	 */
	public Element convertToElement(String sql, Connection connection,
			int maxCount, String tablename, boolean withMetaData)
			throws SQLException {
		Statement s = null;
		// Element result = new Element("result");
		Element result = null;
		try {
			s = connection.createStatement();
			ResultSet rs = s.executeQuery(sql);
			ResultSetMetaData md = rs.getMetaData();

			int columnCount = md.getColumnCount();

			if (withMetaData) {
				Element mdElem = new Element("metadata");
				mdElem.setAttribute("columnCount", String.valueOf(columnCount));
				for (int i = 1; i <= columnCount; i++) {
					Element colElem = new Element("column");
					colElem.setAttribute("name", md.getColumnName(i));
					colElem.setAttribute("tableName", tablename); //md.getTableName(i));
					colElem.addContent(new Element("label").setText(md
							.getColumnLabel(i)));
					colElem
							.addContent(new Element("displaySize")
									.setText(String.valueOf(md
											.getColumnDisplaySize(i))));
					colElem.addContent(new Element("className").setText(md
							.getColumnClassName(i)));
					colElem.addContent(new Element("type").setText(String
							.valueOf(md.getColumnType(i))));
					colElem.addContent(new Element("typeName").setText(md
							.getColumnTypeName(i)));
					//colElem.addContent(new
					// Element("precision").setText(String.valueOf(md.getPrecision(i))));
					//colElem.addContent(new
					// Element("scale").setText(String.valueOf(md.getScale(i))));
					// colElem.addContent(new
					// Element("schemaName").setText(md.getSchemaName(i)));
					//colElem.addContent(new
					// Element("autoIncrement").setText(String.valueOf(md.isAutoIncrement(i))));
					//colElem.addContent(new
					// Element("caseSensitive").setText(String.valueOf(md.isCaseSensitive(i))));
					//colElem.addContent(new
					// Element("currency").setText(String.valueOf(md.isCurrency(i))));
					//colElem.addContent(new
					// Element("definitelyWritable").setText(String.valueOf(md.isDefinitelyWritable(i))));
					//colElem.addContent(new
					// Element("nullable").setText(String.valueOf(md.isNullable(i))));
					//colElem.addContent(new
					// Element("readOnly").setText(String.valueOf(md.isReadOnly(i))));
					//colElem.addContent(new
					// Element("searchable").setText(String.valueOf(md.isSearchable(i))));
					//colElem.addContent(new
					// Element("signed").setText(String.valueOf(md.isSigned(i))));
					//colElem.addContent(new
					// Element("writable").setText(String.valueOf(md.isWritable(i))));
					//colElem.addContent(new
					// Element("catalogName").setText(md.getCatalogName(i)));
					mdElem.addContent(colElem);
					mdElem.addContent("\n");
				}
				result = mdElem;
				result.addContent("\n\n");
				//result.addContent(mdElem);
			}

			SimpleDateFormat ddf = new SimpleDateFormat(dateFormat);
			SimpleDateFormat tdf = new SimpleDateFormat(timeFormat);
			SimpleDateFormat tsdf = new SimpleDateFormat(timestampFormat);

			//Element rows = new Element(tablename.toLowerCase() + "s");
            Element rows = new Element("PatientData");
			while (rs.next()) {
				//Uses row as the element name
				//Element row = new Element("row");
				Element row = new Element(tablename.toLowerCase());
				for (int i = 1; i <= columnCount; i++) {
					String value = null;
					try {
						//Dont use ColumnType because Oracle TimeStamp is
						// oracle.sql.TIMESTAMP
						//Thus use typeName
						//switch (md.getColumnType(i)) {
						String columnType = md.getColumnClassName(i).substring(
								md.getColumnClassName(i).lastIndexOf(".") + 1)
								.toUpperCase();
						if (columnType.equals("DATE")) {
							//switch (md.getColumnType(i)) {
							//    case java.sql.Types.DATE:
							try {
								value = ddf.format(rs.getDate(i));
							} catch (Exception e) {
								Object r = rs.getObject(i);
								if (r == null) {
									value = "";
								} else {
									value = r.toString();
								}
							}
							//break;
							//case java.sql.Types.TIME:
						} else if (columnType.equals("TIME")) {
							try {
								value = tdf.format(rs.getTime(i));
							} catch (Exception e) {
								Object r = rs.getObject(i);
								if (r == null) {
									value = "";
								} else {
									value = r.toString();
								}
							}
							//break;
							//case java.sql.Types.TIMESTAMP:
						} else if (columnType.equals("TIMESTAMP")) {
							try {
								value = tsdf.format(rs.getTimestamp(i));
							} catch (Exception e) {
                                //TODO deal with null timestamps
								String r = rs.getString(i); //.getObject(i);
								if (r == null) {
									value = "";
								} else {
									value = r; //.toString();
								}
							}
							//break;
							//default:
						} else if (columnType.equals("CLOB")) {
							try {
								value = dumpClob(rs.getClob(i));
							} catch (Exception e) {
								Object r = rs.getObject(i);
								if (r == null) {
									value = "";
								} else {
									value = r.toString();
								}
							}
						} else {
							// try to just get it as a string
							/*
							 * Object r = rs.getObject(i); if (r == null) {
							 * value = ""; } else { value = r.toString(); }
							 */
							String r = rs.getString(i);
							if (r == null) {
								value = "";
							} else {
								value = r;
							}
						}
					} catch (Exception e) {
						value = e.toString();
					}
					row.addContent(new Element(md.getColumnLabel(i)
							.toLowerCase())
							.setText((value != null ? value : "")));
					//Uses 'VAL' as the element name
					//row.addContent(new Element("val").setText((value != null
					// ? value : "")));
				}
				rows.addContent(row);
				rows.addContent("\n");
			}
			if (result == null)
				result = rows;
			else
				result.addContent(rows);

			result.addContent("\n");
			return result;
		} finally {
			try {
				if (s != null) {
					s.close();
				}
			} catch (Exception ex) {
			}
		}
	}
	
	/**
	 *  Converts an SQL ResultSet to JDOM Document
	 * 
	 * @param rs   SQL ResultSet
	 *  	 
	 * @param requestDoc   Input select statement parameters in XML format
	 * 
	 * @param ns   Namespace associated with input select statement parameters
	 *                              
	 * @return     JDOM Document
	 * 
	 * @exception SQLException
	 */
	public org.jdom.Document convertToDocument(ResultSet rs, Document requestDoc, Namespace ns)
			throws SQLException {
		
		ResultSetMetaData md = rs.getMetaData();
		int rowCount = 0;

		SimpleDateFormat ddf = new SimpleDateFormat(dateFormat);
		SimpleDateFormat tdf = new SimpleDateFormat(timeFormat);
		SimpleDateFormat tsdf = new SimpleDateFormat(timestampFormat);
		
		// rows == rootElement
		org.jdom.Element rows = new org.jdom.Element("selectData");
		org.jdom.Document jDoc = new org.jdom.Document(rows);
		try {
			while (rs.next()) {
				rowCount++;
				org.jdom.Element row = new org.jdom.Element("patientData");
				
				// parse input request for input table names/column numbers
				int tableColStart = 1;
				int tableColEnd = 0;
				List tables = requestDoc.getRootElement().getChildren("table", ns);
				Iterator tableIterator = tables.iterator();

				while (tableIterator.hasNext()){
					Element tableElement = (org.jdom.Element) tableIterator.next();
					tableColEnd += Integer.parseInt(tableElement.getAttributeValue("numCols"));					
					Element table = new Element((tableElement.getText()).toLowerCase());
					
					for (int i = tableColStart; i <= tableColEnd; i++) {
						String value = null;
					
						try {
							String columnType = md.getColumnClassName(i).substring(
								md.getColumnClassName(i).lastIndexOf(".") + 1)
								.toUpperCase();
							if (columnType.equals("DATE")) {
								try {
									value = ddf.format(rs.getDate(i));
								} catch (Exception e) {
									Object r = rs.getObject(i);
									if (r == null) {
										value = "";
									} else {
										value = r.toString();
									}
								}
							} else if (columnType.equals("TIME")) {
								try {
									value = tdf.format(rs.getTime(i));
								} catch (Exception e) {
									Object r = rs.getObject(i);
									if (r == null) {
										value = "";
									} else {
										value = r.toString();
									}
								}
							} else if (columnType.equals("TIMESTAMP")) {
								try {
									value = tsdf.format(rs.getTimestamp(i));
								} catch (Exception e) {
                                //TODO deal with null timestamps
									String r = rs.getString(i); //.getObject(i);
									if (r == null) {
										value = "";
									} else {
										value = r; //.toString();
									}
								}
							} else if (columnType.equals("CLOB")) {
								try {
									value = dumpClob(rs.getClob(i));
								} catch (Exception e) {
									Object r = rs.getObject(i);
									if (r == null) {
										value = "";
									} else {
										value = r.toString();
									}
								}
							} else {
							// try to just get it as a string
							/*
							 * Object r = rs.getObject(i); if (r == null) {
							 * value = ""; } else { value = r.toString(); }
							 */
								String r = rs.getString(i);
								if (r == null) {
									value = "";
								} else {
									value = r;
								}
							}
						} catch (Exception e) {
							value = e.toString();
						}
						table.addContent(new org.jdom.Element(md.getColumnLabel(i)
								.toLowerCase())
								.setText((value != null ? value : "")));
						table.addContent("\n");
					}
					tableColStart = tableColEnd + 1;
					row.addContent(table);
					row.addContent("\n");
				}
				rows.addContent(row);
				rows.addContent("\n");
			}
			// Fill out result codes/string for normal (success) case
			org.jdom.Element result = new org.jdom.Element("result");
			org.jdom.Element resultCode = new org.jdom.Element("resultCode");
			resultCode.addContent("0");			
			org.jdom.Element resultString = new org.jdom.Element("resultString");
			resultString.addContent(rowCount + " records returned");
			result.addContent(resultCode);
			result.addContent(resultString);
			rows.addContent(result);			
			return jDoc;
		/*	if (result == null)
				result = rows;
			else
				result.addContent(rows);

			result.addContent("\n");
			return result;
			*/
		} catch (Exception ex) {
			org.jdom.Element result = new org.jdom.Element("result");
			org.jdom.Element resultCode = new org.jdom.Element("resultCode");			
			org.jdom.Element resultString = new org.jdom.Element("resultString");
			resultString.addContent(ex.getMessage());
			result.addContent(resultCode);
			result.addContent(resultString);
			rows.addContent(result);			
			ex.printStackTrace();
		}
		//return result;
		return jDoc;
	}

	

	/**
	 * Convert a database clob to string
	 * 
	 * @param Clob
	 *            Description of the Parameter
	 * @return String Description of the Return Value
	 * @exception Exception
	 *                Description of the Exception
	 */

	private String dumpClob(Clob clob) throws Exception {
		// get character stream to retrieve clob data
		Reader instream = clob.getCharacterStream();
		int BUFF_SIZE = 1024;
		char[] buffer = new char[BUFF_SIZE];
		StringBuffer sb = new StringBuffer();
		int length = 0;

		// fetch data
		while ((length = instream.read(buffer)) != -1) {
			sb.append(buffer, 0, length);
		}

		instream.close();
		return sb.toString();
	}

	/**
	 * @param args
	 *            The command line arguments
	public static void main(String[] args) {
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			Connection c = DriverManager.getConnection(
					"jdbc:oracle:thin:@192.168.0.1:1521:oracle", "acuser",
					"acuser");
			SQLResultConvertor conv = new SQLResultConvertor();
			Element e = conv.convertToElement("SELECT * FROM TCriterion", c,
					100, "TCriterion", false);
			Document doc = new Document(e);
			org.jdom.output.XMLOutputter out = new org.jdom.output.XMLOutputter();
			out.setEncoding("ISO-8859-1");
			java.io.FileWriter fw = new java.io.FileWriter(new java.io.File(
					"c:/temp/TCriterionDump.xml"));
			java.io.PrintWriter pw = new java.io.PrintWriter(fw);
			pw.println(out.outputString(doc));
			pw.close();
			fw.close();
			c.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
     */

}
