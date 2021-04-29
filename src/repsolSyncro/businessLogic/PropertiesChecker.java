package repsolSyncro.businessLogic;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;

import repsolSyncro.constants.PropertyConstants;
import repsolSyncro.exceptions.SiaException;
import repsolSyncro.exceptions.SiaExceptionCodes;

public class PropertiesChecker {

	// Logger para poder escribir las trazas del codigo en los logs
	private static Logger log = Logger.getLogger(PropertiesChecker.class);

	// Array con las direcciones de los ficheros si trabajamos todo en csv
	private static String[] ficherosCsvToCsv = { PropertyConstants.PATH_CLIENT_PROPERTY_FILE,
			PropertyConstants.PATH_SERVER_CSV_PROPERTY_FILE, PropertyConstants.PATH_RESULT_PROPERTY_FILE };

	// Array con las direcciones de los ficheros si trabajamos contra BBDD
	private static String[] ficherosCsvToBD = { PropertyConstants.PATH_SERVER_DB_PROPERTY_FILE,
			PropertyConstants.PATH_CLIENT_PROPERTY_FILE };

	// Ruta al fichero con todos los properties
	// private static String PropertiesPath =
	// "C:\\Users\\pdiazs\\eclipse-workspace\\repsolSyncro\\src\\propertiesRoutes.properties";
	private static String PropertiesPath = "C:\\Users\\mparrap\\git\\repsolSyncro\\src\\propertiesRoutes.properties";

	// Elegimos si trabajamos contra csv o contra BBDD
	private static boolean csvToDatabase;

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
		log.trace("iniciamos la comprobacion de los properties");
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

		// Cogemos del fichero si vamos a operar contra BBDD o contra fichero CSV
		csvToDatabase = Boolean.parseBoolean(allProperties.getProperty(PropertyConstants.CSV_TO_DATABASE));

		boolean valido = true;

		String[] ficheros = {};

		log.trace("elegimos como trabajamos");
		// Seleccionamos que ficheros properties y que datos deseamos leer
		// false - csv de cliente y servidor y resultado
		// true - csv de cliente, y acceso a BBDD para servidor y resultado
		if (csvToDatabase) {
			ficheros = ficherosCsvToBD;

		} else {
			ficheros = ficherosCsvToCsv;

		}
		// Recorremos y seleccionamos que metodo deseamos para comprobar los properties
		// si contienen la palabra csv en la "variable" del properties iran a
		// comprobarse con el metodo de los csv, si no iran a BBDD
		for (int i = 0; i < ficheros.length; i++) {
			log.trace("comporvamos properties csv");
			if (ficheros[i].contains("CSV")) {
				valido = checkCsvProperties(allProperties.getProperty(ficheros[i]));

			} else {
				log.trace("comporvamos properties BBDD");
				valido = checkBDProperties(allProperties.getProperty(ficheros[i]));

			}
		}

		return valido;
	}

	/**
	 * Devuelve el valor booleano del properties para conocer el metodo de trabajo
	 * del programa
	 * 
	 * @return Boolean
	 */
	public static boolean getCsvToDatabase() {
		return csvToDatabase;
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
			log.trace("leemos fichero de csv");
			// Leemos la ruta del archivo
			Properties file = new Properties();
			FileInputStream ip = new FileInputStream(src);
			file.load(ip);
			log.trace("comprobamos valores en fichero properties");
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
	private static boolean checkBDProperties(String src) throws SiaException {
		// Iniciamos el booleano de respuesta en false dando por hecho que fracasara y
		// solo pasandolo a true si se dan todas las condiciones
		boolean readed = false;

		try {
			// Leemos la ruta del archivo
			Properties file = new Properties();
			FileInputStream ip = new FileInputStream(src);
			file.load(ip);
			log.trace("leemos fichero de BBDD");
			// Leemos los datos comprobando que no falta ninguno
			file.getProperty(PropertyConstants.DB_DRIVER);
			file.getProperty(PropertyConstants.DB_USERNAME);
			file.getProperty(PropertyConstants.DB_PASSWORD);
			log.trace("comprobamos valores en fichero DDBB");
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
