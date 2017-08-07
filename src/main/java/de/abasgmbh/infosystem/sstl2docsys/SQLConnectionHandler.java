package de.abasgmbh.infosystem.sstl2docsys;

import java.sql.Connection;
import java.sql.DriverManager;

import de.abas.erp.api.gui.TextBox;
import de.abas.erp.db.DbContext;

public class SQLConnectionHandler {

	private Connection connection = null;
	public Connection getConnection()
	{
		return connection;
	}
	
	public SQLConnectionHandler(DbContext ctx, String host, int port, String user, String pass) {
		// Declare the JDBC objects.
		connection = null;
		//ctx.out().println("Connecting");

		try {
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			connection = DriverManager.getConnection("jdbc:sqlserver://" + host +":"+port+";databasename=DX4HELP", user, pass);
			ctx.out().println("Connection established");
		} catch (Exception e) {
			new TextBox(ctx, "Fehler!", "Verbindung zum SQL Server konnte nicht hergestellt werden!" + e.getMessage()).show();

			e.printStackTrace();
		} finally {
			if (connection != null)
				try {
					connection.close();
				} catch (Exception e) {
				}
		}
	}
}
