package repsolSyncro;

import java.util.HashMap;
import java.util.List;

public class EmpCsv {

	// En esta clase lo primero que tenemos que hacer es leer el csv que le pasemos
	// a la clase Csv y luego transformar esos datos en empleados y devolverlos en
	// forma de HashMap
	
	public static HashMap<String, Employee> getMap(String path){
		
		List<String> csvData = Csv.getData(path);
		
		HashMap<String, Employee> hm = transformCsvToEmployees(csvData);
		
		return hm;
	}

	private static HashMap<String, Employee> transformCsvToEmployees(List<String> csvData) {
		// TODO Auto-generated method stub
		return null;
	}
}
