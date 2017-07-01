package config;

import java.util.ArrayList;

public class DomainConfig {

	public int ExpireAdDayCount;
	public String Url;
	public ArrayList<CookieInfo> Cookies;
	
	public DomainConfig() {
		ExpireAdDayCount = 7;
		Cookies = new ArrayList<CookieInfo>();
	}
}
