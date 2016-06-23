/*
 * @author Florian Krauter
 * @version 1.1.2
 * 
 */
package de.ps15.campusquiz;

import java.security.SecureRandom;
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

// TODO: Auto-generated Javadoc
/**
 * Die Klasse QuestionSelector.
 */
@Path("/QuestionService") 
public class QuestionSelector {

	/**  SQL-String, um eine Frage zu einem Fachbereich zu selektieren. */
	String sqlGetQuestion = "SELECT * "
			+ "FROM fragen F "
			+ "JOIN ht_fragen_disziplinen htfd "
			+ "ON htfd.Fragen_ID = F.Fragen_ID "
			+ "JOIN disziplinen D "
			+ "ON htfd.DZ_ID = D.DZ_ID "
			+ "JOIN fachbereiche FB "
			+ "ON FB.FB_ID = D.FB_ID "
			+ "WHERE FB.FB_ID = ? "
			+ "AND F.Fragen_ID = ?";
	
	String sqlGetQuestion2 = "SELECT * "
			+ "FROM fragen F "
			+ "WHERE F.Fragen_ID = ?";
	
	
	/** SQL-String, um Fachbereiche zu zählen. */
	String sqlCountFB = 
			 "SELECT COUNT(*) "
			+"FROM  ht_fragen_disziplinen HTFD "
			+"JOIN disziplinen D "
			+"ON HTFD.DZ_ID = D.DZ_ID "
			+"WHERE D.FB_ID = ?";
	
	/** SQL-String, um Fachbereiche zu zählen. */
	String sqlFragen_IDsFB = 
			 "SELECT htfd.Fragen_ID "
			+"FROM  ht_fragen_disziplinen HTFD "
			+"JOIN disziplinen D "
			+"ON HTFD.DZ_ID = D.DZ_ID "
			+"WHERE D.FB_ID = ?";
	
	/** SQL-String, um mögliche Fragen-IDs zu Subkategorie zu bekommen. */
	String sqlGetFIDsByHk = 
			"SELECT F.Frange_ID "
			+ "FROM fragen F "
			+ "LEFT JOIN ht_fragen_disziplinen HTFD "
			+ "ON F.Fragen_ID = HTFD.Fragen_ID "
			+ "JOIN disziplinen DZ "
			+ "ON HTFD.DZ_ID = DZ.DZ_ID "
			+ "WHERE DZ.FB_ID = 11";
	
	/** SQL-String, um mögliche Fragen-IDs zu Subkategorie zu bekommen. */
	String sqlGetFIDsBySk = 
			"SELECT F.Fragen_ID "
			+ "FROM fragen F "
			+ "LEFT JOIN ht_fragen_disziplinen HTFD "
			+ "ON F.Fragen_ID = HTFD.Fragen_ID "
			+ "WHERE HTFD.DZ_ID = ?";
	
	/** ResultSet um IDs nach Hauptkategorie abzuspeichern. */
	static ResultSet ResultSet_QIDByHk = null;
	
	/** ResultSet um IDs nach Subkategorie abzuspeichern. */
	static ResultSet ResultSet_QIDBySk = null;
	
	/** JSON um die Fragen abzuspeichern. */
	//static JSONArray JSON_Questions = null;
	static JSONObject JSON_Questions = null;
	
	/** Ein Hilfs-Konverter, um ResultSets in JSON zu konvertieren. */
	ResultSetConverter JSONConverter = new ResultSetConverter();
	
	
	/**
	 * Methode um Fragen zu selektieren.
	 *
	 * @param fachbereich Fachbereichs-ID
	 * @param menge Menge an zu selektierenden Fragen
	 * @return Menge an Fragen in JSON-Format
	 * @throws SQLException the SQL exception
	 * @throws ClassNotFoundException the class not found exception
	 */
	@GET
	@Path("/getQuestions")
	@Produces(MediaType.APPLICATION_JSON)
	public String getQuestions(@QueryParam("fachbereich") int fachbereich,@QueryParam("menge") int menge) throws SQLException, ClassNotFoundException{
		int[] hk_ids = getAvailableQ(fachbereich);
		String response = "";
		//TODO: Error message abfangen und als Json Message ausgeben. 
		if(hk_ids.length<menge){
			System.out.println("Zu wenig Fragen");
			return "Zu wenig Fragen";
			//return response = Utility.constructJSON("Question", false);
			
		}else if(menge > 20){
			return "Maximale Menge (max. 20) erreicht!";			
		}else{
			//return response = Utility.constructJSON("Question", true, inquireQuestions(fachbereich, menge, getAvailableQ(fachbereich)));
			return inquireQuestions(fachbereich, menge, hk_ids);
			}
		}
		


	/**
	 * Ermittelt die maximale Menge an Fragen zu einem Fachbereich.
	 *
	 * @param fachbereich Fachbereichs-ID
	 * @return Maximale Menge an Fragen im jeweiligen Fachbereich
	 * @throws SQLException the SQL exception
	 * @throws ClassNotFoundException the class not found exception
	 */
	private int[] getAvailableQ(int fachbereich) throws SQLException, ClassNotFoundException {
			int range = 0;
		
			Connection conn = null;
			Statement stmt = null;
			ResultSet rs = null;  

		      //Register JDBC driver
		      Class.forName("com.mysql.jdbc.Driver");
		      //Open connection
		      //System.out.print("Connecting to database...isAlreadyInDb...");
		      conn = DriverManager.getConnection(Credentials.DB_URL,Credentials.USER,Credentials.PASS);     
		      stmt = conn.createStatement();   
		      
		      PreparedStatement ps = conn.prepareStatement(sqlFragen_IDsFB);
		      ps.setInt(1,fachbereich);
		      rs = ps.executeQuery();
		      
		     /* int size= 0;
		      if (rs != null)   
		      {  
		        rs.beforeFirst();  
		        rs.last();  
		        size = rs.getRow();  
		      } 
		      rs.first();

		      if(size>=0){
		    	  range = rs.getInt(1);
		      }*/
		      
		      //int[] hk_ids = rs.getArray("Fragen_ID").;
		      
		      int size =0;
		      if (rs != null) 
		      {
		        rs.beforeFirst();
		        rs.last();
		        size = rs.getRow();
		        //System.out.println(size+" Elemente");
		      }
		      rs.first();
		      
		      int[] hk_ids = new int[size];
		      while(rs.next()){
		    	  //System.out.println("Baue Array "+rs.getRow()-1);
		    	  hk_ids[rs.getRow()-1] = rs.getInt("Fragen_ID");
		      }
		      
		      stmt.close();
		      conn.close();
		 
		         if(stmt!=null)
		            stmt.close();
		      
		         if(conn!=null)
		            conn.close();
		         //System.out.println("RANGE FÜR FB"+fachbereich+": "+range);
		return hk_ids;
	}

	/**
	 * Selektiert eine Menge an Fragen zu einem Fachbereich.
	 *
	 * @param fachbereich Fachbereichs-ID
	 * @param menge Menge an zu selektierenden Fragen
	 * @param range Maximal mögliche Anzahl an zu selektierenden Fragen in einem Fachbereich
	 * @return JSON-Array mit n(=Menge) Fragen
	 * @throws SQLException the SQL exception
	 * @throws ClassNotFoundException the class not found exception
	 */
	private String inquireQuestions(int fachbereich,int menge,int[] hk_ids) throws SQLException, ClassNotFoundException {
		String query = "";
		int range = hk_ids.length;
		SecureRandom rand = new SecureRandom();
		
		for(int i = 0; i < menge; i++){
			if(i+1 == menge){
				query = query + sqlGetQuestion2;
			}else{
				query = query + sqlGetQuestion2 + " UNION ";			
			}
			System.out.print(i);
		}
		System.out.println(query);
		
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null; 
		
		//Register JDBC driver
	      Class.forName("com.mysql.jdbc.Driver");
	      //Open connection
	      conn = DriverManager.getConnection(Credentials.DB_URL,Credentials.USER,Credentials.PASS);     
	      stmt = conn.createStatement();   
	      
	      PreparedStatement ps = conn.prepareStatement(query);
		
	     // System.out.println(menge*2);
/*	      
		for(int i  = 1; i <= (menge*2); i+=2){		
			int num = 1+rand.nextInt(range);
			System.out.println(i+"Einsetzen in FB "+fachbereich+" "+(i+1)+"Num "+hk_ids[num]);
			ps.setInt(i, fachbereich);
			ps.setInt((i+1), hk_ids[num]);	
		}*/
		
		for(int i  = 1; i <= menge; i++){		
			int num = 1+rand.nextInt(range);
			System.out.println(i+"...   "+"Num "+hk_ids[num]);
			ps.setInt(i, hk_ids[num]);
		}
		
		System.out.println("Query: "+ps.toString());
	      rs = ps.executeQuery();

	      try {
				//JSON_Questions = JSONConverter.convert(rs);
				JSON_Questions = JSONConverter.converter_question(rs);
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

	    
		return JSON_Questions.toString();
	}

	
	
	private ResultSet getQuestionIdListByHauptkategorie(int hauptkategorie) throws SQLException, ClassNotFoundException{
		
		Connection conn = null;
		Statement stmt = null;
		
		//Register JDBC driver
	      Class.forName("com.mysql.jdbc.Driver");
	      //Open connection
	      conn = DriverManager.getConnection(Credentials.DB_URL,Credentials.USER,Credentials.PASS);     
	      stmt = conn.createStatement();   
	      
	      PreparedStatement ps = conn.prepareStatement(sqlGetFIDsByHk);
	      ps.setInt(1, hauptkategorie);
	      ResultSet_QIDByHk = ps.executeQuery();
		
		stmt.close();
	      conn.close();
	 
	         if(stmt!=null)
	            stmt.close();
	      
	         if(conn!=null)
	            conn.close();
		return ResultSet_QIDByHk;
	}
	
private ResultSet getQuestionIdListBySubkategorie(int subkategorie) throws SQLException, ClassNotFoundException{
		
		Connection conn = null;
		Statement stmt = null;
		
		//Register JDBC driver
	      Class.forName("com.mysql.jdbc.Driver");
	      //Open connection
	      conn = DriverManager.getConnection(Credentials.DB_URL,Credentials.USER,Credentials.PASS);     
	      stmt = conn.createStatement();   
	      
	      PreparedStatement ps = conn.prepareStatement(sqlGetFIDsBySk);
	      ps.setInt(1, subkategorie);
	      ResultSet_QIDBySk = ps.executeQuery();
		
		stmt.close();
	      conn.close();
	 
	         if(stmt!=null)
	            stmt.close();
	      
	         if(conn!=null)
	            conn.close();
		return ResultSet_QIDBySk;
	}
}
