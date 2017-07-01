package config;

public class MailConfig {
	public boolean MailSend;
	public String Subject;
    public String ToList;
    public String Username;
    public String Pswrd;
    public String SmtpHost;
    public boolean SmtpAuth;
    public String SmtpPort;
    public String SmtpFactoryPort;
    
	public MailConfig() {
		MailSend = true;
		SmtpAuth = true;
		SmtpPort = "465";
		SmtpFactoryPort = "465";
	}
}
