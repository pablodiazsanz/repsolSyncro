package repsolSyncro.businessLogic;

import repsolSyncro.exceptions.SiaException;

public class EmpFactory {
	
	public static Emp getEmp(String election) throws SiaException {
		
		switch (election) {
		case "CLIENT":
			if (PropertiesChecker.getClientElection()) {
				return new EmpDb("CLIENT");
			} else {
				return new EmpCsv("CLIENT");
			}
			
		case "SERVER":
			if (PropertiesChecker.getClientElection()) {
				return new EmpDb("SERVER");
			} else {
				return new EmpCsv("SERVER");
			}
			
		case "RESULT":
			if (PropertiesChecker.getClientElection()) {
				return new EmpDb("RESULT");
			} else {
				return new EmpCsv("RESULT");
			}
		}
		
		return null;
	}
}
