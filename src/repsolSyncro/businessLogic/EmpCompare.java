package repsolSyncro.businessLogic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import repsolSyncro.entities.EmpTransaction;
import repsolSyncro.entities.Employee;
import repsolSyncro.entities.MyObject;
import repsolSyncro.entities.Transaction;

/**
 * Esta clase sirve para comparar listas de empleados y devolver una lista de
 * transacciones.
 *
 */
public class EmpCompare implements Compare {

	// Logger para poder escribir las trazas del codigo en los logs
	private static Logger log = Logger.getLogger(EmpCompare.class);

	/**
	 * En este m�todo lo que hacemos es obtener las comparaciones de los HashMap del
	 * cliente y del servidor en forma de transacci�n de Empleado.
	 * 
	 * @param clientData El HashMap con los datos del cliente
	 * @param serverData El HashMap con los datos del cliente
	 * @return Una lista de las transacciones de empleados que vamos a hacer
	 */
	public  List<Transaction> getTransactions(HashMap<String, MyObject> clientData,
			HashMap<String, MyObject> serverData) {

		log.trace("Empezamos la comparacion de usuarios");

		// Iniciamos la lista de transacciones
		List<Transaction> transactionList = new ArrayList<Transaction>();
		EmpTransaction empTransaction;

		// En este bucle vamos a recorrer todos los empleados de cliente para
		// compararlos con los del servidor
		for (String i : clientData.keySet()) {

			// Aqui comprobamos si el valor de un empleado es nulo para no compararlo y no
			// hacer ninguna operaci�n. Lo eliminamos para que no opere.
			if (clientData.get(i) == null) {
				serverData.remove(i);

			} else {

				/*
				 * Aqu� vamos a modificar el empleado si tiene alg�n dato modificado y lo
				 * pasamos a la lista de transacciones de empleado como UPDATE, unicamente con
				 * los datos cambiados. Se actualize o no, lo eliminaremos de la lista del
				 * servidor para saber cuales han sido eliminados de la lista de cliente, y asi
				 * saber los que hay que borrar.
				 */

				if (serverData.containsKey(i) && serverData.get(i) != null) {

					// Utilizamos el m�todo compareTo de la clase empleado que se ha implementado
					// para saber si hay algun dato modificado
					if (clientData.get(i).compareTo((Employee) serverData.get(i)) == 1) {

						log.trace("Entramos en el m�todo updateEmployee para actualizar al empleado ["
								+ ((Employee) clientData.get(i)).getId() + "]");

						empTransaction = (EmpTransaction) updateElement(clientData.get(i), serverData.get(i));
						transactionList.add(empTransaction);

						log.debug("Modificando al empleado: " + clientData.get(i).toString() + "\n Datos anteriores: "
								+ serverData.get(i).toString());

					} else {
						log.debug("El empleado con identificador " +((Employee) clientData.get(i)).getId()
								+ " no se cambia, se mantiene igual");

					}

					serverData.remove(i);

				} else {
					// Aqu�, si no se ha modificado, como el empleado no est� en la lista del
					// servidor, lo pasamos a la lista de transacciones como CREATE
					if (serverData.get(i) == null) {
						serverData.remove(i);
					}
					if (!isTlfCorrect(((Employee) clientData.get(i)).getTlf())) {
						log.debug("Creando al empleado: " + clientData.get(i).toString());

						empTransaction = new EmpTransaction("CREATE",(Employee) clientData.get(i));
						transactionList.add(empTransaction);
					} else {
						log.info("telefono incorrecto no creamos usuario");
					}
				}
			}
		}

		// Aqu� pasamos los datos que hay que hay que eliminar y pasarlo a la lista de
		// transacciones de empleado como DELETE
		log.trace("Empezamos el borrado de usuarios");

		for (String key : serverData.keySet()) {
			log.debug("Eliminando al empleado: " + serverData.get(key).toString());

			empTransaction = new EmpTransaction("DELETE",(Employee) serverData.get(key));
			transactionList.add(empTransaction);
		}

		return transactionList;
	}

	/**
	 * 
	 * 
	 * @param tlf
	 * @return
	 */
	private boolean isTlfCorrect(String tlf) {
		boolean correct = true;
		// comprobamos longitud de los telefonos con prefijo, espa�a, francia, alemania
		// o portugal = 12
		if (tlf.length() != 12) {
			correct = false;
			log.info("telefono no insertado longitud erronea");
		}
		if (!tlf.substring(0, 3).equals("+34") || !tlf.substring(0, 3).equals("+49")
				|| !tlf.substring(0, 3).equals("+33") || !tlf.substring(0, 4).equals("+351")) {
			correct = false;
			log.info("telefono no insertado fallo en prefijo");
		}
		return correct;
	}

	/**
	 * En este m�todo vamos a obtener la transaccion del empleado que se modifica.
	 * En los empleados modificados, se necesita tambien pasarle al constructor la
	 * lista de los campos modificados para as�, cuando se ejecuten las operaciones
	 * saber el campo que se ha modificado.
	 * 
	 * @param clientEmployee El empleado de la lista del cliente
	 * @param serverEmployee El empleado de la lista del cliente
	 * @return La transaccion del empleado con el empleado, el estado (UPDATE) y la
	 *         lista de campos modificados
	 */
	public EmpTransaction updateElement(MyObject clientObject, MyObject serverObject) {
		Employee clientEmployee = (Employee) clientObject;
		Employee serverEmployee = (Employee) serverObject;
		List<String> modifiedFields = new ArrayList<String>();
		log.trace("Lista de Strings con los datos que no se modifican creada");

		/*
		 * En los if, comparamos dato a dato para saber cuales han sido modificados, y
		 * si se han modificado, a�adimos a la lista de datos modificados el dato que se
		 * ha modificado. De esta forma, luego sabremos los que hay que modificar y los
		 * que no.
		 */
		if (!clientEmployee.getName().equalsIgnoreCase(serverEmployee.getName())) {
			clientEmployee.setName(clientEmployee.getName());
			log.debug("el empleado [" + clientEmployee.getId() + "] cambia el (nombre) a: {" + clientEmployee.getName()
					+ "}");
			modifiedFields.add("name");
		}
		if (!clientEmployee.getSurname1().equalsIgnoreCase(serverEmployee.getSurname1())) {
			clientEmployee.setSurname1(clientEmployee.getSurname1());
			log.debug("el empleado [" + clientEmployee.getId() + "] cambia el (1� apellido) a: {"
					+ clientEmployee.getSurname1() + "}");
			modifiedFields.add("surname1");
		}
		if (!clientEmployee.getSurname2().equalsIgnoreCase(serverEmployee.getSurname2())) {
			clientEmployee.setSurname2(clientEmployee.getSurname2());
			log.debug("el empleado [" + clientEmployee.getId() + "] cambia el (2� apellido) a: {"
					+ clientEmployee.getSurname2() + "}");
			modifiedFields.add("surname2");
		}
		if (!clientEmployee.getTlf().equalsIgnoreCase(serverEmployee.getTlf())
				&& !isTlfCorrect(clientEmployee.getTlf())) {
			clientEmployee.setTlf(clientEmployee.getTlf());
			log.debug("el empleado [" + clientEmployee.getId() + "] cambia el (telefono) a: {" + clientEmployee.getTlf()
					+ "}");
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

		// Creamos la transaccion de empleado y se la devolvemos con la lista de campos
		// modificados.
		EmpTransaction empTransaction = new EmpTransaction("UPDATE", clientEmployee, modifiedFields);
		return empTransaction;
	}

}
