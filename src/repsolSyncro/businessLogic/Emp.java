package repsolSyncro.businessLogic;

import java.util.HashMap;
import java.util.List;

import repsolSyncro.entities.EmpTransaction;
import repsolSyncro.entities.Employee;
import repsolSyncro.exceptions.SiaException;

public abstract class Emp {

	public abstract HashMap<String, Employee> getMap() throws SiaException;
	
	public abstract void executeTransactions(List<EmpTransaction> transactionsList) throws SiaException;
	
	public abstract String getUpdatedEmployee(EmpTransaction empTransaction);
}
