package repsolSyncro.businessLogic;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;

import org.apache.log4j.Logger;

import repsolSyncro.constants.PropertyConstants;
import repsolSyncro.dataAccess.CsvAccess;
import repsolSyncro.entities.EmpTransaction;
import repsolSyncro.entities.Employee;
import repsolSyncro.exceptions.SiaException;
import repsolSyncro.exceptions.SiaExceptionCodes;

/**
 * En esta clase lo primero que tenemos que hacer es leer el csv que le pasemos
 * a la clase Csv y luego transformar esos datos en empleados y devolverlos en
 * forma de HashMap
 * 
 */
public class EmpCsv {

	private Logger log = Logger.getLogger(EmpCsv.class);
	private Properties file;
	// El path que obtenemos de las propiedades
	private String path = "";
	// El nombre del campo identificador de las cabeceras del csv
	private String id = "";
	// El nombre del campo nombre de las cabeceras del csv
	private String name = "";
	// El nombre del campo primer apellido de las cabeceras del csv
	private String surname1 = "";
	// El nombre del campo segundo apellido de las cabeceras del csv
	private String surname2 = "";
	// El nombre del campo telefono de las cabeceras del csv
	private String phone = "";
	// El nombre del campo email de las cabeceras del csv
	private String email = "";
	// El nombre del campo puesto de trabajo de las cabeceras del csv
	private String job = "";
	// El nombre del campo fecha de contratacion de las cabeceras del csv
	private String hiringDate = "";
	// El nombre del campo salario anual de las cabeceras del csv
	private String yearSalary = "";
	// El nombre del campo baja medica de las cabeceras del csv
	private String sickLeave = "";

	public void setFile(String src) throws SiaException {

		// Cargamos todos los datos del fichero de propiedades en las variables
		try {
			file = new Properties();
			FileInputStream ip = new FileInputStream(src);
			file.load(ip);
			path = file.getProperty(PropertyConstants.CSV_PATH);
			id = file.getProperty(PropertyConstants.CSV_HEAD_ID);
			name = file.getProperty(PropertyConstants.CSV_HEAD_NAME);
			surname1 = file.getProperty(PropertyConstants.CSV_HEAD_SURNAME1);
			surname2 = file.getProperty(PropertyConstants.CSV_HEAD_SURNAME2);
			phone = file.getProperty(PropertyConstants.CSV_HEAD_PHONE);
			email = file.getProperty(PropertyConstants.CSV_HEAD_EMAIL);
			job = file.getProperty(PropertyConstants.CSV_HEAD_JOB);
			hiringDate = file.getProperty(PropertyConstants.CSV_HEAD_HIRING_DATE);
			yearSalary = file.getProperty(PropertyConstants.CSV_HEAD_YEAR_SALARY);
			sickLeave = file.getProperty(PropertyConstants.CSV_HEAD_SICK_LEAVE);

		} catch (FileNotFoundException e) {
			String message = "";
			throw new SiaException(SiaExceptionCodes.MISSING_FILE, message, e);

		} catch (IOException e) {
			String message = "";
			throw new SiaException(SiaExceptionCodes.IN_OUT, message, e);
		}
	}

	/**
	 * Metodo que devuelve la HashMap<String, Employee> con los empleados con su ID
	 * como Key
	 * 
	 * @return HashMap<String, Employee> con los empleados con su ID como Key
	 * @throws SiaException
	 */
	public HashMap<String, Employee> getMap() throws SiaException {

		HashMap<String, Employee> hm = new HashMap<String, Employee>();

		// Obtenemos la lista de los datos del csv de empleados
		List<String> csvData = CsvAccess.getData(path);

		// Obtenemos de la primera linea las columnas y su orden.
		// Damos por hecho que la primera son las columnas por que
		// asi lo hemos pactado y normalizado

		HashMap<Integer, String> columnsOrder = getOrderColums(csvData.get(0));

		// Borramos la primera linea para que no nos moleste
		csvData.remove(0);

		String employeeID = "NO ID";

		// Recorremos linea a linea de la lista de datos del csv para obtener los
		// empleados con el id y meterlos en un HashMap. Si hay algun error con alguno
		// de ellos, lo que haremos será meter en el HashMap el id de clave con el valor
		// nulo y asi sabremos que no hay que operar con ellos.
		for (int i = 0; i < csvData.size(); i++) {
			try {
				// Obtenemos los datos de la linea
				List<String> employeeData = getDataFromLine(csvData.get(i));

				// Obtenemos el id del empleado
				employeeID = getEmployeeID(employeeData, columnsOrder);

				// Añadimos al HashMap el objeto Employee que utiliza de clave el ID de ese
				// empleado
				Employee emp = createEmployee(employeeData, columnsOrder);
				hm.put(employeeID, emp);

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
	 * Este metodo obtiene la ID de un empleado unicamente a traves de la linea de
	 * datos sin necesidad de tener uin objeto empleado creado
	 * 
	 * @param employeeData datos del empleado
	 * @param columnsOrder orden de las columnas
	 * @return String empID con el ID del empleado en cuestion
	 */
	private String getEmployeeID(List<String> employeeData, HashMap<Integer, String> columnsOrder) {

		String empID = null;

		for (int i = 0; i < employeeData.size(); i++) {
			if (columnsOrder.get(i).equals(id)) {
				empID = employeeData.get(i).trim().toUpperCase();

			}
		}

		return empID;
	}

	/**
	 * Crea un objeto empleado a traves de su List<String> de datos y el orden de
	 * las columnas
	 * 
	 * @param employeeData datos del empleado
	 * @param columnsOrder orden de las columnas
	 * @return Employee Con los datos del empleado
	 * @throws ParseException        Se da en caso de que se parsee la fecha
	 * @throws NumberFormatException Se da en caso de que se parsee el salario
	 */
	private Employee createEmployee(List<String> employeeData, HashMap<Integer, String> columnsOrder)
			throws ParseException, NumberFormatException {
		// Declaramos un empleado

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
		return new Employee(empID, empName, empSurname1, empSurname2, empPhone, empEmail, empJob, empHiringDate,
				empYearSalary, empSickLeave);
	}

	/**
	 * Separa el string de una linea en una List<String> de datos en el orden en el
	 * que se encuentre en la linea
	 * 
	 * @param line a trocear
	 * @return List<String> con los datos en el orden en que estan escritos
	 */
	private List<String> getDataFromLine(String line) {
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
	 * Obtiene un HashMap<Integer, String> con el nombre de las columnas en el valor
	 * y la posicion en la que se encuentran en el csv en la key
	 * 
	 * @param lineColums linea con en nombre de las columnas del csv
	 * @return HashMap<Integer, String> con las columnas ordenadas
	 */
	private HashMap<Integer, String> getOrderColums(String lineColums) {
		String[] columnsTitle = lineColums.split(";");
		HashMap<Integer, String> columnsOrder = new HashMap<Integer, String>();
		for (int i = 0; i < columnsTitle.length; i++) {
			columnsOrder.put(i, columnsTitle[i]);
		}
		return columnsOrder;
	}

	/**
	 * Manda crear el csv de resultado de empleados escribiendo la primera linea de
	 * las columnas
	 * 
	 * @throws SiaException
	 */
	private void createEmpCsv() throws SiaException {
		String columsLine = "id;name;first surname;second surname;phone;email;job;hiring_date;year_salary;sick_leave;status";
		CsvAccess.createCSV(columsLine, file.getProperty(PropertyConstants.CSV_PATH));
	}

	/**
	 * Manda escribir la lista de List<EmpTransaction> con los empleados a modificar
	 * y su status(accion que se va a realizar), y en el caso de las modificaciones,
	 * tambien la lista de los campos modificados.
	 * 
	 * @param transactionsList
	 * @throws SiaException
	 */
	public void generateTransactionsCsv(List<EmpTransaction> transactionsList) throws SiaException {

		createEmpCsv();

		for (EmpTransaction empTransaction : transactionsList) {

			if (empTransaction.getStatus().equals("CREATE") || empTransaction.getStatus().equals("DELETE")) {
				System.out.println(empTransaction.toString());
				String line = empTransaction.getEmployee().toCSV() + ";" + empTransaction.getStatus();
				CsvAccess.writeCSV(line, file.getProperty(PropertyConstants.CSV_PATH));

			} else {
				String line = updatedEmployeeToCsv(empTransaction);
				CsvAccess.writeCSV(line, file.getProperty(PropertyConstants.CSV_PATH));

			}
		}
	}

	/**
	 * En este método lo que hacemos es crear una linea de csv con los datos a
	 * modificar del empleado y el estado.
	 * 
	 * @param empTransaction
	 * @return
	 */
	private String updatedEmployeeToCsv(EmpTransaction empTransaction) {
		/*
		 * Estas variables son las que vamos a darle a la cadena updatedData para pasar
		 * los datos. Estas cadenas se van a modificar si la lista modifiedFields
		 * contiene el dato
		 */

		String line = "";

		// Estas variables son las vamos a utilizar para comprobar si el dato ha
		// cambiado o no.
		boolean name = false;
		boolean surname1 = false;
		boolean surname2 = false;
		boolean phone = false;
		boolean email = false;
		boolean job = false;
		boolean hiringDate = false;
		boolean yearSalary = false;
		boolean sickLeave = false;

		// Recorremos la lista para saber que datos no se han cambiado.
		for (int i = 0; i < empTransaction.getModifiedFields().size(); i++) {

			if (empTransaction.getModifiedFields().get(i).equals("name")) {
				name = true;

			} else if (empTransaction.getModifiedFields().get(i).equals("surname1")) {
				surname1 = true;

			} else if (empTransaction.getModifiedFields().get(i).equals("surname2")) {
				surname2 = true;

			} else if (empTransaction.getModifiedFields().get(i).equals("phone")) {
				phone = true;

			} else if (empTransaction.getModifiedFields().get(i).equals("email")) {
				email = true;

			} else if (empTransaction.getModifiedFields().get(i).equals("job")) {
				job = true;

			} else if (empTransaction.getModifiedFields().get(i).equals("hiringDate")) {
				hiringDate = true;

			} else if (empTransaction.getModifiedFields().get(i).equals("yearSalary")) {
				yearSalary = true;

			} else if (empTransaction.getModifiedFields().get(i).equals("sickLeave")) {
				sickLeave = true;
			}

		}

		// Si los datos han sido cambiados, añadimos el dato a la linea que vamos a
		// devolver que es la que se va a insertar en el csv.
		line += empTransaction.getEmployee().getId() + ";";
		if (name) {
			line += empTransaction.getEmployee().getName();
		}
		line += ";";
		if (surname1) {
			line += empTransaction.getEmployee().getSurname1();
		}
		line += ";";
		if (surname2) {
			line += empTransaction.getEmployee().getSurname2();
		}
		line += ";";
		if (phone) {
			line += empTransaction.getEmployee().getTlf();
		}
		line += ";";
		if (email) {
			line += empTransaction.getEmployee().getMail();
		}
		line += ";";
		if (job) {
			line += empTransaction.getEmployee().getJob();
		}
		line += ";";
		if (hiringDate) {
			line += new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(empTransaction.getEmployee().getHiringDate());
		}
		line += ";";
		if (yearSalary) {
			line += empTransaction.getEmployee().getYearSalary();
		}
		line += ";";
		if (sickLeave) {
			line += empTransaction.getEmployee().isSickLeave();
		}
		line += ";";
		line += empTransaction.getStatus();

		return line;
	}
}
