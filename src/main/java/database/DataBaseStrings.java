package database;

public class DataBaseStrings {

	//Version de la Base de datos,tabla de creacion y insercion.
	public static final int VERSION = 0;
	public static final String CREATE_VERSION_TABLE = "CREATE TABLE IF NOT EXISTS Versions"
													+ "(ID INTEGER PRIMARY KEY AUTO_INCREMENT,"
													+ " Version INTEGER);";
	public static final String INSERT_CURRENT_VERSION = "INSERT IGNORE INTO Versions (Version) VALUES(%d);";
	//Creacion de la tabla de comandos para el usuario segun su estado (administrador del grupo etc..).
	public static final String CREATE_COMMANDS_TABLE = "CREATE TABLE IF NOT EXISTS CommandUsers (userId INTEGER PRIMARY KEY,"
													   + " status INTEGER NOT NULL);";
		
	//Sentencia creacion tabla de los usuarios COMPROBADA Y OK.	
	public static final String CREATE_USERS_TABLE = "CREATE TABLE IF NOT EXISTS UsersTable (userId INTEGER PRIMARY KEY,"
			+ "firstName VARCHAR(30), lastName VARCHAR(30), userName VARCHAR(30));";		
	//Sentencia de insert en la tabla de los usuarios COMPROBADA Y OK.
	public static final String INSERT_USERS_TABLE = "INSERT INTO UsersTable (userId, firstName, lastName, userName) VALUES (?, ?, ?, ?);";
	//Sentencia de lectura de la tabla de los usuarios COMPROBADA Y OK.
	public static final String READ_USERS_TABLE = "SELECT * FROM UsersTable WHERE userId = ? ;";
	//Sentencia de eliminacion del usuario de la tabla de usuarios del bot COMPROBADA Y OK.
	public static final String DELETE_USER_FROM_TABLE = "DELETE FROM UsersTable WHERE userId = ?;";
	
	
	//Sentencia de creacion de la tabla de las encuestas COMPROBADA Y OK!
	public static final String CREATE_SURVEY_TABLE = "CREATE TABLE IF NOT EXISTS SurveysTable (surveyId INTEGER PRIMARY KEY AUTO_INCREMENT,"
			+ "userId INTEGER NOT NULL, question VARCHAR (50), answers VARCHAR (500), score VARCHAR (50));";
	//Sentencia de insert en la tabla de las encuestas COMPROBADA Y OK.
	public static final String INSERT_SURVEY = "INSERT INTO SurveysTable (userId, question, answers, score) VALUES (?, ?, ?, ?);";
	//Sentencia de lectura de la tabla de encuestas COMPROBADA Y OK! EN MYSQL SE guarda un "." en vez de una ",".
	public static final String READ_SURVEY = "SELECT question, answers, score FROM SurveysTable WHERE userId = ?;";
	//Sentencia de eliminacion de la encuesta de la tabla de encuestas COMPROBADA Y OK.
	public static final String DELETE_SURVEYS = "DELETE FROM SurveysTable WHERE userId = ?;";
	//Sentencia de comprobacion de si hay encuestas en la BD con relacion al usuario COMPROBADA Y OK.
	public static final String CHECK_SURVEYS = "SELECT * FROM SurveysTable WHERE userId = ? ;";
}
