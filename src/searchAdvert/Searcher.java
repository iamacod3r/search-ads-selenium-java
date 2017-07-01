package searchAdvert;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import com.google.gson.Gson;
import advert.AdvertInfo;
import advert.Domains;
import advert.LastAdItem;
import advert.LastAds;
import config.Config;
import config.CookieInfo;
import config.Key;
import infrastructure.*;

public class Searcher {

	private Config config;
	private WebDriver driver;
	private List<AdvertInfo> newAdList = new ArrayList<AdvertInfo>();
	private Gson gson = new Gson();
	private File file = new File();
	private Mail mail = new Mail();
	private LastAds lastAds = new LastAds();
	private int defaultMaxSize = 2;

	public Searcher() {
	}

	public void Search() throws Exception {

		SetUp();
		CleanLastAds();

		String keyList = "";

		for (Key key : config.Keys) {
			keyList = keyList + key.Key + ", ";

			OfferUp(key);
			CraigsList(key);
			Letgo(key);
		}
		Close5();
		FiveMilesApp();

		if (newAdList.isEmpty()) {
			lastAds.TryCount++;
			if (lastAds.TryCount >= config.IamUpTryCount) {
				mail.Send("I'm up Bro :) for Search Advert", config.MailConfig);

				lastAds.TryCount = 0;
			}
		} else {
			lastAds.TryCount = 0;
			SendMail(keyList);

		}
		SaveLastProcess();

		Close();
	}

	void SetUp() throws Exception {

		String result = file.ReadFile("config.json");
		config = gson.fromJson(result, Config.class);

		String lastAdsStr = file.ReadFile(config.LastAdsPath);
		lastAds = gson.fromJson(lastAdsStr, LastAds.class);

		if (lastAds == null) {
			lastAds = new LastAds();
		}

		if (lastAds.Keys != null) {
			for (Key k : config.Keys) {
				for (Key p : lastAds.Keys) {
					if (k.Key.equals(p.Key)) {
						k.Id = p.Id;
						break;
					}
				}
			}
		}
		switch (config.SeleniumDriver) {
		case Firefox:
			System.setProperty("webdriver.firefox.driver", config.SeleniumDriverPath);
			driver = new FirefoxDriver();
			break;
		case Chrome:
		default:
			System.setProperty("webdriver.chrome.driver", config.SeleniumDriverPath);
			driver = new ChromeDriver();
			break;
		}

		driver.manage().window().maximize();
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
	}

	void Close() throws Exception {
		Thread.sleep(1000);
		driver.close();
	}

	void SendMail(String keyList) throws IOException {
		String mailBody = "<h2>Search for : " + keyList + "</h2>&nbsp; Total Ad Count : " + newAdList.size() + "<hr />";
		String link = "<a href=\"{url}\" target=\"_blank\"><img src=\"{iSrc}\" height=\"40\"/><a/>";
		String imgLink = "<a href=\"{url}\" target=\"_blank\"><img src=\"{iSrc}\" /><a/>";
		String whatsAppUrl = "<a href=\"whatsapp://send?text={url}\" data-action=\"share/whatsapp/share\" target=\"_blank\"><img src=\"https://image.flaticon.com/icons/png/128/134/134937.png\" height=\"40\" /></a>";
		// String whatsAppUrlOld ="<a href=\"whatsapp://send\"
		// data-text=\"{Title}\" data-href=\"{url}\"><img
		// src=\"https://image.flaticon.com/icons/png/128/134/134937.png\"
		// height=\"40\" /></a>";

		String offerUpImgUrl = "https://cdn.geekwire.com/wp-content/uploads/2016/04/offerup.png";
		String craigsListImgUrl = "https://www.opendataphilly.org/uploads/group/20150124-235905.365783logocraigslist.png";
		String letgoImgUrl = "https://us.letgo.com/apple-touch-icon-152x152.png";
		String fiveMilesImgUrl = "https://www.5milesapp.com/images/favicons/favicon-96x96.png";
		String close5ImgUrl = "https://www.close5.com/images/favicons/96x96.png";

		for (AdvertInfo prod : newAdList) {

			String linkImg = "";
			switch (prod.Domain) {
			case CraigsList:
				linkImg = craigsListImgUrl;
				break;
			case FiveMilesApp:
				linkImg = fiveMilesImgUrl;
				break;
			case Letgo:
				linkImg = letgoImgUrl;
				break;
			case OfferUp:
				linkImg = offerUpImgUrl;
				break;
			case Close5:
				linkImg = close5ImgUrl;
				break;

			}

			mailBody = mailBody + "<b>Title : " + prod.Title + "</b> -- <b>" + (prod.Id > 0 ? prod.Id : prod.IdStr)
					+ "</b><br/>";
			mailBody = mailBody + "Price : <b>" + prod.Price + "</b><br/>";
			mailBody = mailBody + "<b>Username :</b> " + prod.Username + " -- " + "<b>Distance :</b> " + prod.Distance
					+ "<br/>";
			mailBody = mailBody + link.replace("{url}", prod.Url).replace("{iSrc}", linkImg) + "&nbsp;&nbsp;&nbsp;"
					+ whatsAppUrl.replace("{url}", java.net.URLEncoder.encode(prod.Url, "UTF-8")) + "<br/>"; // .replace("{Title}",
																												// prod.Title)
			mailBody = mailBody + imgLink.replace("{url}", prod.Url).replace("{iSrc}", prod.ImageUrl) + "<hr />";
		}

		file.SaveFile(mailBody, "test.html");
		config.MailConfig.Subject = config.MailConfig.Subject + " - " + new java.util.Date();
		mail.Send(mailBody, config.MailConfig);

	}

	void CleanLastAds() throws IOException {

		Calendar curDate = Calendar.getInstance();
		
		curDate.add(Calendar.DATE, -(config.Domains.Close5.ExpireAdDayCount));
		Date close5ExpireDate = curDate.getTime();
		
		curDate.add(Calendar.DATE, config.Domains.Close5.ExpireAdDayCount);
		curDate.add(Calendar.DATE, -(config.Domains.Letgo.ExpireAdDayCount));
		Date letgoExpireDate = curDate.getTime();
		
		curDate.add(Calendar.DATE, config.Domains.Letgo.ExpireAdDayCount);
		curDate.add(Calendar.DATE, -(config.Domains.FiveMiles.ExpireAdDayCount));
		Date fiveMilesExpireDate = curDate.getTime();
		
		if (lastAds != null) {
			ArrayList<LastAdItem> newClose5List = new ArrayList<LastAdItem>();
			for (LastAdItem item : lastAds.Close5) {
				if (close5ExpireDate.before(item.AddDate)) {
					newClose5List.add(item);
				}
			}

			ArrayList<LastAdItem> newLetgoList = new ArrayList<LastAdItem>();
			for (LastAdItem item : lastAds.Letgo) {
				if (letgoExpireDate.before(item.AddDate)) {
					newLetgoList.add(item);
				}
			}

			ArrayList<LastAdItem> newFiveMiles = new ArrayList<LastAdItem>();
			for (LastAdItem item : lastAds.FiveMiles) {
				if (fiveMilesExpireDate.before(item.AddDate)) {
					newFiveMiles.add(item);
				}
			}

			lastAds.Close5 = newClose5List;
			lastAds.Letgo = newLetgoList;
			lastAds.FiveMiles = newFiveMiles;

			String lastAddsStr = gson.toJson(lastAds);
			file.SaveFile(lastAddsStr, config.LastAdsPath);

		}
	}

	void OfferUp(Key key) throws InterruptedException, UnsupportedEncodingException {
		String url = config.Domains.OfferUp.Url + java.net.URLEncoder.encode(key.Key, "UTF-8");

		driver.get(url);

		Thread.sleep(2000);

		

		List<WebElement> lastPosting = driver.findElements(By.cssSelector(".item-container"));

		long lastId = 0;

		for (int i = 2; i < lastPosting.size(); i++) {

			WebElement xItem = lastPosting.get(i);
			WebElement xHref = xItem.findElement(By.tagName("a"));

			AdvertInfo prod = new AdvertInfo();

			prod.Url = xHref.getAttribute("href");

			String[] urlParts = prod.Url.split("/");

			prod.Id = Long.parseLong(urlParts[5]);

			if (prod.Id > key.Id.OfferUp) {
				prod.Price = xItem.findElement(By.cssSelector(".item-info-price")).getText();

				if (PriceCheck(prod, key.LowestPrice)) {

					prod.Title = xItem.findElement(By.cssSelector(".item-info-title>a")).getText();

					if (KeyTitleCheck(prod, key)) {
						if (prod.Id > lastId) {
							lastId = prod.Id;
						}

						prod.Domain = Domains.OfferUp;
						if (!IsAlreadyAddedById(prod)) {

							prod.ImageUrl = xItem.findElement(By.tagName("img")).getAttribute("src");
							prod.Distance = xItem.findElement(By.cssSelector(".item-info-distance")).getText();
							prod.Username = xItem.findElement(By.cssSelector(".owner-info-username")).getText();

							newAdList.add(prod);
						}
					}
				}
			}
		}
		if (lastId > key.Id.OfferUp) {
			key.Id.OfferUp = lastId;
		}

	}

	void Close5() throws UnsupportedEncodingException, InterruptedException {

		ArrayList<AdvertInfo> close5Advert = new ArrayList<AdvertInfo>();

		for (Key key : config.Keys) {

			String url = config.Domains.Close5.Url.replace("{key}",
					java.net.URLEncoder.encode(key.Key, "UTF-8").replace("+", "-"));

			driver.get(url);

			Thread.sleep(1000);

			// Region Mile range select
			List<WebElement> ddlMileElements = driver.findElements(By.className("Select__select__2NSWc"));

			ddlMileElements.get(0).click();
			Thread.sleep(300);

			List<WebElement> liMileElements = driver
					.findElements(By.cssSelector(".SelectDropdownListItems__selectDropdownList__1FB7L > li"));

			liMileElements.get(2).click();

			Thread.sleep(300);

			ddlMileElements.get(1).click();
			// EndRegion

			// Region Price Limit
			List<WebElement> priceInput = driver
					.findElements(By.cssSelector(".DesktopPriceFilter__priceInputs__1tRt9>input"));

			priceInput.get(0).sendKeys(String.format("%.0f", key.LowestPrice));
			priceInput.get(0).sendKeys(Keys.ENTER);

			// EndRegion

			Thread.sleep(1000);

			

			List<WebElement> lastPosting = driver.findElements(By.cssSelector(".ItemsListCard__item__8yykb"));

			for (int i = 0; i < lastPosting.size(); i++) {

				WebElement xItem = lastPosting.get(i);
				WebElement xHref = xItem.findElement(By.tagName("a"));

				AdvertInfo ad = new AdvertInfo();
				ad.Url = xHref.getAttribute("href");

				String[] urlArr = ad.Url.split("/");
				ad.IdStr = urlArr[urlArr.length - 1];

				if (IsLastAdCheck(ad, lastAds.Close5) || IsAlreadyAddedByUrl(ad, close5Advert)) {
					continue;
				}

				if (xItem.findElement(By.cssSelector(".PriceText__priceText__1wv7- .value")) != null) {
					ad.Price = xItem.findElement(By.cssSelector(".PriceText__priceText__1wv7- .value")).getText();

					if (PriceCheck(ad, key.LowestPrice)) {

						WebElement titleElement = xItem
								.findElement(By.cssSelector(".ItemsListItem__item__details__description__Qu5fv"));
						if (titleElement != null) {
							ad.Title = titleElement.getText();
							if (KeyTitleCheck(ad, key)) {

								ad.Domain = Domains.Close5;
								ad.ImageUrl = xItem.findElement(By.tagName("img")).getAttribute("src");

								close5Advert.add(ad);
								lastAds.Close5.add(new LastAdItem(ad.Url, new Date(), ad.IdStr));
							}
						}
					}
				}

			}
		}

		newAdList.addAll(Close5Detail(close5Advert));
	}

	ArrayList<AdvertInfo> Close5Detail(ArrayList<AdvertInfo> adItems) throws InterruptedException {

		for (AdvertInfo ad : adItems) {
			driver.get(ad.Url);
			Thread.sleep(1000);
			ad.Username = driver.findElement(By.cssSelector(".ItemDetailsUser__ownerName__1YS78")).getText();
			ad.Distance = driver.findElement(By.cssSelector(".ItemLocation__locationInfo__1U7p1>div")).getText();
		}

		return adItems;
	}

	void CraigsList(Key key) throws InterruptedException, UnsupportedEncodingException {

		long lastId = 0;

		String url = config.Domains.CraigsList.Url + java.net.URLEncoder.encode(key.Key, "UTF-8");

		driver.get(url);

		Thread.sleep(2000);


		WebElement rowObject = driver.findElement(By.cssSelector(".rows"));
		List<WebElement> lastPosting = rowObject.findElements(By.xpath("*"));

		for (int i = 0; i < lastPosting.size(); i++) {

			WebElement xItem = lastPosting.get(i);

			if (xItem.getTagName().toLowerCase(Locale.ENGLISH).equals("li")) {

				AdvertInfo ad = new AdvertInfo();

				WebElement xHref = xItem.findElement(By.cssSelector(".result-title"));

				ad.Url = xHref.getAttribute("href");

				String[] urlParts = ad.Url.split("/");

				ad.Id = Long.parseLong(urlParts[urlParts.length - 1].replace(".html", ""));

				if (ad.Id > key.Id.CraigsList) {

					ad.Price = xItem.findElement(By.cssSelector(".result-meta > .result-price")).getText();

					if (PriceCheck(ad, key.LowestPrice)) {

						ad.Title = xHref.getText();

						if (TitleCheck(ad)) {

							if (ad.Id > lastId) {
								lastId = ad.Id;
							}

							ad.Domain = Domains.CraigsList;

							if (!IsAlreadyAddedById(ad)) {

								ad.ImageUrl = xItem.findElement(By.tagName("img")).getAttribute("src");

								try {
									ad.Distance = xItem.findElement(By.cssSelector(".result-hood")).getText();
								} catch (Exception e) {
								}

								newAdList.add(ad);
							}
						}
					}
				}
			} else if (xItem.getTagName().toLowerCase(Locale.ENGLISH).equals("h4")) {
				break;
			}

		}

		if (lastId > key.Id.CraigsList) {
			key.Id.CraigsList = lastId;
		}

	}

	void FiveMilesApp() throws InterruptedException, UnsupportedEncodingException {

		String url = config.Domains.FiveMiles.Url.replace("{key}",
				java.net.URLEncoder.encode(config.Keys.get(0).Key, "UTF-8").replace("+", " "));

		driver.get(url);

		for (Cookie cook : driver.manage().getCookies()) {
			for (CookieInfo cif : config.Domains.FiveMiles.Cookies) {
				if (cook.getName().equals(cif.Name)) {
					cif.IsCookieSet = true;
				}
			}
		}

		for (CookieInfo cif : config.Domains.FiveMiles.Cookies) {
			if (!cif.IsCookieSet) {
				Calendar curDate = Calendar.getInstance();
				curDate.add(Calendar.DATE, cif.ExpireDay);

				Cookie cookie = new Cookie.Builder(cif.Name, cif.Value).domain(cif.Domain).expiresOn(curDate.getTime())
						.isHttpOnly(cif.IsHttpOnly).isSecure(cif.IsSecure).path(cif.Path).build();

				driver.manage().addCookie(cookie);
				cif.IsCookieSet = true;
			}
		}

		Thread.sleep(1000);

		for (Key key : config.Keys) {
			FiveMilesAppKey(key);
		}
	}

	void FiveMilesAppKey(Key key) throws UnsupportedEncodingException, InterruptedException {

		String url = config.Domains.FiveMiles.Url.replace("{key}",
				java.net.URLEncoder.encode(key.Key, "UTF-8").replace("+", " "));

		driver.get(url);
		Thread.sleep(2000);

		List<WebElement> lastPosting = driver.findElements(By.cssSelector(".waterItem"));

		for (int i = 0; i < lastPosting.size(); i++) {

			WebElement xItem = lastPosting.get(i);
			AdvertInfo ad = new AdvertInfo();
			ad.Url = xItem.findElement(By.cssSelector(".waterItemImg_par")).getAttribute("href");
			ad.Price = xItem.findElement(By.cssSelector(".waterItem_price_now")).getText();

			if (PriceCheck(ad, key.LowestPrice) && !IsLastAdCheck(ad, lastAds.FiveMiles)) {

				WebElement img = xItem.findElement(By.cssSelector(".waterItemImg"));
				ad.Title = img.getAttribute("title");

				if (KeyTitleCheck(ad, key)) {
					ad.Domain = Domains.FiveMilesApp;

					if (!IsAlreadyAddedByUrl(ad)) {

						// https://www.5milesapp.com/item/K3mRPBKQ84Agoyx4/iphone-7-plus
						String[] urlArr = ad.Url.split("/");
						ad.IdStr = urlArr[urlArr.length - 2];

						ad.ImageUrl = img.getAttribute("src");
						ad.Username = xItem.findElement(By.cssSelector(".waterItem_user_name > a")).getText();
						ad.Distance = xItem.findElement(By.cssSelector(".waterItem_distance_pos > a")).getText();

						newAdList.add(ad);
						lastAds.FiveMiles.add(new LastAdItem(ad.Url, new Date(), ad.IdStr));
					}
				}
			}
		}
	}

	void Letgo(Key key) throws InterruptedException, UnsupportedEncodingException {

		ArrayList<AdvertInfo> letgoProdList = new ArrayList<AdvertInfo>();

		String url = config.Domains.Letgo.Url + java.net.URLEncoder.encode(key.Key, "UTF-8");

		driver.get(url);

		Thread.sleep(2000);

		List<WebElement> lastPosting = driver.findElements(By.cssSelector(".img.portrait, .img.landscape"));

		for (int i = 0; i < lastPosting.size(); i++) {

			WebElement xItem = lastPosting.get(i);

			AdvertInfo ad = new AdvertInfo();
			ad.Title = xItem.getAttribute("title");
			String adUrl = xItem.getAttribute("href");
			ad.Url = adUrl;

			if (!IsLastAdCheck(ad, lastAds.Letgo) && !IsAlreadyAddedByUrl(ad, letgoProdList)
					&& KeyTitleCheck(ad, key)) {

				String[] adUrlArr = adUrl.split("_");
				ad.IdStr = adUrlArr[adUrlArr.length - 1];

				ad.ImageUrl = xItem.findElement(By.tagName("img")).getAttribute("src");
				ad.Domain = Domains.Letgo;
				letgoProdList.add(ad);
			}
		}

		newAdList.addAll(LetgoDetail(letgoProdList, key));
	}

	ArrayList<AdvertInfo> LetgoDetail(ArrayList<AdvertInfo> adItems, Key key) throws InterruptedException {
		ArrayList<AdvertInfo> resultList = new ArrayList<AdvertInfo>();
		for (AdvertInfo ad : adItems) {

			driver.get(ad.Url);
			Thread.sleep(1000);
			try {
			ad.Price = driver.findElement(By.cssSelector(".price")).getText();

			if (PriceCheck(ad, key.LowestPrice)) {
				ad.Username = driver.findElement(By.cssSelector(".user>strong")).getText();
				ad.Distance = driver.findElement(By.cssSelector(".map>p")).getText();
				resultList.add(ad);
			}
			lastAds.Letgo.add(new LastAdItem(ad.Url, new Date(), ad.IdStr));
			}
			catch (Exception e) {
			}
		}
		return resultList;
	}

	boolean PriceCheck(AdvertInfo ad, double lowestPrice) {
		String adPrice = ad.Price.toLowerCase().replaceAll("[^0-9,.]", "").replaceAll(",", ".");
		return adPrice.length()> 0 && Double.parseDouble(adPrice) >= lowestPrice;
	}

	// Ilan daha once eklendi ise TRUE eklenmedi ise FALSE dondurur.
	boolean IsLastAdCheck(AdvertInfo ad, ArrayList<LastAdItem> lastAdsArr) {
		for (LastAdItem item : lastAdsArr) {
			if (ad.Url.equals(item.Url)) {
				return true;
			}
		}
		return false;
	}

	// Ilan daha once eklendi ise TRUE eklenmedi ise FALSE dondurur.
	boolean IsAlreadyAddedByUrl(AdvertInfo ad, ArrayList<AdvertInfo> adList) {
		for (int q = 0; q < adList.size(); q++) {
			AdvertInfo item = adList.get(q);
			if (ad.Url.equals(item.Url)) {
				return true;
			}
		}
		return false;
	}

	boolean IsAlreadyAddedByUrl(AdvertInfo ad) {
		for (int q = 0; q < newAdList.size(); q++) {
			AdvertInfo tempAd = newAdList.get(q);
			if (ad.Url.equals(tempAd.Url)) {
				return true;
			}
		}
		return false;
	}

	boolean IsAlreadyAddedById(AdvertInfo ad) {
		for (int q = 0; q < newAdList.size(); q++) {

			AdvertInfo tempAd = newAdList.get(q);

			if (ad.Id == tempAd.Id && ad.Domain == tempAd.Domain) {
				return true;
			}
		}
		return false;
	}

	boolean KeyTitleCheck(AdvertInfo ad, Key key) {
		String[] keyArr = key.Key.toLowerCase(Locale.ENGLISH).split(" ");

		String title = ad.Title.toLowerCase(Locale.ENGLISH);

		int keyCount = 0;
		int searchCount = keyArr.length > defaultMaxSize ? defaultMaxSize : keyArr.length;

		for (String keyWord : keyArr) {
			if (title.contains(keyWord)) {
				keyCount++;
			}

			if (keyCount >= searchCount) {
				return TitleCheck(ad);
			}
		}

		return false;
	}

	boolean TitleCheck(AdvertInfo ad) {
		String title = ad.Title.toLowerCase(Locale.ENGLISH);
		for (String blackWord : config.BlacklistWords) {
			if (title.contains(blackWord.toLowerCase(Locale.ENGLISH))) {
				return false;
			}
		}
		return true;
	}

	void SaveLastProcess() throws IOException {
		String lastAddsStr = gson.toJson(lastAds);
		file.SaveFile(lastAddsStr, config.LastAdsPath);
	}
}
