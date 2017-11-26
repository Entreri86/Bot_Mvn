package services;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.telegram.telegrambots.api.methods.AnswerInlineQuery;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.api.objects.inlinequery.InlineQuery;
import org.telegram.telegrambots.api.objects.inlinequery.result.InlineQueryResultArticle;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import bot.BotConfig;

public class FeedManager {

	private HashMap <Integer, HashMap<Integer,FeedObj>> feedMap;
	
	/**
	 * Constructor por defecto de clase.
	 */
	public FeedManager () {
		
	}
	
	/**
	 * Constructor que segun el parametro entrado recoge o no los valores de la base de datos.
	 * @param user usuario a comprobar.
	 * @param getBackup en caso de true recoger los valores de la base de datos, en caso de false declarar sin recoger nada de la BD.
	 */
	public FeedManager (User user, boolean getBackup) {
		if (getBackup) {
			
		} else {
			feedMap = new HashMap<>();
			HashMap <Integer,FeedObj> feedList = new HashMap <>();
			feedList.put(user.getId(), new FeedObj());
			feedMap.put(user.getId(), feedList);			
		}
	}
	/**
	 * Metodo encargado de compartir el feed con otros canales.
	 * @param userId identificador del usuario.
	 * @param inlineQuery inlineQuery a contestar.
	 * @param Url Url de donde extraer los feeds.
	 * @return AnswerInlineQuery con todo lo necesario para ver la noticia.
	 */
	public AnswerInlineQuery shareFeed (Integer userId, InlineQuery inlineQuery, String Url){
		HashMap <Integer,FeedObj> hashFeed = feedMap.get(userId);
		FeedObj obj = hashFeed.get(userId);
		AnswerInlineQuery answerInlineQuery = new AnswerInlineQuery();
		InlineQueryResultArticle article = obj.getFeedArticle(Url);
		hashFeed.replace(userId, obj);
		feedMap.replace(userId, hashFeed);		
		answerInlineQuery.setResults(article);
		answerInlineQuery.setInlineQueryId(inlineQuery.getId());		
		return answerInlineQuery;
	}
	
	/**
	 * Metodo encargado de enviar el boton para compartir el feed con otros chats.
	 * @param chatId identificador del chat a donde enviar el boton.
	 * @return SendMessage con todo lo necesario para el usuario.
	 */
	public SendMessage sendFeed (Long chatId,String textToSend) {
		SendMessage message = new SendMessage();
		InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
		List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
		//Boton
		List<InlineKeyboardButton> rowInline = new ArrayList<>();		
    	InlineKeyboardButton shareButton = new InlineKeyboardButton();
    	shareButton.setText("Compartir feed.");    	   	
    	shareButton.setSwitchInlineQuery("");    	
    	rowInline.add(shareButton);
    	rowsInline.add(rowInline);
    	markupInline.setKeyboard(rowsInline);    	
		message.setReplyMarkup(markupInline);
		message.setChatId(chatId);
		message.setParseMode(BotConfig.PARSE_MODE);
		message.setText(textToSend);
		return message;
	}
	/**
	 * Metodo encargado de asignar la Url para el objeto Feed.
	 * @param Url Url con el index Xml para el feed.
	 * @param userId identificador del usuario para obtener el objeto Feed.
	 */
	public void setURL (String Url,Integer userId) {
		HashMap <Integer,FeedObj> hashFeed = feedMap.get(userId);
		FeedObj obj = hashFeed.get(userId);
		obj.setUrl(Url);
		hashFeed.replace(userId, obj);
		feedMap.replace(userId, hashFeed);
	}
	/**
	 * Metodo encargado de recuperar la Url del Feed.
	 * @param userId Identificador del usuario para el feed.
	 * @return Url del index Xml del feed.
	 */
	public String getURL (Integer userId) {
		HashMap <Integer,FeedObj> hashFeed = feedMap.get(userId);
		FeedObj obj = hashFeed.get(userId);
		String url = obj.getUrl();
		return url;
	}
	
}
