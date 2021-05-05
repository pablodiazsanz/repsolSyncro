package repsolSyncro.businessLogic;

import java.util.HashMap;
import java.util.List;

import repsolSyncro.entities.EmpTransaction;
import repsolSyncro.entities.Employee;
import repsolSyncro.entities.MyObject;
import repsolSyncro.entities.Transaction;
import repsolSyncro.exceptions.SiaException;

/**
 * Esta clase es una clase para que las dos clases de EmpCsv y EmpDb hereden los
 * métodos que nos interesan para que utilicemos el patrón Factory.
 *
 */
public abstract class Emp {

	// Método en el que devolvemos los empleados recogidos
	public abstract HashMap<String, MyObject> getMap() throws SiaException;
	
	// Método en el que ejecutamos las operaciones contra la bbdd o contra un fichero csv de resultado
	public abstract void executeTransactions(List<Transaction> transactionsList) throws SiaException;

	// Método que utilizamos para obtener la operacion de los empleado modificados
	public abstract String getUpdatedEmployee(Transaction Transaction);
}
