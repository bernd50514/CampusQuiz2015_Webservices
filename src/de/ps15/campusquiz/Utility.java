package de.ps15.campusquiz;

import java.sql.SQLException;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class Utility {
	/**
	 * Null check Method
	 * 
	 * @param txt
	 * @return
	 */
	public static boolean isNotNull(String txt) {
		// System.out.println("Inside isNotNull");
		return txt != null && txt.trim().length() >= 0 ? true : false;
	}
	/**
	 * Method to construct JSON
	 * 
	 * @param tag
	 * @param status
	 * @return
	 */
	public static String constructJSON(String tag, boolean status) {
		JSONObject obj = new JSONObject();
		try {
			obj.put("tag", tag);
			obj.put("status", new Boolean(status));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
		}
		return obj.toString();
	}
	/**
	 * Method to construct JSON
	 * 
	 * @param tag
	 * @param status
	 * @param userinfo
	 * @param email
	 * @param studiengangID
	 * @param uniID
	 * @return
	 */
	public static String constructJSON(String tag, boolean status, String username, String email) {
		JSONObject obj = new JSONObject();
	
		try {
			String UniName = DBConnectionTest.getUniName(username);
			String StudiengangName= DBConnectionTest.getStudiengangName(username); 
			
			obj.put("tag", tag);
			obj.put("status", new Boolean(status));
			obj.put("username", username);
			obj.put("email", email);
			obj.put("studiengang", StudiengangName);
			obj.put("uni", UniName);
	
		} catch (JSONException | SQLException e) {
			// TODO Auto-generated catch block
		}
		return obj.toString();
	}

	/**
	 * Method to construct JSON with Error Msg
	 * 
	 * @param tag
	 * @param status
	 * @param err_msg
	 * @return
	 */
	public static String constructJSON(String tag, boolean status,String err_msg) {
		JSONObject obj = new JSONObject();
		try {
			obj.put("tag", tag);
			obj.put("status", new Boolean(status));
			obj.put("error_msg", err_msg);
		
		} catch (JSONException e) {
			// TODO Auto-generated catch block
		}
		return obj.toString(); 
	}
	
}