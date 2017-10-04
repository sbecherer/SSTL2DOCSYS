package de.abasgmbh.infosystem.sstl2docsys;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import de.abas.erp.db.DbContext;
import de.abas.erp.db.infosystem.standard.st.MultiLevelBOM;
import de.abas.erp.db.infosystem.standard.st.MultiLevelBOM.Row;
import de.abas.erp.db.schema.part.SelectablePart;
import de.abas.erp.db.schema.purchasing.Purchasing;
import de.abas.erp.db.schema.purchasing.SelectablePurchasing;
import de.abas.jfop.base.buffer.BufferFactory;
import de.abas.jfop.base.buffer.GlobalTextBuffer;

public class MultiLevelBomHelper {

	Logger logger = Logger.getLogger(MultiLevelBomHelper.class);
	private DbContext ctx;
	private SQLConnectionHandler sqlconnection;
	private boolean useSQL;
	private String transferFile;

	public MultiLevelBomHelper(DbContext context, SQLConnectionHandler con, boolean useSQLp, String transferFile) {
		this.ctx = context;
		this.sqlconnection = con;
		this.useSQL = useSQLp;
		this.transferFile = transferFile;
	}

	public ArrayList<PartListUser> getBOMList(SelectablePart part, String dmsuser, String abasid) {
		ArrayList<PartListUser> list = new ArrayList<PartListUser>();

		MultiLevelBOM multiLevelBom = ctx.openInfosystem(MultiLevelBOM.class);
		logger.debug("Infosystem SSTL loaded");
		multiLevelBom.setArtikel(part);
		multiLevelBom.setBmitag(false);
		multiLevelBom.setBmitfm(false);
		multiLevelBom.invokeStart();
		logger.debug("Data loaded to IS");

		logger.debug("Creating objects form IS table");
		for (Row row : multiLevelBom.table().getRows()) {
			PartListUser plu = new PartListUser();
			plu.setAbasId(abasid);
			// Feld BelegNr mit Artikelnummer füllen
			plu.setBelegNr(part.getIdno());
			plu.setArtikelNr(part.getIdno());
			plu.setUserLogin(dmsuser);
			plu.setUnterArtikelNr(row.getElem().getIdno());
			list.add(plu);
		}
		logger.debug("Objects created");

		return list;
	}

	public ArrayList<PartList> getBOMList(SelectablePart part, String purchaseIdno) {
		ArrayList<PartList> list = new ArrayList<PartList>();

		MultiLevelBOM multiLevelBom = ctx.openInfosystem(MultiLevelBOM.class);
		logger.debug("Infosystem SSTL loaded");
		multiLevelBom.setArtikel(part);
		multiLevelBom.setBmitag(false);
		multiLevelBom.setBmitfm(false);
		multiLevelBom.invokeStart();
		logger.debug("Data loaded");

		logger.debug("Creating objects from IS table");
		for (Row row : multiLevelBom.table().getRows()) {
			PartList plu = new PartList();
			plu.setAbasId(part.getId().toString());
			plu.setBelegNr(purchaseIdno);
			plu.setArtikelNr(part.getIdno());
			plu.setUnterArtikelNr(row.getElem().getIdno());
			list.add(plu);
		}
		logger.debug("Objects created");

		return list;
	}

	public ArrayList<PartListUser> getBOMListUser(SelectablePart part, String purchaseIdno) {
		ArrayList<PartListUser> list = new ArrayList<PartListUser>();

		MultiLevelBOM multiLevelBom = ctx.openInfosystem(MultiLevelBOM.class);
		logger.debug("Infosystem SSTL loaded");
		multiLevelBom.setArtikel(part);
		multiLevelBom.setBmitag(false);
		multiLevelBom.setBmitfm(false);
		multiLevelBom.invokeStart();
		logger.debug("Data loaded");

		// G-Puffer laden
		BufferFactory bufferFactory = BufferFactory.newInstance(true);
		GlobalTextBuffer globalTextBuffer = bufferFactory.getGlobalTextBuffer();

		// Passwortdatensatz aus G-Puffer laden
		String dmsuser = globalTextBuffer.getStringValue("currUserPwd^altDMSName");

		logger.debug("Creating objects from IS table");
		for (Row row : multiLevelBom.table().getRows()) {
			PartListUser plu = new PartListUser();
			plu.setAbasId(part.getId().toString());
			plu.setUserLogin(dmsuser);
			plu.setBelegNr(purchaseIdno);
			plu.setArtikelNr(part.getIdno());
			plu.setUnterArtikelNr(row.getElem().getIdno());
			list.add(plu);
		}
		logger.debug("Objects created");

		return list;
	}

	public ArrayList<PartList> getProcessBOMList(SelectablePurchasing purchase) {
		ArrayList<PartList> list = new ArrayList<PartList>();

		String ptyp = purchase.getString(Purchasing.META.type);

		if (ptyp.equals("(2)")) {

			RequestProcess rp = new RequestProcess();
			list.addAll(rp.GetList(purchase, ctx, this));

		} else if (ptyp.equals("(3)")) {

			PurchaseOrderProcess po = new PurchaseOrderProcess();
			list.addAll(po.GetList(purchase, ctx, this));

		} else {
			logger.error(
					"getProcessBOMList(SelectablePurchasing purchase), wrong object. \"ptyp\" need to be (2) or (3).");
			// error wrong object
		}

		return list;
	}

	public ArrayList<PartListUser> getProcessBOMListUser(SelectablePurchasing purchase) {
		ArrayList<PartListUser> list = new ArrayList<PartListUser>();

		String ptyp = purchase.getString(Purchasing.META.type);

		if (ptyp.equals("(2)")) {

			RequestProcess rp = new RequestProcess();
			list.addAll(rp.GetListUser(purchase, ctx, this));

		} else if (ptyp.equals("(3)")) {

			PurchaseOrderProcess po = new PurchaseOrderProcess();
			list.addAll(po.GetListUser(purchase, ctx, this));

		} else {
			logger.error(
					"getProcessBOMList(SelectablePurchasing purchase), wrong object. \"ptyp\" need to be (2) or (3).");
			// error wrong object
		}

		return list;
	}

	public void writePartListUserToSQL(ArrayList<PartListUser> plulist) {

		ArrayList<String> statements = new ArrayList<String>();
		logger.debug("Writing Data");

		if (useSQL) {
			logger.debug("Using SQL");
		} else {
			logger.debug("Only logging");
		}

		for (PartListUser partlistuser : plulist) {
			String sql = partlistuser.CreateSQLStatement();
			statements.add(sql);
			logger.debug("SQL statement created --- " + sql);
		}

		if (!useSQL) {
			return;
		}

		boolean result = sqlconnection.ExecuteSQLstatements(statements);

		if (result) {
			logger.debug("SQL statements executed");
		} else {
			logger.error("SQL statements not executed");
		}
	}

	public void writePartListToSQL(ArrayList<PartList> pllist) {

		ArrayList<String> statements = new ArrayList<String>();
		logger.debug("Writing data");

		if (useSQL) {
			logger.debug("Using SQL");
		} else {
			logger.debug("Only logging");
		}

		for (PartList partlist : pllist) {
			String sql = partlist.CreateSQLStatement();
			statements.add(sql);
			logger.debug("SQL statement created --- " + sql);
		}

		if (!useSQL) {
			return;
		}

		boolean result = sqlconnection.ExecuteSQLstatements(statements);

		if (result) {
			logger.debug("SQL statements executed");
		} else {
			logger.error("SQL statements not executed");
		}
	}

	public String getTransferFile() {
		return transferFile;
	}
}
