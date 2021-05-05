package repsolSyncro;

import java.util.HashMap;
import java.util.List;
import org.apache.log4j.Logger;

import repsolSyncro.businessLogic.Compare;
import repsolSyncro.businessLogic.Emp;
import repsolSyncro.businessLogic.EmpCompare;
import repsolSyncro.businessLogic.EmpCsv;
import repsolSyncro.businessLogic.EmpDb;
import repsolSyncro.businessLogic.Factory;
import repsolSyncro.businessLogic.PropertiesChecker;
import repsolSyncro.dataAccess.DbAccess;
import repsolSyncro.entities.EmpTransaction;
import repsolSyncro.entities.Employee;
import repsolSyncro.entities.MyObject;
import repsolSyncro.entities.Transaction;
import repsolSyncro.exceptions.SiaException;

public class MainClass {

	// Logger para poder escribir las trazas del codigo en los logs
	private static Logger log = Logger.getLogger(MainClass.class);

	/**
	 * Esta aplicacion trata de sincronizar los datos de un csv de empleados del
	 * cliente con nuestros datos
	 * 
	 * En el fichero propertiesRoutes.properties decides con una variable booleana
	 * si deseas que la aplicacion sincronice dos csv o si nuestros datos funcionen
	 * directamente con la Base de datos
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			// Comprobamos si las propiedades estan completas
			PropertiesChecker.checker();

			// Leer los empleados de un origen
			Emp empCliente = Factory.getEmp("CLIENT");
			HashMap<String, MyObject> clientData = empCliente.getMap();

			// Leer mis empleados
			Emp empServer = Factory.getEmp("SERVER");
			HashMap<String, MyObject> serverData = empServer.getMap();

			// Sincronizar ambos listados de empleados
			// - Comparo las listas y genero un objeto de operaciones a ejecutar
			Compare transactioner = Factory.getTransactioner();
			List<Transaction> transactionsList = transactioner.getTransactions(clientData, serverData);

			// Ejecutar las operaciones de sincronizacion
			Emp empResult = Factory.getEmp("RESULT");
			empResult.executeTransactions(transactionsList);

			log.info("Sincronización realizada correctamente");

		} catch (SiaException e) {
			log.error("Obtenemos un error -  Código SiaException: [" + e.getErrorCode() + "]", e);
			
		} finally {
			log.trace("Finaliza el programa");
			
		}

	}

}
