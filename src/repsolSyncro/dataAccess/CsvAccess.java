package repsolSyncro.dataAccess;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import repsolSyncro.exceptions.SiaException;
import repsolSyncro.exceptions.SiaExceptionCodes;

public class CsvAccess {

	private static Logger log = Logger.getLogger(CsvAccess.class);

	/**
	 * Devuleve un List<String> con todas las lineas del csv pedido
	 * 
	 * @param path ruta del fichero csv a leer
	 * @return List<String> con las lineas leidas
	 * @throws SiaException
	 */
	public static List<HashMap<String, String>> getData(String path) throws SiaException {
		File f = new File(path);

		log.trace("Ruta del fichero: " + f.getPath());

		FileReader reader = null;
		BufferedReader br = null;

		// Utilizamos un contador de lineas del fichero para obtener informacion
		// acerca de la linea que nos da un error o una excepcion
		int contLine = 1;
		List<HashMap<String, String>> lineList;

		try {
			reader = new FileReader(f);
			br = new BufferedReader(reader);
			log.trace("Accedemos al fichero");
			// Leemos la primera linea, que es la informacion de las columnas
			String line = br.readLine();
			HashMap<Integer, String> columnsLine = obtenerColumnas(line);
			lineList = new ArrayList<HashMap<String, String>>();
			// Con el bucle while recorremos linea por linea el fichero
			while (line != null) {
				try {
					line = br.readLine();
					HashMap<String, String> lineData = getLineMap(line, columnsLine);
					lineList.add(lineData);
				} catch (NullPointerException e) {
					log.warn("Linea (" + contLine + ") del Fichero \"" + path + "\" esta vacia", e);

				}
				contLine++;
			}

		} catch (FileNotFoundException e) {
			String message = "Fichero no encontrado: \"" + path + "\"";
			throw new SiaException(SiaExceptionCodes.MISSING_FILE, message, e);
		} catch (IOException e) {
			String message = "Fallo de entrada o salida";
			throw new SiaException(SiaExceptionCodes.IN_OUT, message, e);
		} catch (NullPointerException e) {
			String message = "El fichero al que accedemos está vacio";
			throw new SiaException(SiaExceptionCodes.EMPTY_FILE, message, e);
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
			throw new SiaException("Fichero con lineas erroneas");
		}
		return lineList;
	}

	private static HashMap<String, String> getLineMap(String line, HashMap<Integer, String> columnsLine) {
		// Creamos un ArrayList para obtener los datos de la linea
		HashMap<String, String> columsLine = new HashMap<String, String>();
		String columData = "";
		// Utilizamos este booleano para saber si abrimos o cerramos las comillas
		boolean openQuotes = false;

		// Utilizamos un valor auxiliar para saber cuando cambiamos de dato
		int columValue = 0;

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
				// Aquí compruebo que si no hay nada en ese dato, me ponga en valor del
				// ArrayList que es un valor nulo
				if (line.charAt(i-1) == ';') {
					columsLine.put(columnsLine.get(columValue), "NULL");
					columValue++;
				} else {
					log.trace("[" + columData.trim().toUpperCase() + "] - " + columData.toString());
					columsLine.put(columnsLine.get(columValue), columData);
					columValue++;
					columData = "";
				}

			} else {
				columData += line.charAt(i);
			}

			if (i == line.length() - 1) {
				log.trace("[" + columData.trim().toUpperCase() + "] - " + columData.toString());
			}

		}
		columsLine.put(columnsLine.get(columValue), columData);
		return columsLine;

	}

	private static HashMap<Integer, String> obtenerColumnas(String line) {
		// Creamos un ArrayList para obtener los datos de la linea
		HashMap<Integer, String> columsLine = new HashMap<Integer, String>();
		String columData = "";
		// Utilizamos este booleano para saber si abrimos o cerramos las comillas
		boolean openQuotes = false;

		// Utilizamos un valor auxiliar para saber cuando cambiamos de dato
		int columValue = 0;

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
				// Aquí compruebo que si no hay nada en ese dato, me ponga en valor del
				// ArrayList que es un valor nulo
				if (columData.length() == 0) {
					columsLine.put(columValue, "NULL");
				} else {
					log.trace("[" + columData.trim().toUpperCase() + "] - " + columData.toString());
					columsLine.put(columValue, columData);
					columValue++;
					columData = "";
				}

			} else {
				columData += line.charAt(i);
			}

			if (i == line.length() - 1) {
				log.trace("[" + columData.trim().toUpperCase() + "] - " + columData.toString());
			}

		}
		columsLine.put(columValue, columData);
		return columsLine;

	}

	/**
	 * Este metodo crea o sobreescribe el fichero result.csv para guardar la
	 * información final.
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
			String message = "Fallo al escribir la información de la cabecera";
			throw new SiaException(SiaExceptionCodes.IN_OUT, message, e);
		}
	}

	/**
	 * Metodo usado para añadir una linea de datos al archivo CSV de resultados
	 *
	 * @param EmpTransaction El empleado que queremos añadir con su status
	 * @param path           direccion que recibimos para escribir
	 * @throws SiaException
	 */
	public static void writeCSV(String csvLinea, String path) throws SiaException {
		try {
			FileWriter fw = new FileWriter(path, true);
			fw.write("\n" + csvLinea);
			fw.close();
			log.info("[" + csvLinea + "]");
		} catch (IOException e) {
			String message = "[" + csvLinea + "] - Fallo al escribir al usuario";
			throw new SiaException(SiaExceptionCodes.IN_OUT, message, e);
		}
	}
}
