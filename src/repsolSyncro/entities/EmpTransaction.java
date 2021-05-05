package repsolSyncro.entities;

import java.util.List;

public class EmpTransaction extends Transaction {

	
	public EmpTransaction(String status, Employee employee) {
		super(status, employee);
		// TODO Auto-generated constructor stub
	}
	

	public EmpTransaction(String status, Employee employee, List<String> modifiedFields) {
		super(status, employee, modifiedFields);
		// TODO Auto-generated constructor stub
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
		return "EmpTransaction [status=" + status + ", employee=" + (Employee) myObject + ", modifiedFields=" + modifiedFields
				+ "]";
	}

}
