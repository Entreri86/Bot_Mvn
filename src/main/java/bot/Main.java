package bot;


import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;

import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.logging.BotLogger;
import org.telegram.telegrambots.logging.BotsFileHandler;

import handlers.MNSpain_bot;

public class Main {
	private static final String LOGTAG = "MAIN";

	public static void main(String[] args) {
		
		 //Iniciamos el Log para controlar los errores que puedan aparecer.	
		 BotLogger.setLevel(Level.ALL);
		 BotLogger.registerLogger(new ConsoleHandler());
		 try {
			 BotLogger.registerLogger(new BotsFileHandler());
		 } catch (IOException ioe){
			 BotLogger.severe(LOGTAG, ioe);
		 }		 		 
		 try {
			 ApiContextInitializer.init();
			 TelegramBotsApi botsApi = new TelegramBotsApi();			 
			 try {
		            botsApi.registerBot(new MNSpain_bot());
		     } catch (TelegramApiException e) {
		    	 	BotLogger.error(LOGTAG, e);//Guardamos mensaje y lo mostramos en pantalla de la consola.
		            e.printStackTrace();
		     }
		 } catch (Exception e){
			 BotLogger.error(LOGTAG, e);//Guardamos mensaje y lo mostramos en pantalla de la consola.
			 e.printStackTrace();
		 }
		

	}

}
