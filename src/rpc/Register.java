package rpc;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONException;
import org.json.JSONObject;

import database.DBConnection;
import database.DBConnectionFactory;

/**
 * Servlet implementation class Register
 */
@WebServlet("/register")
public class Register extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Register() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		DBConnection conn = DBConnectionFactory.getDBConnection(); 
		try {
			JSONObject obj = new JSONObject(); 
			HttpSession session = request.getSession(false); 
			if (session == null) {
				response.setStatus(403);
				obj.put("status", "Session Invalid"); 
			} else {
				String userId = (String) session.getAttribute("user_id"); 
				String name = conn.getFullname(userId); 
				obj.put("status", "OK");
				obj.put("user_id", userId);
				obj.put("name", name);
			 }
			HelperFunctions.writeJsonObject(response, obj); 
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		DBConnection conn = DBConnectionFactory.getDBConnection(); 
		try {
			JSONObject input = HelperFunctions.readJsonObject(request); 
			String userId = input.getString("user_id");
			String pw = input.getString("password");
			String first_name = input.getString("first_name");
			String last_name = input.getString("last_name");
			JSONObject obj = new JSONObject();
			conn.addNewUser(userId, pw, first_name, last_name);
			HttpSession session = request.getSession(); 
			session.setAttribute("user_id", userId);
			// setting session to expire in 10 minutes 
			session.setMaxInactiveInterval(10 * 60);
			// Get user name
			String name = conn.getFullname(userId); 
			obj.put("status", "OK");
			obj.put("user_id", userId);
			obj.put("name", name); 
			HelperFunctions.writeJsonObject(response, obj); 
		} catch (JSONException e) {
			e.printStackTrace();
		}		 
	}

}
