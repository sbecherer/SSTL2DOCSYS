package de.abasgmbh.infosystem.sstl2docsys;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.apache.log4j.Logger;

public class SQLConnectionHandler {

	private Connection connection = null;
	private Logger logger = Logger.getLogger(SQLConnectionHandler.class);

	private String driver;
	private String host;
	private String db;
	private String user;
	private String pass;
	private boolean connected = false;

//	public Connection getConnection() {
//		return connection;
//	}

	public boolean isConnected() {
		return connected;
	}

	// Konstruktor inkl. Verbindungstest
	public SQLConnectionHandler(String pdriver, String phost, String pdb, String puser, String ppass)
			throws ClassNotFoundException {
		// Declare the JDBC objects.
		this.connection = null;
		logger.debug("Testing SQL connection");
		try {
			Class.forName(pdriver);
			this.connection = DriverManager.getConnection("jdbc:sqlserver://" + phost + ";databasename=" + pdb, puser,
					ppass);
			logger.debug("SQL connection established");
			this.connected = true;
			this.driver = pdriver;
			this.host = phost;
			this.db = pdb;
			this.user = puser;
			this.pass = ppass;
		} catch (SQLException e) {
			this.connected = false;
			logger.error("SQL connection could not be established");
			logger.error(e.getMessage());
		} finally {
			if (this.connection != null)
				try {
					this.connection.close();
					logger.debug("SQL connection closed");
				} catch (Exception e) {
				}
		}
	}

	// Führt SQL statements nacheinander aus
	public boolean ExecuteSQLstatements(ArrayList<String> statements) {

		if (!this.isConnected()) {
			logger.error("ExecuteSQLstatements: not connected");
			return false;
		}

		this.connection = null;
		boolean result = false;

		try {
			Class.forName(driver);
			this.connection = DriverManager.getConnection("jdbc:sqlserver://" + host + ";databasename=" + db, user,
					pass);
			logger.debug("SQL connection established");

			Statement stmt = null;
			Connection con = this.connection;
			stmt = con.createStatement();

			for (String sql : statements) {
				stmt.executeUpdate(sql);
				logger.debug("SQL executed --- " + sql);
			}

			result = true;
		} catch (Exception e) {
			logger.error("SQL connection not established");
			logger.error(e.getMessage());
		} finally {
			if (this.connection != null)
				try {
					this.connection.close();
					logger.debug("SQL connection closed");
				} catch (Exception e) {
				}
		}

		return result;
	}

	// Führt einzelnes SQL statement aus
	public boolean ExecuteSQLstatement(String statement) {

		if (!this.isConnected()) {
			logger.error("ExecuteSQLstatements: not connected");
			return false;
		}

		this.connection = null;
		boolean result = false;

		try {
			Class.forName(driver);
			this.connection = DriverManager.getConnection("jdbc:sqlserver://" + host + ";databasename=" + db, user,
					pass);
			logger.debug("SQL connection established");

			Statement stmt = null;
			Connection con = this.connection;
			stmt = con.createStatement();

			stmt.executeUpdate(statement);
			logger.debug("SQL executed --- " + statement);

			result = true;
		} catch (Exception e) {
			logger.error("SQL connection not established");
			logger.error(e.getMessage());
		} finally {
			if (this.connection != null)
				try {
					this.connection.close();
					logger.debug("SQL connection closed");
				} catch (Exception e) {
				}
		}

		return result;
	}

}
