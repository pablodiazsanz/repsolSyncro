package repsolSyncro;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import org.apache.log4j.Logger;

import repsolSyncro.exceptions.SiaException;

public class EmpCsv {

	// En esta clase lo primero que tenemos que hacer es leer el csv que le pasemos
	// a la clase Csv y luego transformar esos datos en empleados y devolverlos en
	// forma de HashMap
	private static Logger log = Logger.getLogger(EmpCsv.class);
	private static String id = "";
	private static String name = "";
	private static String surname1 = "";
	private static String surname2 = "";
	private static String phone = "";
	private static String email = "";
	private static String job = "";
	private static String hiringDate = "";
	private static String yearSalary = "";
	private static String sickLeave = "";

	/**
	 * 
	 * @param path
	 * @return
	 * @throws SiaException
	 */
	public static HashMap<String, Employee> getMap(String path) throws SiaException {
		HashMap<String, Employee> hm = new HashMap<String, Employee>();
		// obtenemos la lista de todas las lienas del csv de empleados
		List<String> csvData = Csv.getData(path);
		// obtenemos de la primera linea las columnas y su orden
		// damos por hecho que la primera son las columnas por que asi lo hemos pactado
		// y normalizado

		HashMap<Integer, String> columnsOrder = getOrderColums(csvData.get(0));
		// borramos la primera linea para que no nos moleste
		csvData.remove(0);
		String employeeID = "NO ID";
		for (int i = 0; i < csvData.size(); i++) {
			try {
				List<String> employeeData = getDataFromLine(csvData.get(i));

				employeeID = getEmployeeID(employeeData, columnsOrder, id);

				// Añadimos al HashMap el objeto Employee que utiliza de clave el ID de ese
				// empleado
				Employee emp = createEmployee(employeeData, columnsOrder);
				hm.put(emp.getId(), emp);
			} catch (NullPointerException e) {
				log.warn("Linea (" + i + ") del Fichero \"" + path + "\" esta vacia", e);

			} catch (IndexOutOfBoundsException e) {
				log.error(
						"ID: [" + employeeID + "] - NºLinea: (" + i + ") - Fichero: \"" + path + "\" - Linea: {"
								+ csvData.get(i) + "}\n No se ha podido crear el objeto empleado. Fallo al leer linea",
						e);
				hm.put(employeeID, null);

			} catch (ParseException e) {
				log.error("ID: [" + employeeID + "] - NºLinea: (" + i + ") - Fichero: \"" + path + "\" - Linea: {"
						+ csvData.get(i) + "}\n No se ha podido crear el objeto empleado.", e);
				hm.put(employeeID, null);

			} catch (NumberFormatException e) {
				log.error("ID: [" + employeeID + "] - NºLinea: (" + i + ") - Fichero: \"" + path + "\" - Linea: {"
						+ csvData.get(i)
						+ "}\n No se ha podido crear el objeto empleado. Numero introducido incorrecto", e);
				hm.put(employeeID, null);

			} catch (Exception e) {
				log.error("Fallo generico en la linea (" + i + ") del Fichero \"" + path + "\"", e);

			}
		}

		return hm;
	}

	/**
	 * 
	 * @param employeeData
	 * @param columnsOrder
	 * @param id2
	 * @return
	 */
	private static String getEmployeeID(List<String> employeeData, HashMap<Integer, String> columnsOrder, String id2) {
		String empID = null;

		for (int i = 0; i < employeeData.size(); i++) {
			if (columnsOrder.get(i).equals(id)) {
				empID = employeeData.get(i).trim().toUpperCase();

			}
		}

		return empID;
	}

	/**
	 * 
	 * @param employeeData
	 * @param columnsOrder
	 * @return
	 * @throws ParseException
	 */
	private static Employee createEmployee(List<String> employeeData, HashMap<Integer, String> columnsOrder)
			throws ParseException {
		// Declaramos un empleado
		Employee createdEmployee = null;

		// Creamos todas las variables vacías que posteriormente añadiremos al empleado
		// creado
		String empID = "", empName = "", empSurname1 = "", empSurname2 = "", empPhone = "", empEmail = "", empJob = "";
		Date empHiringDate = null;
		int empYearSalary = -1;
		boolean empSickLeave = false;

		/*
		 * En este bucle for, vamos a ir recorriendo la lista de los datos que hemos
		 * sacado de la linea. Al recorrer el dato, se le irá preguntando que nombre de
		 * columna posee y comparandola con el nombre de columna que tenemos nosotros en
		 * el servidor para asi obtener los datos y meterlos correctamente en el
		 * empleado que creamos.
		 */
		for (int i = 0; i < employeeData.size(); i++) {
			if (columnsOrder.get(i).equals(id)) {
				empID = employeeData.get(i).trim().toUpperCase();

			} else if (columnsOrder.get(i).equals(name)) {
				empName = employeeData.get(i).trim();

			} else if (columnsOrder.get(i).equals(surname1)) {
				empSurname1 = employeeData.get(i).trim();

			} else if (columnsOrder.get(i).equals(surname2)) {
				empSurname2 = employeeData.get(i).trim();

			} else if (columnsOrder.get(i).equals(phone)) {
				empPhone = employeeData.get(i).trim();

			} else if (columnsOrder.get(i).equals(email)) {
				empEmail = employeeData.get(i).trim();

			} else if (columnsOrder.get(i).equals(job)) {
				empJob = employeeData.get(i).trim();

			} else if (columnsOrder.get(i).equals(hiringDate)) {
				/*
				 * Aquí formateamos la cadena obtenida, que en el caso ideal es una fecha, a un
				 * tipo Date
				 */
				SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
				formatter.setTimeZone(TimeZone.getTimeZone("Europe/Madrid"));

				empHiringDate = formatter.parse(employeeData.get(i));

			} else if (columnsOrder.get(i).equals(yearSalary)) {
				// Aqui formateamos el salario anual a numero entero
				empYearSalary = Integer.parseInt(employeeData.get(i));

			} else if (columnsOrder.get(i).equals(sickLeave)) {
				if (employeeData.get(i).equals("true")) {
					empSickLeave = true;

				}
			}

		}
		return createdEmployee;
	}

	/**
	 * 
	 * @param line
	 * @return
	 */
	private static List<String> getDataFromLine(String line) {
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
	 * @param lineColums
	 * @return
	 */
	private static HashMap<Integer, String> getOrderColums(String lineColums) {
		String[] columnsTitle = lineColums.split(";");
		HashMap<Integer, String> columnsOrder = new HashMap<Integer, String>();
		for (int i = 0; i < columnsTitle.length; i++) {
			columnsOrder.put(i, columnsTitle[i]);
		}
		return columnsOrder;
	}
}
