package edu.harvard.i2b2.crc.dao.setfinder.querybuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Element;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.crc.datavo.ontology.ConceptType;
import edu.harvard.i2b2.crc.delegate.ontology.CallOntologyUtil;

public class SortPanel {
	/** log **/
	protected final Log log = LogFactory.getLog(getClass());

	public List<Element> getSortedPanelList(List<Element> panelList,
			CallOntologyUtil ontologyUtil) throws AxisFault, I2B2DAOException,
			XMLStreamException, JAXBUtilException {

		Map<Integer, Integer> panelTotalMap = new HashMap<Integer, Integer>();
		Map<Integer, Element> panelMap = new HashMap<Integer, Element>();
		int panelIndex = 0;
		List<Element> sortedPanelArray = new ArrayList<Element>();
		for (Iterator<Element> itr = panelList.iterator(); itr.hasNext();) {
			panelIndex++;
			Element panelXml = (org.jdom.Element) itr.next();
			List itemList = panelXml.getChildren("item");
			// calculate the total for each item
			int panelTotal = 0;
			for (Iterator iterator = itemList.iterator(); iterator.hasNext();) {
				Element itemXml = (org.jdom.Element) iterator.next();
				String itemKey = itemXml.getChildText("item_key");
				String itemClass = itemXml.getChildText("class");
				ConceptType conceptType = ontologyUtil.callOntology(itemKey);
				if (conceptType != null) {
					panelTotal += conceptType.getTotalnum();
				}
			}
			panelMap.put(panelIndex, panelXml);
			panelTotalMap.put(panelIndex, panelTotal);
			log.debug("Panel's Total num [" + panelTotal
					+ "] and the panel index [" + panelIndex + "]");

		}

		HashMap yourMap = new HashMap();

		HashMap map = new LinkedHashMap();

		List yourMapKeys = new ArrayList(panelTotalMap.keySet());
		List yourMapValues = new ArrayList(panelTotalMap.values());
		List sortedMapValues = new ArrayList(yourMapValues);

		Collections.sort(sortedMapValues);

		int size = yourMapValues.size();
		int indexInMapValues = 0;
		for (int i = 0; i < size; i++) {
			indexInMapValues = yourMapValues.indexOf(sortedMapValues.get(i));
			map.put(yourMapKeys.get(indexInMapValues), sortedMapValues.get(i));
			yourMapValues.set(indexInMapValues, -1);
		}
		Set ref = map.keySet();
		Iterator it = ref.iterator();
		int panelIndexHash = 0;
		while (it.hasNext()) {
			panelIndexHash = (Integer) it.next();

			sortedPanelArray.add(panelMap.get(panelIndexHash));
		}
		return sortedPanelArray;

	}
}
