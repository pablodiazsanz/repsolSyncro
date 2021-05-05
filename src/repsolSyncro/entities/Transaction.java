package repsolSyncro.entities;

import java.util.List;

/**
 * Esta clase la utilizamos para utilizar las transacciones de objetos y que
 * otras clases las puedan extender
 *
 */
public abstract class Transaction {
	// El estado si es CREATE, UPDATE o DELETE
	protected String status;
	// El objeto que obtenemos de los datos
	protected MyObject myObject;
	// La lista de datos modificados en la transaccion
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
