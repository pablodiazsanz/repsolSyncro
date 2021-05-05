package repsolSyncro.businessLogic;

import java.util.HashMap;
import java.util.List;

import repsolSyncro.entities.MyObject;
import repsolSyncro.entities.Transaction;

/**
 * Esta interfaz la utilizamos para obtener las transacciones de las
 * comparaciones de objetos que implementen esta interfaz.
 *
 */
public interface Compare {

	/**
	 * Este método lo utilizamos para obtener todas las transacciones de las
	 * comparaciones de los objetos que se obtienen del cliente y del servidor.
	 * 
	 * @param clientData Los datos que obtenemos del cliente
	 * @param serverData Los datos que obtenemos del servidor
	 * @return Lista de transacciones que podemos operar
	 */
	public List<Transaction> getTransactions(HashMap<String, MyObject> clientData,
			HashMap<String, MyObject> serverData);

	/**
	 * En este método vemos en que se diferencian dos objetos y obtenemos las
	 * diferencias y las mandamos a ejecutar con la transaccion
	 * 
	 * @param clientData El objeto a comparar obtenido de el cliente
	 * @param serverData El objeto a comparar obtenido del servidor
	 * @return La transaccion a añadir a la lista de transacciones
	 */
	public Transaction updateElement(MyObject clientData, MyObject serverData);

}
