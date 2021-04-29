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

		log.trace("Entramos en el m�todo getData(path)");

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
			// los datos que son los t�tulos de cada columna.
			String line = br.readLine();
			HashMap<Integer, String> columnsLine = getColumns(line);
			log.trace("Columnas del fichero: " + columnsLine);

			// Inicializamos el lineList como un ArrayList
			lineList = new ArrayList<HashMap<String, String>>();

			// Con el bucle while recorremos linea por linea el fichero
			while (line != null) {
				try {
					// Leemos la linea
					line = br.readLine();
					log.trace("Linea " + contLine + " leida");
					if (line.length() > 0) {
						// Obtenemos los datos de cada linea, utilizando de clave el t�tulo de la
						// columna y de valor el dato de la fila.
						HashMap<String, String> lineData = getLineMap(line, columnsLine);
						log.trace("Linea convertida a HashMap: " + lineData);

						// A�adimos el HashMap lineData relleno en la List<HashMap<String, String>>
						lineList.add(lineData);
						log.trace("HashMap a�adido a la lista de HashMaps");
					}
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
			String message = "El fichero al que accedemos est� vacio";
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
	 * Este m�todo lo utilizamos para obtener los datos de cada linea. Para
	 * obtenerlos, cogemos la linea y tenemos en cuenta ciertos problemas como que
	 * un valor pueda obtener el separador ';', que es el que utilizamos. Como
	 * queremos que cada valor que obtenemos de la linea tenga de clave el titulo de
	 * la columna, cogemos los datos de las columnas obtenidos por parametro y se lo
	 * a�adimos al HashMap con el valor. Para obtener los valores, recorremos la
	 * linea caracter a caracter y vamos analizandolos. Finalmente, devolvemos los
	 * datos separados.
	 * 
	 * @param line       La linea que se obtiene del fichero
	 * @param columnLine El HashMap con el titulo de las columnas
	 * @return Un HashMap nuevo con clave el titulo de la columna y con valor el
	 *         dato de la fila.
	 */
	private static HashMap<String, String> getLineMap(String line, HashMap<Integer, String> columnLine) {

		log.trace("Entramos en el m�todo getLineMap(line, columnsLine)");

		// Creamos un HashMap para obtener los datos de la linea, con clave el titulo de
		// la columna.
		HashMap<String, String> lineMap = new HashMap<String, String>();

		// Este va a ser el valor que vamos a ir rellenando seg�n recorramos caracter a
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
			 * Aqui observo si el caracter es una comilla. Si lo es, hago una comprobaci�n
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
			 * caracter a value, y si lo hay, compruebo si el valor es nulo o si lo a�ado
			 * segun lo tenga.
			 */
			if (line.charAt(i) == ';' && openQuotes == false) {
				// Aqu� compruebo que si no hay nada en ese dato, me ponga el valor como nulo.
				// En ambos casos, lo a�adimos al HashMap. Creo la variable mapValue para a�adir
				// un valor u otro.
				String mapValue;

				if (line.charAt(i - 1) == ';') {
					mapValue = "NULL";

				} else {
					mapValue = value;

				}

				// A�adimos al mapa la clave y el valor
				lineMap.put(columnLine.get(columnPosition), mapValue);
				log.trace("Valor a�adido al HashMap: [" + columnLine.get(columnPosition) + ", " + mapValue + "]");

				// Cambiamos la posici�n y establecemos la cadena value vacia
				columnPosition++;
				value = "";

			} else {
				// A�adimos el caracter a la cadena value
				value += line.charAt(i);
			}

		}

		// A�adimos el �ltimo valor con su clave y devolvemos el HashMap
		lineMap.put(columnLine.get(columnPosition), value);
		log.trace("Valor a�adido al HashMap: [" + columnLine.get(columnPosition) + ", " + value + "]");

		return lineMap;

	}

	/**
	 * En este m�todo obtenemos la informacion de la cabecera y las devolvemos con
	 * la posicion y el nombre que obtenemos de cada una de las columnas. En la
	 * cabecera tambien comprobamos si algun valor tiene el separador (;).
	 * 
	 * @param line Traemos la primera linea del c�digo para obtener la informacion
	 *             de las columnas.
	 * @return El HashMap con la posicion y el nombre de cada columna.
	 */
	private static HashMap<Integer, String> getColumns(String line) {

		log.trace("Entramos en el m�todo getColumns(line)");

		// Creamos un HashMap para obtener los datos de la cabecera
		HashMap<Integer, String> columnsLine = new HashMap<Integer, String>();

		// Aqui guardamos el valor de la columna
		String columnValue = "";

		// Utilizamos este booleano para saber si abrimos o cerramos las comillas
		boolean openQuotes = false;

		// Utilizamos un valor auxiliar para saber en que columna estamos.
		int columnPosition = 0;

		// Con este bucle for recorremos caracter por caracter para analizar caracter
		// por caracter y obtener os distintos valores con su posicion
		for (int i = 0; i < line.length(); i++) {

			/*
			 * Aqui observo si el caracter es una comilla. Si lo es, hago una comprobaci�n
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
			 * caracter a columnValue, y si lo hay, compruebo si el valor es nulo o si lo
			 * a�ado segun lo tenga.
			 */
			if (line.charAt(i) == ';' && openQuotes == false) {
				// Aqu� compruebo que si no hay nada en ese dato, me ponga el valor como nulo.
				// En ambos casos, lo a�adimos al HashMap. Creo la variable mapValue para a�adir
				// un valor u otro.
				String mapValue;

				if (line.charAt(i - 1) == ';') {
					mapValue = "NULL";

				} else {
					mapValue = columnValue;

				}

				// A�adimos al mapa la clave y el valor
				columnsLine.put(columnPosition, mapValue);
				log.trace("Valor a�adido al HashMap: [" + columnPosition + ", " + mapValue + "]");

				// Cambiamos la posici�n y establecemos la cadena columnValue vacia
				columnPosition++;
				columnValue = "";

			} else {
				// A�adimos el caracter a la cadena columnValue
				columnValue += line.charAt(i);
			}
		}

		// A�adimos el �ltimo valor con su clave y devolvemos el HashMap
		columnsLine.put(columnPosition, columnValue);
		log.trace("Valor a�adido al HashMap: [" + columnPosition + ", " + columnValue + "]");

		return columnsLine;

	}

	/**
	 * Este m�todo crea o sobreescribe un fichero CSV con la informaci�n de la
	 * cabecera.
	 * 
	 * @param line      La linea con los datos de la cabecera.
	 * @param writePath El path de donde se encuentra el fichero.
	 * @throws SiaException
	 */
	public static void createCSV(String line, String writePath) throws SiaException {

		try {
			// Cogemos el fichero para escribirlo, y no le marcamos como true el parametro
			// de a�adir informaci�n, asi borra todo si existe y sino lo crea
			FileWriter fw = new FileWriter(writePath);

			// Escribimos la linea
			fw.write(line);

			fw.close();
			log.debug("Fichero creado con cabecera: [" + line + "]");

		} catch (IOException e) {
			String message = "Fallo al escribir la informaci�n de la cabecera";
			throw new SiaException(SiaExceptionCodes.IN_OUT, message, e);

		}

	}

	/**
	 * Este m�todo a�ade lineas a un fichero CSV
	 * 
	 * @param csvLinea La linea que le queremos a�adir al fichero
	 * @param path     El path de d�nde se encuentra el fichero
	 * @throws SiaException
	 */
	public static void writeCSV(String csvLinea, String path) throws SiaException {

		try {
			// Cogemos el fichero para escribirlo, y le marcamos como true el parametro
			// de a�adir informaci�n, para asi a�adirle los datos a la siguiente linea.
			FileWriter fw = new FileWriter(path, true);

			// A�adimos la linea al fichero
			fw.write("\n" + csvLinea);
			fw.close();

			log.debug("Linea a�adida: [" + csvLinea + "]");

		} catch (IOException e) {
			String message = "[" + csvLinea + "] - Fallo al escribir al usuario";
			throw new SiaException(SiaExceptionCodes.IN_OUT, message, e);

		}
	}
}
