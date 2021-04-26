package repsolSyncro;

import java.util.HashMap;

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
		if (csvToDatabase) {
			HashMap<String, Employee> serverData = EmpDb.getMap();
		} else {
			HashMap<String, Employee> serverData = EmpCsv.getMap(null);
		}
		
		// Sincronizar ambos listados de empleados
		// - Comparo las listas y genero un objeto de operaciones a ejecutar
		
		
		// Ejecutar las operaciones de sincronizacion
		// - Ejecuto las operaciones
		// - Genero un CSV con las operaciones
		// - Ejecuto las operaciones contra la BBDD
		
		
		// Vemos si tenemos que comparar csv o sincronizar con la base de datos
		// e iniciamos la aplicacion
		
		
	}
	
}
