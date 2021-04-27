package repsolSyncro;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import repsolSyncro.businessLogic.EmpCompare;
import repsolSyncro.businessLogic.EmpCsv;
import repsolSyncro.businessLogic.EmpDb;
import repsolSyncro.businessLogic.PropertiesChecker;
import repsolSyncro.constants.PropertyConstants;
import repsolSyncro.entities.EmpTransaction;
import repsolSyncro.entities.Employee;
import repsolSyncro.exceptions.SiaException;

public class MainClass {

	private static Logger log = Logger.getLogger(MainClass.class);

	private static boolean csvToDatabase;
	private static String PropertiesPath = "C:\\Users\\mparrap\\git\\repsolSyncro\\src\\propertiesRoutes.properties";
	// private static String PropertiesPath =
	// "C:\\Users\\pdiazs\\eclipse-workspace\\repsolSyncro\\src\\propertiesRoutes.properties";

	public static void main(String[] args) {
		EmpCsv empCsv = new EmpCsv();
		try {
			
			// Cargamos las propiedades
			FileInputStream ip = new FileInputStream(PropertiesPath);
			Properties allProperties = new Properties();
			allProperties.load(ip);
			
			// Comprobamos si las propiedades estan completas
			boolean checkAndGo = PropertiesChecker.checker(allProperties, csvToDatabase);
			csvToDatabase = Boolean.parseBoolean(allProperties.getProperty(PropertyConstants.CSV_TO_DATABASE));

			// Comprobamos si queremos sincronizar con la base de datos o con csv
			if (csvToDatabase) {
				checkAndGo = EmpDb.tryConnection(allProperties);
			}

			// Si todo es correcto, arrancamos el programa
			if (checkAndGo) {

				// Leer los empleados de un origen
				// - Leo de un CSV
				empCsv.setFile(allProperties.getProperty(PropertyConstants.PATH_CLIENT_PROPERTY_FILE));
				HashMap<String, Employee> clientData = empCsv.getMap();

				// Leer mis empleado
				// - Leer de un CSV
				// - Leer de base datos
				HashMap<String, Employee> serverData;
				if (csvToDatabase) {
					serverData = EmpDb.getMap();

				} else {
					empCsv.setFile(allProperties.getProperty(PropertyConstants.PATH_SERVER_CSV_PROPERTY_FILE));
					serverData = empCsv.getMap();
				}

				// Sincronizar ambos listados de empleados
				// - Comparo las listas y genero un objeto de operaciones a ejecutar
				List<EmpTransaction> transactionsList = EmpCompare.getTransactions(clientData, serverData);

				// Ejecutar las operaciones de sincronizacion
				// - Ejecuto las operaciones
				// - Genero un CSV con las operaciones
				// - Ejecuto las operaciones contra la BBDD
				if (!csvToDatabase) {
					empCsv.setFile(allProperties.getProperty(PropertyConstants.PATH_RESULT_PROPERTY_FILE));
					empCsv.generateTransactionsCsv(transactionsList);
				} else {
					EmpDb.executeTransactions(transactionsList);

				}
			}
		} catch (FileNotFoundException e) {
			log.error("Ha ocurrido un error de acceso al fichero", e);
		} catch (SiaException e) {
			log.error("Obtenemos un error", e);
		} catch (IOException e) {
			log.error("Ha ocurrido un error de entrada o salida de datos", e);
		} finally {
			log.trace("Finaliza el programa");
		}

	}

}
