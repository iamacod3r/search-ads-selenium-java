package config;

import java.util.ArrayList;

public class Config {
	//Json Config class
	public String LastAdsPath;
	public SeleniumDriver SeleniumDriver;
	public String SeleniumDriverPath;
	public ArrayList<Key> Keys;
	public Domain Domains;
	public String[] BlacklistWords;
	public int IamUpTryCount;
	public MailConfig MailConfig;

	public Config() {
		Keys = new ArrayList<Key>();
		Domains = new Domain();
		IamUpTryCount = 5;
		MailConfig = new MailConfig();
	}
}