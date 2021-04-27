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

	private static boolean csvToDatabase = true;
	private static String PropertiesPath = "C:\\Users\\mparrap\\git\\repsolSyncro\\src\\propertiesRoutes.properties";
	//private static String PropertiesPath = "C:\\Users\\pdiazs\\eclipse-workspace\\repsolSyncro\\src\\propertiesRoutes.properties";

	public static void main(String[] args) {
		EmpCsv empCsv = new EmpCsv();
		try {
			FileInputStream ip = new FileInputStream(PropertiesPath);
			Properties allProperties = new Properties();
			allProperties.load(ip);
			boolean seguir = PropertiesChecker.checker(allProperties, csvToDatabase);
			if(csvToDatabase) {
				seguir = EmpDb.tryConnection(allProperties);
			}
			if (seguir) {
				
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
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (SiaException e) {
			// TODO Auto-generated catch block
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			log.trace("sacabao");
		}

	}

}
