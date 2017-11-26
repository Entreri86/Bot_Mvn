package services;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import org.telegram.telegrambots.api.objects.inlinequery.inputmessagecontent.InputTextMessageContent;
import org.telegram.telegrambots.api.objects.inlinequery.result.InlineQueryResultArticle;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.logging.BotLogger;
import org.xml.sax.InputSource;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;

import bot.BotConfig;
import bot.PersistentValues;

public class FeedObj {
	private static final String LOGTAG = "FeedObj";
	private static final int MAX_LENGTH_DESCRIPTION = 50;
	private static final String NO_FEED = "NO DESCRIPTION AVAILABLE for FEED";
	private PersistentValues persistentValues = new PersistentValues();;
	private String url;	
	private String description;
	private String feedId;
	private String inline_message_id;
	
	/**
	 * Metodo encargado de convertir la Url dada por parametro en un Feed para despues ser publicado en forma de articulo.
	 * @param url Url de donde extraer los resultados.
	 * @return InlineQueryResultArticle con lo necesario para mostrar la noticia.
	 */
	public InlineQueryResultArticle getFeedArticle (String url) {
		InlineQueryResultArticle podcastArticle = new InlineQueryResultArticle();
		Integer inlineQueryResultArticleId = persistentValues.getIdValue();
		try {
			SyndFeed podcast = getSyndFeedForUrl(url);			
			if (podcast != null) {//Si no es nulo				
				if (podcast.getDescription() != null && !podcast.getDescription().equals("") ) {//Si la desc no esta vacia...
					String aux = podcast.getDescription();
					String descSinTabs = aux.replaceAll("\\<[^>]*>", "");//Removemos marcas...
					if(descSinTabs.length() > MAX_LENGTH_DESCRIPTION) {//Si la descripcion es mas largo de la debida..
						podcastArticle.setDescription(descSinTabs.substring(0, MAX_LENGTH_DESCRIPTION));//Recortamos
						this.description = (descSinTabs.substring(0, MAX_LENGTH_DESCRIPTION));
						String text = podcast.getTitle().concat(" ").concat(descSinTabs.substring(0, MAX_LENGTH_DESCRIPTION)).concat(" ").concat(podcast.getLink());
						podcastArticle.setInputMessageContent(new InputTextMessageContent().setParseMode(BotConfig.PARSE_MODE).setMessageText(text));
					} else {//Sino asignamos tal como esta.
						podcastArticle.setDescription(descSinTabs);
						this.description = descSinTabs;
						String text = podcast.getTitle().concat(" ").concat(descSinTabs).concat(" ").concat(podcast.getLink());
						podcastArticle.setInputMessageContent(new InputTextMessageContent().setParseMode(BotConfig.PARSE_MODE).setMessageText(text));
					}					
				}else {
					podcastArticle.setDescription(NO_FEED);
					podcastArticle.setInputMessageContent(new InputTextMessageContent().setParseMode(BotConfig.PARSE_MODE).setMessageText(NO_FEED));
				}	
				podcastArticle.setId("Feed"+inlineQueryResultArticleId);
				inlineQueryResultArticleId += +1;
				persistentValues.saveIdValue(inlineQueryResultArticleId);//Guardamos el siguiente id a asignar.
				podcastArticle.setTitle(podcast.getTitle());//asignamos el titulo...
				podcastArticle.setUrl(podcast.getLink());//Asignamos la URL...
				InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
				List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
				//Boton
				List<InlineKeyboardButton> rowInline = new ArrayList<>();
		    	InlineKeyboardButton shareButton = new InlineKeyboardButton();
		    	shareButton.setText("Compartir feed.");    	   	
		    	shareButton.setSwitchInlineQuery("");
		    	shareButton.setCallbackData("Compartir_Feed");
		    	rowInline.add(shareButton);
		    	rowsInline.add(rowInline);
				podcastArticle.setReplyMarkup(markupInline);				
				//TODO: Faltaria recoger imagen etc...				
			}
		} catch (IllegalArgumentException | IOException | FeedException e) {
			BotLogger.error(LOGTAG+": Excepcion ocurrida mientras se construye el objeto feed desde la url. ", e);//Guardamos mensaje y lo mostramos en pantalla de la consola.
			e.printStackTrace();
		}		
		return podcastArticle;
	}
	
	/**
	 * Metodo encargado de recoger una URL por parametro y devolver el feed con el contenido de la noticia.
	 * @param url url del feed de noticias.
	 * @return SyndFeed articulo del Feed Rss.
	 * @throws MalformedURLException Si esta malformada la URL.
	 * @throws IOException Problemas con el Stream.
	 * @throws IllegalArgumentException Si hay algun error con los Get, Post.
	 * @throws FeedException Excepcion por fallo en la lectura del feed.
	 */
	private SyndFeed getSyndFeedForUrl(String url) throws MalformedURLException, IOException, IllegalArgumentException, FeedException {		
		SyndFeed feed = null;
		InputStream is = null;
		try {
			URLConnection openConnection = new URL(url).openConnection();//Abrimos conexion.
			is = new URL(url).openConnection().getInputStream();//Recogemos Stream.
			if("gzip".equals(openConnection.getContentEncoding())){//Revisamos Stream por si esta codificado y comprimido.
				is = new GZIPInputStream(is);//Si lo esta abrimos GzipStream
			}
			InputSource source = new InputSource(is);//asignamos el origen
			SyndFeedInput input = new SyndFeedInput();
			feed = input.build(source);//recogemos y asignamos el feed resultante.						
		} catch (Exception e){
			BotLogger.error(LOGTAG+": Excepcion ocurrida mientras se construye el objeto feed desde la url. ", e);//Guardamos mensaje y lo mostramos en pantalla de la consola.
			e.printStackTrace();
		} finally {//Cerramos Stream pase lo que pase...
			if( is != null)	is.close();
		}
		return feed;//Devolvemos el Feed.
	}
	
	/**
	 * 
	 * @return
	 */
	public String getInline_message_id() {
		return inline_message_id;
	}
	/**
	 * 
	 * @param inline_message_id
	 */
	public void setInline_message_id(String inline_message_id) {
		this.inline_message_id = inline_message_id;
	}
	/**
	 * 
	 * @return
	 */
	public String getUrl() {
		return url;
	}
	/**
	 * 
	 * @param url
	 */
	public void setUrl(String url) {
		this.url = url;
	}
	/**
	 * 
	 * @return
	 */
	public String getDescription() {
		return description;
	}
	/**
	 * 
	 * @param description
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	/**
	 * 
	 * @return
	 */
	public String getFeedId() {
		return feedId;
	}
	/**
	 * 
	 * @param feedId
	 */
	public void setFeedId(String feedId) {
		this.feedId = feedId;
	}
}
