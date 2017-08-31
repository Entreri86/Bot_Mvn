package services;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.telegram.telegrambots.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.api.methods.AnswerInlineQuery;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.api.objects.inlinequery.InlineQuery;
import org.telegram.telegrambots.api.objects.inlinequery.inputmessagecontent.InputTextMessageContent;
import org.telegram.telegrambots.api.objects.inlinequery.result.InlineQueryResultArticle;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import com.vdurmont.emoji.EmojiParser;
import bot.BotConfig;
import handlers.MNSpain_bot;

public class Poll {		
	private int pollID;//Id de la encuesta.		
	private String [] callBacksData;//Datos de marca para los botones.
	private String surveyText;//Texto de la encuesta.
	public static final String parseMode = "HTML";//Parseo HTML	
	private ArrayList <String> survey;//ArrayList con la encuesta.	
	private Survey oSurvey;//Objeto que representa una encuesta unica.
	//TODO: CREAR Toda la logica con la nueva clase Survey con un HashMap <Integer, List<Survey>> donde se alojen las encuestas.
	/**
	 * 
	 * 
	 */
	public Poll (){		
		pollID = 1;
		oSurvey = new Survey();
		survey = new ArrayList<String>();				
	}
	//TODO: Crear un constructor donde se le pase el Usuario en caso de que este alojado en la BD para recargar los datos.
	/**
	 * Metodo encargado de aumentar la votacion dada.
	 * @param position posicion donde aumentar la puntuacion de voto.
	 */
	public synchronized void addPollScore(int position,Integer userId){
		oSurvey.increaseScore(position);//Aumentamos la puntuacion en 1 en la posicion dada.		
		if (isOnList(userId)){//Si esta en la lista esta cambiando el voto...
			oSurvey.replaceVote(userId, position);//Cambiamos la posicion en el HashMap.			
		} else{			
			oSurvey.insertUserOnList(userId,position);//Metemos al usuario en la lista de votaciones con la posicion votada.
		}		
	}
	/**
	 * Metodo encargado de anular la votacion dada anteriormente por el usuario.
	 * @param userId id del usuario a tratar.
	 */
	public synchronized void reducePollScore (Integer userId){
		int position = oSurvey.getPollPosition(userId);
		oSurvey.decreaseScore(position);//Reducimos la puntuacion en 1 en la posicion dada.		
	}
	/**
	 * Metodo encargado de retornar la posicion que ha votado el usuario en el map.
	 * @param userId id del usuario que ha votado.
	 * @return Posicion de voto en el teclado.
	 */
	public Integer getPollPosition (Integer userId){		
		Integer positionValue = oSurvey.getPollPosition(userId);
		return positionValue;
	}
	/**
	 * Metodo encargado de comprobar que boton a sido pulsado y retornar su pulsacion.
	 * @param callBackData datos del boton pulsado para la comprobacion.
	 * @return numero con la posicion del boton pulsado.
	 */
	public Integer getPollCallbackPos (String callBackData){
		int pos = 0;
		for (int i = 0; i < oSurvey.getAnswerOptions(); i++){//Recorremos todos los botones para conocer el pulsado.
			if (callBacksData[i].equals(callBackData)){				
				pos = i;//Guardamos la posicion.											
			}
		}
		return pos;
	}	
	/**
	 * Metodo encargado de retornar un numero Integer segun la posicion pulsada en el teclado.
	 * @param callBackData Datos a comprobar de la pulsacion dada por el usuario.
	 * @return Numero Integer del 1 al 4 o null en caso de no ser ninguna de las opciones.
	 */
	public Integer getPrivateKeyboardPos (String callBackData){
		switch (callBackData){
		    case BotConfig.UPDATE_BUTTON:
		    	return 1;		    	
		    case BotConfig.VOTE_BUTTON:
		    	return 2;
		    case BotConfig.CLOSE_BUTTON:
		    	return 3;
		    case BotConfig.DELETE_BUTTON:
		    	return 4;
		}
		return null;
	}
	/**
	 * Metodo encargado de retornar la puntuacion dada segun la posicion.
	 * @param position posicion a aumentar el conteo de puntuacion.
	 * @return puntuacion dada segun la posicion.
	 */
	private int getScore (int position){		
		return oSurvey.getValues(position);
	}
	
	/**
	 * Metodo encargado de asignar la pregunta de la encuesta.
	 * @param question pregunta a tratar.
	 */
	public void setQuestion(String question){				
		this.survey.add(question);
	}
	
	/**
	 * Metodo encargado de asignar las respuestas.
	 * @param answer respuesta a tratar.
	 * @param position posicion del array donde se aloja la respuesta.
	 */
	public void setAnswers (String answer){
		this.survey.add(answer);
	}	
	/**
	 * Metodo encargado de crear la encuesta con los datos enviados por el usuario. 
	 */
	public void createSurvey (){
		final String emojiCry = EmojiParser.parseToUnicode(":cry:");
		final String mark = "0%";		
		ArrayList<String> surveys = new ArrayList<String>();
		oSurvey.setAnswerOptions(survey.size()-1);//No necesitamos la pregunta para el conteo.								
		for (int i = 0; i < survey.size();i++){
			oSurvey.initValues(i);//Marcamos a 0 las puntuaciones.			
			if (i == 0){
				surveys.add(survey.get(i));//Primera pos la pregunta.
				oSurvey.setQuestion(survey.get(i));//Asignamos la pregunta al objeto encuesta.
			} else {//A partir de i=1 son respuestas.
				String emojiWhiteSquare = EmojiParser.parseToUnicode(":white_medium_square:");
				surveys.add(survey.get(i));//Posible respuesta.
				oSurvey.setAnswers(survey.get(i));//Guardamos la pregunta para el teclado posterior.				
				surveys.add(emojiWhiteSquare+"  "+mark);//Marca del porcentaje.EMOJI cuadrado vacio.								
			} 
		}//Posiciones 0 pregunta y 1,3,5... Respuestas. Los 2,4,6... seran las marcas porcentuales.		
		surveys.add("\n"+emojiCry+" No ha respondido nadie todavía.");
		survey.clear();
		survey.addAll(surveys);				
	}
	/**
	 * Metodo encargado de actualizar y devolver la lista de la encuesta con la votacion.
	 * @param position posicion a actualizar.
	 * @return Lista con los datos actualizados.
	 */
	public void updateSurvey (int position){
		ArrayList<String> surveys = new ArrayList<String>();
		survey.remove(survey.size()-1);//Borramos marca final.		
		final String mark = "0%";
		final String percent = "%";
		for (int i =0; i < survey.size(); i ++){//Obviamos la pregunta que no necesita ser actualizada i=1.
			if (i == 0){
				surveys.add(survey.get(i));//Pregunta
			}else{
				if (isOddNumber(i)){//si es impar
					surveys.add(survey.get(i));//respuesta
				} else{ //si es par... tiene que haber marca porcentual.
					if (getScore(i/2-1) == 0){						
						String emojiWhiteSquare = EmojiParser.parseToUnicode(":white_medium_square:");
						surveys.add(emojiWhiteSquare+"  "+mark);//Marca del porcentaje. EMOJI cuadrado vacio.
					} else{
						String emojiThumbsUp = EmojiParser.parseToUnicode(":thumbsup:");
						String thumbsUp ="";
						String finalString = "";
						for (int j = 0; j< getScore(i/2-1);j++){//Ponemos tantos dedos como votos haya.
							thumbsUp = thumbsUp+emojiThumbsUp;
						}
						finalString = thumbsUp + " " +getPercent(getScore(i/2-1))+percent;//Añadimos el porcentaje en todo caso.					
						surveys.add(finalString);//Añadimos a la lista
					}
				}
			}
		}		
		String emojiPeopleVoted = EmojiParser.parseToUnicode(":busts_in_silhouette:");
		surveys.add("\n"+emojiPeopleVoted+" "+oSurvey.getPeopleVoted()+" personas han votado hasta ahora.");
		survey.clear();
		survey.addAll(surveys);
		//3 respuestas 15 votos, 1 => 3 votos = 20%, 2 => 7 votos =46,6% , 3 => 5 = 33,3% votos. % = votos / totalVotado * 100		
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
		for (int i = 0; i< survey.size();i++){
			if (i == 0){
				surveyText = inFlagBold + surveyText + survey.get(i) + endFlagBold + jump + jump;//Doble salto para separar mas la pregunta de las respuestas.
			} else{
				surveyText = surveyText + survey.get(i) + jump;//Recogemos el texto en una variable String y le añadimos los saltos para facilitar lectura.	
			}			
		}		
		return surveyText;
	}
	/**
	 * Metodo encargado de enviar al chat privado del usuario con el bot la encuesta para que el usuario la comparta o controle.
	 * @param chatId id del chat del usuario.
	 * @param textToSend texto a enviar con la encuesta.
	 */
	public SendMessage sendFinishedSurvey (Long chatId, String textToSend){
		SendMessage message = new SendMessage();//Iniciamos mensaje y String.				
		message.setChatId(chatId);
		message.setText(textToSend);
		message.setParseMode(parseMode);//Asignamos al mensaje el parseador html para la negrita.		
    	message.setReplyMarkup(createPrivateKeyboard());//Creamos el teclado personalizado.
    	return message;
	}
	
	/**
	 * Metodo encargado de enviar la encuesta al usuario en un mensaje a parte de la contestacion.
	 * @param chatId Id del chat a donde enviar la encuesta.
	 * @param textToSend texto de la encuesta a enviar.
	 */
	public SendMessage sendSurvey (Long chatId, String textToSend){		
		SendMessage message = new SendMessage();//Iniciamos mensaje y String.				
		message.setChatId(chatId);//ID del chat donde se dirige la encuesta.
		message.setText(textToSend);//Texto a enviar.
		message.setParseMode(parseMode);//Asignamos al mensaje el parseador html para la negrita.		
        message.setReplyMarkup(createKeyboard());//Creamos el teclado.        
		return message;
		
	}
	
	/**
	 * Metodo encargado de actualizar la encuesta y su mensaje segun las votaciones.
	 * @param chatId id del Chat donde se esta utilizando la votacion.
	 * @param textToSend texto de la encuesta a actualizar.
	 */
	public EditMessageText updateMessage (String chatId,String textToSend){
		EditMessageText message = new EditMessageText();		
		message.setInlineMessageId(chatId);//ID del mensaje de la InlineQuery.		
		message.setText(textToSend);//Asignamos texto actualizado
		message.setParseMode(parseMode);//Parseo HTML		
		message.setReplyMarkup(updateKeyboard());//Actualizamos el reply y se lo pasamos.		
		return message;
	}
	
	/**
	 * Metodo encargado de actualizar el mensaje privado enviado al chat del usuario.
	 * @param chatId Id del chat a donde enviar el mensaje.
	 * @param messageId Id del mensaje a actualizar.
	 * @param textToSend Texto a enviar en el mensaje.
	 * @return EditMessageText con el mensaje personalizado.
	 */
	public EditMessageText updatePrivateMessage (Long chatId,Integer messageId, String textToSend){
		EditMessageText message = new EditMessageText();		
		message.setMessageId(messageId);
		message.setChatId(chatId);
		message.setText(textToSend);//Asignamos texto actualizado
		message.setParseMode(parseMode);//Parseo HTML		
		message.setReplyMarkup(createPrivateKeyboard());//Actualizamos el reply y se lo pasamos.		
		return message;
	}
	/**
	 * Metodo encargado de crear el teclado en el chat privado con el usuario.
	 * @return InlineKeyboardMarkup personalizado para controlar la encuesta.
	 */
	private InlineKeyboardMarkup createPrivateKeyboard (){
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
        	} else if (i ==1){
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
	private InlineKeyboardMarkup createKeyboard(){		
		InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
		List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();        
        //callBacksData = new String [answerOptions];//Iniciamos array para almacenar los callBack
        callBacksData = new String [oSurvey.getAnswerOptions()];//Iniciamos array para almacenar los callBack
        for (int i =0; i < oSurvey.getAnswerOptions(); i++){//Crearemos tantos botones como respuestas haya.
        	String callBack = "Option";//Marca de datos.
        	List<InlineKeyboardButton> rowInline = new ArrayList<>();
        	InlineKeyboardButton button = new InlineKeyboardButton();        	
        	button.setText(oSurvey.getAnswer(i));
        	button.setCallbackData(callBack+i);//Y la marca de datos.
        	rowInline.add(button);        	
        	callBacksData[i] = callBack + i;//Guardamos en el array la marca de datos. Option1, Option2, Option 3....
        	rowsInline.add(rowInline);            
        }        
        markupInline.setKeyboard(rowsInline);//Asignamos el teclado y devolvemos.		
		return markupInline;
	}
	
	/**
	 * Metodo encargado de gestionar la actualizacion del teclado al realizar votaciones.
	 * @return InlineKeyboardMarkup teclado con la votacion personalizada.
	 */
	private InlineKeyboardMarkup updateKeyboard (){		
		InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
		List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();   
		for (int i =0; i < oSurvey.getAnswerOptions(); i++){
			String callBack = "Option";//Marca de datos.
			List<InlineKeyboardButton> rowInline = new ArrayList<>();
        	InlineKeyboardButton button = new InlineKeyboardButton();        	        	
        	if (oSurvey.getValues(i) == 0){//Si no hay puntuacion en la posicion...  oSurvey.getValues(i) == 0
        		button.setText(oSurvey.getAnswer(i));
        	} else{//Si hay puntuacion...        		        		
        		button.setText(oSurvey.getAnswer(i)+" - "+oSurvey.getValues(i)+" -");//La ponemos en el teclado.
        	}        	
        	button.setCallbackData(callBack+i);//Marcamos el boton con la marca para conocer el boton pulsado.
        	rowInline.add(button);//Añadimos el boton a la lista.        	
        	callBacksData[i] = callBack + i;
        	rowsInline.add(rowInline);//Añadimos la fila a la lista.
		}
		markupInline.setKeyboard(rowsInline);//Asignamos el teclado y devolvemos.
		return markupInline;
	}
	/**
	 * Metodo que devuelve un objeto InputTextMessageContent con el mensaje de la encuesta.
	 * @return InputTextMessageContent con el mensaje de la encuesta y el parseo HTML.
	 */
	private InputTextMessageContent surveyText(){
		InputTextMessageContent inputText = new InputTextMessageContent();
		inputText.setMessageText(surveyText);//Asignamos el texto de la encuesta.		
		inputText.setParseMode(parseMode);//Parseo HTML.
		return inputText;
	}
	/**
	 * Metodo encargardo de aunar lo necesario en un objeto InlineQueryResultArticle para contestar a la consulta. 
	 * @return InlineQueryResultArticle con los datos de la encuesta.
	 */
	private InlineQueryResultArticle surveyArticle(){
		InlineQueryResultArticle article = new InlineQueryResultArticle();
		article.setInputMessageContent(surveyText());//Asignamos el texto de la encuesta.
		article.setReplyMarkup(createKeyboard());//Asignamos el teclado de la encuesta.
		article.setTitle(oSurvey.getQuestion());//El titulo de la encuesta, que se mostrara en la lista.
		article.setId("Encuesta"+pollID);//Id de la encuesta.
		pollID = pollID + 1;//Aumentamos el contador del Id de la encuesta.
		return article;
	}
	/**
	 * Metodo encargado de contestar a la InlineQuery de la encuesta al compartirla.
	 * @param inlineQuery InlineQuery con los datos de la consulta.
	 * @return AnswerInlineQuery con la encuesta personalizada.
	 */
	public AnswerInlineQuery convertToAnswerInlineQuery (InlineQuery inlineQuery){		
		AnswerInlineQuery answerInlineQuery = new AnswerInlineQuery();
		answerInlineQuery.setInlineQueryId(inlineQuery.getId());//Ponemos id de la consulta.
		answerInlineQuery.setResults(surveyArticle());//Rellenamos la consulta con el resultado.		
		return answerInlineQuery;
	}
	/**
	 * Metodo encargado de gestionar la contestacion a un voto o cambio de voto.
	 * @param callBackId Id de la llamada al boton.
	 * @param text Texto a mostrar en la alerta
	 * @return Contestacion personalizada.
	 */
	public AnswerCallbackQuery replyVote (String callBackId, String text){
		AnswerCallbackQuery acq = new AnswerCallbackQuery();
		acq.setCallbackQueryId(callBackId);
		acq.setShowAlert(true);		
		acq.setText(text);
		return acq;
	}
	/**
	 * Metodo encargado de devolver en un String el porcentaje correspondiente de la votacion.
	 * @param score puntuacion de los votos.
	 * @return String con el porcentaje de votos en la posicion.
	 */
	private String getPercent(int score){		
		double percent = score / (double) oSurvey.getPeopleVoted() * 100;
		DecimalFormat format = new DecimalFormat("0.0");
		String finalPercent = format.format(percent);
		return finalPercent;
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
	 * Metodo encargado de comprobar el Id del usuario dado por parametro en la lista de usuarios.
	 * @param userId Id del usuario a comprobar
	 * @return true si el usuario esta en la lista.
	 */
	public boolean isOnList (Integer userId){
		HashMap <Integer, Integer> userIdPos = oSurvey.getUsersIdPos();
		if (userIdPos.containsKey(userId)){
			return true;
		} else {
			return false;
		}		
	}
	
}
