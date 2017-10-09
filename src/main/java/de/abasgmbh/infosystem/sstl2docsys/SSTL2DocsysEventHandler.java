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
import de.abas.erp.axi2.annotation.FieldEventHandler;
import de.abas.erp.axi2.annotation.ScreenEventHandler;
import de.abas.erp.axi2.event.ButtonEvent;
import de.abas.erp.axi2.event.FieldEvent;
import de.abas.erp.axi2.event.ScreenEvent;
import de.abas.erp.axi2.type.ButtonEventType;
import de.abas.erp.axi2.type.FieldEventType;
import de.abas.erp.axi2.type.ScreenEventType;
import de.abas.erp.common.type.IdImpl;
import de.abas.erp.common.type.enums.EnumDialogBox;
import de.abas.erp.db.DbContext;
import de.abas.erp.db.infosystem.custom.owis.SSTL2Docsys;
import de.abas.erp.db.schema.purchasing.Purchasing;
import de.abas.erp.db.schema.vendor.Vendor;
import de.abas.erp.jfop.rt.api.annotation.RunFopWith;
import de.abas.jfop.base.buffer.BufferFactory;
import de.abas.jfop.base.buffer.GlobalTextBuffer;

@EventHandler(head = SSTL2Docsys.class, row = SSTL2Docsys.Row.class)

@RunFopWith(EventHandlerRunner.class)

public class SSTL2DocsysEventHandler {
	final String CONFIGFILE = "owis/sstl2docsys.config.properties";
	final boolean USESQL = true;

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
		SQLConnectionHandler sqlConnectionHandler = null;
		try {
			if (USESQL) {
				sqlConnectionHandler = new SQLConnectionHandler(head.getYsqldriver(), head.getYsqlhost(),
						head.getYsqldb(), head.getYsqluser(), head.getYsqlpass());
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		// Statusabfrage des Verbindungstest
		if (USESQL && !sqlConnectionHandler.isConnected()) {
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

			// G-Puffer laden
			BufferFactory bufferFactory = BufferFactory.newInstance(true);
			GlobalTextBuffer globalTextBuffer = bufferFactory.getGlobalTextBuffer();

			// Passwortdatensatz aus G-Puffer laden
			String dmsuser = globalTextBuffer.getStringValue("currUserPwd^altDMSName");
			// Abas ID erzeugen
			String abasid = head.getYartikel().getId().toString() + "_" + dmsuser + "_" + System.currentTimeMillis();
			
			MultiLevelBomHelper mbh = new MultiLevelBomHelper(ctx, sqlConnectionHandler, USESQL, "");

			ArrayList<PartListUser> list = mbh.getBOMList(head.getYartikel(), dmsuser, abasid);

			logger.debug("Part: " + head.getYartikel().getIdno() + ", found " + list.size() + " sub parts");
			if (list.size() > 0) {
				mbh.writePartListUserToSQL(list);
			}

			PartListUserIndexValues pluiv = new PartListUserIndexValues();
			pluiv.setAbasId(abasid);
			pluiv.setBelegNr(head.getYartikel().getIdno());
			pluiv.setDocumentType("999");
			if (head.getYeinzellief() != null) {
				pluiv.setCuSuNo(head.getYeinzellief().getIdno());
				Vendor sv = head.getYeinzellief();
				pluiv.setCuSuName(sv.getAddr());
			}
			if (USESQL) {
				sqlConnectionHandler.ExecuteSQLstatement(pluiv.CreateSQLStatement());
			}
		} else if (head.getYvorgang() != null) {
			// Ein Vorgang ist gesetzt

			if (head.getYprint()) // Wenn "Print"
			{
				MultiLevelBomHelper mbh = new MultiLevelBomHelper(ctx, sqlConnectionHandler, USESQL,
						head.getYtransferfile());

				ArrayList<PartList> list = mbh.getProcessBOMList(head.getYvorgang());

				logger.debug("Process: " + head.getYvorgang().getIdno() + ", found " + list.size() + " entries");
				if (list.size() > 0) {
					mbh.writePartListToSQL(list);
				}
			} else { // Wenn nicht "Print"
				MultiLevelBomHelper mbh = new MultiLevelBomHelper(ctx, sqlConnectionHandler, USESQL,
						head.getYtransferfile());

				ArrayList<PartListUser> list = mbh.getProcessBOMListUser(head.getYvorgang());

				logger.debug("Process: " + head.getYvorgang().getIdno() + ", found " + list.size() + " entries");
				if (list.size() > 0) {
					mbh.writePartListUserToSQL(list);
				}

				String ptyp = head.getYvorgang().getString(Purchasing.META.type);
				if (ptyp.equals("(2)")) {
					RequestProcess rp = new RequestProcess();
					rp.WriteIndexValue(head.getYvorgang(), ctx, sqlConnectionHandler, USESQL);
				} else if (ptyp.equals("(3)")) {
					PurchaseOrderProcess po = new PurchaseOrderProcess();
					po.WriteIndexValue(head.getYvorgang(), ctx, sqlConnectionHandler, USESQL);
				}
			}
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
		SQLConnectionHandler sqlConnectionHandler = null;
		try {
			sqlConnectionHandler = new SQLConnectionHandler(head.getYsqldriver(), head.getYsqlhost(), head.getYsqldb(),
					head.getYsqluser(), head.getYsqlpass());
		} catch (ClassNotFoundException e) {
			logger.error(e.getMessage());
		}

		// Abfrage des Status des Verbindungstest
		if (!sqlConnectionHandler.isConnected()) {
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
		configProperties.setProperty("productvendor",
				(head.getYeinzellief() != null) ? head.getYeinzellief().getId().toString() : "");

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
				if (configProperties.getProperty("productvendor") != null
						&& !configProperties.getProperty("productvendor").trim().isEmpty()) {
					Vendor vendor = ctx.load(Vendor.class, new IdImpl(configProperties.getProperty("productvendor")));
					if (vendor != null) {
						head.setYeinzellief(vendor);
					}
				}
			} catch (IOException e) {
				logger.error(e.getMessage());
			}

		} else {
		}
	}
}
