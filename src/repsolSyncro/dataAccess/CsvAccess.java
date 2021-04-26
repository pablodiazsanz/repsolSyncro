package repsolSyncro.dataAccess;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;
import org.apache.log4j.Logger;

import repsolSyncro.Employee;
import repsolSyncro.constants.PropertyConstants;
import repsolSyncro.exceptions.SiaException;
import repsolSyncro.exceptions.SiaExceptionCodes;
/**
 * Clase que interactua con los csv, recuperando los datos o escribiendo sobre ellos
 *
 */
public class CsvAccess {

	private Logger log = Logger.getLogger(CsvAccess.class);
	private CsvProperty config;
	private CsvLogic csvlogic = new CsvLogic();

	private String id = "";
	private String name = "";
	private String surname1 = "";
	private String surname2 = "";
	private String phone = "";
	private String email = "";
	private String job = "";
	private String hiringDate = "";
	private String yearSalary = "";
	private String sickLeave = "";

	public CsvAccess(CsvProperty config) throws SiaException {
		this.config = config;
		try {
			id = config.getProperty(PropertyConstants.CSV_HEAD_ID);
			name = config.getProperty(PropertyConstants.CSV_HEAD_NAME);
			surname1 = config.getProperty(PropertyConstants.CSV_HEAD_SURNAME1);
			surname2 = config.getProperty(PropertyConstants.CSV_HEAD_SURNAME2);
			phone = config.getProperty(PropertyConstants.CSV_HEAD_PHONE);
			email = config.getProperty(PropertyConstants.CSV_HEAD_EMAIL);
			job = config.getProperty(PropertyConstants.CSV_HEAD_JOB);
			hiringDate = config.getProperty(PropertyConstants.CSV_HEAD_HIRING_DATE);
			yearSalary = config.getProperty(PropertyConstants.CSV_HEAD_YEAR_SALARY);
			sickLeave = config.getProperty(PropertyConstants.CSV_HEAD_SICK_LEAVE);
		} catch (SiaException e) {
			log.error("property perdida", e);
			throw new SiaException(SiaExceptionCodes.MISSING_PROPERTY, e);
		}
	}

	public void setConfig(CsvProperty config) throws SiaException {
		this.config = config;
		try {
			id = config.getProperty(PropertyConstants.CSV_HEAD_ID);
			name = config.getProperty(PropertyConstants.CSV_HEAD_NAME);
			surname1 = config.getProperty(PropertyConstants.CSV_HEAD_SURNAME1);
			surname2 = config.getProperty(PropertyConstants.CSV_HEAD_SURNAME2);
			phone = config.getProperty(PropertyConstants.CSV_HEAD_PHONE);
			email = config.getProperty(PropertyConstants.CSV_HEAD_EMAIL);
			job = config.getProperty(PropertyConstants.CSV_HEAD_JOB);
			hiringDate = config.getProperty(PropertyConstants.CSV_HEAD_HIRING_DATE);
			yearSalary = config.getProperty(PropertyConstants.CSV_HEAD_YEAR_SALARY);
			sickLeave = config.getProperty(PropertyConstants.CSV_HEAD_SICK_LEAVE);
		} catch (SiaException e) {
			log.error("property perdida", e);
			throw new SiaException(SiaExceptionCodes.MISSING_PROPERTY, e);
		}
	}

	/**
	 * Lee los empleados de un csv, y devuelme la lista en un HasMap organizado por
	 * <id del empledado, objeto empleado>
	 *
	 * @param nameCSV Nombre del csv que quieres leer
	 * @return HasMap De los empleados con su id como key
	 */
	public HashMap<String, Employee> readCSV(String nameCSV) throws SiaException {
		// Creamos el HashMap y obtenemos el fichero CSV
		HashMap<String, Employee> map = new HashMap<>();
		File f = new File(nameCSV);

		log.trace("Ruta del fichero: " + f.getPath());

		FileReader reader = null;
		BufferedReader br = null;

		// Utilizamos un contador de lineas del fichero para obtener informacion
		// acerca de la linea que nos da un error o una excepcion
		int contLine = 2;
		Employee emp = null;

		try {
			reader = new FileReader(f);
			br = new BufferedReader(reader);
			log.trace("Accedemos al fichero");
			// Leemos la primera linea, que es la informacion de las columnas
			String line = br.readLine();
			HashMap<Integer, String> columnsOrder = csvlogic.getOrderColums(line);
			// Con el bucle while recorremos linea por linea el fichero
			while (line != null) {
				String employeeID = "";
				try {
					line = br.readLine();

					// Obtenemos los datos de cada linea y los metemos en un ArrayList
					List<String> employeeData =  csvlogic.getDataFromLine(line);

					// Obtenemos el id del empleado que vamos a crear
					employeeID =  csvlogic.getEmployeeID(employeeData, columnsOrder, id);

					// Añadimos al HashMap el objeto Employee que utiliza de clave el ID de ese
					// empleado
					emp = createEmployee(employeeData, columnsOrder);
					map.put(employeeID, emp);

				} catch (NullPointerException e) {
					log.warn("Linea (" + contLine + ") del Fichero \"" + nameCSV + "\" esta vacia", e);

				} catch (IndexOutOfBoundsException e) {
					log.error("ID: [" + employeeID + "] - NºLinea: (" + contLine + ") - Fichero: \"" + nameCSV
							+ "\" - Linea: {" + line
							+ "}\n No se ha podido crear el objeto empleado. Fallo al leer linea", e);
					map.put(employeeID, null);

				} catch (ParseException e) {
					log.error("ID: [" + employeeID + "] - NºLinea: (" + contLine + ") - Fichero: \"" + nameCSV
							+ "\" - Linea: {" + line + "}\n No se ha podido crear el objeto empleado.", e);
					map.put(employeeID, null);

				} catch (NumberFormatException e) {
					log.error("ID: [" + employeeID + "] - NºLinea: (" + contLine + ") - Fichero: \"" + nameCSV
							+ "\" - Linea: {" + line
							+ "}\n No se ha podido crear el objeto empleado. Numero introducido incorrecto", e);
					map.put(employeeID, null);

				} catch (Exception e) {
					log.error("Fallo generico en la linea (" + contLine + ") del Fichero \"" + nameCSV + "\"", e);

				}

				// Cambiamos la linea del contador
				contLine++;
			}

		} catch (FileNotFoundException e) {
			log.error("Fichero no encontrado: \"" + nameCSV + "\"", e);
			throw new SiaException(SiaExceptionCodes.MISSING_FILE, e);
		} catch (IOException e) {
			log.error("Fallo de entrada o salida", e);
			throw new SiaException(SiaExceptionCodes.IN_OUT, e);
		} catch (NullPointerException e) {
			log.error("El fichero al que accedemos está vacio", e);
			throw new SiaException(SiaExceptionCodes.EMPTY_FILE, e);
		} finally {
			try {
				br.close();
				reader.close();
				log.trace("Lectura finalizada con " + (contLine - 2) + " lineas leidas en fichero " + nameCSV);

			} catch (IOException e) {
				log.error("Fallo de entrada o salida", e);

			}
		}
		if (map.keySet().size() <= 0) {
			throw new SiaException("fichero con lineas erroneas");
		}
		return map;
	}
	
	
	

	/**
	 * Devuelve un empleado sacando los datos de la list de dataEmployee y la
	 * organizacion de orderColumns
	 * 
	 * @param dataEmployee datos del empleado no organizados
	 * @param orderColumns nombre de las columnas con la posicion que tienen
	 * @return objeto empleado con los datos correspondientes
	 * @throws ParseException Devuelva un error de fecha y lo lance el método que lo
	 *                        llama
	 */
	private Employee createEmployee(List<String> dataEmployee, HashMap<Integer, String> orderColumns)
			throws ParseException {

		// Declaramos un empleado
		Employee createdEmployee;

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
		for (int i = 0; i < dataEmployee.size(); i++) {
			if (orderColumns.get(i).equals(id)) {
				empID = dataEmployee.get(i).trim().toUpperCase();

			} else if (orderColumns.get(i).equals(name)) {
				empName = dataEmployee.get(i).trim();

			} else if (orderColumns.get(i).equals(surname1)) {
				empSurname1 = dataEmployee.get(i).trim();

			} else if (orderColumns.get(i).equals(surname2)) {
				empSurname2 = dataEmployee.get(i).trim();

			} else if (orderColumns.get(i).equals(phone)) {
				empPhone = dataEmployee.get(i).trim();

			} else if (orderColumns.get(i).equals(email)) {
				empEmail = dataEmployee.get(i).trim();

			} else if (orderColumns.get(i).equals(job)) {
				empJob = dataEmployee.get(i).trim();

			} else if (orderColumns.get(i).equals(hiringDate)) {
				/*
				 * Aquí formateamos la cadena obtenida, que en el caso ideal es una fecha, a un
				 * tipo Date
				 */
				SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
				formatter.setTimeZone(TimeZone.getTimeZone("Europe/Madrid"));
				empHiringDate = formatter.parse(dataEmployee.get(i));

			} else if (orderColumns.get(i).equals(yearSalary)) {
				// Aqui formateamos el salario anual a numero entero
				empYearSalary = Integer.parseInt(dataEmployee.get(i));

			} else if (orderColumns.get(i).equals(sickLeave)) {
				if (dataEmployee.get(i).equals("true")) {
					empSickLeave = true;

				}
			}
		}

		/*
		 * Creamos el objeto empleado normalizando el id en mayusculas, eliminamos los
		 * espacios al principìo y al final
		 */
		createdEmployee = new Employee(empID, empName, empSurname1, empSurname2, empPhone, empEmail, empJob,
				empHiringDate, empYearSalary, empSickLeave);

		log.debug("[" + empID + "] - Empleado creado: " + createdEmployee);
		return createdEmployee;
	}

	/**
	 * Este metodo crea o sobreescribe el fichero result.csv para guardar la
	 * información final.
	 * 
	 * @throws SiaException
	 */
	public void createCSV() throws SiaException {
		try {
			FileWriter fw;
			fw = new FileWriter(config.getProperty(PropertyConstants.CSV_PATH));
			fw.write("id;name;first surname;second surname;phone;email;job;hiring_date;year_salary;sick_leave;status");
			fw.close();
			log.trace(fw);
		} catch (IOException e) {
			log.error("Fallo al escribir la información de la cabecera");
			throw new SiaException(SiaExceptionCodes.IN_OUT, e);
		}
	}

	/**
	 * Metodo usado para añadir una linea de datos al archivo CSV de resultados
	 *
	 * @param employee El empleado que queremos añadir
	 * @param status   La accion que realizamos con el empleado, DELETE o CREATE
	 * @throws SiaException
	 */
	public void writeCSV(Employee employee, String status) throws SiaException {
		try {
			FileWriter fw = new FileWriter(config.getProperty(PropertyConstants.CSV_PATH), true);
			fw.write("\n" + employee.toCSV() + ";" + status);
			fw.close();
			log.info("[" + employee.getId() + "] - \"" + status + "\"");
		} catch (IOException e) {
			log.error("[" + employee.getId() + "] - Fallo al escribir al usuario");
			throw new SiaException(SiaExceptionCodes.IN_OUT, e);
		}
	}

	/**
	 * Método utilizado para para añadir una linea de datos de un empleado
	 * modificado al archivo CSV de resultados. Cómo para actualizar necesitamos que
	 * los datos que no son una cadena no aparezcan como null o, en el caso del
	 * boolean, false, traemos una lista de si estos datos están actualizados o no.
	 * 
	 * @param updatedEmployee El empleado que queremos añadir
	 * @param extraData       Lista con información de los datos que no son una
	 *                        cadena, los que no se van a modificar.
	 * @param status          La accion que realizamos con el empleado, en este caso
	 *                        UPDATE.
	 * @throws SiaException
	 */
	public void writeUpdatedEmployeeCSV(Employee updatedEmployee, List<String> extraData, String status)
			throws SiaException {

		// Creamos esta variable para enviarle al fichero CSV el contenido.
		String updatedData;

		/*
		 * Estas variables son las que vamos a darle a la cadena updatedData para pasar
		 * los datos. Estas cadenas se van a modificar si la lista extraData no contiene
		 * el dato
		 */
		String hiringDate = "", yearSalary = "", sickLeave = "";

		// Estas variables son las vamos a utilizar para comprobar si el dato ha
		// cambiado o no.
		boolean hiringDateChanged = true, yearSalaryChanged = true, sickLeaveChanged = true;

		// Comprobamos que la lista no está vacia.
		if (!extraData.isEmpty()) {

			// Recorremos la lista para saber que datos no se han cambiado.
			for (int i = 0; i < extraData.size(); i++) {

				if (extraData.get(i).equals("hiringDate")) {
					hiringDateChanged = false;
				}

				if (extraData.get(i).equals("yearSalary")) {
					yearSalaryChanged = false;
				}

				if (extraData.get(i).equals("sickLeave")) {
					sickLeaveChanged = false;
				}

			}
		}

		// Si los datos han sido cambiados, establecemos el dato para pasarselo al
		// fichero CSV.
		if (hiringDateChanged) {
			hiringDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(updatedEmployee.getHiringDate());
		}
		if (yearSalaryChanged) {
			yearSalary = String.valueOf(updatedEmployee.getYearSalary());
		}
		if (sickLeaveChanged) {
			sickLeave = String.valueOf(updatedEmployee.isSickLeave());
		}

		// Metemos los datos en la cadena que vamos a darle al fichero CSV con los datos
		// correctos.
		updatedData = updatedEmployee.getId() + ";" + updatedEmployee.getName() + ";" + updatedEmployee.getSurname1()
				+ ";" + updatedEmployee.getSurname2() + ";" + updatedEmployee.getTlf() + ";" + updatedEmployee.getMail()
				+ ";" + updatedEmployee.getJob() + ";" + hiringDate + ";" + yearSalary + ";" + sickLeave;
		// Añadimos la linea de datos al fichero CSV.
		try {
			FileWriter fw = new FileWriter(config.getProperty(PropertyConstants.CSV_PATH), true);
			fw.write("\n" + updatedData + ";" + status);
			fw.close();
			log.info("[" + updatedEmployee.getId() + "] - \"" + status + "\"");

		} catch (IOException e) {
			log.error("[" + updatedEmployee.getId() + "] - Fallo al escribir al usuario");
			throw new SiaException(SiaExceptionCodes.IN_OUT, e);
		}

	}

}
