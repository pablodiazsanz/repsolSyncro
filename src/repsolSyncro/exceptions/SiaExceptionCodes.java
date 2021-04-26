package repsolSyncro.exceptions;

public class SiaExceptionCodes {

	// El dato de fecha que obtenemos no tiene un formato correcto.
	public static final String PARSE_DATE = "001";
	// 
	public static final String NULL_POINTER = "002";
	// El dato que estamos obteniendo no es un numero.
	public static final String NUMBER_FORMAT  = "003";
	// Falta de columnas a la hora de buscar los datos en una linea.
	public static final String COLUMN_LESS = "004";
	// El fichero al que estás apuntando no existe o no se encuentra.
	public static final String MISSING_FILE = "005";
	// Error de entrada o salida de datos.
	public static final String IN_OUT = "006";
	// El fichero que obtenemos esta vacio.
	public static final String EMPTY_FILE = "007";
	// La propiedad que buscamos no existe.
	public static final String MISSING_PROPERTY = "008";
	// Error de ejecución contra la base de datos
	public static final String SQL_ERROR = "009";
}
