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
import java.util.Iterator;
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
		private static String driver;
		// Usuario que se loguea en la BBDD
		private static String user;
		// Contraseña del usuario que se logea
		private static String pwd;


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
	 * Este metodo ejecuta la query pasada por parametro en la base de datos
	 * señalada por el properties tambien pasado por parametro
	 * 
	 * @param query que va a ser ejecutada en la BBDD
	 * @param file  Properties que posee los datos del driver, usuario y password de la BBDD
	 * @throws SiaException
	 */
	public static void executeStatement(String query) throws SiaException {
		try {
			//Preparamos la conexión
			conn = DriverManager.getConnection(file.getProperty(PropertyConstants.DB_DRIVER),
					file.getProperty(PropertyConstants.DB_USERNAME), file.getProperty(PropertyConstants.DB_PASSWORD));
			log.trace("Conexión lista. Operacion a ejecutar: " + query);
			
			// Conectamos con la base de datos y ejecutamos la operación
			PreparedStatement stmt = conn.prepareStatement(query);
			stmt.execute();
			log.trace("Operacion ejecutada: " + query);
			
			// Cerramos la conexión a la bbdd
			conn.close();
		} catch (SQLException e) {
			String message = "No ha podido ejecutar la siguiente operación: " + query;
			throw new SiaException(SiaExceptionCodes.SQL_ERROR, message, e);
		}
	}
	/**
	 * 
	 * 
	 * @param table
	 * @param file
	 * @return
	 */
	public static List<HashMap<String, String>> getDataFromTable(String table){
		List<HashMap<String, String>> dataList = new ArrayList<HashMap<String, String>>();
		try {
			conn = DriverManager.getConnection(file.getProperty(PropertyConstants.DB_DRIVER),
					file.getProperty(PropertyConstants.DB_USERNAME), file.getProperty(PropertyConstants.DB_PASSWORD));
			PreparedStatement stmt = conn.prepareStatement("SELECT * FROM " + table);
			ResultSet rset = stmt.executeQuery();
			int columnsNumber = rset.getMetaData().getColumnCount();
			while (rset.next()) {
				HashMap<String, String> dataLine = new HashMap<>();
				for (int i = 1; i <= columnsNumber; i++) {
					dataLine.put(rset.getMetaData().getColumnName(i), rset.getString(i));
				}
				dataList.add(dataLine);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return dataList;
	}
	
}
