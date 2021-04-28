package repsolSyncro.dataAccess;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.log4j.Logger;

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

	/**
	 * Este metodo ejecuta la query pasada por parametro en la base de datos
	 * señalada por el properties tambien pasado por parametro
	 * 
	 * @param query que va a ser ejecutada en la BBDD
	 * @param file  Properties que posee los datos del driver, usuario y password de la BBDD
	 * @throws SiaException
	 */
	public static void executeStatement(String query, Properties file) throws SiaException {
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

}
