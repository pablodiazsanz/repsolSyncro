package repsolSyncro;

import java.util.HashMap;
import java.util.List;
import org.apache.log4j.Logger;
import repsolSyncro.businessLogic.EmpCompare;
import repsolSyncro.businessLogic.EmpCsv;
import repsolSyncro.businessLogic.EmpDb;
import repsolSyncro.businessLogic.PropertiesChecker;
import repsolSyncro.entities.EmpTransaction;
import repsolSyncro.entities.Employee;
import repsolSyncro.exceptions.SiaException;

public class MainClass {

	private static Logger log = Logger.getLogger(MainClass.class);

	private static boolean csvToDatabase;
		
	/**
	 * Esta aplicacion trata de sincronizar los datos de un csv de empleados del cliente con nuestros datos
	 * 
	 * En el fichero propertiesRoutes.properties decides con una variable booleana si deseas que la aplicacion sincronice
	 * dos csv o si nuestros datos funcionen directamente con la Base de datos
	 * 
	 * @param args
	 */
	public static void main(String[] args) {	
		try {
			// Comprobamos si las propiedades estan completas
			boolean checkAndGo = PropertiesChecker.checker();
			csvToDatabase = PropertiesChecker.getCsvToDatabase();

			// Comprobamos si queremos sincronizar con la base de datos o con csv
			if (csvToDatabase) {
				checkAndGo = EmpDb.tryConnection();
			}

			// Si todo es correcto, arrancamos el programa
			if (checkAndGo) {

				// Leer los empleados de un origen
				// - Leo de un CSV
				EmpCsv empCsvCliente = new EmpCsv("client");
				HashMap<String, Employee> clientData = empCsvCliente.getMap();

				// Leer mis empleado
				// - Leer de un CSV
				// - Leer de base datos
				HashMap<String, Employee> serverData;
				if (csvToDatabase) {
					serverData = EmpDb.getMap();

				} else {
					EmpCsv empCsvServer = new EmpCsv("server");
					serverData = empCsvServer.getMap();
				}

				// Sincronizar ambos listados de empleados
				// - Comparo las listas y genero un objeto de operaciones a ejecutar
				List<EmpTransaction> transactionsList = EmpCompare.getTransactions(clientData, serverData);

				// Ejecutar las operaciones de sincronizacion
				// - Ejecuto las operaciones
				// - Genero un CSV con las operaciones
				// - Ejecuto las operaciones contra la BBDD
				if (!csvToDatabase) {
					EmpCsv empCsvResult = new EmpCsv("result");
					empCsvResult.generateTransactionsCsv(transactionsList);
				} else {
					EmpDb.executeTransactions(transactionsList);

				}
			}
		} catch (SiaException e) {
			log.error("Obtenemos un error", e);
		} finally {
			log.trace("Finaliza el programa");
		}

	}

}
