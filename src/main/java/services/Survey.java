package services;

import java.util.ArrayList;
import java.util.HashMap;

public class Survey {
	private String question;//Pregunta de la encuesta.
	private ArrayList <String> answers;//Respuestas de la encuesta.	
	private ArrayList <Integer>values;//Puntuaciones de los votos, concuerda con las respuestas en cuanto a posicion.
	private HashMap <Integer, Integer> usersIdPos;//HashMap con clave userID y la posicion de voto.
	private int peopleVoted;//Conteo de personas que han votado.
	private int answerOptions;//Conteo de respuestas.

	
	
	/**
	 * 
	 */
	public Survey (){
		peopleVoted = 0;
		answers = new ArrayList<>();
		values = new ArrayList<>();		
		usersIdPos = new HashMap<>();
	}
	/**
	 * Metodo encargado de transformar el String de puntuaciones dado por parametro en un Array de Integer.
	 * @param scores Puntuaciones a parsear.
	 * @return Integer [] array de puntuaciones.
	 */
	public Integer [] parseScoresToInteger(String scores){
		String [] stringScore = scores.split(".");//Recogemos en un Array todas las puntuaciones con el punto de separacion.
		Integer [] finalScore = new Integer [stringScore.length];//Iniciamos un Array de Integer para parsear los resultados.
		for (int i =0; i < stringScore.length;i++){
			finalScore [i] = Integer.parseInt(stringScore[i]);//Parseamos cada String en un Integer y lo asignamos.
		}		
		return finalScore;
	}
	/**
	 * Metodo encargado de sustituir la puntuacion del voto de un usuario.
	 * @param userId usuario a controlar la posicion del voto.
	 * @param position posicion del voto.
	 */
	public void replaceVote (Integer userId, Integer position){
		this.usersIdPos.replace(userId, position);
	}
	/**
	 * Metodo encargado de retornar la posicion donde el usuario ha votado.
	 * @param userId identificador del usuario.
	 * @return posicion de la votacion.
	 */
	public Integer getPollPosition (Integer userId){
		Integer positionValue = usersIdPos.get(userId);
		return positionValue;
	}
	/**
	 * Metodo que retorna el HashMap del control de posiciones y usuarios que han votado.
	 * @return HashMap con los usuarios y posicion de votos.
	 */
	public HashMap<Integer, Integer> getUsersIdPos() {
		return usersIdPos;
	}
	
	/**
	 * Metodo que inserta el usuario en el HashMap con la posicion de su voto.
	 * @param userId Id del usuario que introducir en el Map.
	 */
	public void insertUserOnList (Integer userId,Integer pos){
		this.usersIdPos.put(userId, pos);
		peopleVotedUp();
	}
	
	/**
	 * Metodo encargado de aumentar el contador de personas que han votado.
	 */
	private void peopleVotedUp(){
		this.peopleVoted = this.peopleVoted + 1; 
	}
	/**
	 * Metodo que retorna la cantidad de personas que han votado hasta ahora en la encuesta.
	 * @return numero de personas que han votado.
	 */
	public int getPeopleVoted() {
		return peopleVoted;
	}
	/**
	 * Metodo que retorna la pregunta de la encuesta.
	 * @return pregunta de la encuesta.
	 */
	public String getQuestion() {
		return question;
	}
	/**
	 * Metodo que asigna la pregunta de la encuesta.
	 * @param question pregunta de la encuesta.
	 */
	public void setQuestion(String question) {
		this.question = question;
	}
	/**
	 * Metodo encargado de retornar la respuesta segun la posicion dada.
	 * @return respuesta segun posicion dada.
	 */
	public String getAnswer(Integer position) {
		return this.answers.get(position);
	}
	/**
	 * Metodo encargado de asignar a la lista de respuestas una respuesta.
	 * @param answer respuesta a asignar.
	 */
	public void setAnswers(String answer) {
		this.answers.add(answer);
	}
	/**
	 * Metodo que retorna la cantidad de respuestas que tiene la encuesta.
	 * @return cantidad de respuestas que tiene la encuesta.
	 */
	public int getAnswerOptions() {
		return answerOptions;
	}
	/**
	 * Metodo encargado de asignar la cantidad de respuestas que tiene la encuesta.
	 * @param answerOptions cantidad de respuestas que tiene la encuesta.
	 */
	public void setAnswerOptions(int answerOptions) {
		this.answerOptions = answerOptions;
	}
	/**
	 * Metodo encargado de retornar la puntuacion de voto segun posicion dada.
	 * @return puntuacion de voto segun posicion dada.
	 */
	public Integer getValues(int position) {
		return values.get(position);
	}
	/**
	 * Metodo encargado de iniciar la lista con valores 0.
	 * @param position posicion a iniciar.
	 */
	public void initValues (int position){
		this.values.add(position, 0);//Iniciamos a 0 las puntuaciones.
	}
	/**
	 * Metodo encargado de incrementar la puntuacion segun la posicion dada.
	 * @param position posicion a incrementar la puntuacion.
	 */
	public void increaseScore(int position) {
		Integer previousVal = getValues(position);
		previousVal = previousVal + 1;
		this.values.add(position, previousVal);		
	}
	/**
	 * Metodo encargado de reducir la puntuacion dada segun la posicion por parametro.
	 * @param position posicion a reducir la puntuacion.
	 */
	public void decreaseScore (int position){
		Integer previousVal = getValues(position);
		previousVal = previousVal - 1;
		values.remove(position);
		this.values.add(position, previousVal);
	}
	

}
