package handlers;


import java.util.HashMap;

import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.api.objects.inlinequery.InlineQuery;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.logging.BotLogger;
import com.vdurmont.emoji.EmojiParser;
import bot.BotConfig;
import bot.CustomUser;
import bot.PasswdTool;
import bot.PasswdTool.HashSalt;
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
	private boolean isLogin = false;
	private boolean havePasswd = false;
	private String userPasswd;
	private HashMap<Integer, Poll> pollMap;
	private HashMap<Integer, CustomUser> usersMap;
	
	/**
	 * Constructor por defecto.
	 */
	public MNSpain_bot () {
		
	}
	/**
	 * Constructor encargado de asignar las opciones por defecto del bot.
	 * @param options
	 */
	public MNSpain_bot(DefaultBotOptions options) {
		super(options);//Le pasamos las opciones para disponer de 30 hilos escuchando (el maximo de la Api de telegram)
	}
	/**
	 * Metodo encargado de gestionar y derivar las actualizaciones que le llegan al bot.
	 */
	@Override
	public void onUpdateReceived(Update update) {
		
		if (registerUser(update)) {
			
		} 
		
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
	private void handleCommand(Update update){		
		String command = update.getMessage().getText();//Comando a tratar.
		SendMessage message= new SendMessage();//Declaramos un mensaje.		
		Long chatId = update.getMessage().getChatId();//Recogemos el id del chat desde donde se interactua con el bot.
		Integer userId = update.getMessage().getFrom().getId();//Y el id del usuario que interactua.
		switch (command){
		case BotConfig.START_COMMAND:
			if (isLogin) {//Si ya ha entrado el pass...
				message.setText(BotConfig.ALREADY_LOGIN_STRING + BotConfig.WELCOME_STRING);
			} else {
				if (havePasswd) {
					message.setText(BotConfig.PASSWD_REQ_STRING);
				} else {
					if (DBManager.getInstance().isUserOnDb(update.getMessage().getFrom().getId())) {//Si esta en la BD...
						message.setText(BotConfig.WELCOME_AGAIN_STRING);
						havePasswd = true; //Marcamos que tiene la contraseña.
					} else message.setText(BotConfig.CREATE_PASSWD_STRING);
				}				
			}						
			break;
		case BotConfig.HELP_COMMAND:
			if (isLogin) {
				message.setText(BotConfig.HELP_STRING);
			} else {
				message.setText(BotConfig.DENNIED_PERMISSIONS_STRING);
			}			
			break;
		case BotConfig.POLL_COMMAND://FIXME: Controlar la segunda pulsacion al comando. Poll = null;
			if (isLogin) {
				if (DBManager.getInstance().checkIfHaveSurveys(userId)){//Si el usuario tiene encuestas en la base de datos...
					System.out.println("Tiene encuestas.");
					poll = new Poll(update.getMessage().getFrom(), true);//Le pasamos el usuario con bool true para que recoja los datos de la BD.
				} else { //Si no tiene encuestas declaramos la clase vacia.
					System.out.println("No tiene encuestas.");
					poll = new Poll(update.getMessage().getFrom(), false);//Iniciamos la clase...Con bool false para que no recoja nada de la BD.
				}
				message.setText(BotConfig.POLL_STRING);			
				isPolling = true;//"Encendemos" el modo encuesta.	
			} else {
				message.setText(BotConfig.DENNIED_PERMISSIONS_STRING);
			}
			break;
		case BotConfig.POLL_COMMAND_DONE:
			if (isLogin) {
				isPolling = false;//Reiniciamos la variable al finalizar el comando.
				haveQuestion = false;//Reiniciamos la variable para la pregunta.
				sendSurvey = true;//Marcamos para enviar la encuesta.				
				message.setText(BotConfig.POLL_DONE_STRING);
			} else {
				message.setText(BotConfig.DENNIED_PERMISSIONS_STRING);
			}
			
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
				sendMessage.setParseMode(Poll.parseMode);
				sendMessage.setText(BotConfig.POLL_QUESTION_STRING+ message.getText() +BotConfig.POLL_FIRST_ANSWER_STRING);
				haveQuestion = true;//Marcamos que hay pregunta.
			} else {//En este estado tenemos la pregunta, asignamos las respuestas.
				poll.setAnswers(message.getText());								
				sendMessage.setText(BotConfig.POLL_ANSWER_STRING);
			}			
		} else if (!isLogin) {			
			if (!havePasswd) {
				if(message.getText().equalsIgnoreCase("Si")) {//Afirma que la contraseña es valida.
					sendMessage.setText(BotConfig.PASSWD_CREATED_STRING);
					havePasswd = true;//Marcamos que dispone de contraseña.
					HashSalt hs;
					try {//Creamos la contraseña cifrada y la insertamos en la base de datos.
						hs = PasswdTool.getHash(userPasswd);
						DBManager.getInstance().insertUserOnDb(update.getMessage().getFrom(), hs.getHash(), hs.getSalt());//Insertamos el usuario en la BD.
					} catch (Exception e) {
						havePasswd = false;//Volvemos a marcar para volver a la contraseña...
						BotLogger.error(LOGTAG, e);//Guardamos mensaje y lo mostramos en pantalla de la consola.
						e.printStackTrace();
					}					
				} else if (message.getText().equalsIgnoreCase("No")) {//Cambio de contraseña.
					sendMessage.setText(BotConfig.CREATE_PASSWD_STRING);
				} else {//Debe de ser la contraseña.
					userPasswd = message.getText();
					sendMessage.setText(BotConfig.CONFIRM_PASSWD_STRING + userPasswd + " ?.");
				}
			} else {//Si tiene pass.
				userPasswd = message.getText();
				String [] hashSalt = DBManager.getInstance().checkUserCredential(message.getFrom().getId());
				boolean ok = PasswdTool.ValidatePass(userPasswd, hashSalt[0], hashSalt[1]);
				if (ok) {
					sendMessage.setText(BotConfig.LOGIN_OK_STRING);
					isLogin = true;//Marcamos el login como iniciado.
				} else {
					sendMessage.setText(BotConfig.LOGIN_NOOK_STRING);
				}				
			}
			
		} else if(update.getMessage().getFrom().getId() != null){//Si el id del usuario no es null...
			Integer id = update.getMessage().getFrom().getId();
			if (id == BotConfig.DEV_ID){//Si es mi id...				
				sendMessage.setText(BotConfig.DEV_WORDS);//Mensaje personalizado...xD
			}	
		} else {//Sino respondemos con el mismo texto enviado por el usuario.			
			sendMessage.setText(update.getMessage().getText());
		}  
        try {        	
        	sendMessage.setChatId(chatId);
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
				    	//FIXME: Añadir borrado de la base de datos sobre la encuesta InlineMssgID!!!
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
		System.out.println("Id de la inlineQuery: "+ query.getId());
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
	
	private boolean registerUser (Update update) {
		SendMessage message = new SendMessage();
		User user = update.getMessage().getFrom();
		Long chatId = update.getMessage().getChatId();//Recogemos el id del chat desde donde se interactua con el bot.
		boolean state = false;
		if (pollMap.containsKey(user.getId())) {//El login se ha realizado anteriormente.			
			state = true;
			return state;
		} else {//Si no esta en el hashmap puede ser que el bot se haya caido, miramos en la BD.
			if (!DBManager.getInstance().isUserOnDb(user.getId())) {//Si no esta en la base de datos...
				CustomUser customUser = usersMap.get(user.getId());
				if (customUser == null) {//Si es null todavia no ha entrado la contraseña...
					message.setText(BotConfig.CREATE_PASSWD_STRING);
					usersMap.put(user.getId(), new CustomUser());
					state = false;					
				} else {// Si no es null deberia de estar enviando la contraseña...
					if (customUser.isHavePasswd() == false) {
						if (update.getMessage().getText().equalsIgnoreCase("Si")) {//Afirma que la contraseña es valida...
							message.setText(BotConfig.PASSWD_CREATED_STRING);							
							HashSalt hs;
							try {//Creamos la contraseña cifrada y la insertamos en la base de datos.
								customUser.setHavePasswd(true);//Marcamos que dispone de contraseña.
								hs = PasswdTool.getHash(userPasswd);
								DBManager.getInstance().insertUserOnDb(update.getMessage().getFrom(), hs.getHash(), hs.getSalt());//Insertamos el usuario en la BD.
							} catch (Exception e) {								
								BotLogger.error(LOGTAG, e);//Guardamos mensaje y lo mostramos en pantalla de la consola.
								e.printStackTrace();
							}
							usersMap.replace(user.getId(), customUser);
							state = false;							
							/**
							 * Si llega hasta aqui significa que la contraseña coincide, y que se ha podido insertar el usuario
							 * dentro de la base de datos, ahora se procedera al Login para comprobar la contraseña.
							 */							
						} else if (update.getMessage().getText().equalsIgnoreCase("No")) {
							message.setText(BotConfig.CREATE_PASSWD_STRING);
							state = false;							
						} else {//Tiene que ser la contraseña...
							userPasswd = message.getText();
							message.setText(BotConfig.CONFIRM_PASSWD_STRING + userPasswd + " ?.");
							state = false;							
						}
					} else if (customUser.isHavePasswd() == true) {//Tiene contraseña, esta en la BD, no deberia de llegar pero...
						userPasswd = message.getText();
						String [] hashSalt = DBManager.getInstance().checkUserCredential(user.getId());
						boolean ok = PasswdTool.ValidatePass(userPasswd, hashSalt[0], hashSalt[1]);
						if (ok) {//Si la contraseña coincide....
							message.setText(BotConfig.LOGIN_OK_STRING);
							customUser.setLogin(true);//Marcamos el login como iniciado.
							usersMap.replace(user.getId(), customUser);//Sustituimos el usuario con el login a Yes
							state = true;							
						} else {
							message.setText(BotConfig.LOGIN_NOOK_STRING);
							state = false;							
						}
					}
				}				
			} else {//Esta en la base de datos pero no se ha logueado todavía.
				CustomUser custUser = usersMap.get(user.getId());
				if (custUser == null) {//Todavía no ha entrado la contraseña...
					message.setText(BotConfig.WELCOME_AGAIN_STRING);
					usersMap.put(user.getId(), new CustomUser());
					state = false;
				} else {//Si no es null tiene que estar enviando la contraseña...
					if (custUser.isLogin()== false) {						
						userPasswd = message.getText();
						String [] hashSalt = DBManager.getInstance().checkUserCredential(user.getId());
						boolean ok = PasswdTool.ValidatePass(userPasswd, hashSalt[0], hashSalt[1]);
						if (ok) {//Si la contraseña coincide....
							message.setText(BotConfig.LOGIN_OK_STRING);
							custUser.setLogin(true);//Marcamos el login como iniciado.
							usersMap.replace(user.getId(), custUser);//Sustituimos el usuario con el login a Yes
							state = true;							
						} else {
							message.setText(BotConfig.LOGIN_NOOK_STRING);
							state = false;							
						}						
					} else if (custUser.isLogin()== true) {//Esta logeado.
						state = true;						
					}
				}
			}			
		}
		try {			
			message.setChatId(chatId);
			execute(message);//Enviamos el mensaje...                        
        } catch (TelegramApiException e) {
        	BotLogger.error(LOGTAG, e);//Guardamos mensaje y lo mostramos en pantalla de la consola.
            e.printStackTrace();
        }			
		return state;
	}
	
}
