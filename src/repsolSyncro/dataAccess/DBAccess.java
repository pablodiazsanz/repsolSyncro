package repsolSyncro.dataAccess;

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

import repsolSyncro.Employee;
import repsolSyncro.constants.DatabaseConstants;
import repsolSyncro.constants.PropertyConstants;
import repsolSyncro.exceptions.SiaException;
import repsolSyncro.exceptions.SiaExceptionCodes;
/**
 * Clase que conecta e interractua con la base de datos
 *
 */
public class DBAccess {

	//objeto que conecta con la BBDD
	private static Connection conn;
	//loggger para poder escribir las trazas del codigo en los logs
	private static Logger log = Logger.getLogger(DBAccess.class);
	//objetos para leer el archivo properties
	private static Properties file;
	private static FileInputStream ip;
	//datos de conexion a la BBDD
	//direccion de la BBDD
	private static String driver;
	//Usuario que se loguea en la BBDD
	private static String user;
	//Contraseña del uisuario que se logea
	private static String pwd;

	/**
	 * Comprueba que la conexion a BBDD se realiza correctamente y 
	 * tambien comprueba que los datos del fichero properties del servidor estan corrrectos
	 * 
	 * @return true si todo esta bien, false si falla algo
	 * @throws SiaException 
	 */
	public static boolean tryConnection() throws SiaException {
		boolean conectado = true;
		try {
			file = new Properties();
			ip = new FileInputStream(PropertyConstants.PATH_SERVER_PROPERTY_FILE);
			file.load(ip);

			driver = file.getProperty(PropertyConstants.DB_DRIVER);
			user = file.getProperty(PropertyConstants.DB_USERNAME);
			pwd = file.getProperty(PropertyConstants.DB_PASSWORD);

			conn = DriverManager.getConnection(driver, user, pwd);
			conn.close();
		} catch (SQLException e) {
			log.error("Error de conexion a la bbdd");
			throw new SiaException(SiaExceptionCodes.SQL_ERROR, e);
		} catch (FileNotFoundException e) {
			log.error("No se ha encontrado el fichero o este no existe");
			throw new SiaException(SiaExceptionCodes.MISSING_FILE, e);
		} catch (IOException e) {
			log.error("Error de entrada o salida de datos");
			throw new SiaException(SiaExceptionCodes.IN_OUT, e);
		}
		return conectado;
	}

	/**
	 * Busca en la BBDD la lista de empleados y la devuelve en un HashMap donde la
	 * clave es su ID
	 * 
	 * @return HasMap<String, Employee> con la lista de empleados y su id por key
	 * @throws SiaException 
	 */
	public static HashMap<String, Employee> getEmployeesFromServer() throws SiaException {
		HashMap<String, Employee> employeeList = new HashMap<String, Employee>();
		try {
			conn = DriverManager.getConnection(driver, user, pwd);
			String query = "SELECT * FROM employee;";
			PreparedStatement stmt = conn.prepareStatement(query);
			ResultSet rset = stmt.executeQuery();
			while (rset.next()) {
				Employee emp = new Employee(rset.getString(DatabaseConstants.ID),
						rset.getString(DatabaseConstants.NAME), rset.getString(DatabaseConstants.SURNAME1),
						rset.getString(DatabaseConstants.SURNAME2), rset.getString(DatabaseConstants.PHONE),
						rset.getString(DatabaseConstants.EMAIL), rset.getString(DatabaseConstants.JOB),
						rset.getDate(DatabaseConstants.HIRING_DATE), rset.getInt(DatabaseConstants.YEAR_SALARY),
						rset.getBoolean(DatabaseConstants.SICK_LEAVE));
				employeeList.put(rset.getString(DatabaseConstants.ID), emp);
			}

		} catch (SQLException e) {
			log.error("Error al obtener datos de la bbdd");
			throw new SiaException(SiaExceptionCodes.SQL_ERROR, e);
		}

		return employeeList;

	}

	/**
	 * 
	 * Modifica el usuario pasado por parametro con los datos NO pasados por el
	 * segundo parametro en la BBDD
	 * 
	 * @param updatedEmployee empeado a modificar
	 * @param extraData       Datos que NO se van a modificar
	 * @throws SiaException 
	 */
	public static void updateEmployee(Employee updatedEmployee, List<String> extraData) throws SiaException {
		// Creamos esta variable para enviarle al fichero CSV el contenido.
		String query = "UPDATE employee SET";

		/*
		 * Estas variables son las que vamos a darle a la cadena updatedData para pasar
		 * los datos. Estas cadenas se van a modificar si la lista extraData no contiene
		 * el dato
		 */

		// Estas variables son las vamos a utilizar para comprobar si el dato ha
		// cambiado o no.
		boolean name = true;
		boolean surname1 = true;
		boolean surname2 = true;
		boolean phone = true;
		boolean email = true;
		boolean job = true;
		boolean hiringDate = true;
		boolean yearSalary = true;
		boolean sickLeave = true;

		// Comprobamos que la lista no está vacia.
		if (!extraData.isEmpty()) {

			// Recorremos la lista para saber que datos no se han cambiado.
			for (int i = 0; i < extraData.size(); i++) {

				if (extraData.get(i).equals("name")) {
					name = false;

				} else if (extraData.get(i).equals("surname1")) {
					surname1 = false;

				} else if (extraData.get(i).equals("surname2")) {
					surname2 = false;

				} else if (extraData.get(i).equals("phone")) {
					phone = false;

				} else if (extraData.get(i).equals("email")) {
					email = false;

				} else if (extraData.get(i).equals("job")) {
					job = false;

				} else if (extraData.get(i).equals("hiringDate")) {
					hiringDate = false;

				} else if (extraData.get(i).equals("yearSalary")) {
					yearSalary = false;

				} else if (extraData.get(i).equals("sickLeave")) {
					sickLeave = false;
				}

			}
		}

		// Si los datos han sido cambiados, establecemos el dato para pasarselo al
		// fichero CSV.
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

		// Metemos los datos en la cadena que vamos a darle al fichero CSV con los datos
		// correctos.
		query = query.substring(0, query.length() - 1);
		query += " WHERE ID = '" + updatedEmployee.getId() + "';";
		// Añadimos la linea de datos al fichero CSV.

		try {
			conn = DriverManager.getConnection(driver, user, pwd);
			PreparedStatement stmt = conn.prepareStatement(query);
			log.trace(query);
			stmt.executeUpdate();
			log.info("[" + updatedEmployee.getId() + "] - UPDATE");
		} catch (SQLException e) {
			log.error("No se han podido modificar datos de la bbdd");
			throw new SiaException(SiaExceptionCodes.SQL_ERROR, e);
		}

	}

	/**
	 * Da de alta al empleado pasado por parametro en la base de datos
	 * 
	 * @param emp empleado a dar de alta
	 * @throws SiaException 
	 */
	public static void createEmployee(Employee emp) throws SiaException {
		try {
			conn = DriverManager.getConnection(driver, user, pwd);
			String query = "INSERT INTO employee(id,name,first_surname,second_surname,phone,email,job,hiring_date,year_salary,sick_leave) VALUES ('"
					+ emp.getId() + "','" + emp.getName() + "','" + emp.getSurname1() + "','" + emp.getSurname2() + "',"
					+ emp.getTlf() + ",'" + emp.getMail() + "','" + emp.getJob() + "',parsedatetime('"
					+ new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(emp.getHiringDate())
					+ "', 'dd/MM/yyyy HH:mm:ss')," + emp.getYearSalary() + "," + emp.isSickLeave() + ");";
			log.trace(query);
			PreparedStatement stmt = conn.prepareStatement(query);
			stmt.execute();
			log.info("[" + emp.getId() + "] - CREATE");
		} catch (SQLException e) {
			log.error("no ha podido crear al empleado:" + emp.getId());
			throw new SiaException(SiaExceptionCodes.SQL_ERROR, e);
		}
	}

	/**
	 * Borra el empleado pasado por parametro de la base de datos
	 * 
	 * @param emp empleado a borrar
	 * @throws SiaException 
	 */
	public static void deleteEmployee(Employee emp) throws SiaException {
		try {
			conn = DriverManager.getConnection(driver, user, pwd);
			String query = "DELETE FROM employee WHERE ID = '" + emp.getId() + "';";
			log.trace(query);
			PreparedStatement stmt = conn.prepareStatement(query);
			stmt.execute();
			log.info("[" + emp.getId() + "] - DELETE");
		} catch (SQLException e) {
			log.error("no ha podido borrar al empleado:" + emp.getId());
			throw new SiaException(SiaExceptionCodes.SQL_ERROR, e);
		}
	}

}
