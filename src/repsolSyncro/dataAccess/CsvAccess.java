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

/**
 * En esta clase lo que hacemos es operar con los ficheros CSV. Va a tener los
 * metodos de devolver datos, de crear un nuevo fichero, y de escribir datos en
 * el fichero csv.
 *
 */
public class CsvAccess {

	// Logger para poder escribir las trazas del codigo en los logs
	private static Logger log = Logger.getLogger(CsvAccess.class);

	/**
	 * Devuelve una List<HashMap<String, String>> con todas las lineas del fichero
	 * CSV pedido.
	 * 
	 * @param path Ruta del fichero CSV que vamos a leer
	 * @return List<HashMap<String, String>> Lista que contiene el HashMap<String,
	 *         String> con los datos separados. El HashMap<String, String> utiliza
	 *         como Key el nombre de las columnas, y de valor el dato de la fila
	 *         correspondiente.
	 * @throws SiaException Lanza las excepciones generadas
	 */
	public static List<HashMap<String, String>> getData(String path) throws SiaException {

		log.trace("Entramos en el método getData(path)");

		// Inicializamos la List<HashMap<String, String>> lineList
		List<HashMap<String, String>> lineList;

		// Inicializamos el FileReader y el BufferedReader a null
		FileReader reader = null;
		BufferedReader br = null;

		// Utilizamos un contador de lineas del fichero para obtener informacion
		// acerca de la linea que nos da un error o una excepcion
		int contLine = 1;
		log.trace("Contador de linea del fichero: " + contLine);

		try {
			// Creamos un File y le pasamos la ruta del fichero que queremos abrir.
			File f = new File(path);
			log.trace("Ruta del fichero: " + f.getPath());

			// Le pasamos al FileReader el fichero y al BufferedReader el lector del fichero
			// para poder trabajar con el.
			reader = new FileReader(f);
			br = new BufferedReader(reader);

			log.trace("Accedemos al fichero");

			// Leemos la primera linea, que es la informacion de las columnas, y obtenemos
			// los datos que son los títulos de cada columna.
			String line = br.readLine();
			HashMap<Integer, String> columnsLine = obtenerColumnas(line);
			log.trace("Columnas del fichero: " + columnsLine);

			// Inicializamos el lineList como un ArrayList
			lineList = new ArrayList<HashMap<String, String>>();

			// Con el bucle while recorremos linea por linea el fichero
			while (line != null) {
				try {
					// Leemos la linea
					line = br.readLine();
					log.trace("Linea " + contLine + " leida");

					// Obtenemos los datos de cada linea, utilizando de clave el título de la
					// columna y de valor el dato de la fila.
					HashMap<String, String> lineData = getLineMap(line, columnsLine);
					log.trace("Linea convertida a HashMap: " + lineData);

					// Añadimos el HashMap lineData relleno en la List<HashMap<String, String>>
					lineList.add(lineData);
					log.trace("HashMap añadido a la lista de HashMaps");

				} catch (NullPointerException e) {
					// Esto es un aviso, no es un error, porque si el fichero tiene una linea vacia
					// lo que hacemos es saltarla.
					log.warn("Linea (" + contLine + ") del Fichero \"" + path + "\" esta vacia", e);

				}

				// Cambiamos de linea
				contLine++;
				log.trace("Cambiamos de linea. Linea nueva: " + contLine);
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

		// Aqui comprobamos que el fichero que nos pasan no contienen datos que no
		// concuerdan con lo acordado.
		if (lineList.size() <= 0) {
			throw new SiaException("Fichero con lineas erroneas");

		}

		// Devolvemos la lista de HashMap
		return lineList;
	}

	/**
	 * Este método lo utilizamos para obtener los datos de cada linea. Para
	 * obtenerlos, cogemos la linea y tenemos en cuenta ciertos problemas como que
	 * un valor pueda obtener el separador ';', que es el que utilizamos. Como
	 * queremos que cada valor que obtenemos de la linea tenga de clave el titulo de
	 * la columna, cogemos los datos de las columnas obtenidos por parametro y se lo
	 * añadimos al HashMap con el valor. Para obtener los valores, recorremos la
	 * linea caracter a caracter y vamos analizandolos.Finalmente, devolvemos los
	 * datos separados.
	 * 
	 * @param line       La linea que se obtiene del fichero
	 * @param columnLine El HashMap con el titulo de las columnas
	 * @return Un HashMap nuevo con clave el titulo de la columna y con valor el
	 *         dato de la fila.
	 */
	private static HashMap<String, String> getLineMap(String line, HashMap<Integer, String> columnLine) {

		log.trace("Entramos en el método getLineMap(line, columnsLine)");

		// Creamos un HashMap para obtener los datos de la linea, con clave el titulo de
		// la columna.
		HashMap<String, String> lineMap = new HashMap<String, String>();

		// Este va a ser el valor que vamos a ir rellenando según recorramos caracter a
		// caracter
		String value = "";

		// Utilizamos este booleano para saber si abrimos o cerramos las comillas
		boolean openQuotes = false;

		// Utilizamos un valor auxiliar para saber cuando cambiamos de dato
		int columnPosition = 0;

		// Con este bucle for recorremos caracter por caracter para sacar los datos uno
		// a uno
		for (int i = 0; i < line.length(); i++) {

			/*
			 * Aqui observo si el caracter es una comilla. Si lo es, hago una comprobación
			 * para saber si inicio el valor o lo finalizo
			 */
			if (line.charAt(i) == '"') {
				if (openQuotes) {
					openQuotes = false;
				} else {
					openQuotes = true;
				}
			}

			/*
			 * Aqui decido si hay un cambio de valor o si no lo hay. Si no lo hay, sumo el
			 * caracter a value, y si lo hay, compruebo si el valor es nulo o si lo añado
			 * segun lo tenga.
			 */
			if (line.charAt(i) == ';' && openQuotes == false) {
				// Aquí compruebo que si no hay nada en ese dato, me ponga el valor como nulo.
				// En ambos casos, lo añadimos al HashMap. Creo la variable mapValue para añadir
				// un valor u otro.
				String mapValue;

				if (line.charAt(i - 1) == ';') {
					mapValue = "NULL";

				} else {
					mapValue = value;

				}

				// Añadimos al mapa la clave y el valor
				lineMap.put(columnLine.get(columnPosition), mapValue);
				log.trace("Valor añadido al HashMap: [" + columnLine.get(columnPosition) + ", " + mapValue + "]");

				// Cambiamos la posición y establecemos la cadena value vacia
				columnPosition++;
				value = "";

			} else {
				// Añadimos el caracter a la cadena value
				value += line.charAt(i);
			}

		}

		// Añadimos el último valor con su clave y devolvemos el HashMap
		lineMap.put(columnLine.get(columnPosition), value);
		return lineMap;

	}

	/**
	 * En este método obtenemos la informacion de las columnas y las devolvemos con la posicion y el nombre que obtenemos
	 * 
	 * @param line
	 * @return
	 */
	private static HashMap<Integer, String> obtenerColumnas(String line) {
		// Creamos un HashMap para obtener los datos de la linea
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
			FileWriter fw = new FileWriter(writePath);
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
