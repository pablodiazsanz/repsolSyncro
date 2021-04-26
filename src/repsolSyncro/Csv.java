package repsolSyncro;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import repsolSyncro.exceptions.SiaException;
import repsolSyncro.exceptions.SiaExceptionCodes;

public class Csv {

	private static Logger log = Logger.getLogger(Csv.class);

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
		int contLine = 2;
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
				lineList.add(line);
				contLine++;
			}

		} catch (FileNotFoundException e) {
			log.error("Fichero no encontrado: \"" + path + "\"", e);
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
}
