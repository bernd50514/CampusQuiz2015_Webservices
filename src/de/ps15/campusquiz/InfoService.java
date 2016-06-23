package de.ps15.campusquiz;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@Path("/InfoService")
public class InfoService {

	String sqlGetFachbereiche = 
			"SELECT * "
			+ "FROM fachbereiche";
	
	String sqlGetStudiengaenge = 
			"SELECT * "
			+ "FROM studiengaenge";
	
	String sqlGetSubkategorienByHauptkategorie = 
			"SELECT DZ.DZ_ID, DZ.Vollname, DZ.Kurzname "
			+ "FROM disziplinen DZ "
			+ "LEFT OUTER JOIN fachbereiche FB "
			+ "ON DZ.FB_ID = FB.FB_ID "
			+ "WHERE DZ.FB_ID = ?";
	
	//static JSONArray JSON_Hauptkategorien = null;
	//static JSONArray JSON_Studiengaenge = null;
	static JSONArray JSON_Subkategorien = null;
	static JSONObject JSON_Hauptkategorien = null;
 	static JSONObject JSON_Studiengaenge = null;
	//static JSONObject JSON_Subkategorien = null;
	
	ResultSetConverter JSONConverter = new ResultSetConverter();
@GET
@Path("/getStudiengaenge")
@Produces(MediaType.APPLICATION_JSON)
public String getStudiengaenge() throws ClassNotFoundException, SQLException {
		
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;  

	      //Register JDBC driver
	      Class.forName("com.mysql.jdbc.Driver");
	      //Open connection
	      //System.out.print("Connecting to database...isAlreadyInDb...");
	      conn = DriverManager.getConnection(Credentials.DB_URL,Credentials.USER,Credentials.PASS);     
	      stmt = conn.createStatement();   
	      
	      PreparedStatement ps = conn.prepareStatement(sqlGetStudiengaenge);
	      rs = ps.executeQuery();
	      try {
	    	  JSON_Studiengaenge = JSONConverter.converter_studiengang(rs);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	      
	      stmt.close();
	      conn.close();
	 
	         if(stmt!=null)
	            stmt.close();
	      
	         if(conn!=null)
	            conn.close();

	    
		return JSON_Studiengaenge.toString();
		}


@GET
@Path("/getFb")
@Produces(MediaType.APPLICATION_JSON)
public String getFb() throws ClassNotFoundException, SQLException {
	
	Connection conn = null;
	Statement stmt = null;
	ResultSet rs = null;  

      //Register JDBC driver
      Class.forName("com.mysql.jdbc.Driver");
      //Open connection
      //System.out.print("Connecting to database...isAlreadyInDb...");
      conn = DriverManager.getConnection(Credentials.DB_URL,Credentials.USER,Credentials.PASS);     
      stmt = conn.createStatement();   
      
      PreparedStatement ps = conn.prepareStatement(sqlGetFachbereiche);
      rs = ps.executeQuery();
      try {
    	  JSON_Hauptkategorien = JSONConverter.converter_fachbereiche(rs);
	} catch (JSONException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
     
      stmt.close();
      conn.close();
 
         if(stmt!=null)
            stmt.close();
      
         if(conn!=null)
            conn.close();

    
	return JSON_Hauptkategorien.toString();
	}

	@GET
	@Path("/getSubkategorieByHauptkategorie")
	@Produces(MediaType.APPLICATION_JSON)
	public String getSubkategorieByHauptkategorie(@QueryParam("hauptkategorie") int hauptkategorie) throws SQLException, ClassNotFoundException{
		
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;  

	      //Register JDBC driver
	      Class.forName("com.mysql.jdbc.Driver");
	      //Open connection
	      //System.out.print("Connecting to database...isAlreadyInDb...");
	      conn = DriverManager.getConnection(Credentials.DB_URL,Credentials.USER,Credentials.PASS);     
	      stmt = conn.createStatement();  
	      
	      
		      PreparedStatement ps = conn.prepareStatement(sqlGetSubkategorienByHauptkategorie);
		      ps.setInt(1, hauptkategorie);
		      rs = ps.executeQuery();
		      try {
		    	  JSON_Subkategorien = JSONConverter.convert(rs);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	      
	      stmt.close();
	      conn.close();
	 
	         if(stmt!=null)
	            stmt.close();
	      
	         if(conn!=null)
	            conn.close();

		
		return JSON_Subkategorien.toString();
	}
}
