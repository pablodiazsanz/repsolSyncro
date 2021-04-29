package repsolSyncro.businessLogic;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

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

	// Objetos para leer el archivo properties
	private static Properties file;
	private static FileInputStream ip;

	// Datos de conexion a la BBDD
	// Direccion de la BBDD
	private static String driver;
	// Usuario que se loguea en la BBDD
	private static String user;
	// Contraseña del usuario que se logea
	private static String pwd;

	public EmpDb() {
	}

	/**
	 * Comprueba que la conexion a BBDD se realiza correctamente y tambien comprueba
	 * que los datos del fichero properties del servidor estan corrrectos
	 * 
	 * @return true si todo esta bien
	 * @throws SiaException
	 */
	public static boolean tryConnection() throws SiaException {
		// Devolvemos si esta conectado a la bbdd
		boolean connected = true;

		try {
			// Obtenemos el fichero con las propiedades.
			file = new Properties();
			ip = new FileInputStream(
					PropertiesChecker.getAllProperties().getProperty(PropertyConstants.PATH_SERVER_DB_PROPERTY_FILE));
			file.load(ip);

			log.trace("Propiedades obtenidas. Cargamos las propiedades en nuestras variables estáticas.");

			// Cargamos las propiedades en nuestras variables
			driver = file.getProperty(PropertyConstants.DB_DRIVER);
			user = file.getProperty(PropertyConstants.DB_USERNAME);
			pwd = file.getProperty(PropertyConstants.DB_PASSWORD);

			log.trace("Probamos la conexión con la bbdd");

			// Conectamos a la bbdd
			conn = DriverManager.getConnection(driver, user, pwd);
			log.trace("Conexión realizada.");

			conn.close();

		} catch (SQLException e) {
			String message = "Error de conexion a la bbdd";
			throw new SiaException(SiaExceptionCodes.SQL_ERROR, message, e);

		} catch (FileNotFoundException e) {
			String message = "No se ha encontrado el fichero o este no existe";
			throw new SiaException(SiaExceptionCodes.MISSING_FILE, message, e);

		} catch (IOException e) {
			String message = "Error de entrada o salida de datos";
			throw new SiaException(SiaExceptionCodes.IN_OUT, message, e);

		}
		return connected;
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
		try {
			// Conectamos con la BBDD
			conn = DriverManager.getConnection(driver, user, pwd);
			log.trace("Conexion establecida");

			// Ponemos la query para obtener la tabla de empleados
			String query = "SELECT * FROM employee;";
			log.trace("Query: " + query);

			// Ejecutamos la operacion y obtenemos los datos medante ResultSet
			PreparedStatement stmt = conn.prepareStatement(query);
			ResultSet rset = stmt.executeQuery();
			log.trace("Query ejecutada");

			// Recorremos linea a linea del ResultSet y vamos sacando los empleados uno a
			// uno y lo añadimos a la lista de empleados
			while (rset.next()) {
				Employee emp = new Employee(rset.getString(DatabaseConstants.ID),
						rset.getString(DatabaseConstants.NAME), rset.getString(DatabaseConstants.SURNAME1),
						rset.getString(DatabaseConstants.SURNAME2), rset.getString(DatabaseConstants.PHONE),
						rset.getString(DatabaseConstants.EMAIL), rset.getString(DatabaseConstants.JOB),
						rset.getTimestamp(DatabaseConstants.HIRING_DATE), rset.getInt(DatabaseConstants.YEAR_SALARY),
						rset.getBoolean(DatabaseConstants.SICK_LEAVE));

				log.trace("Empleado obtenido: [" + emp.toString() + "]");
				employeeList.put(rset.getString(DatabaseConstants.ID), emp);

			}

		} catch (SQLException e) {
			String message = "Error al obtener datos de la bbdd";
			throw new SiaException(SiaExceptionCodes.SQL_ERROR, message, e);
		}

		log.trace("Lista de empleados: [" + employeeList + "]");
		return employeeList;

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
				DbAccess.executeStatement(query, file);

			} else if (empTransaction.getStatus().equals("DELETE")) {
				String query = "DELETE FROM employee WHERE ID = '" + empTransaction.getEmployee().getId() + "';";

				log.trace("Query a ejecutar: " + query);
				DbAccess.executeStatement(query, file);

			} else {
				// Para el UPDATE, obtenemos el empleado desde otro método en el que comprobamos
				// los datos a modificar
				String query = getQueryUpdatedEmployee(empTransaction.getEmployee(),
						empTransaction.getModifiedFields());

				log.trace("Query a ejecutar: " + query);
				DbAccess.executeStatement(query, file);
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
