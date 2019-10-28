package external;
import entity.Item;
import entity.Item.ItemBuilder;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TicketMasterAPI {
	private static final String URL = "https://app.ticketmaster.com/discovery/v2/events.json"; 
	private static final String DEFAULT_TERM = ""; // no restriction
	private static final String API_KEY = "bOag4mbaV0XqOGxS04laT1unqMp3dlcp";
	
	public List<Item> search(double lat, double lon, String term) { 
		// Encode term in url since it may contain special characters 
		if (term == null) {
			term = DEFAULT_TERM;
		}
		try {
			term = java.net.URLEncoder.encode(term, "UTF-8"); 
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// Convert lat/lon to geo point
		String geoHash = GeoHash.encodeGeohash(lat, lon, 8);
		
		// Make your url query part like: "apikey=12345&geoPoint=abcd&keyword=music&radius=50"
		String query = String.format("apikey=%s&geoPoint=%s&keyword=%s&radius=%s", API_KEY, geoHash, term, 50);
		
		try {
			// Open a HTTP connection between your Java application and TicketMaster based on url
			HttpURLConnection connection = (HttpURLConnection)new URL(URL +"?" + query).openConnection();
			
			// Set requrest method to GET
			connection.setRequestMethod("GET");
			
			// Send request to TicketMaster and get response, response code could be returned directly
			// response body is saved in InputStream of connection.
			int responseCode = connection.getResponseCode(); 
			System.out.println("\nSending 'GET' request to URL : " + URL + "?" + query); 
			System.out.println("Response Code : " + responseCode);
			
			// Now read response body to get events data
			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream())); 
			String inputLine;
			StringBuilder response = new StringBuilder(); 
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			JSONObject obj = new JSONObject(response.toString()); 
			if (obj.isNull("_embedded")) {
				return new ArrayList<>();
			}
			JSONObject embedded = obj.getJSONObject("_embedded"); 
			JSONArray events = embedded.getJSONArray("events"); 
			return getItemList(events);
		} catch (Exception e) { 
			e.printStackTrace();
		}
		return new ArrayList<>();
	}
	
	private void queryAPI(double lat, double lon) { 
		List<Item> itemList = search(lat, lon, null); 
		try {
			for (Item item : itemList) {
				JSONObject jsonObject = item.toJSONObject(); 
				System.out.println(jsonObject);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
	
	// Helper methods
	private JSONObject getVenue(JSONObject event) throws JSONException {
		if (!event.isNull("_embedded")) {
			JSONObject embedded = event.getJSONObject("_embedded"); 
			if (!embedded.isNull("venues")) {
				JSONArray venues = embedded.getJSONArray("venues"); 
				if (venues.length() > 0) {
					return venues.getJSONObject(0);
				} 
			}
		}
		return null;
	}

	private String getImageUrl(JSONObject event) throws JSONException {
		if(!event.isNull("images")) {
			JSONArray array = event.getJSONArray("images"); 
			for(int i = 0; i < array.length(); i++) {
				JSONObject image = array.getJSONObject(i); 
				if(!image.isNull("url")) {
					return image.getString("url");
				}
			 }
		}
		return null;
	}

	private Set<String> getCategories(JSONObject event) throws JSONException {
		if (!event.isNull("classifications")) {
			JSONArray classifications = event.getJSONArray("classifications"); 
			Set<String> categories = new HashSet<>();
			for (int i = 0; i < classifications.length(); i++) {
				JSONObject classification = classifications.getJSONObject(i); 
				if (!classification.isNull("segment")) {
					JSONObject segment = classification.getJSONObject("segment");
					if (!segment.isNull("name")) {
						String name = segment.getString("name"); 
						categories.add(name);
					} 
				}
			}
			return categories;
		}
		return null; 
	}

	// Convert JSONArray to a list of item objects.
	private List<Item> getItemList(JSONArray events) throws JSONException {
		List<Item> itemList = new ArrayList<>();
		for (int i = 0; i < events.length(); ++i) {
			JSONObject event = events.getJSONObject(i);
			ItemBuilder builder = new ItemBuilder();
			if (!event.isNull("name")) {
				builder.setName(event.getString("name"));
			}
			if (!event.isNull("id")) {
				builder.setItemId(event.getString("id"));
			}
			if (!event.isNull("url")) {
				builder.setUrl(event.getString("url"));
			}
			if (!event.isNull("rating")) {
				builder.setRating(event.getDouble("rating"));
			}
			if (!event.isNull("distance")) {
				builder.setDistance(event.getDouble("distance"));
			}
			// set address
			JSONObject venue = getVenue(event);
			if (venue != null) {
				StringBuilder sb = new StringBuilder(); 
				if (!venue.isNull("address")) {
					JSONObject address = venue.getJSONObject("address"); 
					if (!address.isNull("line1")) {
						sb.append(address.getString("line1"));
					}
					if (!address.isNull("line2")) {
						sb.append(address.getString("line2"));
					}
					if (!address.isNull("line3")) {
						sb.append(address.getString("line3")); 
					}
					sb.append(",");
				}
				if (!venue.isNull("city")) {
					JSONObject city = venue.getJSONObject("city");
					if (!city.isNull("name")) {
						sb.append(city.getString("name"));
					}
				}
				builder.setAddress(sb.toString());
			}
			builder.setImageUrl(getImageUrl(event));
			builder.setCategories(getCategories(event));
			Item item = builder.build();
			itemList.add(item);
		}
		return itemList;
	}
	
	/**
	 * Main entry for sample TicketMaster API requests. 
	 */
	public static void main(String[] args) {
		TicketMasterAPI tmApi = new TicketMasterAPI(); // Mountain View, CA
		// tmApi.queryAPI(37.38, -­122.08);
		// Houston, TX
		tmApi.queryAPI(29.682684, -95.295410);
	}
	
}
