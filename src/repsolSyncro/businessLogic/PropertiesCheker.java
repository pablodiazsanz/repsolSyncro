package repsolSyncro.businessLogic;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;

import repsolSyncro.constants.PropertyConstants;
import repsolSyncro.exceptions.SiaException;
import repsolSyncro.exceptions.SiaExceptionCodes;

public class PropertiesCheker {
	
	private static Logger log = Logger.getLogger(PropertiesCheker.class);
	private static String[] ficheros = { PropertyConstants.PATH_SERVER_DB_PROPERTY_FILE,
			PropertyConstants.PATH_CLIENT_PROPERTY_FILE, PropertyConstants.PATH_SERVER_CSV_PROPERTY_FILE,
			PropertyConstants.PATH_RESULT_PROPERTY_FILE };

	public static boolean checker(Properties allProperties) throws SiaException {
		boolean valido = true;
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
	 * Metodo que busca en el fichero .properties si todos los datos tienen valor y
	 * devuelve true de ser afirmativo y false de ser negativo
	 * 
	 * @return boolean true si se encuentran todos los datos en el properties, false
	 *         si no
	 * @throws SiaException
	 */
	
	private static boolean checkCsvProperties(String src) throws SiaException {
		boolean readed = false;
		try {
			Properties file = new Properties();
			FileInputStream ip = new FileInputStream(src);
			file.load(ip);
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
	
	private static boolean checkBDProperties(String src) throws SiaException {
		boolean readed = false;
		try {
			Properties file = new Properties();
			System.out.println(src);
			FileInputStream ip = new FileInputStream(src);
			file.load(ip);
			file.getProperty(PropertyConstants.DB_DRIVER);
			file.getProperty(PropertyConstants.DB_USERNAME);
			file.getProperty(PropertyConstants.DB_PASSWORD);
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
