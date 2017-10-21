package bot;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.Random;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.telegram.telegrambots.logging.BotLogger;

public class PasswdTool {

	private static final Random random = new Random();
	private static final PasswdTool PASSWD_TOOL = new PasswdTool();
	private static final String LOGTAG = "PasswdTool";
    /**
     * Obtiene el hash y la sal a partir de una contraseña 
     */
    public static HashSalt getHash(String password) throws Exception {		
        byte[] salt = new byte[16];
		// Genera la sal de forma aleatoria
		random.nextBytes(salt);
		KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 128);
		try {
			// Obtiene una instancia para el algoritmo
			SecretKeyFactory f = SecretKeyFactory.getInstance("PBEWithHmacSHA256AndAES_128");
			// Genera el hash
            byte[] hash = f.generateSecret(spec).getEncoded();
            Base64.Encoder enc = Base64.getEncoder();
            // convierte hash y sal a cadena de texto
            return PASSWD_TOOL.new HashSalt(enc.encodeToString(hash), enc.encodeToString(salt));
		} catch (NoSuchAlgorithmException e) {
			BotLogger.error(LOGTAG, e);//Guardamos mensaje y lo mostramos en pantalla de la consola.
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			BotLogger.error(LOGTAG, e);//Guardamos mensaje y lo mostramos en pantalla de la consola.
			e.printStackTrace();
	    }		
		throw new Exception("No se pudo crear hash");
    }
    
    public static boolean ValidatePass(String password, String stringHash, String stringSalt) {
        Base64.Decoder dec = Base64.getDecoder();
		// Convierte la sal a arreglo de bytes
		byte[] salt = dec.decode(stringSalt);
		KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 128);
		try {
			// Obtiene instancia del algoritmo
            SecretKeyFactory f = SecretKeyFactory.getInstance("PBEWithHmacSHA256AndAES_128");
            // Genera el hash como arreglo de bytes
            byte[] hash = f.generateSecret(spec).getEncoded();
            Base64.Encoder enc = Base64.getEncoder();
            // convierte el hash a cadena de texto
            String currentHash = enc.encodeToString(hash);
            // compara si los hash son iguales
            return currentHash.equals(stringHash);
		} catch (NoSuchAlgorithmException e) {
			BotLogger.error(LOGTAG, e);//Guardamos mensaje y lo mostramos en pantalla de la consola.
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			BotLogger.error(LOGTAG, e);//Guardamos mensaje y lo mostramos en pantalla de la consola.
			e.printStackTrace();
		}	
		return false;
    }
    
    class HashSalt {
        private String hash;
        private String salt;
    	
        public HashSalt(String hash, String salt) {
    	this.hash = hash;
    	this.salt = salt;
        }

		public String getHash() {
			return hash;
		}

		public void setHash(String hash) {
			this.hash = hash;
		}

		public String getSalt() {
			return salt;
		}

		public void setSalt(String salt) {
			this.salt = salt;
		}
     
    }

}
