package csvRepsol.constants;

/**
 * Constantes de busqueda en los valores de los ficheros properties
 *
 *los valores se organizan de la siguiente manera
 *FORMATO DE NOMBRES:
 *	- PATH: si empiezan por PATH son direccion de los propios properties
 *	- DB: si empiezan por DB son datos de un properties que apunta a una BD
 *	- CSV: si empiezan por CSV son datos de un properties que apunta a un CSV
 *		- _HEAD: si la segunda palabra despues de CSV es _HEAD el dato es el nombre que tiene
 *				 la columna que equivale al valor en el CSV
 */
public class PropertyConstants {
	//direccion de los fichero properties
	public static final String PATH_CLIENT_PROPERTY_FILE = "C:\\Users\\pdiazs\\eclipse-workspace\\csvRepsol\\src\\clientConfig.properties";
	public static final String PATH_SERVER_PROPERTY_FILE = "C:\\Users\\pdiazs\\eclipse-workspace\\csvRepsol\\src\\serverConfig.properties";
	public static final String PATH_RESULT_PROPERTY_FILE = "C:\\Users\\pdiazs\\eclipse-workspace\\csvRepsol\\src\\resultConfig.properties";
	
	//public static final String PATH_CLIENT_PROPERTY_FILE = "C:\\Users\\mparrap\\git\\csvRepsol\\src\\clientConfig.properties";
	//public static final String PATH_SERVER_PROPERTY_FILE = "C:\\Users\\mparrap\\git\\csvRepsol\\src\\serverConfig.properties";
	//public static final String PATH_RESULT_PROPERTY_FILE = "C:\\Users\\mparrap\\git\\csvRepsol\\src\\resultConfig.properties";
	
	//Direccion de la BBDD
	public static final String DB_DRIVER = "DEFAULT.Database.Driver";
	//nombre del usuario que loguiea en BBDD
	public static final String DB_USERNAME = "DEFAULT.Database.Username";
	//contraeña del usuario que logea en BBDD
	public static final String DB_PASSWORD = "DEFAULT.Database.Password";
	//direccion del csv a leer o escribir
	public static final String CSV_PATH = "DEFAULT.File.CSV";
	public static final String CSV_HEAD_ID = "DEFAULT.File.CSV.head.ID";
	public static final String CSV_HEAD_NAME = "DEFAULT.File.CSV.head.NAME";
	public static final String CSV_HEAD_SURNAME1 = "DEFAULT.File.CSV.head.FIRST_SURNAME" ;
	public static final String CSV_HEAD_SURNAME2 = "DEFAULT.File.CSV.head.SECOND_SURNAME";
	public static final String CSV_HEAD_PHONE = "DEFAULT.File.CSV.head.PHONE";
	public static final String CSV_HEAD_EMAIL = "DEFAULT.File.CSV.head.EMAIL";
	public static final String CSV_HEAD_JOB = "DEFAULT.File.CSV.head.JOB";
	public static final String CSV_HEAD_HIRING_DATE = "DEFAULT.File.CSV.head.HIRING_DATE";
	public static final String CSV_HEAD_YEAR_SALARY = "DEFAULT.File.CSV.head.YEAR_SALARY";
	public static final String CSV_HEAD_SICK_LEAVE = "DEFAULT.File.CSV.head.SICK_LEAVE";
}
