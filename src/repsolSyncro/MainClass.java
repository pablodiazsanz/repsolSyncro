package repsolSyncro;

import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

public class MainClass {

	private static Logger log = Logger.getLogger(MainClass.class);
	
	private static boolean csvToDatabase = false;

	public static void main(String[] args) {

		// Leer los empleados de un origen
		// - Leo de un CSV
		HashMap<String, Employee> clientData = EmpCsv.getMap(null);
		
		// Leer mis empleado
		// - Leer de un CSV
		// - Leer de base datos
		HashMap<String, Employee> serverData;
		if (csvToDatabase) {
			serverData = EmpDb.getMap();
		} else {
			serverData = EmpCsv.getMap(null);
		}
		
		// Sincronizar ambos listados de empleados
		// - Comparo las listas y genero un objeto de operaciones a ejecutar
		List<EmpTransaction> transactionsList = EmpCompare.getTransactions(clientData, serverData);
		
		// Ejecutar las operaciones de sincronizacion
		// - Ejecuto las operaciones
		// - Genero un CSV con las operaciones
		// - Ejecuto las operaciones contra la BBDD
		if (csvToDatabase) {
			EmpCsv.generateTransactionsCsv(transactionsList);
		} else {
			EmpDb.executeTransactions(transactionsList);
		}
		
	}
	
}
