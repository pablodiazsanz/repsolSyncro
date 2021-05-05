package repsolSyncro.entities;

import java.util.List;

public abstract class Transaction {
	protected String status;
	protected MyObject myObject;
	protected List<String> modifiedFields;

	
	public Transaction(String status, MyObject myObject) {
		this.status = status;
		this.myObject = myObject;
	}

	public Transaction(String status, MyObject myObject, List<String> modifiedFields) {
		this.status = status;
		this.myObject = myObject;
		this.modifiedFields = modifiedFields;
	}

	public abstract List<String> getModifiedFields();

	public abstract String getStatus();

}
