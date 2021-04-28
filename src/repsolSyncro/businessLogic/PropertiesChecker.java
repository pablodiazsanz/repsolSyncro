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
	// loguer de la clase
	private static Logger log = Logger.getLogger(PropertiesChecker.class);
	// array con las direcciones de los ficheros si trabajamos todo en csv
	private static String[] ficherosCsvToCsv = { PropertyConstants.PATH_CLIENT_PROPERTY_FILE,
			PropertyConstants.PATH_SERVER_CSV_PROPERTY_FILE, PropertyConstants.PATH_RESULT_PROPERTY_FILE };
	// array con las direcciones de los ficheros si trabajamos contra BBDD
	private static String[] ficherosCsvToBD = { PropertyConstants.PATH_SERVER_DB_PROPERTY_FILE,
			PropertyConstants.PATH_CLIENT_PROPERTY_FILE };
	// ruta al fichero con todos los properties
	// private static String PropertiesPath =
	// "C:\\Users\\pdiazs\\eclipse-workspace\\repsolSyncro\\src\\propertiesRoutes.properties";
	private static String PropertiesPath = "C:\\Users\\mparrap\\git\\repsolSyncro\\src\\propertiesRoutes.properties";
	// eleccion de manera de trabajo
	private static boolean csvToDatabase;
	// fichero properties del programa que gusrada los datos generales,
	// incluye direcciones al resto de properties y forma de trabajo seleccionada
	private static Properties allProperties;

	/**
	 * Metodo que nos confirma si todos los datos en los archivos properties estan
	 * disponibles o si debemos no iniciar el programa
	 * 
	 * @param allProperties fichero properties con la direccion dle resto de
	 *                      properties
	 * @param csvToDatabase true si trabajamos contra BBDD false si es full csv
	 * @return true si todo es correcto false si no lo es
	 * @throws SiaException
	 */
	public static boolean checker() throws SiaException {
		allProperties = new Properties();
		try {
			FileInputStream ip = new FileInputStream(PropertiesPath);
			allProperties.load(ip);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		csvToDatabase = Boolean.parseBoolean(allProperties.getProperty(PropertyConstants.CSV_TO_DATABASE));
		boolean valido = true;
		String[] ficheros = {};
		// seleccionamos que ficheros properties y que datos deseamos leer
		// false - csv de cliente y servidor y resultado
		// true -  csv de cliente, y acceso a BBDD para servidor y resultado
		if (csvToDatabase) {
			ficheros = ficherosCsvToBD;
		} else {
			ficheros = ficherosCsvToCsv;
		}
		//recorremos y seleccionamos que metodo deseamos para comprobar los properties
		//si contienen la palabra csv en la "variable" del properties iran a comprobarse con
		//	el metodo de los csv, si no iran a BBDD
		for (int i = 0; i < ficheros.length; i++) {
			if (ficheros[i].contains("CSV")) {
				valido = checkCsvProperties(allProperties.getProperty(ficheros[i]));
			} else {
				valido = checkBDProperties(allProperties.getProperty(ficheros[i]));
			}
		}

		return valido;
	}

	/**
	 * devuelve el valos booleano del properties para conocer el metodo de trabajo del programa
	 * 
	 * @return Boolean
	 */
	public static boolean getCsvToDatabase() {
		return csvToDatabase;
	}

	/**
	 * devuelve el fichero de properties general
	 * 
	 * @return Properties
	 */
	public static Properties getAllProperties() {
		return allProperties;
	}

	/**
	 * Metodo que busca en el fichero .properties si todos los datos tienen valor y
	 * devuelve true de ser afirmativo y false de ser negativo
	 * 
	 * @return boolean true si se encuentran todos los datos en el properties, false
	 *         si no
	 * @throws SiaException
	 */
	private static boolean checkCsvProperties(String src) throws SiaException {
		//iniciamos el booleano de respuesta en false dando por hecho que fracasara y solo pasandolo a true
		//si se dan todas las condiciones
		boolean readed = false;
		try {
			//leemos la ruta del archivo
			Properties file = new Properties();
			FileInputStream ip = new FileInputStream(src);
			file.load(ip);
			//leemos los datos comprobando que no falta ninguno
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
			//solo se pasa a true en caso de que pase por todos los datos del fichero properties
			readed = true;
			log.trace("Fichero config leido exitosamente");
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
	 * @return boolean true si se encuentran todos los datos en el properties, false
	 *         si no
	 * @throws SiaException
	 */
	private static boolean checkBDProperties(String src) throws SiaException {
		//iniciamos el booleano de respuesta en false dando por hecho que fracasara y solo pasandolo a true
		//si se dan todas las condiciones
		boolean readed = false;
		try {
			//leemos la ruta del archivo
			Properties file = new Properties();
			System.out.println(src);
			FileInputStream ip = new FileInputStream(src);
			file.load(ip);
			//leemos los datos comprobando que no falta ninguno
			file.getProperty(PropertyConstants.DB_DRIVER);
			file.getProperty(PropertyConstants.DB_USERNAME);
			file.getProperty(PropertyConstants.DB_PASSWORD);
			//solo se pasa a true en caso de que pase por todos los datos del fichero properties
			readed = true;
			log.trace("Fichero config leido exitosamente");
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
