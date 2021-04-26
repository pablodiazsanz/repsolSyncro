package repsolSyncro;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Objeto empleado que gusrada los datos que nos interesas de estos
 *
 */
public class Employee implements Comparable<Employee> {
	private String id, name, surname1, surname2, tlf, mail, job;
	private Date hiringDate;
	private int yearSalary;
	private boolean sickLeave;

	public Employee() {
	}

	public Employee(String id, String name, String surname1, String surname2, String tlf, String mail, String job,
			Date hiringDate, int yearSalary, boolean sickLeave) {
		super();
		this.id = id;
		this.name = name;
		this.surname1 = surname1;
		this.surname2 = surname2;
		this.tlf = tlf;
		this.mail = mail;
		this.job = job;
		this.hiringDate = hiringDate;
		this.yearSalary = yearSalary;
		this.sickLeave = sickLeave;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSurname1() {
		return surname1;
	}

	public void setSurname1(String surname1) {
		this.surname1 = surname1;
	}

	public String getSurname2() {
		return surname2;
	}

	public void setSurname2(String surname2) {
		this.surname2 = surname2;
	}

	public String getTlf() {
		return tlf;
	}

	public void setTlf(String tlf) {
		this.tlf = tlf;
	}

	public String getMail() {
		return mail;
	}

	public void setMail(String mail) {
		this.mail = mail;
	}

	public String getJob() {
		return job;
	}

	public void setJob(String job) {
		this.job = job;
	}

	public Date getHiringDate() {
		return hiringDate;
	}

	public void setHiringDate(Date hiringDate) {
		this.hiringDate = hiringDate;
	}

	public int getYearSalary() {
		return yearSalary;
	}

	public void setYearSalary(int yearSalary) {
		this.yearSalary = yearSalary;
	}

	public boolean isSickLeave() {
		return sickLeave;
	}

	public void setSickLeave(boolean sickLeave) {
		this.sickLeave = sickLeave;
	}

	@Override
	public String toString() {

		// Formateamos la fecha para obtenerla como queremos
		String hiringDateFormatted = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(hiringDate);

		return "Employee [id=" + id + ", name=" + name + ", surname1=" + surname1 + ", surname2=" + surname2 + ", tlf="
				+ tlf + ", mail=" + mail + ", job=" + job + ", hiringDate=" + hiringDateFormatted + ", yearSalary="
				+ yearSalary + ", sickLeave=" + sickLeave + "]";
	}

	/**
	 *
	 * Escribe la linea del empleado en formato CSV.
	 *
	 * @return String con la linea en csv
	 */
	public String toCSV() {

		// Formateamos la fecha para pasarsela al fichero CSV como queremos
		String hiringDateFormatted = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(hiringDate);

		return id + ";" + name + ";" + surname1 + ";" + surname2 + ";" + tlf + ";" + mail + ";" + job + ";"
				+ hiringDateFormatted + ";" + yearSalary + ";" + sickLeave;
	}

	@Override
	public int compareTo(Employee o) {

		if (!this.getName().equalsIgnoreCase(o.getName().toLowerCase())
				|| !this.getSurname1().equalsIgnoreCase(o.getSurname1())
				|| !this.getSurname2().equalsIgnoreCase(o.getSurname2()) || !this.getTlf().equalsIgnoreCase(o.getTlf())
				|| !this.getMail().equalsIgnoreCase(o.getMail()) || !this.getJob().equalsIgnoreCase(o.getJob())
				|| this.getHiringDate().compareTo(o.getHiringDate()) != 0 || this.getYearSalary() != o.getYearSalary()
				|| this.isSickLeave() != o.isSickLeave()) {
			
			return 1;
		}

		return 0;
	}
}
