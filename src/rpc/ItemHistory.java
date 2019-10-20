package rpc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import database.DBConnection;
import database.DBConnectionFactory;
import entity.Item;

/**
 * Servlet implementation class ItemHistory
 */
@WebServlet("/history")
public class ItemHistory extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ItemHistory() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String userId = request.getParameter("user_id"); 
		JSONArray array = new JSONArray();
		DBConnection conn = DBConnectionFactory.getDBConnection(); 
		Set<Item> items = conn.getFavoriteItems(userId);
		for (Item item : items) {
			JSONObject obj = item.toJSONObject(); 
			try {
				obj.append("favorite", true); 
			} catch (JSONException e) {
				e.printStackTrace(); 
			}
			array.put(obj);
		}
		HelperFunctions.writeJsonArray(response, array);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			JSONObject input = HelperFunctions.readJsonObject(request); 
			String userId = input.getString("user_id");
			JSONArray item_array = input.getJSONArray("favorite"); 
			List<String> itemIds = new ArrayList<>();
			for (int i = 0; i < item_array.length(); i++) {
				String itemId = item_array.get(i).toString(); 
				itemIds.add(itemId);
			}
			DBConnection conn = DBConnectionFactory.getDBConnection();
			conn.setFavoriteItems(userId, itemIds);
			HelperFunctions.writeJsonObject(response, new JSONObject().put("result","SUCCESS"));
		}catch(Exception e) {
			e.printStackTrace();
		}		
	}

	/**
	 * @see HttpServlet#doDelete(HttpServletRequest, HttpServletResponse)
	 */
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			JSONObject input = HelperFunctions.readJsonObject(request); 
			String userId = input.getString("user_id");
			JSONArray item_array = input.getJSONArray("favorite"); 
			List<String> itemIds = new ArrayList<>();
			for (int i = 0; i < item_array.length(); i++) {
				String itemId = item_array.get(i).toString(); 
				itemIds.add(itemId);
			}
			DBConnection conn = DBConnectionFactory.getDBConnection();
			conn.unsetFavoriteItems(userId, itemIds);
			HelperFunctions.writeJsonObject(response, new JSONObject().put("result","SUCCESS"));
		}catch(Exception e) {
			e.printStackTrace();
		}		
	}

}
