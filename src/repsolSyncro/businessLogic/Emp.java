package repsolSyncro.businessLogic;

import java.util.HashMap;
import java.util.List;

import repsolSyncro.entities.EmpTransaction;
import repsolSyncro.entities.Employee;
import repsolSyncro.exceptions.SiaException;

/**
 * Esta clase es una clase para que las dos clases de EmpCsv y EmpDb hereden los
 * m�todos que nos interesan para que utilicemos el patr�n Factory.
 *
 */
public abstract class Emp {

	// M�todo en el que devolvemos los empleados recogidos
	public abstract HashMap<String, Employee> getMap() throws SiaException;
	
	// M�todo en el que ejecutamos las operaciones contra la bbdd o contra un fichero csv de resultado
	public abstract void executeTransactions(List<EmpTransaction> transactionsList) throws SiaException;

	// M�todo que utilizamos para obtener la operacion de los empleado modificados
	public abstract String getUpdatedEmployee(EmpTransaction empTransaction);
}
