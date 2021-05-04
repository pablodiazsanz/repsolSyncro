package repsolSyncro.dataAccess;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import repsolSyncro.businessLogic.PropertiesChecker;
import repsolSyncro.constants.PropertyConstants;
import repsolSyncro.exceptions.SiaException;
import repsolSyncro.exceptions.SiaExceptionCodes;

/**
 * En esta clase lo que hacemos es ejecutar operaciones contra la base de datos,
 * las cuales pueden ser SELECT, CREATE, UPDATE y DELETE.
 *
 */
public class DbAccess {

	// Objeto que conecta con la BBDD
	private static Connection conn;
	// Logger para poder escribir las trazas del codigo en los logs
	private static Logger log = Logger.getLogger(DbAccess.class);
	// Objetos para leer el archivo properties
	private static Properties file;
	private static FileInputStream ip;

	// Datos de conexion a la BBDD
	// Direccion de la BBDD
	private String driver;
	// Usuario que se loguea en la BBDD
	private String user;
	// Contrase�a del usuario que se logea
	private String pwd;

	public DbAccess(Properties file) throws SiaException {
		driver = file.getProperty(PropertyConstants.DB_DRIVER);
		user = file.getProperty(PropertyConstants.DB_USERNAME);
		pwd = file.getProperty(PropertyConstants.DB_PASSWORD);

	}

	/**
	 * Comprueba que la conexion a BBDD se realiza correctamente y tambien comprueba
	 * que los datos del fichero properties del servidor estan corrrectos
	 * 
	 * @return true si todo esta bien
	 * @throws SiaException
	 */
	public static boolean tryConnection(String propertyPath) throws SiaException {
		// Devolvemos si esta conectado a la bbdd
		boolean connected = true;

		try {
			// Obtenemos el fichero con las propiedades.
			file = new Properties();
			ip = new FileInputStream(PropertiesChecker.getAllProperties().getProperty(propertyPath));
			file.load(ip);

			log.trace("Propiedades obtenidas. Cargamos las propiedades en nuestras variables est�ticas.");

			// Cargamos las propiedades en nuestras variables
			String driver = file.getProperty(PropertyConstants.DB_DRIVER);
			String user = file.getProperty(PropertyConstants.DB_USERNAME);
			String pwd = file.getProperty(PropertyConstants.DB_PASSWORD);

			log.trace("Probamos la conexi�n con la bbdd");

			// Conectamos a la bbdd
			conn = DriverManager.getConnection(driver, user, pwd);
			log.trace("Conexi�n realizada.");

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
	 * Este metodo ejecuta la query pasada por parametro en la base de datos
	 * se�alada por el properties tambien pasado por parametro
	 * 
	 * @param query que va a ser ejecutada en la BBDD
	 * @param file  Properties que posee los datos del driver, usuario y password de
	 *              la BBDD
	 * @throws SiaException
	 */
	public void executeStatement(String query) throws SiaException {
		try {
			// Preparamos la conexi�n
			conn = DriverManager.getConnection(file.getProperty(PropertyConstants.DB_DRIVER),
					file.getProperty(PropertyConstants.DB_USERNAME), file.getProperty(PropertyConstants.DB_PASSWORD));
			log.trace("Conexi�n lista. Operacion a ejecutar: " + query);

			// Conectamos con la base de datos y ejecutamos la operaci�n
			PreparedStatement stmt = conn.prepareStatement(query);
			stmt.execute();
			log.trace("Operacion ejecutada: " + query);

			// Cerramos la conexi�n a la bbdd
			conn.close();
		} catch (SQLException e) {
			String message = "No ha podido ejecutar la siguiente operaci�n: " + query;
			throw new SiaException(SiaExceptionCodes.SQL_ERROR, message, e);
		}
	}

	/**
	 * En este m�todo lo que hacemos es recuperar toda la informaci�n de la tabla de
	 * la base de datos y la almacenamos en una List<HashMap<String, String>> con el
	 * primer String del HashMap con el nombre de la columna y el segundo String el
	 * dato que se obtiene del ResultSet.
	 * 
	 * @param table El nombre de la tabla de la que recogemos la informacion
	 * @return dataList La List<HashMap<String, String>> con los datos de la tabla
	 * @throws SiaException
	 */
	public List<HashMap<String, String>> getDataFromTable(String table) throws SiaException {

		// Creamos la List<HashMap<String, String>> para almacenar los datos
		List<HashMap<String, String>> dataList = new ArrayList<HashMap<String, String>>();
		try {
			// Conectamos con la bbdd
			conn = DriverManager.getConnection(file.getProperty(PropertyConstants.DB_DRIVER),
					file.getProperty(PropertyConstants.DB_USERNAME), file.getProperty(PropertyConstants.DB_PASSWORD));

			log.trace("Conectado a la bbdd");

			// Preparamos la consulta a la tabla
			PreparedStatement stmt = conn.prepareStatement("SELECT * FROM " + table);
			log.trace("Nombre de la tabla a la que vamos a hacer la consulta: " + table);

			// Ejecutamos la consulta
			ResultSet rset = stmt.executeQuery();

			// Obtenemos el numero de columnas que tiene la tabla
			int columnsNumber = rset.getMetaData().getColumnCount();

			// Recorremos las lineas de consulta y le pasamos los datos a la lisya
			while (rset.next()) {

				// Creamos un HashMap para ir agregando linea a linea
				HashMap<String, String> dataLine = new HashMap<>();
				for (int i = 1; i <= columnsNumber; i++) {
					dataLine.put(rset.getMetaData().getColumnName(i), rset.getString(i));
				}

				log.trace(dataLine);
				dataList.add(dataLine);
			}
		} catch (SQLException e) {
			String message = "Error en la conexion a la bbdd";
			throw new SiaException(SiaExceptionCodes.SQL_ERROR, message, e);

		}
		return dataList;
	}

}
