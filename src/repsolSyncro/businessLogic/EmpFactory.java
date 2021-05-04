package repsolSyncro.businessLogic;

import repsolSyncro.exceptions.SiaException;

/**
 * Factoria de objetos Emp que decide si trabajan con Base de Datos o csv
 *
 */
public class EmpFactory {
	/**
	 * Devuelve el objeto emp que apunta al tipo de origen que corresponde de los
	 * properties
	 * 
	 * @param election Si queremos tener el cliente, servidor o resultado
	 * @return Un nuevo objeto Emp
	 * @throws SiaException
	 */
	public static Emp getEmp(String election) throws SiaException {

		// Utilizamos un switch para la eleccion de los Emp. Si es por ejemplo, el de
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
}
