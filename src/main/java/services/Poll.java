package services;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.telegram.telegrambots.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.api.methods.AnswerInlineQuery;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.api.objects.inlinequery.InlineQuery;
import org.telegram.telegrambots.api.objects.inlinequery.inputmessagecontent.InputTextMessageContent;
import org.telegram.telegrambots.api.objects.inlinequery.result.InlineQueryResult;
import org.telegram.telegrambots.api.objects.inlinequery.result.InlineQueryResultArticle;
import bot.BotConfig;
import database.DBManager;
import bot.PersistentValues;


public class Poll {	
	private User user;
	private boolean isInBd;//Bool para controlar si se ha insertado en la BD la encuesta.
	private boolean isBackingUp;
	public static final String parseMode = "HTML";//Parseo HTML	
	private Survey oSurvey;//Objeto que representa una encuesta unica.
	private HashMap <Integer, List<Survey>> userSurveyList;//HashMap con lista de encuestas por usuario.
	private HashMap <Integer, List<InlineQueryResult>> userSurveyResultArticlelist;//HashMap con lista de articulos por usuario.
	private PersistentValues persistentValues;
	/**
	 * 
	 * 
	 */
	public Poll (){				
		oSurvey = new Survey();		
		userSurveyList = new HashMap<>();
		//List <Survey> surveyList = new ArrayList <Survey>();
		userSurveyResultArticlelist = new HashMap<>();		
	}
	/**
	 * 
	 * @param user
	 */
	public Poll (User user, boolean bool){
		this.user = user;
		isInBd = false;//TODO: Puede dar problemas con la BD si ya esta adentro, PROVISIONAL!!!
		isBackingUp = false;
		persistentValues = new PersistentValues();		
		if (bool){//Si true, esta en la bd recogemos datos...			
			userSurveyList = new HashMap<>();//Declaramos HashMap de lista de Encuestas por usuario.
			ArrayList <Survey> surveysList = DBManager.getInstance().getSurveysFromDb(user.getId());//Creamos una lista recogiendo los datos de la BD.			
			userSurveyList.put(user.getId(), surveysList);//La añadimos al HashMap.			
			oSurvey = new Survey();//Declaramos clase nueva.
			userSurveyResultArticlelist = new HashMap<>();//Declaramos HashMap con lista de articulos (encuestas) que compartir por el server de telegram.
			List <InlineQueryResult> list = convertToResultArticle(surveysList);//Convertimos la lista de Survey en lista de articulos para compartir.
			userSurveyResultArticlelist.put(user.getId(), list);//Añadimos la lista al HashMap.
		} else {//en caso contrario creamos listas nuevas.
			userSurveyList = new HashMap<>();//Declaramos HashMap de lista de Encuestas por usuario.
			List <Survey> surveysList = new ArrayList<>();//Creamos un nuevo ArrayList para alojar en el HashMap las Encuestas.
			userSurveyList.put(user.getId(), surveysList);//La añadimos al HashMap.
			oSurvey = new Survey();//Declaramos clase nueva.
			userSurveyResultArticlelist = new HashMap<>();//Declaramos HashMap con lista de articulos (encuestas) que compartir por el server de telegram.
			List <InlineQueryResult> list = new ArrayList<>();//Creamos un nuevo ArrayList para alojar en el HashMap las Encuestas en formato InlineQueryResultArticle...
			userSurveyResultArticlelist.put(user.getId(), list);//Añadimos la lista al HashMap.
		}			
	}
	
	/**
	 * Metodo encargado de aumentar la votacion dada.
	 * @param position posicion donde aumentar la puntuacion de voto.
	 */
	public synchronized void addPollScore(int position, Integer userId){
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
		return this.oSurvey.getPollCallbackPos(callBackData);
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
	 * Metodo encargado de asignar la pregunta de la encuesta.
	 * @param question pregunta a tratar.
	 */
	public void setQuestion(String question){						
		this.oSurvey.setQuestion(question);//Asignamos la pregunta a la encuesta.
	}
	
	/**
	 * Metodo encargado de asignar las respuestas.
	 * @param answer respuesta a tratar.
	 * @param position posicion del array donde se aloja la respuesta.
	 */
	public void setAnswers (String answer){		
		this.oSurvey.setAnswers(answer);
	}	
	/**
	 * Metodo encargado de crear la encuesta con los datos enviados por el usuario. 
	 */
	public void createSurvey (){		
		this.oSurvey.createSurvey();				
	}
	/**
	 * Metodo encargado de actualizar y devolver la lista de la encuesta con la votacion.
	 * @param position posicion a actualizar.
	 * @return Lista con los datos actualizados.
	 */
	public void updateSurvey (){		
		this.oSurvey.updateSurvey();	
	}
	/**
	 * Metodo encargado de convertir el ArrayList de la encuesta en un String personalizado.
	 * @param survey encuesta a convertir.
	 * @return texto personalizado.
	 */
	public String createSurveyString (){		
		return this.oSurvey.createSurveyString();
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
    	message.setReplyMarkup(this.oSurvey.createPrivateKeyboard());//Creamos el teclado personalizado.
    	return message;
	}	
	/**
	 * Metodo encargado de enviar la encuesta al usuario en un mensaje a parte de la contestacion.
	 * @param chatId Id del chat a donde enviar la encuesta.
	 * @param textToSend texto de la encuesta a enviar.
	 */
	public SendMessage sendSurvey (Long chatId, String textToSend){		
		SendMessage message = new SendMessage();//Iniciamos mensaje.				
		message.setChatId(chatId);//ID del chat donde se dirige la encuesta.
		message.setText(textToSend);//Texto a enviar.
		message.setParseMode(parseMode);//Asignamos al mensaje el parseador html para la negrita.		
        message.setReplyMarkup(this.oSurvey.createKeyboard());//Creamos el teclado.        
		return message;		
	}	
	/**
	 * Metodo encargado de actualizar la encuesta y su mensaje segun las votaciones.
	 * @param inlineMsgId id del mensaje donde se esta utilizando la votacion.
	 * @param textToSend texto de la encuesta a actualizar.
	 */
	public EditMessageText updateMessage (String inlineMsgId, String textToSend){
		EditMessageText message = new EditMessageText();		
		message.setInlineMessageId(inlineMsgId);//ID del mensaje de la InlineQuery del chat donde se esta votando.
		this.oSurvey.setInlineMsgId(inlineMsgId);//Fijamos el Id del mensaje por si se necesita en usos posteriores (restaurar BD etc)
		message.setText(textToSend);//Asignamos texto actualizado
		message.setParseMode(parseMode);//Parseo HTML		
		message.setReplyMarkup(this.oSurvey.updateKeyboard());//Actualizamos el reply y se lo pasamos.		
		return message;
	}	
	/**
	 * Metodo encargado de actualizar el mensaje privado enviado al chat del usuario.
	 * @param chatId Id del chat a donde enviar el mensaje.
	 * @param messageId Id del mensaje a actualizar.
	 * @param textToSend Texto a enviar en el mensaje.
	 * @return EditMessageText con el mensaje personalizado.
	 */
	public EditMessageText updatePrivateMessage (Long chatId, Integer messageId, String textToSend){
		EditMessageText message = new EditMessageText();		
		message.setMessageId(messageId);//Id del mensaje privado del chat con el usuario que creo la encuesta.
		message.setChatId(chatId);//Id del chat privado con el usuario del bot.
		message.setText(textToSend);//Asignamos texto actualizado
		message.setParseMode(parseMode);//Parseo HTML		
		message.setReplyMarkup(this.oSurvey.createPrivateKeyboard());//Actualizamos el reply y se lo pasamos.		
		return message;
	}	
	/**
	 * Metodo que devuelve un objeto InputTextMessageContent con el mensaje de la encuesta.
	 * @return InputTextMessageContent con el mensaje de la encuesta y el parseo HTML.
	 */
	private InputTextMessageContent surveyText(){
		InputTextMessageContent inputText = new InputTextMessageContent();
		inputText.setMessageText(this.oSurvey.getSurveyText());//Asignamos el texto de la encuesta.		
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
		article.setReplyMarkup(this.oSurvey.createKeyboard());//Asignamos el teclado de la encuesta.
		article.setTitle(oSurvey.getQuestion());//El titulo de la encuesta, que se mostrara en la lista.
		Integer inlineQueryResultArticleId = persistentValues.getIdValue();//Recogemos del fichero el id que toca.
		this.oSurvey.setInlineQueryResultArticleId("Encuesta"+inlineQueryResultArticleId);//Guardamos el Id de la encuesta en la clase por si hay que restaurar.
		article.setId("Encuesta"+inlineQueryResultArticleId);//Id de la encuesta.
		inlineQueryResultArticleId += 1;//Aumentamos el contador del Id de la encuesta.
		persistentValues.saveIdValue(inlineQueryResultArticleId);//Guardamos el siguiente id a asignar.
		return article;
	}
	/**
	 * Metodo encargado de contestar a la InlineQuery de la encuesta al compartirla.
	 * @param inlineQuery InlineQuery con los datos de la consulta.
	 * @return AnswerInlineQuery con la encuesta personalizada.
	 */
	public AnswerInlineQuery convertToAnswerInlineQuery (Integer userId, InlineQuery inlineQuery){		
		AnswerInlineQuery answerInlineQuery = new AnswerInlineQuery();
		answerInlineQuery.setInlineQueryId(inlineQuery.getId());//Ponemos id de la consulta.
		List <InlineQueryResult> list = userSurveyResultArticlelist.get(userId);//Recogemos lista de articuloes
		InlineQueryResultArticle article = surveyArticle();//Creamos articulo nuevo.
		list.add(article);//Añadimos a la lista
		userSurveyResultArticlelist.put(userId, list);//Y la lista al HashMap del usuario.
		answerInlineQuery.setResults(list);//Rellenamos la consulta con el resultado.		
		if (isInBd){//Si ya esta en la BD la encuesta....
			if (!isBackingUp){//Si no se ha iniciado la tarea del BackUp...
				startBackUp();//Empezamos a hacer el backUp
				isBackingUp = true;//Marcamos para que no abra mas hilos que uno (por si comparte la misma encuesta varias veces.
			}			
		} else {//Si no lo esta...
			DBManager.getInstance().insertSurvey(userId, oSurvey);//La insertamos en la BD.
			isInBd = true;//Reflejamos que ya esta en la bd.
		}		
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
		acq.setText(text);
		return acq;
	}	
	/**
	 * Metodo encargado de transformar una lista de objetos Survey en objetos InlineQueryResultArticle
	 * para compartirla despues con el boton del chat privado.
	 * @param surveysList Lista de objetos Survey.
	 * @return List de objetos InlineQueryResultArticle.
	 */
	private List<InlineQueryResult> convertToResultArticle(List <Survey> surveysList){
		List <InlineQueryResult> articlesList = new ArrayList <InlineQueryResult>();
		for(Survey survey : surveysList){//Por cada encuesta de la lista...
			InputTextMessageContent message = new InputTextMessageContent();//Creamos un contenido.
			message.setMessageText(survey.getSurveyText());//Asignamos el texto de la encuesta.
			message.setParseMode(parseMode);//Parseo HTML
			InlineQueryResultArticle article = new InlineQueryResultArticle();//Creamos un articulo
			article.setInputMessageContent(message);//Asignamos contenido.
			article.setReplyMarkup(survey.createKeyboard());//Creamos el teclado.
			article.setTitle(survey.getQuestion());//Asignamos de titulo la pregunta.
			article.setId(survey.getInlineQueryResultArticleId());//Recogemos el id.
			articlesList.add(article);//Añadimos el articulo a la lista.
		}			
		return articlesList;//Y retornamos la lista.
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
	/**
	 * Metodo que crea un nuevo hilo por cada objeto Survey creado para mantener actualizada la encuesta y sus resultados
	 * en la base de datos.
	 */
	private void startBackUp (){//FIXME: Objeto service de clase para detenerlo??
		BackUpSurvey command = new BackUpSurvey();//Creamos el "comando" que realizara el BackUp 
		ScheduledExecutorService service = Executors.newScheduledThreadPool(1);//Con un solo hilo bastaria.
		System.out.println("Se esta respaldando en hilo secundario...");
		service.scheduleAtFixedRate(command, 1, 3, TimeUnit.HOURS);//Que cada tres horas haga un backUp con un retardo de 1 hora el inicial.		
	}
	/**
	 * Clase privada que implementa Runnable y se encarga de la tarea de actualizacion de valores de la tabla SurveysTable.
	 * @author otalf
	 *
	 */
	private final class BackUpSurvey implements Runnable {
	   /**
	    * Metodo encargado de ejecutar la actualizacion de la tabla SurveysTable en la Base de datos.
	    */
	   @Override	   
       public void run() {		   
		   DBManager.getInstance().updateSurvey(user.getId(), oSurvey);//Actualizamos la encuesta.
       }
	}
	
	
}
