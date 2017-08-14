package de.abasgmbh.infosystem.sstl2docsys;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import org.apache.log4j.Logger;

import de.abas.erp.api.gui.ButtonSet;
import de.abas.erp.api.gui.TextBox;
import de.abas.erp.axi.event.EventException;
import de.abas.erp.axi.screen.ScreenControl;
import de.abas.erp.axi2.EventHandlerRunner;
import de.abas.erp.axi2.annotation.ButtonEventHandler;
import de.abas.erp.axi2.annotation.EventHandler;
import de.abas.erp.axi2.event.ButtonEvent;
import de.abas.erp.axi2.type.ButtonEventType;
import de.abas.erp.db.DbContext;
import de.abas.erp.db.infosystem.custom.owis.SSTL2Docsys;
import de.abas.erp.db.schema.purchasing.PurchaseOrder;
import de.abas.erp.db.schema.purchasing.Purchasing;
import de.abas.erp.db.schema.purchasing.Request;
import de.abas.erp.db.schema.vendor.SelectableVendor;
import de.abas.erp.db.schema.vendor.Vendor;
import de.abas.erp.db.schema.vendor.VendorContact;
import de.abas.erp.jfop.rt.api.annotation.RunFopWith;
import de.abas.erp.axi2.annotation.FieldEventHandler;
import de.abas.erp.axi2.event.FieldEvent;
import de.abas.erp.axi2.type.FieldEventType;
import de.abas.erp.common.type.enums.EnumDialogBox;
import de.abas.erp.axi2.annotation.ScreenEventHandler;
import de.abas.erp.axi2.event.ScreenEvent;
import de.abas.erp.axi2.type.ScreenEventType;

@EventHandler(head = SSTL2Docsys.class, row = SSTL2Docsys.Row.class)

@RunFopWith(EventHandlerRunner.class)

public class SSTL2DocsysEventHandler {
	private static String CONFIGFILE = "owis/sstl2docsys.config.properties";

	static final boolean useSQL = true;
	Logger logger = Logger.getLogger(SSTL2DocsysEventHandler.class);

	@ButtonEventHandler(field = "start", type = ButtonEventType.AFTER)
	public void startAfter(ButtonEvent event, ScreenControl screenControl, DbContext ctx, SSTL2Docsys head)
			throws EventException {

		logger.debug("Start button pressed");

		// Prüfung der Pflichtfelder
		if (head.getYsqldriver().isEmpty() || head.getYsqlhost().isEmpty() || head.getYsqldb().isEmpty()
				|| head.getYsqluser().isEmpty() || head.getYsqlpass().isEmpty()) {
			screenControl.setNote("Bitte füllen Sie die benötigten Felder für die SQL Verbindung aus.");
			if (head.getYshowbox()) {
				new TextBox(ctx, "Info", "Bitte füllen Sie die benötigten Felder für die SQL Verbindung aus.");
			}
			logger.info("Required fields for sql connection not filled.");
			return;
		}

		// SQL Verbindung testen 
		SQLConnectionHandler sqlcon = null;
		try {
			sqlcon = new SQLConnectionHandler(head.getYsqldriver(), head.getYsqlhost(), head.getYsqldb(),
					head.getYsqluser(), head.getYsqlpass());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		// Statusabfrage des Verbindungstest
		if (sqlcon.getConnection() == null) {
			screenControl.setNote("Verbindung zum SQL Server konnte nicht hergestellt werden!");
			if (head.getYshowbox()) {
				new TextBox(ctx, "Fehler!", "Verbindung zum SQL Server konnte nicht hergestellt werden!").show();
			}
			return;
		}

		if (head.getYshowbox() && ((head.getYartikel() != null) || (head.getYvorgang() != null))) {
			TextBox textbox = new TextBox(ctx, "Attention",
					"Sollen die Daten zum angegebenen SQL Server übertragen werden?");
			textbox.setButtons(ButtonSet.YES_NO);
			EnumDialogBox show = textbox.show();

			if (show == EnumDialogBox.No) {
				logger.debug("User exit.");
				return;
			}
		}

		if (head.getYartikel() != null) {
			// Ein Artikel ist gesetzt
			MultiLevelBomHelper mbh = new MultiLevelBomHelper(ctx, sqlcon, useSQL);

			ArrayList<PARTLISTUSER> list = mbh.getBOMList(head.getYartikel());

			logger.debug("Part: " + head.getYartikel().getIdno() + ", found " + list.size() + " sub parts");
			if (list.size() > 0) {
				mbh.writePartListUserToSQL(list);
			}
		} else if (head.getYvorgang() != null) {
			//Ein Vorgang ist gesetzt
			MultiLevelBomHelper mbh = new MultiLevelBomHelper(ctx, sqlcon, useSQL);

			ArrayList<PARTLIST> list = mbh.getProcessBOMList(head.getYvorgang());

			logger.debug("Process: " + head.getYvorgang().getIdno() + ", found " + list.size() + " entries");
			if (list.size() > 0) {
				mbh.writePartListToSQL(list);
			}

			Request rq = null;
			PurchaseOrder po = null;
			String ptyp = head.getYvorgang().getString(Purchasing.META.type);
			if (ptyp.equals("(2)")) {
				rq = ctx.load(Request.class, head.getYvorgang().id());
			} else if (ptyp == "(3)") {
				po = ctx.load(PurchaseOrder.class, head.getYvorgang().id());
			}

			PARTLISTUSERINDEXVALUES pluiv = new PARTLISTUSERINDEXVALUES();
			pluiv.setAbasId(head.getYvorgang().getId().toString());
			pluiv.setBelegNr(head.getYvorgang().getIdno());
			pluiv.setDocumentType((ptyp.equals("(2)")) ? "Anfrage" : "Bestellung");
			pluiv.setDocumentDate((ptyp.equals("(2)")) ? rq.getDateFrom().toString() : po.getDateFrom().toString());
			pluiv.setCuSuNo((ptyp.equals("(2)")) ? rq.getVendor().getIdno() : po.getVendor().getIdno());
			SelectableVendor sv = (ptyp.equals("(2)")) ? rq.getVendor() : po.getVendor();
			pluiv.setCuSuName((sv instanceof Vendor) ? ((Vendor) sv).getAddr() : ((VendorContact) sv).getAddr());

			sqlcon.ExecuteSQLstatement(
					"INSERT INTO [dbo].[TBL_SUPPLIERWEB_PARTLIST_USER_INDEXVALUES] ([AbasID], [BelegNr], [DocumentType], [DocumentDate], [CuSuNo], [CuSuName]) "
							+ "VALUES ('" + pluiv.getAbasId() + "', '" + pluiv.getBelegNr() + "', '"
							+ pluiv.getDocumentType() + "', '" + pluiv.getDocumentDate() + "', '" + pluiv.getCuSuNo()
							+ "', '" + pluiv.getCuSuName() + "')");
		} else {
			logger.error("No part selected.");
			screenControl.setNote("Bitte wählen Sie einen Artikel oder eine Anfrage/Bestellung aus!");
			if (head.getYshowbox()) {
				new TextBox(ctx, "Fehler!", "Bitte wählen Sie einen Artikel oder eine Anfrage/Bestellung aus!").show();
			}
			return;
		}

		screenControl.setNote("Die Daten wurden erfolgreich zum SQL Server übertragen.");
		if (head.getYshowbox()) {
			new TextBox(ctx, "Info", "Die Daten wurden erfolgreich zum SQL Server übertragen.").show();
		}
	}

	// Verbindungstest
	@ButtonEventHandler(field = "ysqltestconn", type = ButtonEventType.AFTER)
	public void ysqltestconnAfter(ButtonEvent event, ScreenControl screenControl, DbContext ctx, SSTL2Docsys head)
			throws EventException {

		// Pflichtfelder prüfen
		if (head.getYsqldriver().isEmpty() || head.getYsqlhost().isEmpty() || head.getYsqldb().isEmpty()
				|| head.getYsqluser().isEmpty() || head.getYsqlpass().isEmpty()) {
			screenControl.setNote("Bitte füllen Sie die benötigten Felder für die SQL Verbindung aus.");
			if (head.getYshowbox()) {
				new TextBox(ctx, "Info", "Bitte füllen Sie die benötigten Felder für die SQL Verbindung aus.");
			}
			return;
		}

		// Objekt erzeugen und damit den Verbindungstest ausführen
		SQLConnectionHandler sch = null;
		try {
			sch = new SQLConnectionHandler(head.getYsqldriver(), head.getYsqlhost(), head.getYsqldb(),
					head.getYsqluser(), head.getYsqlpass());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		// Abfrage des Status des Verbindungstest
		if (sch.getConnection() == null) {
			screenControl.setNote("Verbindung zum SQL Server konnte nicht hergestellt werden!");
			if (head.getYshowbox()) {
				new TextBox(ctx, "Fehler!", "Verbindung zum SQL Server konnte nicht hergestellt werden!").show();
			}
		} else {
			screenControl.setNote("SQL connection established");
			if (head.getYshowbox()) {
				new TextBox(ctx, "Info", "SQL connection established").show();
			}
		}
	}

	@FieldEventHandler(field = "yartikel", type = FieldEventType.EXIT)
	public void yartikelExit(FieldEvent event, ScreenControl screenControl, DbContext ctx, SSTL2Docsys head)
			throws EventException {
		if (head.getYartikel() != null) {
			screenControl.setProtection(head, SSTL2Docsys.META.yvorgang, true);
		} else {
			screenControl.setProtection(head, SSTL2Docsys.META.yvorgang, false);
		}
	}

	@FieldEventHandler(field = "yvorgang", type = FieldEventType.EXIT)
	public void yvorgangExit(FieldEvent event, ScreenControl screenControl, DbContext ctx, SSTL2Docsys head)
			throws EventException {
		if (head.getYvorgang() != null) {
			screenControl.setProtection(head, SSTL2Docsys.META.yartikel, true);
		} else {
			screenControl.setProtection(head, SSTL2Docsys.META.yartikel, false);
		}
	}

	@ButtonEventHandler(field = "ysaveconfig", type = ButtonEventType.AFTER)
	public void ysaveconfigAfter(ButtonEvent event, ScreenControl screenControl, DbContext ctx, SSTL2Docsys head)
			throws EventException {
		File propertiesFile = new File(CONFIGFILE);

		Properties configProperties = new Properties();

		configProperties.setProperty("sqldriver", head.getYsqldriver());
		configProperties.setProperty("sqlhost", head.getYsqlhost());
		configProperties.setProperty("sqldb", head.getYsqldb());
		configProperties.setProperty("sqluser", head.getYsqluser());
		configProperties.setProperty("sqlpassword", head.getYsqlpass());

		FileOutputStream out;
		try {
			out = new FileOutputStream(propertiesFile);
			configProperties.store(out, "---config SSTL2Docsys---");
			out.close();
			screenControl.setNote("Config saved...");
		} catch (FileNotFoundException e) {
			logger.error("Reading properies file " + CONFIGFILE + "\n" + e.getMessage());
		} catch (IOException e) {
			logger.error("Reading properies file " + CONFIGFILE + "\n" + e.getMessage());
		}
	}

	@ScreenEventHandler(type = ScreenEventType.ENTER)
	public void screenEnter(ScreenEvent event, ScreenControl screenControl, DbContext ctx, SSTL2Docsys head)
			throws EventException {
		getConfigInMask(head, ctx);
	}

	private void getConfigInMask(SSTL2Docsys head, DbContext ctx) {

		File propertiesFile = new File(CONFIGFILE);

		if (propertiesFile.exists()) {

			FileInputStream in;

			try {
				in = new FileInputStream(propertiesFile);
				Properties configProperties = new Properties();
				configProperties.load(in);
				in.close();

				// config contains all properties read from the file
				head.setYsqldriver(configProperties.getProperty("sqldriver"));
				head.setYsqlhost(configProperties.getProperty("sqlhost"));
				head.setYsqldb(configProperties.getProperty("sqldb"));
				head.setYsqluser(configProperties.getProperty("sqluser"));
				head.setYsqlpass(configProperties.getProperty("sqlpassword"));
			} catch (IOException e) {
			}

		} else {
		}
	}
}
