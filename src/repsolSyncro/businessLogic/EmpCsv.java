package repsolSyncro.businessLogic;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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

	// Logger para poder escribir las trazas del codigo en los logs
	private Logger log = Logger.getLogger(EmpCsv.class);
	// Objeto que apunta al properties que nos interese
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

	/**
	 * Constructor de la clase que lee los ficheros csv de empleadoA, se le pasa por
	 * parametro a donde quieres que apunte.
	 * 
	 * @param fileName Fichero csv al que apunta, client, server o result segun
	 *                 convenga
	 * @throws SiaException
	 */
	public EmpCsv(String fileName) throws SiaException {
		// Aqui declaramos y seleccionamos a que direccion va a puntar el properties
		// para leer los elementos segun nos interese
		String src = "";
		log.trace("Accedemos al fichero de: " + fileName);

		if (fileName.toLowerCase().equals("client")) {
			src = PropertiesChecker.getAllProperties().getProperty(PropertyConstants.PATH_CLIENT_PROPERTY_FILE);
		} else if (fileName.toLowerCase().equals("server")) {
			src = PropertiesChecker.getAllProperties().getProperty(PropertyConstants.PATH_SERVER_CSV_PROPERTY_FILE);
		} else if (fileName.toLowerCase().equals("result")) {
			src = PropertiesChecker.getAllProperties().getProperty(PropertyConstants.PATH_RESULT_PROPERTY_FILE);
		}
		log.trace("Se carga el fichero: [" + src + "]");

		try {
			// Cargamos el archivo una vez elegido a donde apunta nuestro objeto
			file = new Properties();
			FileInputStream ip = new FileInputStream(src);
			file.load(ip);

			// Cargamos los datos de properties en variables de la clase
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
			log.trace("Se cargan los datos del fichero: " + src);

		} catch (FileNotFoundException e) {
			String message = "fichero " + src + " no encontrado";
			throw new SiaException(SiaExceptionCodes.MISSING_FILE, message, e);

		} catch (IOException e) {
			String message = "error leyendo el fichero properties: " + src;
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
		List<HashMap<String, String>> csvData = CsvAccess.getData(path);
		log.trace("obtenemos los datos del csv");
		String employeeID = "NO ID";

		// Recorremos linea a linea de la lista de datos del csv para obtener los
		// empleados con el id y meterlos en un HashMap. Si hay algun error con alguno
		// de ellos, lo que haremos ser� meter en el HashMap el id de clave con el valor
		// nulo y asi sabremos que no hay que operar con ellos.
		for (int i = 0; i < csvData.size(); i++) {
			try {
				// Obtenemos los datos de la linea
				// Obtenemos el id del empleado
				employeeID = csvData.get(i).get(id);

				// A�adimos al HashMap el objeto Employee que utiliza de clave el ID de ese
				// empleado
				Employee emp = createEmployee(csvData.get(i));
				hm.put(employeeID, emp);

			} catch (ParseException e) {
				hm.put(employeeID, null);

				String message = "ID: [" + employeeID + "] - N�Linea: (" + i + ") - Fichero: \"" + path
						+ "\" - Linea: {" + csvData.get(i) + "}\n No se ha podido crear el objeto empleado.";
				throw new SiaException(SiaExceptionCodes.PARSE_DATE, message, e);

			} catch (NumberFormatException e) {
				hm.put(employeeID, null);

				String message = "ID: [" + employeeID + "] - N�Linea: (" + i + ") - Fichero: \"" + path
						+ "\" - Linea: {" + csvData.get(i)
						+ "}\n No se ha podido crear el objeto empleado. Numero introducido incorrecto";
				throw new SiaException(SiaExceptionCodes.NUMBER_FORMAT, message, e);

			} catch (Exception e) {

				String message = "Fallo generico en la linea (" + i + ") del Fichero \"" + path + "\"";
				throw new SiaException(SiaExceptionCodes.GENERIC, message, e);

			}
		}
		log.trace("Empleados recuperados y organizados");
		return hm;
	}

	/**
	 * Metodo que crea el objeto empleado a partir de la su coleccion de datos
	 * extraida del csv
	 * 
	 * @param empData HashMap<String, String> con los datos del empleado, la key es
	 *                la columna
	 * @return Employee con los datos del empleado
	 * @throws ParseException        en caso de error en HiringDate, o en SickLeave
	 * @throws NumberFormatException en caso de error en el YearSalary
	 */
	private Employee createEmployee(HashMap<String, String> empData) throws ParseException, NumberFormatException {
		Date empHiringDate = null;
		int empYearSalary = -1;
		boolean empSickLeave = false;
		// parseamos la fecha de striong a objeto Date
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		formatter.setTimeZone(TimeZone.getTimeZone("Europe/Madrid"));
		empHiringDate = formatter.parse(empData.get(hiringDate));
		// Aqui formateamos el salario anual a numero entero
		empYearSalary = Integer.parseInt(empData.get(yearSalary));
		// Parseamos el boleano de la baja
		empSickLeave = Boolean.parseBoolean(empData.get(sickLeave));
		log.trace("parseamos todos los datos necesarios del csv ");
		return new Employee(empData.get(id), empData.get(name), empData.get(surname1), empData.get(surname2),
				empData.get(phone), empData.get(email), empData.get(job), empHiringDate, empYearSalary, empSickLeave);
	}

	/**
	 * Manda crear el csv de resultado de empleados escribiendo la primera linea de
	 * las columnas
	 * 
	 * @throws SiaException
	 */
	private void createEmpCsv() throws SiaException {
		// linea escrita para los csv de resultados de empleados
		String columsLine = "id;name;first surname;second surname;phone;email;job;hiring_date;year_salary;sick_leave;status";
		CsvAccess.createCSV(columsLine, file.getProperty(PropertyConstants.CSV_PATH));
	}

	/**
	 * Manda escribir la lista de List<EmpTransaction> con los empleados a modificar
	 * y su status(accion que se va a realizar), y en el caso de las modificaciones,
	 * tambien la lista de los campos modificados.
	 * 
	 * @param transactionsList La lista de transacciones con las operaciones a
	 *                         ejecutar
	 * @throws SiaException
	 */
	public void generateTransactionsCsv(List<EmpTransaction> transactionsList) throws SiaException {
		// Creamos el nuevo csv
		createEmpCsv();
		log.trace("Creamos o sobreescribimos un fichero CSV");

		// Empezamos las transacciones
		for (EmpTransaction empTransaction : transactionsList) {
			// Si se crean o se destruyen escribimos todos los datos
			if (empTransaction.getStatus().equals("CREATE") || empTransaction.getStatus().equals("DELETE")) {
				System.out.println(empTransaction.toString());
				String line = empTransaction.getEmployee().toCSV() + ";" + empTransaction.getStatus();
				CsvAccess.writeCSV(line, file.getProperty(PropertyConstants.CSV_PATH));

			} else {
				// Al modificar solo ecribimos los datos a cambiar
				String line = updatedEmployeeToCsv(empTransaction);
				CsvAccess.writeCSV(line, file.getProperty(PropertyConstants.CSV_PATH));

			}
		}
	}

	/**
	 * En este m�todo lo que hacemos es crear una linea de csv con los datos a
	 * modificar del empleado y el estado.
	 * 
	 * @param empTransaction Transaccion de modificacion.
	 * @return Linea obtenida de la conversi�n de la transaccion del empleado.
	 */
	private String updatedEmployeeToCsv(EmpTransaction empTransaction) {

		// Creamos la linea que vamos a devolver y a la que le iremos a�adiendo la
		// modificacion
		String line = "";

		// Estos booleanos con los nombres de los campos son los vamos a utilizar para
		// comprobar si el dato ha cambiado o no.
		boolean name = false;
		boolean surname1 = false;
		boolean surname2 = false;
		boolean phone = false;
		boolean email = false;
		boolean job = false;
		boolean hiringDate = false;
		boolean yearSalary = false;
		boolean sickLeave = false;

		// Recorremos la lista para saber que datos se han modificado.
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

		// Si los datos han sido cambiados, a�adimos el dato a la linea que vamos a
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
