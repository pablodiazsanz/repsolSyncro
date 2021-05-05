package repsolSyncro.businessLogic;

import repsolSyncro.businessLogic.employees.EmpCompare;
import repsolSyncro.businessLogic.employees.EmpCsv;
import repsolSyncro.businessLogic.employees.EmpDb;
import repsolSyncro.exceptions.SiaException;

/**
 * Factoria de objetos ObjectTool que decide si trabajan con Base de Datos o ficheros CSV
 *
 */
public class Factory {
	/**
	 * Devuelve el objeto ObjectTool que apunta al tipo de origen que corresponde de
	 * los properties
	 * 
	 * @param election Si queremos tener el cliente, servidor o resultado
	 * @return Un nuevo objeto ObjectTool
	 * @throws SiaException
	 */
	public static ObjectTool getObject(String election) throws SiaException {

		// Utilizamos un switch para la eleccion de los ObjectTool. Si es por ejemplo,
		// el de
		// CLIENT, habra que elegir si actuamos contra bbdd o ontra csv, que eso ya se
		// obtiene cuando se chequean las propiedades.
		switch (election) {
		case "CLIENT":
			if (PropertiesChecker.getClientElection()) {
				return new EmpDb("CLIENT");
			} else {
				return new EmpCsv("CLIENT");
			}

		case "SERVER":
			if (PropertiesChecker.getServerElection()) {
				return new EmpDb("SERVER");
			} else {
				return new EmpCsv("SERVER");
			}

		case "RESULT":
			if (PropertiesChecker.getResultElection()) {
				return new EmpDb("RESULT");
			} else {
				return new EmpCsv("RESULT");
			}
		}

		return null;
	}

	/**
	 * En este método obtenemos el comparador en el que vamos a comparar los mapas
	 * de los objetos obtenidos
	 * 
	 * @return Comparador
	 */
	public static Compare getTransactioner() {

		return new EmpCompare();
	}
}
