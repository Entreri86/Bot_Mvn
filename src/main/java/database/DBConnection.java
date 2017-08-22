package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.telegram.telegrambots.logging.BotLogger;
import bot.BuildVariables;

public class DBConnection {	
	private static final String LOGTAG = "CONNECTIONDB";
	private Connection currentConnection;
	
	/**
	 * Constructor encargado de iniciar la conexion.
	 */
	public DBConnection (){
		this.currentConnection = openConnection();//Abrimos la conexion.
	}
	
	/**
	 * Metodo encargado de abrir la conexion con la base de datos.
	 * @return Conexion con la base de datos.
	 */
	private Connection openConnection(){
		Connection connection = null;		
		try {
			Class.forName(BuildVariables.CONTROLLER_DB).newInstance();//Creamos instancia pasandole el controlador
			//Abrimos la conexion pasandole los datos de la conexion.
			connection = DriverManager.getConnection(BuildVariables.LINK_DB,BuildVariables.USER_DB,BuildVariables.PASSWORD);
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
			BotLogger.error(LOGTAG, e);
			e.printStackTrace();
		}		
		return connection;
	}
	
	/**
	 * Metodo encargado de cerrar la conexion.
	 */
	public void closeConnection (){		
		try {
			this.currentConnection.close();
		} catch (SQLException e) {
			BotLogger.error(LOGTAG, e);
			e.printStackTrace();
		}
	}
	
	/**
	 * Metodo encargado de ejecutar la consulta dada por parametro.
	 * @param query Consulta a ejecutar.
	 * @return Resultado de la ejecucion de la consulta.
	 * @throws SQLException Excepcion de tipo SQL.
	 */
	public ResultSet runSqlQuery (String query) throws SQLException{
		final Statement statement;
		statement = this.currentConnection.createStatement();
		//TODO: Comprobar los parametros para SQL Inyection.
		return statement.executeQuery(query);
	}
	
	/**
	 * Metodo encargado de ejecutar la consulta dada por parametro, multiples resultados.
	 * @param query consulta a ejecutar.
	 * @return false en caso de fallar la consulta.
	 * @throws SQLException en caso de fallo de la consulta.
	 */
	public Boolean executeQuery (String query) throws SQLException{
		final Statement statement = this.currentConnection.createStatement();		
		return statement.execute(query);		
	}
	
	/**
	 * Metodo encargado de realizar consultas preparadas previamente.
	 * @param query consulta a ejecutar.
	 * @return consulta precompilada.
	 * @throws SQLException en caso de fallar la consulta.
	 */
	public PreparedStatement getPreparedStatement (String query) throws SQLException{
		return this.currentConnection.prepareStatement(query);
	}
	/**
	 * Metodo encargado de realizar consultas preparadas previamente.
	 * @param query consulta a ejecutar.
	 * @param flags estado de la db.
	 * @return consulta precompilada.
	 * @throws SQLException en caso de fallar la consulta.
	 */
	public PreparedStatement getPreparedStatement (String query,int flags) throws SQLException{
		return this.currentConnection.prepareStatement(query,flags);
	}
	/**
	 * Metodo encargado de iniciar las transacciones en la db.
	 * @throws SQLException en caso de no poder iniciar la transaccion.
	 */
	public void initTransaction () throws SQLException{
		this.currentConnection.setAutoCommit(false);
	}
	/**
	 * Metodo encargado de enviar los cambios a la db y finalizar la transaccion.
	 * @throws SQLException si falla el rollback.
	 */
	public void commitTransaction () throws SQLException{		
		try {
			this.currentConnection.commit();//Enviamos los cambios
		}catch (SQLException e) {//Si falla y la conexion esta abierta volvemos al estado anterior.
			if ( this.currentConnection != null){
				this.currentConnection.rollback();
			}
			
		}finally{//En todo caso ponemos el autoCommit en falso.
			this.currentConnection.setAutoCommit(false);
		}
	}
}
