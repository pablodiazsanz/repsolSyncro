package repsolSyncro.businessLogic;

import repsolSyncro.exceptions.SiaException;
/**
 * Factoria de objetos Emp que decide si trabajn conm Base de Datos
 * o csv 
 *
 */
public class EmpFactory {
	/**
	 * devuelve el objeto emp que apunta al tipo de origen que corresponde de los properties
	 * 
	 * @param election
	 * @return
	 * @throws SiaException
	 */
	public static Emp getEmp(String election) throws SiaException {
		
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
