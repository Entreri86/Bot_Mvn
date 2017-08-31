package database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.logging.BotLogger;

import services.Survey;

public class DBManager {
	  private static final String LOGTAG = "DATABASEMANAGER";	  
	  private static volatile DBManager dbInstance;
	  private static volatile DBConnection connection;
	  private boolean isCreated = false;
	  /**
	   * 
	   */
	  private DBManager (){
		  connection = new DBConnection();//Conectamos con la DB.
		  /**		   
		   * final int currentVersion = connetion.checkVersion();
        	 BotLogger.info(LOGTAG, "Current db version: " + currentVersion);
        		if (currentVersion < CreationStrings.version) {
            		recreateTable(currentVersion);
        		}
		   *  De momento no necesario.
		   */
		  checkTables();//Comprobamos si hay que crear las tablas en caso de ser necesario.
	  }
	  /**
	   * 
	   * @return
	   */
	  public static DBManager getInstance (){
		  DBManager currentInstance = null;//quitamos el final por el compilador.
		  if (dbInstance == null){//Si no esta iniciada la instancia...
			  synchronized (DBManager.class) {//Sincronizamos el bloque para que solo se pueda acceder una vez al mismo tiempo
				if (dbInstance == null){//Si no esta iniciada la instancia...La iniciamos
					dbInstance = new DBManager(); 
				} else{
					currentInstance = dbInstance;
				}
			}
		  } else {
			  currentInstance = dbInstance;
		  }
		  return currentInstance;
	  }
	  /**
	   * Metodo encargado de comprobar si las tablas estan creadas, en caso de no estar creadas deriva al metodo createTables.
	   */
	  private void checkTables(){
		  if (isCreated){
			  System.out.println("Tablas ya creadas.");
		  } else{
			  createTables();
		  }
	  }
	  /**
	   * Metodo encargado de crear las tablas en la base de datos.
	   */
	  private void createTables() {		  			  
		//Si no estan creadas las tablas..		
		try {
			connection.executeQuery(DataBaseStrings.CREATE_USERS_TABLE);//Creamos la tabla de usuarios por si no estaba creada.
			connection.executeQuery(DataBaseStrings.CREATE_SURVEY_TABLE);//Creamos la tabla de encuestas por si no estaba creada.
			isCreated = true;//Marcamos como creadas.
		} catch (SQLException e) {
			BotLogger.error(LOGTAG, e);
			e.printStackTrace();			
		}		  		  		  		 
	  }
	  /**
	   * Metodo encargado de insertar un usuario en la base de datos.
	   * @param user Usuario a introducir en la tabla de usuarios.
	   * @return true si se ha podido llevar a cabo la sentencia.
	   */
	  public boolean insertUserOnDb (User user){
		  Integer userId = user.getId();
		  String firstName = user.getFirstName();
		  String lastName = user.getLastName();
		  String userName = user.getUserName();		  
		  try {
			PreparedStatement statement = connection.getPreparedStatement(DataBaseStrings.INSERT_USERS_TABLE);//Recogemos el PreparedSt...
			//Asignamos los parametros de la sentencia.
			statement.setInt(1, userId);
			statement.setString(2, firstName);
			statement.setString(3, lastName);
			statement.setString(4, userName);
			statement.executeUpdate();//Ejecutamos la sentencia.
			return true;
		} catch (SQLException e) {
			BotLogger.error(LOGTAG, e);
			e.printStackTrace();
			return false;
		}	  		 		  
	  }
	  /**
	   * Metodo encargado de borrar al usuario de la tabla de usuarios de la base de datos.
	   * @param user Identificador del usuario a borrar.
	   * @return true si la sentencia ha sido ejecutada.
	   */
	  public boolean deleteUserOnDb (Integer userId){		  
		  try {
			PreparedStatement statement = connection.getPreparedStatement(DataBaseStrings.DELETE_USER_FROM_TABLE);//Recogemos el PreparedSt...
			//Asignamos el parametro de la sentencia.
			statement.setInt(1, userId);
			statement.executeUpdate();//Ejecutamos la sentencia.
			return true;
		} catch (SQLException e) {
			BotLogger.error(LOGTAG, e);
			e.printStackTrace();
			return false;
		}	 
	  }
	  /**
	   * Metodo encargado de devolver el nombre y segundo nombre del usuario concatenados.
	   * @param userId identificador del usuario a buscar.
	   * @return String concatenado del usuario con su nombre y segundo nombre.
	   */
	  public String getUserFromDb (Integer userId){
		  String userName = ""; 
		  try {
			PreparedStatement statement = connection.getPreparedStatement(DataBaseStrings.READ_USERS_TABLE);
			statement.setInt(1, userId);
			ResultSet result = statement.executeQuery();
			userName = userName.concat(result.getString(2).concat(" ").concat(result.getString(3)));
			return userName;
		} catch (SQLException e) {
			BotLogger.error(LOGTAG, e);
			e.printStackTrace();
			return null;
		} 
	  }
	  /**
	   * Metodo encargado de comprobar si el usuario dado por parametro esta insertado en la base de datos.
	   * @param userId identificador del usuario a comprobar.
	   * @return true si esta en la base de datos.
	   */
	  public boolean checkUserOnDb (Integer userId){
		  try {
			PreparedStatement statement = connection.getPreparedStatement(DataBaseStrings.READ_USERS_TABLE);
			statement.setInt(1, userId);
			statement.executeQuery();
			return true;
		} catch (SQLException e) {
			BotLogger.error(LOGTAG, e);
			e.printStackTrace();
			return false;
		}
	  }
	  /**
	   * Metodo encargado de insertar los datos de la encuesta en la base de datos.
	   * @param userId identificador del usuario.
	   * @param question pregunta de la encuesta.
	   * @param answers respuestas de la encuesta.
	   * @param answerScore puntuaciones de la encuesta.
	   * @return true si la sentencia es ejecutada correctamente.
	   */
	  public boolean insertSurvey (Integer userId, String question, String [] answers, Integer [] answerScore){
		  String answersToDb = "";
		  String scoreToDb = "";
		  String dot = ".";
		  for (String answer: answers){
			  answersToDb.concat(answer).concat(dot);//Añadimos al String la pregunta y un punto de separacion como marca.
		  }
		  for (int i =0; i <answerScore.length;i++){
			  String aux ="";//Declaramos el auxiliar y lo rellenamos con la puntuacion.			  
			  aux = answerScore[i].toString();
			  scoreToDb.concat(aux).concat(dot);//Concatenamos y marcamos con un punto para despues conocer las puntuaciones.					  
		  }
		  try {
			PreparedStatement statement = connection.getPreparedStatement(DataBaseStrings.INSERT_SURVEY);
			statement.setInt(2, userId);//Empieza en 2 el parametro por el surveyId autoincrement.
			statement.setString(3, question);//Pregunta
			statement.setString(4, answersToDb);//Respuestas
			statement.setString(5, scoreToDb);//Puntuaciones.
			statement.executeUpdate();//Ejecutamos la sentencia.
			return true;
		} catch (SQLException e) {
			BotLogger.error(LOGTAG, e);
			e.printStackTrace();
			return false;
		}		  
	  }
	  /**
	   * Metodo encargado de borrar de la base de datos las encuestas relacionadas con el usuario.
	   * @param user identificador del usuario.
	   * @return true si se logra ejecutar la sentencia.
	   */
	public boolean deleteSurveysOnDb (Integer userId){		
		try {
			PreparedStatement statement = connection.getPreparedStatement(DataBaseStrings.DELETE_SURVEYS);//Recogemos el PreparedSt...
			statement.setInt(1, userId);
			statement.executeUpdate();//Ejecutamos la sentencia.
			return true;
		} catch (SQLException e) {
			BotLogger.error(LOGTAG, e);
			e.printStackTrace();
			return false;
		}		
	}
	/**
	 * Metodo encargado de recoger los datos de las encuestas en la base de datos
	 * que el usuario haya podido realizar durante el uso del bot y devolverlas en una lista.
	 * @param userId identificador del usuario.
	 * @return List<Survey> con todas las encuestas realizadas por el usuario.
	 */
	public List<Survey> getSurveysFromDb(Integer userId){
		List <Survey> surveys = new ArrayList <Survey>();//Declaramos la lista de Objetos encuesta.
		try {
			PreparedStatement statement = connection.getPreparedStatement(DataBaseStrings.READ_SURVEY);//Recogemos el PreparedSt...
			statement.setInt(1, userId);//Asignamos parametro y ejecutamos consulta.
			ResultSet result = statement.executeQuery();			
			while (result.next()){//Mientras haya resultados...
				Survey survey = new Survey();//Creamos un objeto encuesta a rellenar.
				survey.setQuestion(result.getString("question"));//Asignamos la pregunta recogiendola del ResultSet.
				String answers = result.getString("answers");//Recogemos las respuestas.
				String [] splitAnswers = answers.split(".");//Recortamos los resultados segun la marca.
				for (int i =0; i < splitAnswers.length;i++){
					survey.setAnswers(splitAnswers[i]);//Añadimos al objeto encuesta.
				}
				//Metodo en Survey para quitar los puntos y devolver un array de Integer limpio.
				Integer [] scores = survey.parseScoresToInteger(result.getString("score"));//Parseamos los resultados
				for (int i =0; i < scores.length;i++){
					survey.increaseScore(scores[i]);//Los añadimos al objeto encuesta.
				}
				surveys.add(survey);//Añadimos el objeto encuesta a la lista de encuestas.
			}			
		} catch (SQLException e) {
			BotLogger.error(LOGTAG, e);
			e.printStackTrace();
		}
		return surveys;
	}
	
	
}
