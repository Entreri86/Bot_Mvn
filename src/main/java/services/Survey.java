package services;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import com.vdurmont.emoji.EmojiParser;
import bot.BotConfig;

public class Survey {
	private String question;//Pregunta de la encuesta.
	private String surveyText;//Texto final de la encuesta.
	private String inlineMsgId;//Id del mensaje del chat donde se estaba realizando la votacion.	
	private String inlineQueryResultArticleId;//Id del Articulo para la lista de articulos.	
	private ArrayList <String> answers;//Respuestas de la encuesta.	
	private ArrayList <Integer>values;//Puntuaciones de los votos, concuerda con las respuestas en cuanto a posicion.
	private ArrayList <String> surveyMessage;//Lista para crear los mensajes de la encuesta, con marcas porcentuales etc.
	private ArrayList <String> callBackDataList;//Lista con los datos de identificacion de los botones.
	private HashMap <Integer, Integer> usersIdPos;//HashMap con clave userID y la posicion de voto.
	private int peopleVoted;//Conteo de personas que han votado.
	private int answerOptions;//Conteo de respuestas.
	//TODO: Añadir todos los campos faltantes en la BD!!!
	/**
	 * 
	 */
	public Survey (){
		peopleVoted = 0;
		callBackDataList = new ArrayList<>();
		surveyMessage = new ArrayList<>();
		answers = new ArrayList<>();
		values = new ArrayList<>();		
		usersIdPos = new HashMap<>();
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
	 * Metodo encargado de comprobar que boton a sido pulsado y retornar su pulsacion.
	 * @param callBackData datos del boton pulsado para la comprobacion.
	 * @return numero con la posicion del boton pulsado.
	 */
	public Integer getPollCallbackPos (String callBackData){
		int pos = 0;
		for (int i =0; i < callBackDataList.size(); i++){
			if (callBackDataList.get(i).equals(callBackData)) {
				pos = i;
			}
		}		
		return pos;
	}
	/**
	 * 
	 * @return
	 */
	public String getSurveyText() {
		return surveyText;
	}
	/**
	 * Metodo que retorna el HashMap del control de posiciones y usuarios que han votado.
	 * @return HashMap con los usuarios y posicion de votos.
	 */
	public HashMap<Integer, Integer> getUsersIdPos() {
		return usersIdPos;
	}
	/**
	 * 
	 * @return
	 */
	public String getInlineMsgId() {
		return inlineMsgId;
	}
	/**
	 * 
	 * @param inlineMsgId
	 */
	public void setInlineMsgId(String inlineMsgId) {
		this.inlineMsgId = inlineMsgId;
	}
	/**
	 * 
	 * @return
	 */
	public String getInlineQueryResultArticleId() {
		return inlineQueryResultArticleId;
	}
	/**
	 * 
	 * @param inlineQueryResultArticleId
	 */
	public void setInlineQueryResultArticleId(String inlineQueryResultArticleId) {
		this.inlineQueryResultArticleId = inlineQueryResultArticleId;
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
	 * Metodo encargado de detectar si un numero dado por parametro es impar.
	 * @param num numero a inspeccionar.
	 * @return true en caso de ser impar, false en caso de ser par.
	 */
	private boolean isOddNumber (int num){
		if (num%2 !=0){
			return true;//es impar
		} else {
			return false;//es par
		}
	}
	/**
	 * Metodo encargado de devolver en un String el porcentaje correspondiente de la votacion.
	 * @param score puntuacion de los votos.
	 * @return String con el porcentaje de votos en la posicion.
	 */
	private String getPercent(int score){		
		double percent = score / (double) getPeopleVoted() * 100;
		DecimalFormat format = new DecimalFormat("0.0");
		String finalPercent = format.format(percent);
		return finalPercent;
	}
	/**
	 * Metodo encargado de crear la encuesta con los datos enviados por el usuario. 
	 */
	public void createSurvey (){
		final String emojiCry = EmojiParser.parseToUnicode(":cry:");
		final String mark = "0%";		
		ArrayList<String> surveys = new ArrayList<String>();
		setAnswerOptions(answers.size());								
		for (int i = 0; i < answers.size()+1;i++){//Subimos uno por la pregunta para el conteo.
			initValues(i);//Marcamos a 0 las puntuaciones.			
			if (i == 0){
				surveys.add(question);//Primera pos la pregunta.				
			} else {//A partir de i=1 son respuestas.
				String emojiWhiteSquare = EmojiParser.parseToUnicode(":white_medium_square:");
				surveys.add(answers.get(i-1));//Posible respuesta -1 por diferencia de posiciones.								
				surveys.add(emojiWhiteSquare+"  "+mark);//Marca del porcentaje.EMOJI cuadrado vacio.								
			} 
		}//Posiciones 0 pregunta y 1,3,5... Respuestas. Los 2,4,6... seran las marcas porcentuales.		
		surveys.add("\n"+emojiCry+" No ha respondido nadie todavía.");
		surveyMessage.clear();
		surveyMessage.addAll(surveys);				
	}
	/**
	 * Metodo encargado de convertir el ArrayList de la encuesta en un String personalizado.
	 * @param survey encuesta a convertir.
	 * @return texto personalizado.
	 */
	public String createSurveyString (){
		String jump = "\n";//Salto de linea.
		String inFlagBold = "<b>";//Marcas para el subrayado del texto.
		String endFlagBold = "</b>";		
		surveyText = "";
		for (int i = 0; i< surveyMessage.size();i++){
			if (i == 0){
				surveyText = inFlagBold + surveyText + surveyMessage.get(i) + endFlagBold + jump + jump;//Doble salto para separar mas la pregunta de las respuestas.
			} else{
				surveyText = surveyText + surveyMessage.get(i) + jump;//Recogemos el texto en una variable String y le añadimos los saltos para facilitar lectura.	
			}			
		}		
		return surveyText;
	}
	/**
	 * Metodo encargado de actualizar y devolver la lista de la encuesta con la votacion.
	 * @param position posicion a actualizar.
	 * @return Lista con los datos actualizados.
	 */
	public void updateSurvey (){
		ArrayList<String> surveys = new ArrayList<String>();
		surveyMessage.remove(surveyMessage.size()-1);//Borramos marca final.		
		final String mark = "0%";
		final String percent = "%";
		for (int i =0; i < surveyMessage.size(); i ++){//Obviamos la pregunta que no necesita ser actualizada i=1.
			if (i == 0){
				surveys.add(surveyMessage.get(i));//Pregunta
			}else{
				if (isOddNumber(i)){//si es impar
					surveys.add(surveyMessage.get(i));//respuesta
				} else{ //si es par... tiene que haber marca porcentual.
					if (getValues(i/2-1) == 0){						
						String emojiWhiteSquare = EmojiParser.parseToUnicode(":white_medium_square:");
						surveys.add(emojiWhiteSquare+"  "+mark);//Marca del porcentaje. EMOJI cuadrado vacio.
					} else{
						String emojiThumbsUp = EmojiParser.parseToUnicode(":thumbsup:");
						String thumbsUp ="";
						String finalString = "";
						for (int j = 0; j< getValues(i/2-1);j++){//Ponemos tantos dedos como votos haya.
							thumbsUp = thumbsUp+emojiThumbsUp;
						}
						finalString = thumbsUp + " " +getPercent(getValues(i/2-1))+percent;//Añadimos el porcentaje en todo caso.					
						surveys.add(finalString);//Añadimos a la lista
					}
				}
			}
		}		
		String emojiPeopleVoted = EmojiParser.parseToUnicode(":busts_in_silhouette:");
		surveys.add("\n"+emojiPeopleVoted+" "+getPeopleVoted()+" personas han votado hasta ahora.");
		surveyMessage.clear();
		surveyMessage.addAll(surveys);
		//3 respuestas 15 votos, 1 => 3 votos = 20%, 2 => 7 votos =46,6% , 3 => 5 = 33,3% votos. % = votos / totalVotado * 100		
	}	
	/**
	 * Metodo encargado de crear el teclado en el chat privado con el usuario.
	 * @return InlineKeyboardMarkup personalizado para controlar la encuesta.
	 */
	public InlineKeyboardMarkup createPrivateKeyboard (){
		InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
		List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
		//Primer boton
		List<InlineKeyboardButton> rowInline = new ArrayList<>();
    	InlineKeyboardButton shareButton = new InlineKeyboardButton();
    	shareButton.setText("Compartir encuesta.");    	   	
    	shareButton.setSwitchInlineQuery("");   	    	
    	rowInline.add(shareButton);
    	rowsInline.add(rowInline);
    	//Segundo boton.
    	List<InlineKeyboardButton> rowInline2 = new ArrayList<>();
    	InlineKeyboardButton updateButton = new InlineKeyboardButton();
    	updateButton.setText("Actualizar resultados");
    	updateButton.setCallbackData(BotConfig.UPDATE_BUTTON);    	
    	rowInline2.add(updateButton);
    	rowsInline.add(rowInline2);
    	//Añadimos los demas botones en la tercera fila.
    	List<InlineKeyboardButton> rowInline3 = new ArrayList<>();
    	for (int i =0; i < 3;i++){    		
        	InlineKeyboardButton button = new InlineKeyboardButton();
        	if (i ==0){
        		button.setText("Votar");
        		button.setCallbackData(BotConfig.VOTE_BUTTON);
        		rowInline3.add(button);
        	} else if (i == 1){
        		button.setText("Cerrar");
        		button.setCallbackData(BotConfig.CLOSE_BUTTON);
        		rowInline3.add(button);
        	} else if (i == 2){
        		button.setText("Borrar");
        		button.setCallbackData(BotConfig.DELETE_BUTTON);
        		rowInline3.add(button);
        	}
    	}
    	rowsInline.add(rowInline3);
    	markupInline.setKeyboard(rowsInline);
    	return markupInline;
	}
	/**
	 * Metodo encargado de crear en primera instancia el teclado de la votacion.	 
	 * @return InlineKeyboardMarkup teclado con la votacion personalizada.
	 */
	public InlineKeyboardMarkup createKeyboard(){		
		InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
		List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();                        
        for (int i =0; i < getAnswerOptions(); i++){//Crearemos tantos botones como respuestas haya.
        	String callBack = "Option";//Marca de datos.
        	List<InlineKeyboardButton> rowInline = new ArrayList<>();
        	InlineKeyboardButton button = new InlineKeyboardButton();        	
        	button.setText(getAnswer(i));
        	button.setCallbackData(callBack+i);//Y la marca de datos.
        	rowInline.add(button);
        	callBackDataList.add(i, callBack+i);//Guardamos en la lista la marca de datos. Option1, Option2, Option 3....        	
        	rowsInline.add(rowInline);            
        }        
        markupInline.setKeyboard(rowsInline);//Asignamos el teclado y devolvemos.		
		return markupInline;
	}
	/**
	 * Metodo encargado de gestionar la actualizacion del teclado al realizar votaciones.
	 * @return InlineKeyboardMarkup teclado con la votacion personalizada.
	 */
	public InlineKeyboardMarkup updateKeyboard (){		
		InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
		List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();   
		for (int i =0; i < getAnswerOptions(); i++){
			String callBack = "Option";//Marca de datos.
			List<InlineKeyboardButton> rowInline = new ArrayList<>();
        	InlineKeyboardButton button = new InlineKeyboardButton();        	        	
        	if (getValues(i) == 0){//Si no hay puntuacion en la posicion...  oSurvey.getValues(i) == 0
        		button.setText(getAnswer(i));
        	} else{//Si hay puntuacion...        		        		
        		button.setText(getAnswer(i)+" - "+getValues(i)+" -");//La ponemos en el teclado.
        	}        	
        	button.setCallbackData(callBack+i);//Marcamos el boton con la marca para conocer el boton pulsado.
        	rowInline.add(button);//Añadimos el boton a la lista.
        	callBackDataList.remove(i);
        	callBackDataList.add(i, callBack+i);        	
        	rowsInline.add(rowInline);//Añadimos la fila a la lista.
		}
		markupInline.setKeyboard(rowsInline);//Asignamos el teclado y devolvemos.
		return markupInline;
	}
}
