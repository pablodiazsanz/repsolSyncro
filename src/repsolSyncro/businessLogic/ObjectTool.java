package repsolSyncro.businessLogic;

import java.util.HashMap;
import java.util.List;

import repsolSyncro.entities.MyObject;
import repsolSyncro.entities.Transaction;
import repsolSyncro.entities.employees.EmpTransaction;
import repsolSyncro.entities.employees.Employee;
import repsolSyncro.exceptions.SiaException;

/**
 * Esta interfaz la utilizamos para que las clases que las implenten obtengan
 * datos y ejecuten transacciones contra ficheros o bbdd, entre otros.
 *
 */
public interface ObjectTool {

	/**
	 * Método en el que obtenemos datos recogidos de algun fichero o bbdd.
	 * 
	 * @return HashMap<String, MyObject> con los objetos recogidos
	 * @throws SiaException
	 */
	public HashMap<String, MyObject> getMap() throws SiaException;

	/**
	 * Método en el que ejecutamos las operaciones contra la bbdd o contra un
	 * fichero csv de resultado
	 * 
	 * @param transactionsList La lista de transacciones que contiene objetos
	 * @throws SiaException
	 */
	public void executeTransactions(List<Transaction> transactionsList) throws SiaException;

	/**
	 * Método que utilizamos para obtener la operacion de los objetos modificados
	 * 
	 * @param Transaction La transaccion del objeto que modificamos
	 * @return La query que queremos insertar a un fichero o ejecutar conta una bbdd
	 */
	public String getUpdatedObject(Transaction Transaction);
}
