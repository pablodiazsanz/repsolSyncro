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

import repsolSyncro.constants.DatabaseConstants;
import repsolSyncro.constants.PropertyConstants;
import repsolSyncro.entities.EmpTransaction;
import repsolSyncro.entities.Employee;
import repsolSyncro.exceptions.SiaException;
import repsolSyncro.exceptions.SiaExceptionCodes;

public class EmpDb {

	// objeto que conecta con la BBDD
	private static Connection conn;
	// loggger para poder escribir las trazas del codigo en los logs
	private static Logger log = Logger.getLogger(EmpDb.class);
	// objetos para leer el archivo properties
	private static Properties file;
	private static FileInputStream ip;
	// datos de conexion a la BBDD
	// direccion de la BBDD
	private static String driver;
	// Usuario que se loguea en la BBDD
	private static String user;
	// Contrase�a del uisuario que se logea
	private static String pwd;

	/**
	 * Comprueba que la conexion a BBDD se realiza correctamente y tambien comprueba
	 * que los datos del fichero properties del servidor estan corrrectos
	 * @param allProperties 
	 * 
	 * @return true si todo esta bien, false si falla algo
	 * @throws SiaException
	 */
	public static boolean tryConnection(Properties allProperties) throws SiaException {
		boolean conectado = true;
		try {
			file = new Properties();
			ip = new FileInputStream(allProperties.getProperty(PropertyConstants.PATH_SERVER_DB_PROPERTY_FILE));
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
	public static HashMap<String, Employee> getMap() throws SiaException {
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
						rset.getTimestamp(DatabaseConstants.HIRING_DATE), rset.getInt(DatabaseConstants.YEAR_SALARY),
						rset.getBoolean(DatabaseConstants.SICK_LEAVE));
				employeeList.put(rset.getString(DatabaseConstants.ID), emp);

			}

		} catch (SQLException e) {
			log.error("Error al obtener datos de la bbdd");
			throw new SiaException(SiaExceptionCodes.SQL_ERROR, e);
		}

		return employeeList;

	}

	public static void executeTransactions(List<EmpTransaction> transactionsList) throws SiaException {

		for (EmpTransaction empTransaction : transactionsList) {
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
				DbAccess.executeStatement(query, file);

			} else if (empTransaction.getStatus().equals("DELETE")) {
				String query = "DELETE FROM employee WHERE ID = '" + empTransaction.getEmployee().getId() + "';";
				DbAccess.executeStatement(query, file);
			} else {
				String query = getQueryUpdatedEmployee(empTransaction.getEmployee(), empTransaction.getModifiedFields());
				DbAccess.executeStatement(query, file);
			}
		}
	}

	public static String getQueryUpdatedEmployee(Employee updatedEmployee, List<String> modifiedFields) throws SiaException {
		// Creamos esta variable para enviarle al fichero CSV el contenido.
		String query = "UPDATE employee SET";

		/*
		 * Estas variables son las que vamos a darle a la cadena updatedData para pasar
		 * los datos. Estas cadenas se van a modificar si la lista extraData no contiene
		 * el dato
		 */

		// Estas variables son las vamos a utilizar para comprobar si el dato ha
		// cambiado o no.
		boolean name = false;
		boolean surname1 = false;
		boolean surname2 = false;
		boolean phone = false;
		boolean email = false;
		boolean job = false;
		boolean hiringDate = false;
		boolean yearSalary = false;
		boolean sickLeave = false;

		// Recorremos la lista para saber que datos no se han cambiado.
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

		return query;
	}

}
