package repsolSyncro.entities.employees;

import java.util.List;

import repsolSyncro.entities.Transaction;

/**
 * Clase la cual implementa la interfaz Transaction y obtiene transacciones de
 * objetos Employee.
 *
 */
public class EmpTransaction extends Transaction {

	public EmpTransaction(String status, Employee employee) {
		super(status, employee);
	}

	public EmpTransaction(String status, Employee employee, List<String> modifiedFields) {
		super(status, employee, modifiedFields);
	}

	public List<String> getModifiedFields() {
		return modifiedFields;
	}

	public String getStatus() {
		return status;
	}

	public Employee getEmployee() {
		return (Employee) myObject;
	}

	@Override
	public String toString() {
		return "EmpTransaction [status=" + status + ", employee=" + (Employee) myObject + ", modifiedFields="
				+ modifiedFields + "]";
	}

}
