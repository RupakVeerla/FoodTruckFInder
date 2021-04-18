package assis.oracle;

import java.io.Console;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONObject;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

/**
 * @author rupak FoodTruckFinder fetches json data from
 *         https://data.sfgov.org/resource/jjew-r69b.json and prints Name and
 *         location on console
 * 
 *         Example NAME ADDRESS ---- ------- La Jefa 531 BAY SHORE BLVD Munch A
 *         Bunch 1850 MISSION ST
 */
public class FoodTruckFinder {
	public static void main(String[] args) {
		String host = "https://data.sfgov.org/resource/jjew-r69b.json";
		String xAppTokenkey = "UcUjKU9Ep6HDh374ILUvrlCFW";
		LocalDateTime current = LocalDateTime.now();
		int dayOfWeek = DayOfWeek.from(current).getValue();
		if (dayOfWeek == 7) {
			dayOfWeek = 0;
		}
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
		String time = current.format(formatter);

		String query = String.format("start24<='%1$s' AND end24>'%1$s' AND dayorder='%2$d'", time, dayOfWeek);
		JSONArray trucks = null;
		try {
			trucks = Unirest.get(host).queryString("$where", query).queryString("$select", "applicant,location")
					.queryString("$order", "applicant ASC").header("X-App-Token", xAppTokenkey).asJson().getBody()
					.getArray();
		} catch (UnirestException e) {
			System.out.println(e);
		}

		if (trucks == null) {
			System.out.println("No Food Trucks available right now!");
			return;
		}
		consoleOutput(trucks);
	}

	/**
	 * consoleOutput takes JSONArray as input and display results in pages of 10
	 * 
	 * @param trucks JSONArray of trucks
	 */
	public static void consoleOutput(JSONArray trucks) {
		Console console = System.console();
		if (console == null) {
			System.out.println("No Console Available");
			return;
		}

		Iterator<Object> foodTruck = trucks.iterator();
		String fmt = "%1$4s %2$80s%n";

		while (foodTruck.hasNext()) {
			console.format(fmt, "NAME", "ADDRESS");
			console.format(fmt, "----", "-------");
			for (int i = 0; i < 10; i++) {
				if (!foodTruck.hasNext())
					break;
				JSONObject truck = (JSONObject) foodTruck.next();
				console.format(fmt, truck.get("applicant"), truck.get("location"));
			}
			if (foodTruck.hasNext()) {
				console.format("Press <Enter> for more.");
				console.readLine();
			}
		}
	}
}
