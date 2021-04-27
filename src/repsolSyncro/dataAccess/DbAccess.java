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
	
	/**
	 * Este metodo ejecuta la query pasada por parametro en la base de datos se�alada por el properties tambien pasado por parametro
	 * 
	 * @param query que va a ser ejecutada en la BBDD
	 * @param file que posee los datos de diraccion usuario y contrase�a de la BBDD
	 * @throws SiaException
	 */
	public static void executeStatement(String query, Properties file) throws SiaException {
		try {
			conn = DriverManager.getConnection(file.getProperty(PropertyConstants.DB_DRIVER),
					file.getProperty(PropertyConstants.DB_USERNAME), 
					file.getProperty(PropertyConstants.DB_PASSWORD));
			log.trace(query);
			PreparedStatement stmt = conn.prepareStatement(query);
			stmt.execute();
			conn.close();
		} catch (SQLException e) {
			String message = "No ha podido ejecutar la siguiente operaci�n: " + query;
			throw new SiaException(SiaExceptionCodes.SQL_ERROR, message, e);
		}
	}

}
