import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.stream.Collectors;

import org.jibble.pircbot.*;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class bot extends PircBot {
	boolean findingWeather = false; // gets ready to read info to find weather
	boolean findingHistory = false; // gets ready to read info to find history
	public bot() {
		// bot name
		this.setName("theWeatherHistoryBot");
	}
	
	public void onMessage(String channel, String sender, String login, String hostname, String message) {

		String city; // city to find the weather of
		String date; // date inputed
		String month; // month inputed
		String history; // contains the history string 
		// will find the weather of the specified city
		if(message.length() > 0 && findingWeather == true) {
			city = GetWeather.gettingWeather(message);
			sendMessage(channel,city);
			findingWeather = false;
		}
		// will find the history of the specified date given
		if(findingHistory == true && message.contains("/")) {
			
			month =  message.substring(0, 2);
			date =  message.substring(3);
			history = GetHistory.gettingHistory(date, month);
			sendMessage(channel,history);
			for(int i = 0; i < GetHistory.eventLength; i++)
				sendMessage(channel,GetHistory.loopHist(i, GetHistory.result));
			findingHistory = false;
		}
		// will find the history of today
		if(findingHistory == true && message.contains("today")) {
			
			history = GetHistory.gettingHistory();
			sendMessage(channel,history);
			for(int i = 0; i < GetHistory.eventLength; i++)
				sendMessage(channel,GetHistory.loopHist(i, GetHistory.result));
			findingHistory = false;
		}
		// will print that the date inputed was not in format
		if(message.length() > 0 && findingHistory == true) {
			sendMessage(channel,"Date choosen was invalid");
			findingWeather = false;
		}
		// will ask for input for history
		if(message.contains("history")) {
			sendMessage(channel,"What day do you want the history of(mm/dd) or type 'today' for todays history:");
			findingHistory = true;
		}
		// will ask for input for weather		
		if(message.contains("weather")) {
			sendMessage(channel,"What city are you looking for: ");
			findingWeather = true;
		}
		// will reply if user says hello 
		if(message.contains("Hello")) {
			sendMessage(channel, "Hey " + sender + " !");
		}
	}

}

class GetWeather {
	// makes a weather url and returns the parsed info.
	public static String gettingWeather(String city) {
		String myAPIurlString = "http://api.openweathermap.org/data/2.5/weather?q=";
		String myAPIToken = "&APPID=02d775b90ad2d1c1a2cdb8b45dfe2c21";
		String weatherURL = myAPIurlString + city + myAPIToken;
		try {
			URL url = new URL(weatherURL);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String result = rd.lines().collect(Collectors.joining());
			return parseJsonFunction(result);

		} catch (IOException e) {
			return "City not found.";
			//throw new RuntimeException(e);
		}
	}
	// parses the information from the weather url to only get the temperature then converts to celcius and farenheit
	public static String parseJsonFunction(String json) {
		JsonObject object = new JsonParser().parse(json).getAsJsonObject();
		System.out.println(object);
		// If this doesn’t make sense - take another look at the JSON Response I
		// provided on Slide 5
		String cityName = object.get("name").getAsString();
		JsonObject sys = object.getAsJsonObject("sys");
		String countyName = sys.get("country").getAsString();
		JsonObject main = object.getAsJsonObject("main");
		double temp = main.get("temp").getAsDouble();
		double celcius = getCelsius(temp);
		double fahrenheit = getFahrenheit(celcius);
		String celcForm = String.format("%.2f", celcius);
		String faheForm = String.format("%.2f", fahrenheit);
		return "The temprature in " + cityName + "," + countyName + " is: "
				+ temp + " Kelvin, "
				+ celcForm + " Celsius, "
				+ faheForm + " Fahrenheit.";
	}
	static double getCelsius(double tempInK) {
		return tempInK - 273.15;
	}
	static double getFahrenheit(double tempInC) {
		return (tempInC * (9.0/5.0)) + 32;
	}
}

class GetHistory {
	public static int eventLength = 0; // the number of events that happened on the date
	public static String result = ""; // the result gotten from the url
	
	// gets history of today 
	public static String gettingHistory() {
		String todayDate = "https://history.muffinlabs.com/date";
		try {
			URL url = new URL(todayDate);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			result = rd.lines().collect(Collectors.joining());
			return parseJsonFunction(result);

		} catch (IOException e) {
			return "What was entered was not valid";
			//throw new RuntimeException(e);
		}
	}
	// gets history of given date
	public static String gettingHistory(String date, String month) {
		String myAPIurlString = "https://history.muffinlabs.com/date/";
		String dateURL = myAPIurlString + month + "/" + date;
		try {
			URL url = new URL(dateURL);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			result = rd.lines().collect(Collectors.joining());
			return parseJsonFunction(result);

		} catch (IOException e) {
			return "What was entered was not valid";
			//throw new RuntimeException(e);
		}
	}
	// parses the history from url to only have the important information and returns the string of the day being used
	public static String parseJsonFunction(String json) {
		JsonObject object = new JsonParser().parse(json).getAsJsonObject();
		String date = object.get("date").getAsString();
		JsonObject data = object.getAsJsonObject("data");
		JsonArray events = data.getAsJsonArray("Events");
		eventLength = events.size();
		return "This is everything interesting that has happened on " + date + " in history: ";
	}
	// this will print each entry inside the json array that is needed and is in a loop that will message each entry.
	static String loopHist(int i, String json) {
		JsonObject object = new JsonParser().parse(json).getAsJsonObject();
		JsonObject data = object.getAsJsonObject("data");
		JsonArray events = data.getAsJsonArray("Events");
		JsonObject element = events.get(i).getAsJsonObject();
		String year = element.get("year").getAsString();
		String text = element.get("text").getAsString();
		return "Year " + year + ": " + text + "   ";
	}
}













