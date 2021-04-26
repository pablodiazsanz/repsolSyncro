package repsolSyncro.dataAccess;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.log4j.Logger;

import repsolSyncro.constants.PropertyConstants;
import repsolSyncro.exceptions.SiaException;
import repsolSyncro.exceptions.SiaExceptionCodes;

public class DbAccess {

	// objeto que conecta con la BBDD
	private static Connection conn;
	// loggger para poder escribir las trazas del codigo en los logs
	private static Logger log = Logger.getLogger(DbAccess.class);
	// objetos para leer el archivo properties
	private static Properties file;
	private static FileInputStream ip;
	// datos de conexion a la BBDD
	// direccion de la BBDD
	private static String driver;
	// Usuario que se loguea en la BBDD
	private static String user;
	// Contraseña del uisuario que se logea
	private static String pwd;
	
	/**
	 * Comprueba que la conexion a BBDD se realiza correctamente y tambien comprueba
	 * que los datos del fichero properties del servidor estan corrrectos
	 * 
	 * @return true si todo esta bien, false si falla algo
	 * @throws SiaException
	 */
	
	public static boolean tryConnection() throws SiaException {
		boolean conectado = true;
		try {
			file = new Properties();
			ip = new FileInputStream(PropertyConstants.PATH_SERVER_DB_PROPERTY_FILE);
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

	public static void executeStatement(String query) throws SiaException {
		try {
			conn = DriverManager.getConnection(driver, user, pwd);
			log.trace(query);
			PreparedStatement stmt = conn.prepareStatement(query);
			stmt.execute();
			conn.close();
		} catch (SQLException e) {
			log.error("no ha podido ejecutar la operación");
			throw new SiaException(SiaExceptionCodes.SQL_ERROR, e);
		}
	}

}
