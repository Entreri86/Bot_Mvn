package database;

public class DBManager {
	  private static final String LOGTAG = "DATABASEMANAGER";
	  
	  private static volatile DBManager dbInstance;
	  private static volatile DBConnection connection;
	  
	  private DBManager (){
		  connection = new DBConnection();//Conectamos con la DB.
		  /**
		   * final int currentVersion = connetion.checkVersion();
		   * final int currentVersion = connetion.checkVersion();
        	 BotLogger.info(LOGTAG, "Current db version: " + currentVersion);
        		if (currentVersion < CreationStrings.version) {
            		recreateTable(currentVersion);
        		}
		   *  De momento no necesario.
		   */		  
	  }
	  
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

}
