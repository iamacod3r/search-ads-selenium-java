package advert;

import java.util.Date;

public class LastAdItem {
	public String Id;
	public String Url;
	public Date AddDate;

	public LastAdItem() {

	}

	public LastAdItem(String url, Date addDate) {
		Url = url;
		AddDate = addDate;
	}

	public LastAdItem(String url, Date addDate, String id) {
		Url = url;
		AddDate = addDate;
		Id = id;
	}
}