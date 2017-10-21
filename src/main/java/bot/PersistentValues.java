package bot;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import org.telegram.telegrambots.logging.BotLogger;

public class PersistentValues {
	
	private static final String LOGTAG = "PersistentValues";	   
    private final String fileName = "persistentValues.txt";    
    
    /**
     * Metodo encargado de guardar el id en un fichero para su recuperacion posterior.
     * @param idValue valor a almacenar.
     */
    public void saveIdValue (Integer idValue){
    	if (new File(fileName).exists()){//Si el fichero existe...
    		try (FileWriter fileWriter = new FileWriter(new File(fileName))){//De esta manera nos ahorramos el close() Java7
    			synchronized (fileWriter) {//Sincronizamos el objeto para evitar inconsistencias y mejorar el rendimiento.
    				System.out.println("ID parametro Persistent values: "+ idValue);
					fileWriter.write(idValue);//Sobreescribimos el valor del fichero.
					fileWriter.flush();//Guardamos el valor del Id.
				}    			
    		} catch (IOException e) {
    			BotLogger.error(LOGTAG, e);//Guardamos mensaje y lo mostramos en pantalla de la consola.
				e.printStackTrace();
			}
    	} else{//Si no esta creado...
    		try(FileWriter fileWriter = new FileWriter(new File(fileName))){//De esta manera nos ahorramos el close() Java7
    			//Creamos el fichero
    			synchronized (fileWriter) {//Sincronizamos el objeto para evitar inconsistencias y mejorar el rendimiento.
					fileWriter.write(1);//Valor por defecto al no estar creado el fichero.
					fileWriter.flush();//Guardamos el valor del Id.
				}    			
    		} catch (IOException e) {
    			BotLogger.error(LOGTAG, e);//Guardamos mensaje y lo mostramos en pantalla de la consola.
				e.printStackTrace();
			}    		    		
    	}
    }
    
    /**
     * Metodo encargado de recoger el valor id alojado en el fichero y devolverlo.
     * @return Id para utilizar en las encuestas.
     */
    public Integer getIdValue (){
    	Integer value = 0;
    	if (new File(fileName).exists()){//Si el fichero existe...
    		try (FileReader fileReader = new FileReader (new File(fileName))){
    			synchronized (fileReader) {//Sincronizamos el objeto para evitar inconsistencias y mejorar el rendimiento.
					value = fileReader.read();//Leemos el valor del fichero.
					System.out.println("ID en fichero Persistent values: "+ value);
				}    			
    		} catch (FileNotFoundException e) {
    			BotLogger.error(LOGTAG, e);//Guardamos mensaje y lo mostramos en pantalla de la consola.
				e.printStackTrace();
			} catch (IOException e) {
				BotLogger.error(LOGTAG, e);//Guardamos mensaje y lo mostramos en pantalla de la consola.
				e.printStackTrace();
			}
    	} else {//Si no existe es por ser la primera ejecucion, valor del id 1.
    		value = 1;
    	}
    	return value;
    }
}
