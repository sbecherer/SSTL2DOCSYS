package de.abasgmbh.infosystem.sstl2docsys;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import de.abas.erp.db.DbContext;
import de.abas.erp.db.infosystem.standard.st.MultiLevelBOM;
import de.abas.erp.db.infosystem.standard.st.MultiLevelBOM.Row;
import de.abas.erp.db.schema.part.Product;
import de.abas.erp.db.schema.part.SelectablePart;
import de.abas.erp.db.schema.purchasing.PurchaseOrder;
import de.abas.erp.db.schema.purchasing.Purchasing;
import de.abas.erp.db.schema.purchasing.Request;
import de.abas.erp.db.schema.purchasing.SelectablePurchasing;
import de.abas.jfop.base.buffer.BufferFactory;
import de.abas.jfop.base.buffer.GlobalTextBuffer;

public class MultiLevelBomHelper {

	Logger logger = Logger.getLogger(MultiLevelBomHelper.class);
	private DbContext ctx;
	private SQLConnectionHandler sqlconnection;
	private boolean useSQL;

	public MultiLevelBomHelper(DbContext context, SQLConnectionHandler con, boolean useSQLp) {
		this.ctx = context;
		this.sqlconnection = con;
		this.useSQL = useSQLp;
	}

	public ArrayList<PARTLISTUSER> getBOMList(SelectablePart part) {
		ArrayList<PARTLISTUSER> list = new ArrayList<PARTLISTUSER>();

		MultiLevelBOM multiLevelBom = ctx.openInfosystem(MultiLevelBOM.class);
		logger.debug("Infosystem SSTL loaded");
		multiLevelBom.setArtikel(part);
		multiLevelBom.setBmitag(false);
		multiLevelBom.setBmitfm(false);
		multiLevelBom.invokeStart();
		logger.debug("Data loaded to IS");

		// G-Puffer laden
		BufferFactory bufferFactory = BufferFactory.newInstance(true);
		GlobalTextBuffer globalTextBuffer = bufferFactory.getGlobalTextBuffer();

		// Passwortdatensatz aus G-Puffer laden
		String dmsuser = globalTextBuffer.getStringValue("currUserPwd^altDMSName");
		// Abas ID erzeugen
		String abasid = part.getId().toString() + "_" + dmsuser + "_" + System.currentTimeMillis();
		
		logger.debug("Creating objects form IS table");
		for (Row row : multiLevelBom.table().getRows()) {
			PARTLISTUSER plu = new PARTLISTUSER();
			plu.setAbasId(abasid);
			plu.setArtikelNr(part.getIdno());
			plu.setUserLogin(dmsuser);
			plu.setUnterArtikelNr(row.getElem().getIdno());
			list.add(plu);
		}
		logger.debug("Objects created");

		return list;
	}
	public ArrayList<PARTLIST> getBOMList(SelectablePart part, SelectablePurchasing purchase) {
		ArrayList<PARTLIST> list = new ArrayList<PARTLIST>();

		MultiLevelBOM multiLevelBom = ctx.openInfosystem(MultiLevelBOM.class);
		logger.debug("Infosystem SSTL loaded");
		multiLevelBom.setArtikel(part);
		multiLevelBom.setBmitag(false);
		multiLevelBom.setBmitfm(false);
		multiLevelBom.invokeStart();
		logger.debug("Data loaded");

		logger.debug("Creating objects from IS table");
		for (Row row : multiLevelBom.table().getRows()) {
			PARTLIST plu = new PARTLIST();
			plu.setAbasId(purchase.getId().toString());
			plu.setBelegNr(purchase.getIdno());
			plu.setArtikelNr(part.getIdno());
			plu.setUnterArtikelNr(row.getElem().getIdno());
			list.add(plu);
		}
		logger.debug("Objects created");

		return list;
	}
	public ArrayList<PARTLIST> getProcessBOMList(SelectablePurchasing purchase) {
		ArrayList<PARTLIST> list = new ArrayList<PARTLIST>();
		
		String ptyp = purchase.getString(Purchasing.META.type);
		
		if (ptyp.equals("(2)")){
			Request rq = ctx.load(Request.class, purchase.id());
			
			for (Request.Row row : rq.getTableRows()) {
				SelectablePart p = row.getProduct();
				if (p instanceof Product){
					list.addAll(getBOMList(p, purchase));
				}
			}
		} else if (ptyp == "(3)"){
			PurchaseOrder po = ctx.load(PurchaseOrder.class, purchase.id());
			
			for (PurchaseOrder.Row row : po.getTableRows()) {
				SelectablePart p = row.getProduct();
				if (p instanceof Product){
					list.addAll(getBOMList(p, purchase));
				}
			}
		}
		else{
			//error wrong object
		}
		
		return list;
	}
	
	public void writePartListUserToSQL(ArrayList<PARTLISTUSER> plulist) {

		ArrayList<String> statements = new ArrayList<String>();
		logger.debug("Writing Data");

		if (useSQL) {
			logger.debug("Using SQL");
		} else {
			logger.debug("Only logging");
		}

		for (PARTLISTUSER partlistuser : plulist) {
			String sql = "INSERT INTO [dbo].[TBL_SUPPLIERWEB_PARTLIST_USER] ([AbasID], [UserLogin], [ArtikelNr], [UnterArtikelNr]) "
					+ "VALUES ('" + partlistuser.getAbasId() + "', '" + partlistuser.getUserLogin() + "', '"
					+ partlistuser.getArtikelNr() + "', '" + partlistuser.getUnterArtikelNr() + "')";
			statements.add(sql);
			logger.debug("SQL statement created --- " + sql);
		}

		boolean result = sqlconnection.ExecuteSQLstatements(statements);
		
		if (result){
			logger.debug("SQL statements executed");
		}
		else {
			logger.error("SQL statements not executed");
		}
	}
	public void writePartListToSQL(ArrayList<PARTLIST> pllist) {

		ArrayList<String> statements = new ArrayList<String>();
		logger.debug("Writing data");

		if (useSQL) {
			logger.debug("Using SQL");
		} else {
			logger.debug("Only logging");
		}

		for (PARTLIST partlist : pllist) {
			String sql = "INSERT INTO [dbo].[TBL_SUPPLIERWEB_PARTLIST] ([AbasID], [BelegNr], [ArtikelNr], [UnterArtikelNr]) "
					+ "VALUES ('" + partlist.getAbasId() + "', '" + partlist.getBelegNr() + "', '"
					+ partlist.getArtikelNr() + "', '" + partlist.getUnterArtikelNr() + "')";
			statements.add(sql);
			logger.debug("SQL statement created --- " + sql);
		}

		boolean result = sqlconnection.ExecuteSQLstatements(statements);
		
		if (result){
			logger.debug("SQL statements executed");
		}
		else {
			logger.error("SQL statements not executed");
		}
	}
}
