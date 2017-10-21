package handlers;


import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.inlinequery.InlineQuery;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.logging.BotLogger;
import com.vdurmont.emoji.EmojiParser;
import bot.BotConfig;
import database.DBManager;
import services.Poll;


public class MNSpain_bot extends TelegramLongPollingBot {
	private static final String LOGTAG = "MNSpain";		
	private Poll poll;	
	private boolean isPolling = false;//Si esta en modo encuesta recogiendo respuestas...
	private boolean haveQuestion = false;//Si hay que controlar la pregunta...
	private boolean sendSurvey = false;//Si se a enviado la encuesta al chat privado...
	private boolean isClosed = false;//Si esta o no cerrada la encuesta...
	private boolean isUpdated = false;//Si se ha actualizado la encuesta previamente en el chat privado...	
	
	public MNSpain_bot() {
		
	}
	/**
	 * Metodo encargado de gestionar y derivar las actualizaciones que le llegan al bot.
	 */
	@Override
	public void onUpdateReceived(Update update) {	
		if (update.hasMessage() && update.getMessage().isCommand()){//Si es un comando...
			handleCommand(update);
		} else if(update.hasMessage() && update.getMessage().hasText()){//Si es un mensaje...
			handleMessage(update);
		} else if (update.hasCallbackQuery()){//Si es una pulsacion de boton de un teclado...
			try {
				handleCallbackQuery(update);
			} catch (TelegramApiException e) {
				BotLogger.error(LOGTAG, e);//Guardamos mensaje y lo mostramos en pantalla de la consola.
				e.printStackTrace();
			}
		} else if (update.hasInlineQuery()){//Si es una consulta inline...
			handleInlineQuery(update);
		}		
	}
	
	/**
	 * Metodo encargado de gestionar los comandos dirijidos por los usuarios al bot.	 
	 * @param update actualizacion de estado de Telegram.
	 */
	private void handleCommand( Update update){		
		String command = update.getMessage().getText();//Comando a tratar.
		SendMessage message= new SendMessage();//Declaramos un mensaje.		
		Long chatId = update.getMessage().getChatId();//Recogemos el id del chat desde donde se interactua con el bot.
		Integer userId = update.getMessage().getFrom().getId();//Y el id del usuario que interactua.
		switch (command){
		case BotConfig.START_COMMAND:			
			if(DBManager.getInstance().isUserOnDb(userId)){//Si el usuario esta en la Base de datos...				
				message.setText(BotConfig.WELCOME_AGAIN_STRING+DBManager.getInstance().getUserFromDb(userId)+ BotConfig.WELCOME_AGAIN_STRING_SECOND);
				  
			} else {//Si el usuario no esta en la BD...
				DBManager.getInstance().insertUserOnDb(update.getMessage().getFrom());//Insertamos el usuario en la BD.
				message.setText(BotConfig.WELCOME_STRING);
			}			
			break;
		case BotConfig.HELP_COMMAND:			
			message.setText(BotConfig.HELP_STRING);
			break;
		case BotConfig.POLL_COMMAND://FIXME: Controlar la segunda pulsacion al comando. Poll = null;
			if (DBManager.getInstance().checkIfHaveSurveys(userId)){//Si el usuario tiene encuestas en la base de datos...
				System.out.println("Tiene encuestas.");
				poll = new Poll(update.getMessage().getFrom(), true);//Le pasamos el usuario con bool true para que recoja los datos de la BD.
			} else { //Si no tiene encuestas declaramos la clase vacia.
				System.out.println("No tiene encuestas.");
				poll = new Poll(update.getMessage().getFrom(), false);//Iniciamos la clase...Con bool false para que no recoja nada de la BD.
			}
			message.setText(BotConfig.POLL_STRING);			
			isPolling = true;//"Encendemos" el modo encuesta.			
			break;
		case BotConfig.POLL_COMMAND_DONE:
			isPolling = false;//Reiniciamos la variable al finalizar el comando.
			haveQuestion = false;//Reiniciamos la variable para la pregunta.
			sendSurvey = true;//Marcamos para enviar la encuesta.				
			message.setText(BotConfig.POLL_DONE_STRING);
			break;		
		}		
		try {			
			message.setChatId(chatId);
			execute(message);//Enviamos el mensaje...            
            if (sendSurvey == true){
            	poll.createSurvey();//Creamos la encuesta 
            	execute(poll.sendFinishedSurvey(chatId, poll.createSurveyString()));//Enviamos encuesta antes de compartir.
            	sendSurvey = false;//Marcamos como no enviada despues de haberlo hecho.
            }
        } catch (TelegramApiException e) {
        	BotLogger.error(LOGTAG, e);//Guardamos mensaje y lo mostramos en pantalla de la consola.
            e.printStackTrace();
        }
	}	
	/**
	 * Metodo encargado de gestionar los mensajes que llegan al bot.
	 * @param update actualizacion de estado.
	 */
	private void handleMessage (Update update){
		Message message = update.getMessage();
		SendMessage sendMessage = new SendMessage();
		Long chatId = update.getMessage().getChatId();
		if (isPolling == true){//Si el comando de la encuesta ha sido pulsado, modo encuesta...			
			if (haveQuestion == false){//Si es falso todavia no se ha asignado la pregunta...
				poll.setQuestion(message.getText());//Asignamos	la pregunta.			
				sendMessage.setChatId(chatId);
				sendMessage.setParseMode(Poll.parseMode);
				sendMessage.setText(BotConfig.POLL_QUESTION_STRING+ message.getText() +BotConfig.POLL_FIRST_ANSWER_STRING);
				haveQuestion = true;//Marcamos que hay pregunta.
			} else {//En este estado tenemos la pregunta, asignamos las respuestas.
				poll.setAnswers(message.getText());				
				sendMessage.setChatId(chatId);
				sendMessage.setText(BotConfig.POLL_ANSWER_STRING);
			}			
		} else if(update.getMessage().getFrom().getId() != null){//Si el id del usuario no es null...
			Integer id = update.getMessage().getFrom().getId();
			if (id == BotConfig.DEV_ID){//Si es mi id...
				sendMessage.setChatId(chatId);
				sendMessage.setText(BotConfig.DEV_WORDS);//Mensaje personalizado...xD
			}	
		} else {//Sino respondemos con el mismo texto enviado por el usuario.
			sendMessage.setChatId(chatId);
			sendMessage.setText(update.getMessage().getText());
		}        		
        try {        	
            execute(sendMessage);//Enviamos mensaje. 
        } catch (TelegramApiException e) {
        	BotLogger.error(LOGTAG, e);//Guardamos mensaje y lo mostramos en pantalla de la consola.
            e.printStackTrace();
        }
	}
	
	/**
	 * Metodo encargado de gestionar las CallBackQueries que puedan llegar al bot.
	 * @param update actualizacion del estado.
	 * @throws TelegramApiException 
	 */
	private void handleCallbackQuery (Update update) throws TelegramApiException{		
		String inlineMsgId = update.getCallbackQuery().getInlineMessageId();//Id del chat.		
		Integer userId = update.getCallbackQuery().getFrom().getId();//Id del usuario a controlar!!!		
		String callBackQueryId = update.getCallbackQuery().getId();//Id de la callbackQuery.		
		String callBackData = update.getCallbackQuery().getData();//Datos del boton pulsado.		
		boolean isOnList = poll.isOnList(userId);//Miramos si el usuario esta en la lista.		
		if (isClosed){//Si la votacion esta cerrada...
			Integer buttonPos = poll.getPrivateKeyboardPos(callBackData);//Comprobamos que sean botones del chat privado..
			if (buttonPos == null){//Si ha pulsado un boton de voto de la votacion estando cerrada...
				String text = "La votación esta cerrada "+EmojiParser.parseToUnicode(":customs:")+".";
				execute(poll.replyVote(callBackQueryId,text));//Mensaje informativo...
			} else{//Si ha pulsado alguno de los 4 botones del chat privado con el bot (el de compartir va a parte).
				String text;//Cadena para las contestaciones.
				Integer messageId;
				Long privateChatId;
				switch (buttonPos){
				    case 1://Boton de actualizar.
				    	messageId = update.getCallbackQuery().getMessage().getMessageId();//Recogemos el id del mensaje del chat.
				    	privateChatId = update.getCallbackQuery().getMessage().getChatId();//Y el id del chat.
				    	if (isUpdated){//Si ya ha sido actualizada...
				    		text = "Resultados actualizados anteriormente! "+ EmojiParser.parseToUnicode(":wink:")+".";
					    	execute(poll.replyVote(callBackQueryId,text));//Mensaje informativo...
				    	} else {
				    		execute(poll.updatePrivateMessage(privateChatId,messageId, poll.createSurveyString()));
				    		text = "Resultados actualizados! "+ EmojiParser.parseToUnicode(":wink:")+".";
					    	execute(poll.replyVote(callBackQueryId,text));//Mensaje informativo...
					    	isUpdated = true;//Reflejamos la actualizacion...
				    	}				    	
				    	break;
				    case 2://Boton de abrir votacion.
				    	isClosed = false;//Cambiamos valor para abrir la votacion.
				    	text = "Votación abierta! "+ EmojiParser.parseToUnicode(":wink:")+".";
				    	execute(poll.replyVote(callBackQueryId,text));//Mensaje informativo...
				    	break;
				    case 3://Boton de cerrar votacion
				    	text = "La votación ya esta cerrada! "+ EmojiParser.parseToUnicode(":customs:")+".";
				    	execute(poll.replyVote(callBackQueryId,text));//Mensaje informativo...
				    	break;
				    case 4://Boton de borrado de votacion.
				    	messageId = update.getCallbackQuery().getMessage().getMessageId();//Recogemos el id del mensaje del chat.
				    	privateChatId = update.getCallbackQuery().getMessage().getChatId();//Y el id del chat.
				    	execute (new DeleteMessage(privateChatId,messageId));//Borramos el mensaje
				    	text = "Votación eliminada! "+ EmojiParser.parseToUnicode(":see_no_evil:")+".";
				    	execute(poll.replyVote(callBackQueryId,text));//Mensaje informativo...
				    	break;
				}
			}
		} else{//Si la votacion esta abierta...
			Integer buttonPos = poll.getPrivateKeyboardPos(callBackData);
			if (buttonPos == null){//Si no ha pulsado ninguno de los botones del chat privado...							
				if (isOnList){//Si esta en la lista de votos...
					try {
						isUpdated = false;//Reflejamos que se puede actualizar la encuesta.
						int pos = poll.getPollCallbackPos(callBackData);//Recogemos la posicion del boton pulsado.
						int prevPos = poll.getPollPosition(userId);//Recogemos la posicion que voto anteriormente.
						if (pos == prevPos){//Ya ha votado en esa posicion.
							String text = "Ya has votado en esta posición "+ EmojiParser.parseToUnicode(":tired_face:")+".";
							execute(poll.replyVote(callBackQueryId,text));//Mensaje informativo...
						} else {//Esta cambiando el voto.
							poll.reducePollScore(userId);//Reducimos el voto introducido anteriormente.
							poll.addPollScore(pos, userId);//Ponemos el voto en la posicion nueva.
							poll.updateSurvey();//Actualizamos la encuesta
							String text = "Cambio de voto registrado correctamente "+EmojiParser.parseToUnicode(":wink:")+".";
							execute(poll.replyVote(callBackQueryId, text));//Mensaje informativo...
							execute(poll.updateMessage(inlineMsgId, poll.createSurveyString()));//Actualizamos la encuesta.
						}				
					} catch (TelegramApiException e) {
						BotLogger.error(LOGTAG, e);//Guardamos mensaje y lo mostramos en pantalla de la consola.
						e.printStackTrace();
					}
				} else {//Si no ha votado todavia...
					isUpdated = false;//Reflejamos que se puede actualizar la encuesta.
					int pos = poll.getPollCallbackPos(callBackData);//Recogemos la posicion del boton pulsado.			
					poll.addPollScore(pos,userId);//Aumentamos la puntuacion en la posicion dada.
					try {
						String text = "Voto registrado correctamente "+EmojiParser.parseToUnicode(":wink:")+".";
						poll.updateSurvey();//Actualizamos la lista
						execute(poll.replyVote(callBackQueryId,text));//Mensaje informativo...
						execute(poll.updateMessage(inlineMsgId, poll.createSurveyString()));//Actualizamos la encuesta.						
					} catch (TelegramApiException e) {
						BotLogger.error(LOGTAG, e);//Guardamos mensaje y lo mostramos en pantalla de la consola.
						e.printStackTrace();
					}	
				}		
			} else{//Si ha pulsado algun boton del chat privado estando abierta la votacion...
				String text;//Cadena para las contestaciones.
				Integer messageId;
				Long privateChatId;
				switch (buttonPos){
				    case 1://Boton de actualizar.				 			    	
				    	messageId = update.getCallbackQuery().getMessage().getMessageId();//Recogemos el id del mensaje del chat.
				    	privateChatId = update.getCallbackQuery().getMessage().getChatId();//Y el id del chat.
				    	if (isUpdated){
				    		text = "Resultados actualizados anteriormente! "+ EmojiParser.parseToUnicode(":wink:")+".";
					    	execute(poll.replyVote(callBackQueryId,text));//Mensaje informativo...
				    	} else {
				    		execute(poll.updatePrivateMessage(privateChatId,messageId, poll.createSurveyString()));//AQUI PUEDE FALLAR
				    		text = "Resultados actualizados! "+ EmojiParser.parseToUnicode(":wink:")+".";
					    	execute(poll.replyVote(callBackQueryId,text));//Mensaje informativo...
					    	isUpdated = true;//Reflejamos la actualizacion...
				    	}				    	
				    	break;
				    case 2://Boton de abrir votacion.				    	
				    	text = "La votación ya esta abierta! "+ EmojiParser.parseToUnicode(":wink:")+".";
				    	execute(poll.replyVote(callBackQueryId,text));//Mensaje informativo...
				    	break;
				    case 3://Boton de cerrar votacion
				    	isClosed = true;//Cerramos la votacion.
				    	text = "Votación cerrada! "+ EmojiParser.parseToUnicode(":customs:")+".";
				    	execute(poll.replyVote(callBackQueryId,text));//Mensaje informativo...
				    	break;
				    case 4://Boton de borrado de votacion.
				    	messageId = update.getCallbackQuery().getMessage().getMessageId();//Recogemos el id del mensaje del chat.
				    	privateChatId = update.getCallbackQuery().getMessage().getChatId();//Y el id del chat.
				    	execute (new DeleteMessage(privateChatId,messageId));
				    	text = "Votación eliminada! "+ EmojiParser.parseToUnicode(":see_no_evil:")+".";
				    	execute(poll.replyVote(callBackQueryId,text));//Mensaje informativo...
				    	break;
				}
			}
		}		
	}
	/**
	 * Metodo encargado de gestionar las InlineQueries.
	 * @param update actualizacion del estado.
	 */
	private void handleInlineQuery (Update update){
		InlineQuery query = update.getInlineQuery();
		Integer userId = update.getInlineQuery().getFrom().getId();//Y el id del usuario que interactua.
		try {
			execute(poll.convertToAnswerInlineQuery(userId, query));//Contestamos a la inlineQuery compartiendo la encuesta.				
		} catch (TelegramApiException e) {
			BotLogger.error(LOGTAG, e);//Guardamos mensaje y lo mostramos en pantalla de la consola.
			e.printStackTrace();
		}
	}
	/**
	 * Metodo que devuelve el nombre del bot dado a Botfather.
	 */
	@Override
	public String getBotUsername() {		
		return BotConfig.BOT_USER_NAME;
	}

	/**
	 * Metodo que devuelve el token asignado por Botfather.
	 */
	@Override
	public String getBotToken() {		
		return BotConfig.BOT_TOKEN;
	}	
}
