package repsolSyncro.businessLogic;

import java.util.HashMap;
import java.util.List;

import repsolSyncro.entities.MyObject;
import repsolSyncro.entities.Transaction;

public interface Compare {

	public abstract List<Transaction> getTransactions(HashMap<String, MyObject> clientData,
			HashMap<String, MyObject> serverData);
	public abstract Transaction updateElement(MyObject clientData, MyObject serverData);

}
