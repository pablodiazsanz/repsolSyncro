package repsolSyncro.entities;

import repsolSyncro.entities.employees.Employee;

/**
 * Creamos una interfaz para que se implemente en los objetos que creemos
 * nosotros, para tener en este caso el metodo toCSV implementado.
 *
 */
public interface MyObject extends Comparable<Employee> {

	/**
	 * Escribimos los datos que tenga el objeto pasado a CSV
	 * 
	 * @return La linea del objeto pasada a CSV
	 */
	public String toCSV();
	
}
