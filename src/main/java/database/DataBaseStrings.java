package database;

public class DataBaseStrings {
	//TABLA DE USUARIOS!!
	//Sentencia creacion tabla de los usuarios COMPROBADA Y OK.
	public static final String CREATE_USERS_TABLE = "CREATE TABLE IF NOT EXISTS mnspainbot.users ("
			 										+" userId INT NOT NULL,"
			 										+" firstName VARCHAR(30) NULL,"
			 										+" lastName VARCHAR(30) NULL,"
			 										+" userName VARCHAR(30) NULL,"
			 										+" userCredential VARCHAR(100) NOT NULL,"
			 										+" PRIMARY KEY (userId))"
			 										+" ENGINE = InnoDB;";			
	//Sentencia de insert en la tabla de los usuarios COMPROBADA Y OK.
	public static final String INSERT_USERS_TABLE = "INSERT INTO users (userId, firstName, lastName, userName) VALUES (?, ?, ?, ?);";
	//Sentencia de lectura de la tabla de los usuarios COMPROBADA Y OK.
	public static final String READ_USERS_TABLE = "SELECT * FROM users WHERE userId = ? ;";
	//Sentencia de eliminacion del usuario de la tabla de usuarios del bot COMPROBADA Y OK.
	public static final String DELETE_USER_FROM_TABLE = "DELETE FROM users WHERE userId = ?;";	
	//TABLA DE ENCUESTAS!!!
	//Sentencia de creacion de la tabla de las encuestas COMPROBADA Y OK!
	public static final String CREATE_SURVEY_TABLE = "CREATE TABLE IF NOT EXISTS mnspainbot.surveys ("
			  										+"surveyId VARCHAR(50) NOT NULL,"
												    +"userSurveyId INT NOT NULL,"
												    +"question VARCHAR(50) NULL,"
												    +"answers VARCHAR(500) NULL,"
												    +"score VARCHAR(100) NULL,"
												    +"peopleVoted INT NULL,"
												    +"answerOptions INT NULL,"
												    +"inlineQueryResultArticleId VARCHAR(200) NOT NULL,"
												    +"surveyText VARCHAR(500) NULL,"
												    +"inlineMsgId VARCHAR(200) NOT NULL,"
												    +"PRIMARY KEY (surveyId),"
												    +"INDEX userId_idx (userSurveyId ASC),"
												    +"CONSTRAINT userId"
												    +" FOREIGN KEY (userSurveyId)"
												    +" REFERENCES mnspainbot.users (userId)"
												    +"ON DELETE CASCADE"
												    +"ON UPDATE CASCADE)"
												    +"ENGINE = InnoDB;";
	//Sentencia de insert en la tabla de las encuestas COMPROBADA Y OK.
	public static final String INSERT_SURVEY = "INSERT INTO surveys (surveyId ,userSurveyId, question, answers, score, peopleVoted, answerOptions, inlineQueryResultArticleId, surveyText, inlineMsgId) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
	//Sentencia de lectura de la tabla de encuestas COMPROBADA Y OK! EN MYSQL SE guarda un "." en vez de una ",".
	public static final String READ_SURVEY = "SELECT question, answers, score, peopleVoted, answerOptions, inlineMsgId, inlineQueryResultArticleId, surveyText FROM surveys WHERE userId = ?;";
	//Sentencia de eliminacion de la encuesta de la tabla de encuestas COMPROBADA Y OK.
	public static final String DELETE_SURVEYS = "DELETE FROM surveys WHERE surveyId = ?;";//Antes borraba segun usuario, ahora borra encuesta unica.
	//Sentencia de actualizacion de la encuesta en la tabla COMPROBADA Y OK!.//TODO: Quizas se tenga que actualizar UPDATE SURVEY.
	public static final String UPDATE_SURVEY = "UPDATE surveys SET question = ?, answers = ?, score = ?, peopleVoted = ?, answerOptions = ?, surveyText = ? WHERE userId = ? AND inlineQueryResultArticleId = ?;";
	//Sentencia de comprobacion de si hay encuestas en la BD con relacion al usuario COMPROBADA Y OK.
	public static final String CHECK_SURVEYS = "SELECT * FROM surveys WHERE userId = ? ;";
	//Sentencia de creacion de la tabla de FEEDS RSS COMPROBADA Y OK!
	public static final String CREATE_FEED_TABLE = "CREATE TABLE IF NOT EXISTS mnspainbot.rssFeeds ("
													+"feedId INT NOT NULL AUTO_INCREMENT,"
													+"userAdminId INT NOT NULL,"
													+"urlFeed VARCHAR(512) NOT NULL,"
													+"description VARCHAR (256) NOT NULL,"
													+"PRIMARY KEY (feedId),"
													+"INDEX userId_idz (userAdminId ASC),"
													+"CONSTRAINT userIdFeed"
													 +" FOREIGN KEY (userAdminId)"
													 +" REFERENCES mnspainbot.users (userId)"
													 +" ON DELETE CASCADE"
													 +" ON UPDATE CASCADE)"
													 +" ENGINE = InnoDB;";
	//Sentencia de lectura de los feeds de la tabla segun usuario administrador, COMPROBADA Y OK!
	public static final String READ_FEED_URL = "SELECT * FROM rssFeeds WHERE userAdminId = ? ;";
	//Sentencia de insert de los feeds de la tabla , COMPROBADA Y OK!
	public static final String INSERT_FEED = "INSERT INTO rssFeeds (userAdminId, urlFeed, description) VALUES (?, ?, ?) ; ";
	//Sentencia de update de los feeds de la tabla, COMPROBADA Y OK!
	public static final String UPDATE_FEED = "UPDATE rssFeeds SET urlFeed = ?, description = ? WHERE userAdminId = ? ; ";
	//Sentencia de borrado de un feed unico segun su ID, COMPROBADA Y OK!.
	public static final String DELETE_FEED = "DELETE FROM rssFeed WHERE feedId = ? ; ";
	//Sentencia de borrado de todos los feeds segun el ID del administrador, COMPROBADA Y OK!.
	public static final String DELETE_ALL_FEED = "DELETE FROM rssFeed WHERE userAdminId = ? ;";
	//Sentencia de creacion de la tabla de chats suscritos al Feed COMPROBADA Y OK!
	public static final String CREATE_FEED_SUSCRIBERS_TABLE = "CREATE TABLE IF NOT EXISTS mnspainbot.feedSuscribers ("
															   +"chatId BIGINT(64) NOT NULL,"
															   +"feedId INT NOT NULL,"
															   +"PRIMARY KEY (chatId),"
															   +"INDEX feedId_idx (feedId ASC),"
															   +"CONSTRAINT feedId"
															     +"FOREIGN KEY (feedId)"
															     +"REFERENCES mnspainbot.rssFeeds (feedId)"
															     +"ON DELETE NO ACTION"
															     +"ON UPDATE NO ACTION)"
															   +"ENGINE = InnoDB;";
	//Sentencia de lectura de la tabla feedSuscribers que recoge todos los chats que estan suscritos a un determinado feed, COMPROBADA Y OK!
	public static final String READ_FEED_SUSCRIBER = "SELECT * FROM feedSuscribers WHERE feedId = ? ; ";
	//Sentencia de insert de la tabla feedSuscribers, el FeedId debe de ser el mismo que en la tabla rssFeed COMPROBADA Y OK!
	public static final String INSERT_FEED_SUSCRIBER = "INSERT INTO feedSuscribers (chatId, feedId) VALUES (?, ?) ; ";
	//Sentencia de update de la tabla feedSuscribers, el FeedId debe de ser el mismo que en la tabla rssFeed COMPROBADA Y OK!
	public static final String UPDATE_FEED_SUSCRIBER = "UPDATE rssFeeds SET chatId = ?, feedId = ? WHERE feedId = ? ; ";
	//Sentencia de borrado de feedSuscribers, borra la suscripcion de un chat a un determinado feed, COMPROBADA Y OK!
	public static final String DELETE_FEED_SUSCRIBER = "DELETE FROM feedSuscribers WHERE chatId = ? ;";
}
