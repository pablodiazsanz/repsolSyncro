package csvRepsol;

import java.util.HashMap;

import org.apache.log4j.Logger;

import csvRepsol.businessLogic.Manager;
import csvRepsol.constants.PropertyConstants;
import csvRepsol.dataAccess.CsvAccess;
import csvRepsol.dataAccess.DBAccess;
import csvRepsol.dataAccess.CsvProperty;
import csvRepsol.entities.Employee;
import csvRepsol.exceptions.SiaException;

public class MainClass {

	private static Logger log = Logger.getLogger(MainClass.class);

	/*
	 * ----------------------- IMPORTANTE ------------------------ Para cambiar si
	 * queremos comparar dos CSV y sacar un tercero o si queremos sincronizar el CSV
	 * del cliente con una base de datos tenemos que hacer lo siguiente.
	 * 
	 * Cambiar a true si: Vamos a sincronizar con la bbdd. Cambiar a false si: Vamos
	 * a comparar dos CSV y sacar un tercero con operaciones
	 */
	private static boolean csvToDatabase = false;

	public static void main(String[] args) {
		
		
		// Ller los empleados de un origen
		//   - 	- leo de un CSV
		
		
		// Leer mis empleado
		//   	 - ller de un CSV
		//		- Leer de base datos
		
		// Sincronizar ambos listados de empleados
		//	- COmparo las listas y genero un objeto de operaciones a ejecutar
		
		// Ejecutar las operaciones de sincronizacion
		//	- Ejecuto las operaciones
		//		- Genero un CSV con las operaciones
		//		- Ejecuto las operaciones contra la BBDD
		
		
		

		// Vemos si tenemos que comparar csv o sincronizar con la base de datos
		// e iniciamos la aplicacion
		if (csvToDatabase) {
			startDatabaseSynchronization();

		} else {
			startCsvComparation();

		}
	}

	private static void startCsvComparation() {

		// Inicializamos las propiedades del cliente, del servidor y del fichero de
		// operaciones
		CsvProperty clientConfig = new CsvProperty(PropertyConstants.PATH_CLIENT_PROPERTY_FILE);
		CsvProperty serverConfig = new CsvProperty(PropertyConstants.PATH_SERVER_PROPERTY_FILE);
		CsvProperty resultConfig = new CsvProperty(PropertyConstants.PATH_RESULT_PROPERTY_FILE);

		// Comprobamos que los datos que obtenemos de los ficheros estén rellenos
		try {
			if (clientConfig.checkConfig() && serverConfig.checkConfig() && resultConfig.checkConfig()) {
				log.trace("Arranca la aplicación");

					// Creamos o sobreescribimos el fichero CSV que vamos a sacar con las
					// operaciones
					CsvAccess csvAccess = new CsvAccess(resultConfig);

					csvAccess.createCSV();

					log.trace("Se ha creado o sobreescrito el tercer CSV correctamente");

					// Establecemos en el objeto CsvAccess el fichero de propiedades del cliente
					csvAccess.setConfig(clientConfig);

					// Obtenemos los datos del fichero del cliente
					HashMap<String, Employee> clientData = csvAccess
							.readCSV(clientConfig.getProperty(PropertyConstants.CSV_PATH));
					log.trace("Se han obtenido los datos del fichero del cliente correctamente");

					// Establecemos en el objeto CsvAccess el fichero de propiedades del servidor
					csvAccess.setConfig(serverConfig);

					// Obtenemos los datos del fichero del servidor
					HashMap<String, Employee> serverData = csvAccess
							.readCSV(serverConfig.getProperty(PropertyConstants.CSV_PATH));

					log.trace("Se han obtenido los datos del fichero del servidor correctamente");

					// Comparamos los datos que obtenemos de ambos servidores y sacamos el tercer
					// fichero CSV con las operaciones realizadas por el cliente.
					csvAccess.setConfig(resultConfig);

					Manager manager = new Manager(csvAccess);
					manager.compare(clientData, serverData, csvToDatabase);
					log.trace("Se han realizado las comparaciones correctamente y en el tercer CSV se "
							+ "han metido las creaciones, modificaciones y eliminaciones de empleados");

				log.trace("Finaliza la aplicación");
			}
		} catch (SiaException e) {
			log.error("ha ocorrido un error", e);
		}

	}

	private static void startDatabaseSynchronization() {
		// Inicializamos las propiedades del cliente
		CsvProperty clientConfig = new CsvProperty(PropertyConstants.PATH_CLIENT_PROPERTY_FILE);

		CsvAccess csvAccess;
		Manager manager;

		try {
			// Comprobamos que el fichero de cliente este relleno y que se acceda a la base
			// de datos correctamente, con los datos del fichero de servidor
			if (clientConfig.checkConfig() && DBAccess.tryConnection()) {
				log.trace("Arranca la aplicación");

				// Establecemos en el objeto CsvAccess el fichero de propiedades de operaciones
				csvAccess = new CsvAccess(clientConfig);

				// Obtenemos los datos del fichero del cliente en forma de HashMap
				HashMap<String, Employee> clientData = csvAccess
						.readCSV(clientConfig.getProperty(PropertyConstants.CSV_PATH));
				log.trace("Se han obtenido los datos del fichero del cliente correctamente");

				// Obtenemos los datos de la base de datos en forma de HashMap
				HashMap<String, Employee> serverDataDB = DBAccess.getEmployeesFromServer();

				// Comparamos el fichero con la base de datos y se sincroniza la base de datos
				// para obtener los cambios del cliente
				manager = new Manager();

				manager.compare(clientData, serverDataDB, csvToDatabase);
				log.trace("Se ha sincronizado correctamente la base de datos");
			}

		} catch (SiaException e) {
			log.error("Ha ocurrido un error", e);

		}

		log.trace("Finaliza la aplicación");

	}
}
