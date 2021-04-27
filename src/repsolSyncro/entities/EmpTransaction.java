package repsolSyncro.entities;

import java.util.List;

public class EmpTransaction {

	private String status;
	private Employee employee;
	private List<String> modifiedFields;
	
	public EmpTransaction(String status, Employee employee) {
		this.status = status;
		this.employee = employee;
	}
	
	public EmpTransaction(String status, Employee employee, List<String> modifiedFields) {
		this.status = status;
		this.employee = employee;
		this.modifiedFields = modifiedFields;
	}


	public List<String> getModifiedFields() {
		return modifiedFields;
	}

	public String getStatus() {
		return status;
	}

	public Employee getEmployee() {
		return employee;
	}

	@Override
	public String toString() {
		return "EmpTransaction [status=" + status + ", employee=" + employee + ", modifiedFields=" + modifiedFields
				+ "]";
	}
	
	

}
