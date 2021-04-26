package repsolSyncro.dataAccess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
/**
 * Esta clase se usa para sacar la logica de la clase de CsvAccess
 *
 */
public class CsvLogic {
	
	private Logger log = Logger.getLogger(CsvLogic.class);
	
	/**
	 * Devuelve un HashMap ordenando el nombre de cada columna con su posicion
	 * la key es la posicion que tiene en la linea y el valor es el nombre
	 * 
	 * @param line string con la linea del csv que organiza las columnas
	 * @return HashMap<Integuer, String>
	 */
	public HashMap<Integer, String> getOrderColums(String line) {
		String[] columnsTitle = line.split(";");
		HashMap<Integer, String> columnsOrder = new HashMap<Integer, String>();
		for (int i = 0; i < columnsTitle.length; i++) {
			columnsOrder.put(i, columnsTitle[i]);
		}
		return columnsOrder;
	}

	/**
	 * 
	 * Método que utilizamos para obtener toda la información de una linea.
	 * 
	 * @param line La linea del fichero que queremos recorrer
	 * @return La lista de datos con la que vamos a crear un empleado para meterlo
	 *         en el HashMap
	 */
	public List<String> getDataFromLine(String line) {

		// Creamos un ArrayList para obtener los datos de la linea
		List<String> employeeData = new ArrayList<String>();

		// Añadimos el primer dato
		employeeData.add("");

		// Utilizamos este booleano para saber si abrimos o cerramos las comillas
		boolean openQuotes = false;

		// Utilizamos un valor auxiliar para saber cuando cambiamos de dato
		int employeeValue = 0;

		// Con este bucle for recorremos caracter por caracter para sacar los datos uno
		// a uno
		for (int i = 0; i < line.length(); i++) {

			/*
			 * Aqui observo si el caracter es una comilla. Si lo es, hago una comprobación
			 * para saber si inicio el dato o lo finalizo
			 */
			if (line.charAt(i) == '"') {
				if (openQuotes) {
					openQuotes = false;
				} else {
					openQuotes = true;
				}
			}

			/*
			 * Aqui decido si hay un cambio de valor o si no lo hay. Si lo hay, añado un
			 * nuevo valor vacio al ArrayList, y si no lo hay, sumo lo que contiene el valor
			 * del ArrayList actual a lo existente
			 */
			if (line.charAt(i) == ';' && openQuotes == false) {
				employeeValue++;
				log.trace("[" + employeeData.get(0).trim().toUpperCase() + "] - " + employeeData.toString());
				employeeData.add("");

				// Aquí compruebo que si no hay nada en ese dato, me ponga en valor del
				// ArrayList que es un valor nulo
				if (employeeData.get(employeeValue - 1).length() == 0) {
					employeeData.set(employeeValue - 1, "NULL");
				}

			} else {
				employeeData.set(employeeValue, employeeData.get(employeeValue) + line.charAt(i));
			}

			if (i == line.length() - 1) {
				log.trace("[" + employeeData.get(0).trim().toUpperCase() + "] - " + employeeData.toString());
			}

		}

		return employeeData;
	}

	/**
	 * 
	 * Método que utilizamos para obtener el ID del empleado que vamos a crear.
	 * 
	 * @param employeeData La lista de datos que tenemos del empleado
	 * @param columnsOrder El HashMap en el que tenemos cada columna y su cabecera
	 * @return El ID del empleado
	 */
	public String getEmployeeID(List<String> employeeData, HashMap<Integer, String> columnsOrder, String id) {
		String empID = null;

		for (int i = 0; i < employeeData.size(); i++) {
			if (columnsOrder.get(i).equals(id)) {
				empID = employeeData.get(i).trim().toUpperCase();

			}
		}

		return empID;
	}
}
