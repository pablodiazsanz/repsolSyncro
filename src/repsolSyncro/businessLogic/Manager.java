package repsolSyncro.businessLogic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import repsolSyncro.Employee;
import repsolSyncro.dataAccess.CsvAccess;
import repsolSyncro.dataAccess.DBAccess;
import repsolSyncro.exceptions.SiaException;
/**
 * Clase que introduce la logica de negocio entorno a la comparacion de los datos
 * indiferentemente del origen de estos
 *
 */
public class Manager {

	private Logger log = Logger.getLogger(Manager.class);
	private CsvAccess dao;
	
	public Manager() {}

	public Manager(CsvAccess dao) {
		this.dao = dao;
	}

	/**
	 * Este método compara las listas que se obtienen desde los csv del cliente y
	 * del servidor y analiza si hay que modificar algún usuario, dar de alta o dar
	 * de baja.
	 *
	 * @param clientData Esta lista contiene los empleados que tiene el csv del
	 *                   cliente
	 * @param serverData Esta lista contiene los empleados que tiene el csv del
	 *                   servidor
	 * @param dao        Le pasamos el objeto CsvAccess que estamos utilizando
	 * @throws SiaException
	 */
	public void compare(HashMap<String, Employee> clientData, HashMap<String, Employee> serverData,
			boolean csvToDB) throws SiaException {
		log.trace("Empezamos la comparacion de usuarios");

		// En este bucle vamos a recorrer todos los empleados de cliente para
		// compararlos con los del servidor
		for (String i : clientData.keySet()) {

			if (clientData.get(i) == null) {
				serverData.remove(i);

			} else {

				/*
				 * Aquí vamos a modificar el empleado si tiene algún dato modificado y lo
				 * pasamos al tercer CSV como un empleado que se ha modificado, solo con los
				 * datos cambiados. Se actualize o no, lo eliminaremos de la lista del servidor
				 * para saber cuales han sido eliminados de la lista de cliente, y asi saber los
				 * que hay que borrar.
				 */

				if (serverData.containsKey(i) && serverData.get(i) != null) {
					if (!clientData.get(i).getName().equalsIgnoreCase(serverData.get(i).getName().toLowerCase())
							|| !clientData.get(i).getSurname1().equalsIgnoreCase(serverData.get(i).getSurname1())
							|| !clientData.get(i).getSurname2().equalsIgnoreCase(serverData.get(i).getSurname2())
							|| !clientData.get(i).getTlf().equalsIgnoreCase(serverData.get(i).getTlf())
							|| !clientData.get(i).getMail().equalsIgnoreCase(serverData.get(i).getMail())
							|| !clientData.get(i).getJob().equalsIgnoreCase(serverData.get(i).getJob())
							|| clientData.get(i).getHiringDate().compareTo(serverData.get(i).getHiringDate()) != 0
							|| clientData.get(i).getYearSalary() != serverData.get(i).getYearSalary()
							|| clientData.get(i).isSickLeave() != serverData.get(i).isSickLeave()) {

						log.trace("Entramos en el métodp updateEmployee para actualizar al empleado ["
								+ clientData.get(i).getId() + "]");
						updateEmployee(clientData.get(i), serverData.get(i), csvToDB);
						log.debug("Modificando al empleado: " + clientData.get(i).toString() + "\n datos anteriores: "
								+ serverData.get(i).toString());

					} else {
						log.debug("El empleado con identificador " + clientData.get(i).getId()
								+ " no se cambia, se mantiene igual");

					}

					serverData.remove(i);
				} else {
					// Aquí, si no se ha modificado, como el empleado no está en la lista del
					// servidor
					// lo pasamos al tercer CSV como un nuevo empleado que se ha creado.
					if (serverData.get(i) == null) {
						serverData.remove(i);
					}
					log.debug("Creando al empleado: " + clientData.get(i).toString());
					if (csvToDB) {
						DBAccess.createEmployee(clientData.get(i));
					} else {
						dao.writeCSV(clientData.get(i), "CREATE");
					}
				}
			}
		}
		// Aquí pasamos al tercer CSV los empleados que se encuentran en la lista del
		// servidor pero
		// que han sido eliminados de la lista del cliente, por lo tanto los que se van
		// a eliminar.
		log.trace("Empezamos el borrado de usuarios");
		for (String key : serverData.keySet()) {
			log.debug("Eliminando al empleado: " + serverData.get(key).toString());
			if (csvToDB) {
				DBAccess.deleteEmployee(serverData.get(key));
			} else {
				dao.writeCSV(serverData.get(key), "DELETE");
			}
		}
	}

	/**
	 *
	 * En este método se comparan los empleados que tienen el mismo identificador de
	 * las listas del servidor y del cliente y se devuelve un empleado nuevo,
	 * únicamente con las modificaciones realizadas.
	 *
	 * @param clientEmployee Este empleado se obtiene de la lista de clentes.
	 * @param serverEmployee Este empleado se obtiene de la lista de clentes.
	 * @return Devuelve el empleado con el identificador y con las modificaciones
	 *         que tiene.
	 * @throws SiaException
	 */
	private void updateEmployee(Employee clientEmployee, Employee serverEmployee, boolean csvToDB) throws SiaException {
		// Creamos un empleado vacío con el id de los que vamos a comparar
		Employee updatedEmployee = new Employee(clientEmployee.getId(), "", "", "", "", "", "", null, -1, false);
		log.trace("Empleado vacío creado");
		List<String> extraData = new ArrayList<String>();
		log.trace("Lista de Strings con los datos que no se modifican creada");

		/*
		 * En los if, comparamos dato a dato para saber cuales han sido modificados, y
		 * si se han modificado, metemos el dato del cliente en el empleado que
		 * devolvemos.
		 * 
		 * Si no se ha modificado le pasamos el titulo del dato que no se modifica a la
		 * lista para que el manager sepa los que no se han modificado.
		 */
		if (!clientEmployee.getName().equalsIgnoreCase(serverEmployee.getName())) {
			updatedEmployee.setName(clientEmployee.getName());
			log.debug("el empleado [" + updatedEmployee.getId() + "] cambia el (nombre) a: {"
					+ updatedEmployee.getName() + "}");
		} else {
			extraData.add("name");
		}
		if (!clientEmployee.getSurname1().equalsIgnoreCase(serverEmployee.getSurname1())) {
			updatedEmployee.setSurname1(clientEmployee.getSurname1());
			log.debug("el empleado [" + updatedEmployee.getId() + "] cambia el (1º apellido) a: {"
					+ updatedEmployee.getSurname1() + "}");
		} else {
			extraData.add("surname1");
		}
		if (!clientEmployee.getSurname2().equalsIgnoreCase(serverEmployee.getSurname2())) {
			updatedEmployee.setSurname2(clientEmployee.getSurname2());
			log.debug("el empleado [" + updatedEmployee.getId() + "] cambia el (2º apellido) a: {"
					+ updatedEmployee.getSurname2() + "}");
		} else {
			extraData.add("surname2");
		}
		if (!clientEmployee.getTlf().equalsIgnoreCase(serverEmployee.getTlf())) {
			updatedEmployee.setTlf(clientEmployee.getTlf());
			log.debug("el empleado [" + updatedEmployee.getId() + "] cambia el (telefono) a: {"
					+ updatedEmployee.getTlf() + "}");
		} else {
			extraData.add("phone");
		}
		if (!clientEmployee.getMail().equalsIgnoreCase(serverEmployee.getMail())) {
			updatedEmployee.setMail(clientEmployee.getMail());
			log.debug("el empleado [" + updatedEmployee.getId() + "] cambia el (Email) a: {" + updatedEmployee.getMail()
					+ "}");
		} else {
			extraData.add("email");
		}
		if (!clientEmployee.getJob().equalsIgnoreCase(serverEmployee.getJob())) {
			updatedEmployee.setJob(clientEmployee.getJob());
			log.debug("el empleado [" + updatedEmployee.getId() + "] cambia el (puesto de trabajo) a: {"
					+ updatedEmployee.getJob() + "}");
		} else {
			extraData.add("job");
		}
		if (clientEmployee.getHiringDate().compareTo(serverEmployee.getHiringDate()) != 0) {
			updatedEmployee.setHiringDate(clientEmployee.getHiringDate());
			log.debug("el empleado [" + updatedEmployee.getId() + "] cambia el (Fecha de contratacion) a: {"
					+ updatedEmployee.getHiringDate() + "}");
		} else {
			extraData.add("hiringDate");
		}
		if (clientEmployee.getYearSalary() != serverEmployee.getYearSalary()) {
			updatedEmployee.setYearSalary(clientEmployee.getYearSalary());
			log.debug("el empleado [" + updatedEmployee.getId() + "] cambia el (Salario anual) a: {"
					+ updatedEmployee.getYearSalary() + "}");
		} else {
			extraData.add("yearSalary");
		}
		if (clientEmployee.isSickLeave() != serverEmployee.isSickLeave()) {
			updatedEmployee.setSickLeave(clientEmployee.isSickLeave());
			log.debug("el empleado [" + updatedEmployee.getId() + "] cambia el (Baja) a: {"
					+ updatedEmployee.isSickLeave() + "}");
		} else {
			extraData.add("sickLeave");
		}

		if (csvToDB) {
			DBAccess.updateEmployee(clientEmployee, extraData);
		} else {
			dao.writeUpdatedEmployeeCSV(updatedEmployee, extraData, "UPDATE");
			log.trace("Mandamos a escribir el usuario en el csv result");
		}
	}
}
