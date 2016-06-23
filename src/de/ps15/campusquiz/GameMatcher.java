/*
 * @author Florian Krauter
 * @version 0.1337 Nifty Shenanigans
 * 
 */

package de.ps15.campusquiz;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

// TODO: Auto-generated Javadoc
/**
 * The Class GameMatcher.
 */
@Path("/GameMatchingService") 
public class GameMatcher {
	
	/** SQL-String, um offene Spiele zu zählen. */
	String sqlCountOpenGames = 
			 "SELECT COUNT(*) "
			 + "FROM  gamesearch G "
			 + "JOIN fachbereiche F "
			 + "ON G.FB_ID = F.FB_ID "
			 + "WHERE G.FB_ID = ?";
	
	/** SQL-String, um neue Spielgesuche anzulegen. */
	String sqlWriteNewGameSearch = 
			"INSERT INTO gamesearch ( username ,  fb_id ,  exp_date ) "
			+ "VALUES (?,?,DATE_ADD(now(), INTERVAL ? HOUR))";
	
	/** SQL-String, um offene Spiel zu zählen. */
	String sqlSelectOpenGame = 
			 "SELECT * "
			 + "FROM  gamesearch G "
			 + "WHERE G.FB_ID = ? "
			 + "LIMIT 5";
	
	/** SQL-String, um neue Multiplayer-Spiele zu starten. */
	String sqlCreateGame = 
			"	INSERT INTO games (username1, username2, exp_date, fb_id, activeplayer, stage) "
			+ "VALUES (?,?,DATE_ADD(now(), INTERVAL ? HOUR),?,1,1)";
	
	/** SQL-String, um offene Spiel zu zählen. */
	String sqlRemoveGameSearch = "DELETE FROM gamesearch WHERE search_id = ?";
		
	/** SQL-String, um offene Spiel von Speziellem User in Game-DB zu zählen. */
	String sqlCountUserSearches = "SELECT COUNT(*) FROM gamesearch WHERE username = ?";
	
	/** SQL-String, um offene Spiel von Speziellem User in Game-Search-DB zu zählen. */
	String sqlCountUserGames = "SELECT COUNT(*) FROM games WHERE username1 = ? OR username2 = ?";
	
	/** SQL-String, um offene Spiele zwischen zwei Nutzern zu suchen */
	String sqlCountActiveVsGames = 
			"SELECT COUNT(*) "
			+ "FROM games "
			+ "WHERE username1 = ? AND username2 = ? "
			+ "OR username1 = ? AND username2 = ?";
	
	String sqlGetActiveGameID = 
			"SELECT G.game_id "
			+ "FROM games G "
			+ "WHERE G.username1 = ? "
			+ "AND G.username2 = ?";
	
	String sqlInsertStage = 
			"INSERT INTO game_stages "
			+ "(Stage_ID, Game_ID, Stage_Nr, Frage1_ID, Frage2_ID, Frage3_ID, User1_Pt, User2_Pt, F1U1_Result, F2U1_Result, F3U1_Result, F1U2_Result, F2U2_Result, F3U2_Result) "
			+ "VALUES (NULL, ?, ?, NULL, NULL, NULL, 0, 0, NULL, NULL, NULL, NULL, NULL, NULL);";



	
	/** Ein Hilfs-Konverter, um ResultSets in JSON zu konvertieren. */
	ResultSetConverter JSONConverter = new ResultSetConverter();
	
	
/**
 * Place game request.
 *
 * @param fachbereich the fachbereich
 * @param username the username
 * @param hours the hours
 * @return the string
 * @throws ClassNotFoundException the class not found exception
 * @throws SQLException the SQL exception
 */
@GET
@Path("/placeGameRequest")
	public String placeGameRequest(@QueryParam("fachbereich") int fachbereich,@QueryParam("username") String username,@QueryParam("hours") int hours) throws ClassNotFoundException, SQLException{
		if(isAlreadyInDatabases(username)){
			int availGames = searchAvailableGames(fachbereich);
			if(availGames == 0){
				registerGameSearch(fachbereich, username, hours);
				return "Gesuch aufgegeben";
			}else{
				int gameId = registerNewGame(fachbereich,username);
				if(gameId>=0){
					return "Vorhandene Games gefunden! - Game gematched, ID: "+gameId;
				}else{
					return "Bereits vorhandene VS-Spiele"; 
				}
				//Game starten und Sucheintrag entfernen
			}
		}else{
			//User hat bereits offene Spiel-Gesuche oder offene Spiele
			return "User hat bereits offene Spiel-Gesuche oder offene Spiele";
		}
	}

	/**
	 * Register new game.
	 *
	 * @param fachbereich the fachbereich
	 * @param username1 the username1
	 * @return the int
	 * @throws ClassNotFoundException the class not found exception
	 * @throws SQLException the SQL exception
	 */
	private int registerNewGame(int fachbereich, String username1) throws ClassNotFoundException, SQLException{
		String username2 = "";
		int gameSearchId = 0;
		int hours = 72;
		
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		ResultSet rsVS = null;

		      //Register JDBC driver
		      Class.forName(Credentials.JDBC_DRIVER);
		      //Open connection
		      conn = DriverManager.getConnection(Credentials.DB_URL,Credentials.USER,Credentials.PASS);     
		      stmt = conn.createStatement();  
		      
			      PreparedStatement ps = conn.prepareStatement(sqlSelectOpenGame);
			      ps.setInt(1, fachbereich);
			      rs = ps.executeQuery();
			      
			      //Select GameSearch
			      //Lookup each username-combination if already in VS-Mode
			      while (rs.next()) {
			    	  username2 = rs.getString("username");
			    	  gameSearchId = rs.getInt("search_id");
			    	  
			    	  
			    	  //Lookup VS-Games
			    	  PreparedStatement psVS = conn.prepareStatement(sqlCountActiveVsGames);
				      psVS.setString(1, username1);
				      psVS.setString(2, username2);
				      psVS.setString(3, username2);
				      psVS.setString(4, username1);
				      //System.out.println("LOOKUP: "+psVS.toString());
				      rsVS = psVS.executeQuery();
				      
				      //Get count
				      int size= 0;
				      if (rsVS != null)   
				      {  
				        rsVS.beforeFirst();  
				        rsVS.last(); 
				        size = rsVS.getInt("COUNT(*)");
				        //size = rsVS.getRow();
				      } 
				      rsVS.first();
				      
				      //If no VS-Game is found, break and continue with registering
				      if(size == 0){
				    	  //System.out.println("No VS-Game Found  "+size);
				    	  break;
				      }else{
				    	  return -1;				    	  
				      }	      
			      }
			      
			      
			      //Remove GameSearch
			      PreparedStatement ps2 = conn.prepareStatement(sqlRemoveGameSearch);
			      ps2.setInt(1, gameSearchId);
			      //System.out.println(ps2.toString());
			      ps2.execute();
			      
			      //Register Game in GAMES-DATABASE
			      PreparedStatement ps3 = conn.prepareStatement(sqlCreateGame);
			      //username1, username2, exp_date, fb_id, activeplayer, stage
			      ps3.setString(1,username1);
			      ps3.setString(2, username2);
			      ps3.setInt(3, hours);
			      ps3.setInt(4, fachbereich);
			      //System.out.println(ps3.toString());
			      ps3.execute();
			      
			      //GetActiveGame
			      PreparedStatement ps4 = conn.prepareStatement(sqlGetActiveGameID);
			      ps4.setString(1, username1);
			      ps4.setString(2, username2);
			      ResultSet rsActiveGameID = ps4.executeQuery();      
			      
			      int activeGameID = 0;
			      if (rsActiveGameID != null)   
			      {  
			    	rsActiveGameID.beforeFirst();  
			    	rsActiveGameID.last(); 
			    	activeGameID = rsActiveGameID.getInt("game_id");
			      } 
			      rsActiveGameID.first();
			      
			      //Register 4 Stages for GameId
			      PreparedStatement ps5 = conn.prepareStatement(sqlInsertStage);//+" "+sqlInsertStage+" "+sqlInsertStage+" "+sqlInsertStage);
			      System.out.println(ps5.toString());
			      
			      for(int i = 0; i < 4 ; i++){
			    	  ps5.setInt(1, activeGameID);	//GameId
				      ps5.setInt(2, i+1); 			//Stage
				      ps5.execute();
			      }		      
			      
			      
			      
		      
		      stmt.close();
		      conn.close();
		 
	         if(stmt!=null)
	            stmt.close();
	      
	         if(conn!=null)
	            conn.close();
		         
	       //System.out.println("Username2: "+username2+" | Gamesearch_ID: "+gameSearchId);		
		
		return gameSearchId;
	}

	/**
	 * Register game search.
	 *
	 * @param fachbereich the fachbereich
	 * @param username the username
	 * @param hours the hours
	 * @return true, if successful
	 * @throws ClassNotFoundException the class not found exception
	 * @throws SQLException the SQL exception
	 */
	private boolean registerGameSearch(int fachbereich, String username, int hours) throws ClassNotFoundException, SQLException{
		Connection conn = null;
		Statement stmt = null; 

		      //Register JDBC driver
		      Class.forName(Credentials.JDBC_DRIVER);
		      //Open connection
		      conn = DriverManager.getConnection(Credentials.DB_URL,Credentials.USER,Credentials.PASS);     
		      stmt = conn.createStatement();   
		      
		      PreparedStatement ps = conn.prepareStatement(sqlWriteNewGameSearch);
		      ps.setString(1, username);
		      ps.setInt(2,fachbereich);
		      ps.setInt(3,hours);
		      ps.execute();
		      
		      stmt.close();
		      conn.close();
		 
		         if(stmt!=null)
		            stmt.close();
		      
		         if(conn!=null)
		            conn.close();
		return true;
	}
	
	

	/**
	 * Search available games.
	 *
	 * @param fachbereich the fachbereich
	 * @return the int
	 * @throws SQLException the SQL exception
	 * @throws ClassNotFoundException the class not found exception
	 */
	private int searchAvailableGames(int fachbereich) throws SQLException, ClassNotFoundException{
		 int gamesInDb = 0;
		 
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;  

		      //Register JDBC driver
		      Class.forName(Credentials.JDBC_DRIVER);
		      //Open connection
		      conn = DriverManager.getConnection(Credentials.DB_URL,Credentials.USER,Credentials.PASS);     
		      stmt = conn.createStatement();   
		      
		      PreparedStatement ps = conn.prepareStatement(sqlCountOpenGames);
		      ps.setInt(1, fachbereich);
		      rs = ps.executeQuery();
		      
		      int size= 0;
		      if (rs != null)   
		      {  
		        rs.beforeFirst();  
		        rs.last();  
		        size = rs.getRow();  
		      } 
		      rs.first();
		      
		      if(size>=0){
		    	  gamesInDb = rs.getInt(1);
		      }
		      
		      stmt.close();
		      conn.close();
		 
		         if(stmt!=null)
		            stmt.close();
		      
		         if(conn!=null)
		            conn.close();
		
		return gamesInDb;
	}
	
	/**
	 * Checks if is already in databases.
	 *
	 * @param username the username
	 * @return true, if is already in databases
	 * @throws ClassNotFoundException the class not found exception
	 * @throws SQLException the SQL exception
	 */
	private boolean isAlreadyInDatabases(String username) throws ClassNotFoundException, SQLException{
		boolean inSearchDb = false;
		//boolean inGameDb = false;
		
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null; 
		//ResultSet rs2 = null;

		      //Register JDBC driver
		      Class.forName(Credentials.JDBC_DRIVER);
		      //Open connection
		      conn = DriverManager.getConnection(Credentials.DB_URL,Credentials.USER,Credentials.PASS);     
		      stmt = conn.createStatement();   
		      
		      PreparedStatement ps = conn.prepareStatement(sqlCountUserSearches);
		      ps.setString(1, username);
		      //System.out.println(ps.toString());
		      rs = ps.executeQuery();
		      
		      int size= 0;
		      if (rs != null)   
		      {  
		        rs.beforeFirst();  
		        rs.last();  
		        size = rs.getRow();  
		      } 
		      rs.first();
		      
		      if(size>=0){
		    	  if(rs.getInt(1)>0){inSearchDb=true;};
		      }
		      
		      if(stmt!=null)
		            stmt.close();
		      
		         if(conn!=null)
		            conn.close();
/*		      
		      PreparedStatement ps2 = conn.prepareStatement(sqlCountUserGames);
		      ps2.setString(1, username);
		      ps2.setString(2, username);
		      System.out.println(ps2.toString());
		      rs2 = ps.executeQuery();
		      
		      int size2= 0;
		      if (rs2 != null)   
		      {  
		        rs2.beforeFirst();  
		        rs2.last();  
		        size2 = rs2.getRow();  
		      } 
		      rs2.first();
		      
		      if(size2>=0){
		    	  if(rs2.getInt(1)>0){inGameDb=true;};
		      }
		      
		      stmt.close();
		      conn.close();
		 
		         if(stmt!=null)
		            stmt.close();
		      
		         if(conn!=null)
		            conn.close();*/
		//System.out.println("Benutzer in Spiele-Tabelle:"+inGameDb);
		//System.out.println("Benutzer in Gesuche-Tabelle:"+inSearchDb);
		//if(inGameDb || inSearchDb){
		if(inSearchDb){
			return false;
		}else{
			return true;
		}
	}

}
