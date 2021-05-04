package repsolSyncro.businessLogic;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;

import org.apache.log4j.Logger;

import repsolSyncro.constants.DatabaseConstants;
import repsolSyncro.constants.PropertyConstants;
import repsolSyncro.dataAccess.DbAccess;
import repsolSyncro.entities.EmpTransaction;
import repsolSyncro.entities.Employee;
import repsolSyncro.exceptions.SiaException;
import repsolSyncro.exceptions.SiaExceptionCodes;

/**
 * En esta clase lo que hacemos es establecer la conexión con la base de datos,
 * prueba su conexión, obtiene los datos de la tabla empleados y tambien manda
 * ejecutar operaciones a la clase DbAccess.
 *
 */
public class EmpDb {

	// Objeto de conexion a la BBDD
	private static Connection conn;

	// Logger para poder escribir las trazas del codigo en los logs
	private static Logger log = Logger.getLogger(EmpDb.class);
	//tabla sobre la que tabajamos
	private static String table = "employee";

	
	public EmpDb() {
	}

	

	/**
	 * Busca en la BBDD la lista de empleados y la devuelve en un HashMap donde la
	 * clave es su ID
	 * 
	 * @return HasMap<String, Employee> con la lista de empleados como value y su id
	 *         por key
	 * @throws SiaException
	 */
	public static HashMap<String, Employee> getMap() throws SiaException {
		HashMap<String, Employee> employeeList = new HashMap<String, Employee>();
		List<HashMap<String, String>> dataList = DbAccess.getDataFromTable(table);
		for (HashMap<String, String> dataline : dataList) {

			try {
				Employee emp = createEmployee(dataline);
				employeeList.put(emp.getId(), emp);
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		log.trace("Lista de empleados: [" + employeeList + "]");
		return employeeList;

	}

	private static Employee createEmployee(HashMap<String, String> empData)
			throws ParseException, NumberFormatException {

		Date empHiringDate = null;
		int empYearSalary = -1;
		boolean empSickLeave = false;
		// parseamos la fecha de striong a objeto Date

		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		formatter.setTimeZone(TimeZone.getTimeZone("Europe/Madrid"));
		empHiringDate = formatter.parse(empData.get(DatabaseConstants.HIRING_DATE));

		// Aqui formateamos el salario anual a numero entero
		empYearSalary = Integer.parseInt(empData.get(DatabaseConstants.YEAR_SALARY));
		// Parseamos el boleano de la baja
		empSickLeave = Boolean.parseBoolean(empData.get(DatabaseConstants.SICK_LEAVE));
		log.trace("Parseamos todos los datos necesarios del csv");

		// Creamos el empleado con los datos obtenidos
		Employee emp = new Employee(empData.get(DatabaseConstants.ID), empData.get(DatabaseConstants.NAME),
				empData.get(DatabaseConstants.SURNAME1), empData.get(DatabaseConstants.SURNAME2),
				empData.get(DatabaseConstants.PHONE), empData.get(DatabaseConstants.EMAIL),
				empData.get(DatabaseConstants.JOB), empHiringDate, empYearSalary, empSickLeave);
		log.trace(emp);
		return emp;
	}

	/**
	 * En este metodo mandamos ejecutar en la base de datos H2 todos los posibles
	 * CREATE, DELETE y UPDATE que se han pedido.
	 * 
	 * @param transactionsList La lista de operaciones que obtenemos de la
	 *                         comparacion
	 * @throws SiaException
	 */
	public static void executeTransactions(List<EmpTransaction> transactionsList) throws SiaException {

		// Recorremos transaccion por transaccion y ejecutamos operacion por operacion.
		for (EmpTransaction empTransaction : transactionsList) {

			log.trace("Transaccion: [" + empTransaction.toString() + "]. Operacion a realizar: "
					+ empTransaction.getStatus());

			// Comprobamos el estado de la transacción para hacer un tipo de query u otro
			if (empTransaction.getStatus().equals("CREATE")) {
				String query = "INSERT INTO employee(id,name,first_surname,second_surname,phone,email,job,hiring_date,year_salary,sick_leave) VALUES ('"
						+ empTransaction.getEmployee().getId() + "','" + empTransaction.getEmployee().getName() + "','"
						+ empTransaction.getEmployee().getSurname1() + "','"
						+ empTransaction.getEmployee().getSurname2() + "'," + empTransaction.getEmployee().getTlf()
						+ ",'" + empTransaction.getEmployee().getMail() + "','" + empTransaction.getEmployee().getJob()
						+ "',parsedatetime('"
						+ new SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
								.format(empTransaction.getEmployee().getHiringDate())
						+ "', 'dd/MM/yyyy HH:mm:ss')," + empTransaction.getEmployee().getYearSalary() + ","
						+ empTransaction.getEmployee().isSickLeave() + ");";

				log.trace("Query a ejecutar: " + query);
				DbAccess.executeStatement(query);

			} else if (empTransaction.getStatus().equals("DELETE")) {
				String query = "DELETE FROM employee WHERE ID = '" + empTransaction.getEmployee().getId() + "';";

				log.trace("Query a ejecutar: " + query);
				DbAccess.executeStatement(query);

			} else {
				// Para el UPDATE, obtenemos el empleado desde otro método en el que comprobamos
				// los datos a modificar
				String query = getQueryUpdatedEmployee(empTransaction.getEmployee(),
						empTransaction.getModifiedFields());

				log.trace("Query a ejecutar: " + query);
				DbAccess.executeStatement(query);
			}
		}
	}

	/**
	 * Aqui obtenemos la query de UPDATE de un empleado para mandarla ejecutar
	 * 
	 * @param updatedEmployee El empleado que se modifica
	 * @param modifiedFields  Los campos a modificar
	 * @return La query UPDATE
	 * @throws SiaException
	 */
	public static String getQueryUpdatedEmployee(Employee updatedEmployee, List<String> modifiedFields)
			throws SiaException {
		// Creamos esta variable para iniciar la query que vamos a mandar.
		String query = "UPDATE employee SET";

		// Estos booleanos con los nombres de los campos son los que vamos a utilizar
		// para comprobar si el dato ha cambiado o no.
		boolean name = false;
		boolean surname1 = false;
		boolean surname2 = false;
		boolean phone = false;
		boolean email = false;
		boolean job = false;
		boolean hiringDate = false;
		boolean yearSalary = false;
		boolean sickLeave = false;

		// Recorremos la lista para saber que datos se han cambiado.
		for (int i = 0; i < modifiedFields.size(); i++) {

			if (modifiedFields.get(i).equals("name")) {
				name = true;

			} else if (modifiedFields.get(i).equals("surname1")) {
				surname1 = true;

			} else if (modifiedFields.get(i).equals("surname2")) {
				surname2 = true;

			} else if (modifiedFields.get(i).equals("phone")) {
				phone = true;

			} else if (modifiedFields.get(i).equals("email")) {
				email = true;

			} else if (modifiedFields.get(i).equals("job")) {
				job = true;

			} else if (modifiedFields.get(i).equals("hiringDate")) {
				hiringDate = true;

			} else if (modifiedFields.get(i).equals("yearSalary")) {
				yearSalary = true;

			} else if (modifiedFields.get(i).equals("sickLeave")) {
				sickLeave = true;
			}

		}

		// Si los datos han sido cambiados, establecemos el dato para añadirlo a la
		// query.
		if (name) {
			query += " name = '" + updatedEmployee.getName() + "',";
		}
		if (surname1) {
			query += " first_surname = '" + updatedEmployee.getSurname1() + "',";
		}
		if (surname2) {
			query += " second_surname = '" + updatedEmployee.getSurname2() + "',";
		}
		if (phone) {
			query += " phone = '" + updatedEmployee.getTlf() + "',";
		}
		if (email) {
			query += " email = '" + updatedEmployee.getMail() + "',";
		}
		if (job) {
			query += " job = '" + updatedEmployee.getJob() + "',";
		}
		if (hiringDate) {
			query += " hiring_date = parsedatetime('"
					+ new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(updatedEmployee.getHiringDate())
					+ "', 'dd/MM/yyyy HH:mm:ss'),";
		}
		if (yearSalary) {
			query += " year_salary = '" + updatedEmployee.getYearSalary() + "',";
		}
		if (sickLeave) {
			query += " sick_leave = '" + updatedEmployee.isSickLeave() + "',";
		}

		// Completamos la query y se la devolvemos para ejecutarla en la base de datos.
		query = query.substring(0, query.length() - 1);
		query += " WHERE ID = '" + updatedEmployee.getId() + "';";

		return query;
	}

}
