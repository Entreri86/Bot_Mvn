package bot;


public class CustomUser {
	
	private boolean isLogin = false;
	private boolean havePasswd = false;
	private boolean isPolling = false;//Si esta en modo encuesta recogiendo respuestas...
	private boolean haveQuestion = false;//Si hay que controlar la pregunta...
	private boolean sendSurvey = false;//Si se a enviado la encuesta al chat privado...
	private boolean isClosed = false;//Si esta o no cerrada la encuesta...
	private boolean isUpdated = false;//Si se ha actualizado la encuesta previamente en el chat privado...
	private boolean sendUserManual = false;//Si hay que enviar el manual de usuario....
	private boolean sendFeed = false;
	private boolean gettingFeed = false;
	private boolean isSharing = false;
	
	public boolean isSharing() {
		return isSharing;
	}
	public void setSharing(boolean isSharing) {
		this.isSharing = isSharing;
	}
	public boolean isGettingFeed() {
		return gettingFeed;
	}
	public void setGettingFeed(boolean gettingFeed) {
		this.gettingFeed = gettingFeed;
	}
	public boolean isSendFeed() {
		return sendFeed;
	}
	public void setSendFeed(boolean sendFeed) {
		this.sendFeed = sendFeed;
	}
	public boolean isPolling() {
		return isPolling;
	}
	public void setPolling(boolean isPolling) {
		this.isPolling = isPolling;
	}
	public boolean isHaveQuestion() {
		return haveQuestion;
	}
	public void setHaveQuestion(boolean haveQuestion) {
		this.haveQuestion = haveQuestion;
	}
	public boolean isSendSurvey() {
		return sendSurvey;
	}
	public void setSendSurvey(boolean sendSurvey) {
		this.sendSurvey = sendSurvey;
	}
	public boolean isClosed() {
		return isClosed;
	}
	public void setClosed(boolean isClosed) {
		this.isClosed = isClosed;
	}
	public boolean isUpdated() {
		return isUpdated;
	}
	public void setUpdated(boolean isUpdated) {
		this.isUpdated = isUpdated;
	}
	public boolean isSendUserManual() {
		return sendUserManual;
	}
	public void setSendUserManual(boolean sendUserManual) {
		this.sendUserManual = sendUserManual;
	}
	public boolean isLogin() {
		return isLogin;
	}
	public void setLogin(boolean isLogin) {
		this.isLogin = isLogin;
	}
	public boolean isHavePasswd() {
		return havePasswd;
	}
	public void setHavePasswd(boolean havePasswd) {
		this.havePasswd = havePasswd;
	}
	
	
}
