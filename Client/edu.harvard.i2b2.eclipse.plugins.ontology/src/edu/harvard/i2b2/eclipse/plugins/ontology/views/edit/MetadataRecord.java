package edu.harvard.i2b2.eclipse.plugins.ontology.views.edit;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.IAction;

import edu.harvard.i2b2.ontclient.datavo.vdo.ConceptType;
import edu.harvard.i2b2.ontclient.datavo.vdo.DeleteChildType;

public class MetadataRecord {

	private ConceptType metadata = null;
	private NodeBrowser browser = null;
	private String type = null;
	private TreeData parentData = null;
	private String symbol = null;
	private String synonym = null;
	private List<String> synonyms = new ArrayList<String>();
	private boolean synonymEditFlag = false;
	private boolean updateSyncIconFlag = false;
	private boolean valueMetadataFlag = false;
	private IAction syncAction = null;
	


	private static MetadataRecord thisInstance;
	static {
		thisInstance = new MetadataRecord();
	}

	public static MetadataRecord getInstance() {
		return thisInstance;
	}
	
	public ConceptType getMetadata(){
		if (metadata == null)
			metadata = new ConceptType();
		
		return metadata;
	}
	
	public TreeData getParentData(){		
		return parentData;
	}
	
	
	public void setMetadata(TreeNode node){
		if (metadata == null)
			metadata = new ConceptType();
		
		parentData = node.getData();
		
		metadata.setColumndatatype(node.getData().getColumndatatype());
		metadata.setColumnname(node.getData().getColumnname());
		metadata.setFacttablecolumn(node.getData().getFacttablecolumn());
		metadata.setKey(node.getData().getKey());
		metadata.setLevel(node.getData().getLevel()+1);
		metadata.setOperator(node.getData().getOperator());
		metadata.setTablename(node.getData().getTablename());
		metadata.setDimcode(node.getData().getDimcode());
		metadata.setTooltip(node.getData().getTooltip());
		metadata.setSynonymCd("N");
		metadata.setValuetypeCd(node.getData().getValuetypeCd());
		if(metadata.getValuetypeCd() == null)
			metadata.setValuetypeCd("");
	}
	
	
	public void setType(String recordType){
		type = recordType;
		if (metadata == null)
			metadata = new ConceptType();
		
		if(type.equals("Folder"))
			metadata.setVisualattributes("FAE");
		else if (type.equals("Item"))
			metadata.setVisualattributes("LAE");
		else if (type.equals("Container"))
			metadata.setVisualattributes("CAE");		
	}
	
	public String getType(){
		return type;
	}
	
	public void setSymbol(String symbol){
		this.symbol = symbol;
	}
	public String getSymbol(){
		return symbol;
	}
	
	public void setSynonym(String synonym){
		this.synonym = synonym;
	}
	public String getSynonym(){
		return synonym;
	}
	
	public void registerBrowser(NodeBrowser nodeBrowser){
		browser = nodeBrowser;
	}
	
	public NodeBrowser getBrowser(){
		return browser;
	}
	
	public void addSynonym(String synonym){
		synonyms.add(synonym);
	}
	
	public void removeSynonym(String synonym){
		synonyms.remove(synonym);
	}
	public List<String> getSynonyms(){
		return synonyms;
	}

	public boolean isUpdateSyncIconFlag() {
		return updateSyncIconFlag;
	}

	public void setUpdateSyncIconFlag(boolean updateSyncIconFlag) {
		this.updateSyncIconFlag = updateSyncIconFlag;
	}
	

	public void setSynonymEditFlag(boolean flag){
		this.synonymEditFlag = flag;
	}
	public boolean isSynonymEditFlag(){
		return this.synonymEditFlag;
	}
	
	public void setValueMetadataFlag(boolean flag){
		this.valueMetadataFlag = flag;
	}
	public boolean isValueMetadataFlag(){
		return this.valueMetadataFlag;
	}
	
	public IAction getSyncAction() {
		return syncAction;
	}

	public void setSyncAction(IAction action) {
		this.syncAction = action;
	}
	
	  public DeleteChildType getDeleteChildType(){
	    	DeleteChildType delChild = new DeleteChildType();
	    	
	    	delChild.setKey(metadata.getKey());
	    	delChild.setBasecode(metadata.getBasecode());
	    	delChild.setLevel(metadata.getLevel());
	    	delChild.setName(metadata.getName());
	    	delChild.setSynonymCd(metadata.getSynonymCd());
	    	delChild.setVisualattribute(metadata.getVisualattributes());
	    	
	    	return delChild;
	    }
	
	public void clear(){
		if(metadata != null) {
			metadata.setColumndatatype("");
			metadata.setColumnname("");
			metadata.setFacttablecolumn("");
			metadata.setKey("");
			metadata.setOperator("");
			metadata.setTablename("");
			metadata.setDimcode("");
			metadata.setTooltip("");
			metadata.setVisualattributes("");
		}
		if((synonyms != null) || (!synonyms.isEmpty()))
			synonyms.clear();
		synonymEditFlag = false;
		updateSyncIconFlag = false;
		valueMetadataFlag = false;
	}

	public String validate(){
		String message = "";
		
		if(metadata.getKey() == null)
			message = message + "Item key is empty \n";
		
		if(metadata.getName() == null)
			message = message + "Item name is empty \n";
		
		if(metadata.getSynonymCd() == null)
			message = message + "Synonym code is empty \n";
		
		if(metadata.getVisualattributes() == null)
			message = message + "Item type is empty \n";
		
		if(metadata.getBasecode() == null)
			message = message + "Concept code is empty \n";
		
		if(metadata.getFacttablecolumn() == null)
			message = message + "Fact table column name is empty \n";
		
		if(metadata.getTablename() == null)
			message = message + "Table name is empty \n";
		
		if(metadata.getColumnname() == null)
			message = message + "Column name is empty \n";
		
		if(metadata.getColumndatatype() == null)
			message = message + "Column data type is empty \n";
		
		if(metadata.getOperator() == null)
			message = message + "Operator is empty \n";
		
		if(metadata.getDimcode() == null)
			message = message + "Dimension code is empty \n";
		
		return message;
	}

	
}
