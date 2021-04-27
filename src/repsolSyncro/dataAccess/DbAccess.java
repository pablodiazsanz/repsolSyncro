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
	// Contrase�a del uisuario que se logea
	private static String pwd;
	
	
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
			log.error("no ha podido ejecutar la operaci�n");
			throw new SiaException(SiaExceptionCodes.SQL_ERROR, e);
		}
	}

}
