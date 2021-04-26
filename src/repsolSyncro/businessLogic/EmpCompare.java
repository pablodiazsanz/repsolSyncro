package repsolSyncro.businessLogic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import repsolSyncro.entities.EmpTransaction;
import repsolSyncro.entities.Employee;

public class EmpCompare {
	
	private static Logger log = Logger.getLogger(EmpCompare.class);

	public static List<EmpTransaction> getTransactions(HashMap<String, Employee> clientData,
			HashMap<String, Employee> serverData) {

		log.trace("Empezamos la comparacion de usuarios");
		
		List<EmpTransaction> transactionList = new ArrayList<EmpTransaction>();
		EmpTransaction empTransaction;

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
					
					if (clientData.get(i).compareTo(serverData.get(i)) == 1) {
						log.trace("Entramos en el métodp updateEmployee para actualizar al empleado ["
								+ clientData.get(i).getId() + "]");
						empTransaction = updateEmployee(clientData.get(i), serverData.get(i));
						transactionList.add(empTransaction);
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
					empTransaction = new EmpTransaction("CREATE", clientData.get(i));
					transactionList.add(empTransaction);
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
			empTransaction = new EmpTransaction("DELETE", clientData.get(key));
			transactionList.add(empTransaction);
		}
		
		return transactionList;
	}

	private static EmpTransaction updateEmployee(Employee clientEmployee, Employee serverEmployee) {
		
		List<String> modifiedFields = new ArrayList<String>();
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
			clientEmployee.setName(clientEmployee.getName());
			log.debug("el empleado [" + clientEmployee.getId() + "] cambia el (nombre) a: {"
					+ clientEmployee.getName() + "}");
			modifiedFields.add("name");
		}
		if (!clientEmployee.getSurname1().equalsIgnoreCase(serverEmployee.getSurname1())) {
			clientEmployee.setSurname1(clientEmployee.getSurname1());
			log.debug("el empleado [" + clientEmployee.getId() + "] cambia el (1º apellido) a: {"
					+ clientEmployee.getSurname1() + "}");
			modifiedFields.add("surname1");
		}
		if (!clientEmployee.getSurname2().equalsIgnoreCase(serverEmployee.getSurname2())) {
			clientEmployee.setSurname2(clientEmployee.getSurname2());
			log.debug("el empleado [" + clientEmployee.getId() + "] cambia el (2º apellido) a: {"
					+ clientEmployee.getSurname2() + "}");
			modifiedFields.add("surname2");
		}
		if (!clientEmployee.getTlf().equalsIgnoreCase(serverEmployee.getTlf())) {
			clientEmployee.setTlf(clientEmployee.getTlf());
			log.debug("el empleado [" + clientEmployee.getId() + "] cambia el (telefono) a: {"
					+ clientEmployee.getTlf() + "}");
			modifiedFields.add("phone");
		}
		if (!clientEmployee.getMail().equalsIgnoreCase(serverEmployee.getMail())) {
			clientEmployee.setMail(clientEmployee.getMail());
			log.debug("el empleado [" + clientEmployee.getId() + "] cambia el (Email) a: {" + clientEmployee.getMail()
					+ "}");
			modifiedFields.add("email");
		}
		if (!clientEmployee.getJob().equalsIgnoreCase(serverEmployee.getJob())) {
			clientEmployee.setJob(clientEmployee.getJob());
			log.debug("el empleado [" + clientEmployee.getId() + "] cambia el (puesto de trabajo) a: {"
					+ clientEmployee.getJob() + "}");
			modifiedFields.add("job");
		}
		if (clientEmployee.getHiringDate().compareTo(serverEmployee.getHiringDate()) != 0) {
			clientEmployee.setHiringDate(clientEmployee.getHiringDate());
			log.debug("el empleado [" + clientEmployee.getId() + "] cambia el (Fecha de contratacion) a: {"
					+ clientEmployee.getHiringDate() + "}");
			modifiedFields.add("hiringDate");
		}
		if (clientEmployee.getYearSalary() != serverEmployee.getYearSalary()) {
			clientEmployee.setYearSalary(clientEmployee.getYearSalary());
			log.debug("el empleado [" + clientEmployee.getId() + "] cambia el (Salario anual) a: {"
					+ clientEmployee.getYearSalary() + "}");
			modifiedFields.add("yearSalary");
		}
		if (clientEmployee.isSickLeave() != serverEmployee.isSickLeave()) {
			clientEmployee.setSickLeave(clientEmployee.isSickLeave());
			log.debug("el empleado [" + clientEmployee.getId() + "] cambia el (Baja) a: {"
					+ clientEmployee.isSickLeave() + "}");
			modifiedFields.add("sickLeave");
		}
		
		EmpTransaction empTransaction = new EmpTransaction("UPDATE", clientEmployee, modifiedFields);
		return empTransaction;
	}

}
