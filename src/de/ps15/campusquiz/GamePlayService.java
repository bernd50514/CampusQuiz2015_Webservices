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



@Path("/GamePlayService") 
public class GamePlayService {
	
	static String sqlStageSubmitUser1 = 
			"UPDATE game_stages "
			+ "SET F1U1_Result = ?, F2U1_Result = ?, F3U1_Result = ? "
			+ "WHERE game_id = ? "
			+ "AND Stage_Nr = ?";
	
	static String sqlStageSubmitUser2 = 
			"UPDATE game_stages "
			+ "SET F1U2_Result = ?, F2U2_Result = ?, F3U2_Result = ? "
			+ "WHERE game_id = ? "
			+ "AND Stage_Nr = ?";
	
	static String sqlGetUsernames = 
			"SELECT username1, username2 "
			+ "FROM games "
			+ "WHERE game_id = ?;";

	// TODO: usernummer über Userid (String) ermitteln
	@GET
	@Path("/submitStageResults")	
	public String submitStageResults(@QueryParam("game_id") int game_id,@QueryParam("username") String username,@QueryParam("stage") int stage,@QueryParam("Q1") boolean Q1,@QueryParam("Q2") boolean Q2,@QueryParam("Q3") boolean Q3) throws ClassNotFoundException, SQLException{
		Connection conn = null;
		Statement stmt = null;
		String sqlSelector = null;
		
		//Register JDBC driver
	      Class.forName(Credentials.JDBC_DRIVER);
	      //Open connection
	      conn = DriverManager.getConnection(Credentials.DB_URL,Credentials.USER,Credentials.PASS);     
	      stmt = conn.createStatement(); 
	      
	      
		int Answer1 = 0;
		int Answer2 = 0;
		int Answer3 = 0;
		
		int user = 0;
	
		
		if(Q1){Answer1=1;}
		if(Q2){Answer2=1;}
		if(Q3){Answer3=1;}
		
		PreparedStatement ps_usernames = conn.prepareStatement(sqlGetUsernames);
		ps_usernames.setInt(1, game_id);
		ResultSet rs_usernames = ps_usernames.executeQuery();
		
		while(rs_usernames.next()){
			String temp_user1 = rs_usernames.getString("username1");
			String temp_user2 = rs_usernames.getString("username2");
			if(temp_user1.equalsIgnoreCase(username)){
				user = 1;
			}else if(temp_user2.equalsIgnoreCase(username)){
				user = 2;
			}else{
				user = -1;
			}
		}
		
				
		if(user == 1){
			sqlSelector = sqlStageSubmitUser1;
		}else if(user == 2){
			sqlSelector = sqlStageSubmitUser2;		
		}else{
			return "Error";
		}
		
		
	      PreparedStatement ps = conn.prepareStatement(sqlSelector);
	      ps.setInt(1, Answer1);
	      ps.setInt(2, Answer2);
	      ps.setInt(3, Answer3);
	      ps.setInt(4, game_id);
	      ps.setInt(5, stage);
	      System.out.println(ps.toString());
	      ps.executeUpdate();
	      
	      if(stmt!=null)
	            stmt.close();
	      
	         if(conn!=null)
	            conn.close();
		
	         checkGameEnd(stage, user);
		return "GO AWAY >:O  ";
	}
	
	private boolean checkGameEnd(int stage, int usernum){
		if(stage == 4 && usernum == 1){
			//Close Game && Compute Score
		}
		return false;
	}

	private boolean generateQuestions(int stage, int hauptkategorie, int subkategorie){
		//
		
		
		return false;
	}
	
}
