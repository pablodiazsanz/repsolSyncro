package repsolSyncro.dataAccess;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import repsolSyncro.constants.PropertyConstants;
import repsolSyncro.entities.EmpTransaction;
import repsolSyncro.entities.Employee;
import repsolSyncro.exceptions.SiaException;
import repsolSyncro.exceptions.SiaExceptionCodes;

public class CsvAccess {

	private static Logger log = Logger.getLogger(CsvAccess.class);

	/**
	 * Devuleve un List<String> con todas las lineas del csv pedido
	 * 
	 * @param path ruta del fichero csv a leer
	 * @return Lis<String> con las lineas leidas
	 * @throws SiaException 
	 */
	public static List<String> getData(String path) throws SiaException {
		File f = new File(path);

		log.trace("Ruta del fichero: " + f.getPath());

		FileReader reader = null;
		BufferedReader br = null;

		// Utilizamos un contador de lineas del fichero para obtener informacion
		// acerca de la linea que nos da un error o una excepcion
		int contLine = 1;
		List<String> lineList;

		try {
			reader = new FileReader(f);
			br = new BufferedReader(reader);
			log.trace("Accedemos al fichero");
			// Leemos la primera linea, que es la informacion de las columnas
			String line = br.readLine();
			lineList = new ArrayList<String>();
			// Con el bucle while recorremos linea por linea el fichero
			while (line != null) {
				try {
				lineList.add(line);
				line = br.readLine();
				} catch (NullPointerException e) {
					log.warn("Linea (" + contLine + ") del Fichero \"" + path + "\" esta vacia", e);

				}
				contLine++;
			}

		} catch (FileNotFoundException e) {
			log.error("Fichero no encontrado: \"" + path + "\"", e);
			throw new SiaException(SiaExceptionCodes.MISSING_FILE, e);
		} catch (IOException e) {
			log.error("Fallo de entrada o salida", e);
			throw new SiaException(SiaExceptionCodes.IN_OUT, e);
		} catch (NullPointerException e) {
			log.error("El fichero al que accedemos est� vacio", e);
			throw new SiaException(SiaExceptionCodes.EMPTY_FILE, e);
		} finally {
			try {
				br.close();
				reader.close();
				log.trace("Lectura finalizada con " + (contLine - 2) + " lineas leidas en fichero " + path);

			} catch (IOException e) {
				log.error("Fallo de entrada o salida", e);

			}
		}
		if (lineList.size() <= 0) {
			throw new SiaException("fichero con lineas erroneas");
		}
		return lineList;
	}
	
	/**
	 * Este metodo crea o sobreescribe el fichero result.csv para guardar la
	 * informaci�n final.
	 * 
	 * @throws SiaException
	 */
	public static void createCSV(String line, String writePath) throws SiaException {
		try {
			FileWriter fw;
			fw = new FileWriter(writePath);
			fw.write(line);
			fw.close();
			log.trace(fw);
		} catch (IOException e) {
			log.error("Fallo al escribir la informaci�n de la cabecera");
			throw new SiaException(SiaExceptionCodes.IN_OUT, e);
		}
	}
	
	/**
	 * Metodo usado para a�adir una linea de datos al archivo CSV de resultados
	 *
	 * @param EmpTransaction El empleado que queremos a�adir con su status
	 * @param path direccion que recibimos para escribir
	 * @throws SiaException
	 */
	public static void writeCSV(String csvLinea, String path) throws SiaException {
		try {
			FileWriter fw = new FileWriter(path, true);
			fw.write("\n" +csvLinea);
			fw.close();
			log.info("[" + csvLinea + "]");
		} catch (IOException e) {
			log.error("[" + csvLinea + "] - Fallo al escribir al usuario");
			throw new SiaException(SiaExceptionCodes.IN_OUT, e);
		}
	}
}
