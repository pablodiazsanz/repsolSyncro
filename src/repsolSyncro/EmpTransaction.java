package repsolSyncro;

public class EmpTransaction {

	private String status;
	private Employee employee;
	
	public EmpTransaction(String status, Employee employee) {
		this.status = status;
		this.employee = employee;
	}

	public String getStatus() {
		return status;
	}

	public Employee getEmployee() {
		return employee;
	}

	@Override
	public String toString() {
		return "EmpTransaction [status=" + status + ", employee=" + employee + "]";
	}

}
