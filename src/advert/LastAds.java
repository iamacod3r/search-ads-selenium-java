package advert;

import java.util.ArrayList;

import config.Key;

public class LastAds {

	public ArrayList<LastAdItem> Letgo;
	public ArrayList<LastAdItem> Close5;
	public ArrayList<LastAdItem> FiveMiles;
	public ArrayList<Key> Keys;
	public int TryCount;
	
	public LastAds() {
		Keys = new ArrayList<Key>();
		TryCount = 0;
		Letgo = new ArrayList<LastAdItem>();
		Close5 = new ArrayList<LastAdItem>();
		FiveMiles = new ArrayList<LastAdItem>();
	}
}