package de.ps15.campusquiz;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Context;

@Path("/activate")
public class Activate {
	// HTTP Get Method
	@GET
	// Path: http://localhost/<appln-folder-name>/activate/doActivation
	@Path("/doActivation")
	// Produces JSON as response
	@Produces(MediaType.APPLICATION_JSON)
	// Query parameters are parameters:
	// http://locnamealhost/useraccount/activate/doActivation?activeCode=12312313asdasdassdf876dsfuia.....
	public String doActivation(@Context UriInfo info)
			throws NoSuchAlgorithmException, InvalidKeySpecException, Exception {

		String response = "";
		String activationCode = info.getQueryParameters()
				.getFirst("activeCode");// get activation Code
		int retCode = processActivate(activationCode);
		if (retCode == 0) {
			response = "Herzlich Glückwunsch! Ihr Konto ist jetzt aktiviert! Bitte loggen Sie ein!";
			//response = Utility.constructJSON("activate", true,"Herzlich Glückwunsch! Ihr Konto ist jetzt aktiviert! Bitte loggen Sie ein!");
		} else if (retCode == 1) {
			response = Utility.constructJSON("activate", false,
					"No Such user exits! Please go to registration");
		} else if (retCode == 2) {
			//response = Utility.constructJSON("activate", false,
				//	"Activation Time out!");
			response = "Activation Time out!";
		} else if (retCode == 3) {
			response = Utility.constructJSON("activate", false,
					"SQL Error occured");
		}
		return response;
	}

	public int processActivate(String activationCode)
			throws NoSuchAlgorithmException, InvalidKeySpecException, Exception {
		int result = 0; // not works
		Connection dbConn = null;
		try {
			try {
				dbConn = DBConnection.createConnection();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		
			PreparedStatement ps = null;
			String sql = "SELECT email, activation_code, expiresActionDate FROM user WHERE (activation_Code = ?)";
			ps = dbConn.prepareStatement(sql);

			ps.setString(1, activationCode);
			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				
				String dbUserEmail = rs.getString("email");
				String dbExpiredDate = rs.getString("expiresActionDate");
				System.out.println("Test useremail in DB: " + dbUserEmail);
				if (dbUserEmail == null) {
					
					System.out.println("No such user");
					result = 1;
				} else {

					DateFormat dateFormat = new SimpleDateFormat(
							"yyyy-MM-dd hh:mm:ss");
					long expires = dateFormat.parse(dbExpiredDate).getTime();
					long now = System.currentTimeMillis();

					if (expires > now) {
						String updateSql = "UPDATE user SET active= 1 WHERE(email=?)";
						ps = dbConn.prepareStatement(updateSql);
						ps.setString(1, dbUserEmail);
						ps.executeUpdate();
						System.out.println("done! account activated!");
						result = 0; // get it !
					} else {
						System.out.println("time out!");
						result = 2; // another fail result!
					}

				}

			}

		} catch (SQLException sqle) {
			System.out.println(sqle.toString());
			result = 3; // sql fatal failure

		}
		return result;
	}

}