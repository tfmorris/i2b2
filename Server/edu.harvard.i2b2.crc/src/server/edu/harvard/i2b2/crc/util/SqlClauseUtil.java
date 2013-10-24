package edu.harvard.i2b2.crc.util;

import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.exception.I2B2Exception;

/**
 * Class to build sql clause from the input, to catch sql injection attack.
 * 
 * 
 */
public class SqlClauseUtil {
	protected final static Log log = LogFactory.getLog(SqlClauseUtil.class);

	public final static String REGEXP_IN_CLAUSE = ",(?!(?:[^',]|[^'],[^'])+')";

	/**
	 * Rebuild the sql IN clause from the input value constrain
	 * 
	 * @param theValueCons
	 * @param encloseSingleQuote
	 * @return
	 */
	public static String buildINClause(String theValueCons,
			boolean encloseSingleQuote) {

		// add '' for each inValues
		int i = 0;
		String singleInValue = "", inConstrainValue = "", singleQuote = "";

		if (encloseSingleQuote) {
			singleQuote = "'";
		}
		theValueCons = theValueCons.trim();
		if (theValueCons.startsWith("(")) {
			theValueCons = theValueCons.substring(1, theValueCons.length() - 1);
		}

		String[] inValues = null;
		if (encloseSingleQuote) {
			inValues = theValueCons.split(REGEXP_IN_CLAUSE);
		} else {
			inValues = theValueCons.split(",");
		}
		while (i < inValues.length) {
			if (encloseSingleQuote) {
				singleInValue = inValues[i].substring(1,
						inValues[i].length() - 1);
			} else {
				singleInValue = inValues[i];
			}
			inConstrainValue += singleQuote
					+ singleInValue.replaceAll("'", "''") + singleQuote;
			if (i + 1 < inValues.length) {
				inConstrainValue += ",";
			}
			i++;
			log.debug("Rebuilding the IN Clause with regex ["
					+ REGEXP_IN_CLAUSE + "], element value [" + singleInValue
					+ "] and the built value [" + inConstrainValue + "]");
		}

		return inConstrainValue;

	}

	/**
	 * Rebuild the sql BETWEEN clause from the input value constrain
	 * 
	 * @param betweenConstraint
	 * @return
	 * @throws I2B2Exception
	 */
	public static String buildBetweenClause(String betweenConstraint)
			throws I2B2Exception {
		StringTokenizer st = new StringTokenizer(betweenConstraint);
		String firstElement = "", andElement = "", thirdElement = "";
		if (st.countTokens() == 3) {
			firstElement = st.nextToken();
			andElement = st.nextToken();
			thirdElement = st.nextToken();
			if (!andElement.equalsIgnoreCase("and")) {
				throw new I2B2Exception("Invalid between clause ["
						+ betweenConstraint + "]");
			}
		} else {
			throw new I2B2Exception("Invalid between clause ["
					+ betweenConstraint + "]");
		}
		return firstElement.replaceAll("'", "''") + " and "
				+ thirdElement.replaceAll("'", "''");
	}

}
