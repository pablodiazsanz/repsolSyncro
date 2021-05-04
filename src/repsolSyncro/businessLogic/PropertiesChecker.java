package repsolSyncro.businessLogic;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;

import repsolSyncro.constants.PropertyConstants;
import repsolSyncro.dataAccess.DbAccess;
import repsolSyncro.exceptions.SiaException;
import repsolSyncro.exceptions.SiaExceptionCodes;

public class PropertiesChecker {

	// Logger para poder escribir las trazas del codigo en los logs
	private static Logger log = Logger.getLogger(PropertiesChecker.class);

	// Ruta al fichero con todos los properties
	private static String PropertiesPath = "C:\\Users\\pdiazs\\eclipse-workspace\\repsolSyncro\\src\\propertiesRoutes.properties";
	//private static String PropertiesPath = "C:\\Users\\mparrap\\git\\repsolSyncro\\src\\propertiesRoutes.properties";

	private static boolean clientElection;
	private static boolean serverElection;
	private static boolean resultElection;

	// Fichero .properties del programa que guarda los datos generales,
	// incluye direcciones al resto de properties y forma de trabajo seleccionada
	private static Properties allProperties;

	/**
	 * Metodo que nos confirma si todos los datos en los archivos properties estan
	 * disponibles o si debemos no iniciar el programa
	 * 
	 * @return true si la comprobacion es correcta
	 * @throws SiaException si la comprobación es incorrecta
	 */
	public static boolean checker() throws SiaException {
		log.trace("Iniciamos la comprobacion de los properties");
		// Inicializamos allProperties
		allProperties = new Properties();

		try {
			// Cargamos al properties la ruta con el FileInputStream
			FileInputStream ip = new FileInputStream(PropertiesPath);
			allProperties.load(ip);

		} catch (FileNotFoundException e) {
			String message = "Fichero no encontrado: [" + PropertiesPath + "]";
			log.error(SiaExceptionCodes.MISSING_FILE + " - " + message, e);
			throw new SiaException(SiaExceptionCodes.MISSING_FILE, message, e);

		} catch (IOException e) {
			String message = "Fallo de entrada o salida";
			log.error(SiaExceptionCodes.IN_OUT + " - " + message, e);
			throw new SiaException(SiaExceptionCodes.IN_OUT, message, e);

		}

		// Cargamos las opciones dependiendo del origen de cada parte
		clientElection = Boolean.parseBoolean(allProperties.getProperty(PropertyConstants.CLIENT_ELECTION));
		serverElection = Boolean.parseBoolean(allProperties.getProperty(PropertyConstants.SERVER_ELECTION));
		resultElection = Boolean.parseBoolean(allProperties.getProperty(PropertyConstants.RESULT_ELECTION));

		log.trace("Elegimos como trabajamos");
		// Comprobamos las properties de cada origen dependiendo de como se trabajen
		// si alguno falla saltaria una excepcion y nunca llegaria al return por lo que
		// el programa pararia ahi.
		
		// true - Base de datos
		// false - csv
		if (clientElection) {
			checkDbProperties(allProperties.getProperty(PropertyConstants.PATH_CLIENT_PROPERTY_FILE));
			DbAccess.tryConnection(PropertyConstants.PATH_CLIENT_PROPERTY_FILE);
		} else {
			checkCsvProperties(allProperties.getProperty(PropertyConstants.PATH_CLIENT_PROPERTY_FILE));
		}

		if (serverElection) {
			checkDbProperties(allProperties.getProperty(PropertyConstants.PATH_SERVER_PROPERTY_FILE));
			DbAccess.tryConnection(PropertyConstants.PATH_SERVER_PROPERTY_FILE);
		} else {
			checkCsvProperties(allProperties.getProperty(PropertyConstants.PATH_SERVER_PROPERTY_FILE));
		}

		if (resultElection) {
			checkDbProperties(allProperties.getProperty(PropertyConstants.PATH_RESULT_PROPERTY_FILE));
			DbAccess.tryConnection(PropertyConstants.PATH_RESULT_PROPERTY_FILE);
		} else {
			checkCsvProperties(allProperties.getProperty(PropertyConstants.PATH_RESULT_PROPERTY_FILE));
		}

		return true;
	}

	/**
	 * Devuelve el fichero de properties general
	 * 
	 * @return Properties
	 */
	public static Properties getAllProperties() {
		return allProperties;
	}

	/**
	 * Devuelve el valor booleano de forma de trabajo del cliente
	 * 
	 * @return boolean
	 */
	public static boolean getClientElection() {
		return clientElection;
	}

	/**
	 * Devuelve el valor booleano de forma de trabajo del server
	 * 
	 * @return boolean
	 */
	public static boolean getServerElection() {
		return serverElection;
	}

	/**
	 * Devuelve el valor booleano de forma de trabajo del resultado
	 * 
	 * @return boolean
	 */
	public static boolean getResultElection() {
		return resultElection;
	}

	/**
	 * Metodo que busca en el fichero .properties si todos los datos tienen valor y
	 * devuelve true de ser afirmativo y false de ser negativo.
	 * 
	 * @param src Obtiene el path del fichero de propiedades que vamos a comprobar
	 * @return boolean true si se encuentran todos los datos en el properties, false
	 *         si no
	 * @throws SiaException
	 */
	private static boolean checkCsvProperties(String src) throws SiaException {
		// Iniciamos el booleano de respuesta en false dando por hecho que fracasara y
		// solo pasandolo a true si se dan todas las condiciones
		boolean readed = false;

		try {
			log.trace("Leemos fichero de csv");
			// Leemos la ruta del archivo
			Properties file = new Properties();
			FileInputStream ip = new FileInputStream(src);
			file.load(ip);
			log.trace("Comprobamos valores en fichero properties");
			// Leemos los datos comprobando que no falta ninguno
			file.getProperty(PropertyConstants.CSV_PATH);
			file.getProperty(PropertyConstants.CSV_HEAD_ID);
			file.getProperty(PropertyConstants.CSV_HEAD_NAME);
			file.getProperty(PropertyConstants.CSV_HEAD_SURNAME1);
			file.getProperty(PropertyConstants.CSV_HEAD_SURNAME2);
			file.getProperty(PropertyConstants.CSV_HEAD_PHONE);
			file.getProperty(PropertyConstants.CSV_HEAD_EMAIL);
			file.getProperty(PropertyConstants.CSV_HEAD_JOB);
			file.getProperty(PropertyConstants.CSV_HEAD_HIRING_DATE);
			file.getProperty(PropertyConstants.CSV_HEAD_YEAR_SALARY);
			file.getProperty(PropertyConstants.CSV_HEAD_SICK_LEAVE);

			// Solo se pasa a true en caso de que pase por todos los datos del fichero
			// properties
			readed = true;
			log.trace("Fichero leido exitosamente: [" + src + "]");

		} catch (FileNotFoundException e) {
			log.error("Fichero no encontrado", e);
			throw new SiaException(SiaExceptionCodes.MISSING_FILE, e);

		} catch (IOException e) {
			log.error("Fallo de entrada o salida", e);
			throw new SiaException(SiaExceptionCodes.MISSING_FILE, e);
		}
		return readed;
	}

	/**
	 * Metodo que busca en el fichero .properties si todos los datos tienen valor y
	 * devuelve true de ser afirmativo y false de ser negativo
	 * 
	 * @param src Obtiene el path del fichero de propiedades que vamos a comprobar
	 * @return boolean true si se encuentran todos los datos en el properties, false
	 *         si no
	 * @throws SiaException
	 */
	private static boolean checkDbProperties(String src) throws SiaException {
		// Iniciamos el booleano de respuesta en false dando por hecho que fracasara y
		// solo pasandolo a true si se dan todas las condiciones
		boolean readed = false;

		try {
			// Leemos la ruta del archivo
			Properties file = new Properties();
			FileInputStream ip = new FileInputStream(src);
			file.load(ip);
			log.trace("Leemos fichero de BBDD");
			// Leemos los datos comprobando que no falta ninguno
			file.getProperty(PropertyConstants.DB_DRIVER);
			file.getProperty(PropertyConstants.DB_USERNAME);
			file.getProperty(PropertyConstants.DB_PASSWORD);
			file.getProperty(PropertyConstants.DB_TABLE);
			log.trace("Comprobamos valores en fichero BBDD");
			// Solo se pasa a true en caso de que pase por todos los datos del fichero
			// properties
			readed = true;
			log.trace("Fichero leido exitosamente: [" + src + "]");

		} catch (FileNotFoundException e) {
			log.error("Fichero no encontrado", e);
			throw new SiaException(SiaExceptionCodes.MISSING_FILE, e);

		} catch (IOException e) {
			log.error("Fallo de entrada o salida", e);
			throw new SiaException(SiaExceptionCodes.MISSING_FILE, e);
		}
		return readed;
	}

}
