package config;

public class CookieInfo {

	public String Name;
	public String Value;
	public String Domain;
	public String Path;
	public boolean IsHttpOnly;
	public boolean IsSecure;
	public int ExpireDay;
	public boolean IsCookieSet; // No needed Json File

	public CookieInfo() {
		IsHttpOnly = true;
		IsSecure = false;
		ExpireDay = 730; // day count
		Path = "/";
		IsCookieSet = false;
	}
}