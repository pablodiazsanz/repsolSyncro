package repsolSyncro.businessLogic;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
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
public class EmpDb extends Emp {

	// Logger para poder escribir las trazas del codigo en los logs
	private static Logger log = Logger.getLogger(EmpDb.class);
	// Tabla sobre la que trabajamos
	private static String table;

	private static Properties file;
	private static FileInputStream ip;

	private DbAccess db;

	public EmpDb(String fileName) throws SiaException {
		try {
			file = new Properties();
			if (fileName.equals("CLIENT")) {

				ip = new FileInputStream(
						PropertiesChecker.getAllProperties().getProperty(PropertyConstants.PATH_CLIENT_PROPERTY_FILE));
				file.load(ip);

				db = new DbAccess(file);

			} else if (fileName.equals("SERVER")) {
				ip = new FileInputStream(
						PropertiesChecker.getAllProperties().getProperty(PropertyConstants.PATH_CLIENT_PROPERTY_FILE));
				file.load(ip);

				db = new DbAccess(file);
			} else if (fileName.equals("RESULT")) {
				ip = new FileInputStream(
						PropertiesChecker.getAllProperties().getProperty(PropertyConstants.PATH_CLIENT_PROPERTY_FILE));
				file.load(ip);

				db = new DbAccess(file);
			}

			table = file.getProperty(PropertyConstants.DB_TABLE);
		} catch (FileNotFoundException e) {
			String message = "No se ha encontrado el fichero o este no existe";
			throw new SiaException(SiaExceptionCodes.MISSING_FILE, message, e);

		} catch (IOException e) {
			String message = "Error de entrada o salida de datos";
			throw new SiaException(SiaExceptionCodes.IN_OUT, message, e);
		}

	}

	/**
	 * Metodo que devuelve el HashMap<String, Employee> con los empleados con su ID
	 * como Key de la base de datos que llamemos.
	 * 
	 * @return HashMap<String, Employee> con la lista de empleados como value y su
	 *         id por key
	 * @throws SiaException
	 */
	public HashMap<String, Employee> getMap() throws SiaException {
		// Creamos el HashMap que vamos a rellenar con el id y con los empleados.
		HashMap<String, Employee> employeeList = new HashMap<String, Employee>();

		// Aqui recuperamos los datos de la bbdd en forma de List<HashMap<String,
		// String>>. El primer String del HashMap es el nombre de la columna y el
		// segundo String el dato que le corresponde.
		List<HashMap<String, String>> dataList = db.getDataFromTable(table);
		log.trace("Datos recuperados de la tabla " + table);

		// Recorremos la lista de los datos de la bbdd y vamos generando los empleados y
		// metiendolos en el HashMap que vamos a devolver.
		for (HashMap<String, String> dataline : dataList) {
			// Creamos el empleado
			Employee emp = createEmployee(dataline);
			// Lo metemos en el HashMap
			employeeList.put(emp.getId(), emp);
			log.trace("Añadimos el employee al HashMap: " + emp);
		}

		log.trace("Lista de empleados: [" + employeeList + "]");
		return employeeList;

	}

	/**
	 * Metodo que crea el objeto empleado a partir de la coleccion de datos extraida
	 * de la BBDD
	 * 
	 * @param empData HashMap<String, String> con los datos del empleado, la key es
	 *                la columna
	 * @return Employee con los datos del empleado
	 * @throws SiaException
	 */
	private static Employee createEmployee(HashMap<String, String> empData) throws SiaException {

		Employee emp = null;

		try {
			Date empHiringDate = null;
			int empYearSalary = -1;
			boolean empSickLeave = false;

			// Parseamos la fecha de String a Date
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			formatter.setTimeZone(TimeZone.getTimeZone("Europe/Madrid"));
			empHiringDate = formatter.parse(empData.get(DatabaseConstants.HIRING_DATE));

			// Aqui formateamos el salario anual a numero entero
			empYearSalary = Integer.parseInt(empData.get(DatabaseConstants.YEAR_SALARY));
			// Parseamos el boleano de la baja
			empSickLeave = Boolean.parseBoolean(empData.get(DatabaseConstants.SICK_LEAVE));
			log.trace("Parseamos todos los datos necesarios del csv");

			// Creamos el empleado con los datos obtenidos
			emp = new Employee(empData.get(DatabaseConstants.ID), empData.get(DatabaseConstants.NAME),
					empData.get(DatabaseConstants.SURNAME1), empData.get(DatabaseConstants.SURNAME2),
					empData.get(DatabaseConstants.PHONE), empData.get(DatabaseConstants.EMAIL),
					empData.get(DatabaseConstants.JOB), empHiringDate, empYearSalary, empSickLeave);
			log.trace(emp);

		} catch (NumberFormatException e) {
			String message = "Fallo al introducir el salario anual";
			throw new SiaException(SiaExceptionCodes.NUMBER_FORMAT, message, e);

		} catch (ParseException e) {
			String message = "Fallo al formatear la fecha de contratacion";
			throw new SiaException(SiaExceptionCodes.PARSE_DATE, message, e);

		}

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
	public void executeTransactions(List<EmpTransaction> transactionsList) throws SiaException {

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
				db.executeStatement(query);

			} else if (empTransaction.getStatus().equals("DELETE")) {
				String query = "DELETE FROM employee WHERE ID = '" + empTransaction.getEmployee().getId() + "';";

				log.trace("Query a ejecutar: " + query);
				db.executeStatement(query);

			} else {
				// Para el UPDATE, obtenemos el empleado desde otro método en el que comprobamos
				// los datos a modificar
				String query = getUpdatedEmployee(empTransaction);

				log.trace("Query a ejecutar: " + query);
				db.executeStatement(query);
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
	public String getUpdatedEmployee(EmpTransaction empTransaction) {
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
		for (int i = 0; i < empTransaction.getModifiedFields().size(); i++) {

			if (empTransaction.getModifiedFields().get(i).equals("name")) {
				name = true;

			} else if (empTransaction.getModifiedFields().get(i).equals("surname1")) {
				surname1 = true;

			} else if (empTransaction.getModifiedFields().get(i).equals("surname2")) {
				surname2 = true;

			} else if (empTransaction.getModifiedFields().get(i).equals("phone")) {
				phone = true;

			} else if (empTransaction.getModifiedFields().get(i).equals("email")) {
				email = true;

			} else if (empTransaction.getModifiedFields().get(i).equals("job")) {
				job = true;

			} else if (empTransaction.getModifiedFields().get(i).equals("hiringDate")) {
				hiringDate = true;

			} else if (empTransaction.getModifiedFields().get(i).equals("yearSalary")) {
				yearSalary = true;

			} else if (empTransaction.getModifiedFields().get(i).equals("sickLeave")) {
				sickLeave = true;
			}

		}

		// Si los datos han sido cambiados, establecemos el dato para añadirlo a la
		// query.
		if (name) {
			query += " name = '" + empTransaction.getEmployee().getName() + "',";
		}
		if (surname1) {
			query += " first_surname = '" + empTransaction.getEmployee().getSurname1() + "',";
		}
		if (surname2) {
			query += " second_surname = '" + empTransaction.getEmployee().getSurname2() + "',";
		}
		if (phone) {
			query += " phone = '" + empTransaction.getEmployee().getTlf() + "',";
		}
		if (email) {
			query += " email = '" + empTransaction.getEmployee().getMail() + "',";
		}
		if (job) {
			query += " job = '" + empTransaction.getEmployee().getJob() + "',";
		}
		if (hiringDate) {
			query += " hiring_date = parsedatetime('"
					+ new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(empTransaction.getEmployee().getHiringDate())
					+ "', 'dd/MM/yyyy HH:mm:ss'),";
		}
		if (yearSalary) {
			query += " year_salary = '" + empTransaction.getEmployee().getYearSalary() + "',";
		}
		if (sickLeave) {
			query += " sick_leave = '" + empTransaction.getEmployee().isSickLeave() + "',";
		}

		// Completamos la query y se la devolvemos para ejecutarla en la base de datos.
		query = query.substring(0, query.length() - 1);
		query += " WHERE ID = '" + empTransaction.getEmployee().getId() + "';";

		return query;
	}

}
