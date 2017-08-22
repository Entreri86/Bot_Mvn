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
	//ID de la tarea, userId para conocer el usuario que asigna la tarea, TIMESTAMP para la hora,minutos,segundos,mes,año y dia
	//de la tarea a recordar, el titulo de la tarea a recordar, la descripcion de la tarea a recordar, boolean para controlar si esta
	//o no esta hecha la tarea.
	public static final String CREATE_TASK_TABLE = "CREATE TABLE IF NOT EXISTS TaskToRemember (taskId INTEGER PRIMARY KEY AUTO_INCREMENT,"
													+ "userId INTEGER NOT NULL, title VARCHAR(30) NOT NULL, description VARCHAR (60),"
													+ "taskTime TIMESTAMP NOT NULL);";
	
			
			
	
}
