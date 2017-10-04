package de.abasgmbh.infosystem.sstl2docsys;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import de.abas.erp.common.type.IdImpl;
import de.abas.erp.db.DbContext;
import de.abas.erp.db.schema.part.Product;
import de.abas.erp.db.schema.purchasing.Request;
import de.abas.erp.db.schema.purchasing.SelectablePurchasing;
import de.abas.erp.db.schema.vendor.SelectableVendor;
import de.abas.erp.db.schema.vendor.Vendor;
import de.abas.erp.db.schema.vendor.VendorContact;

public class RequestProcess {
	Logger logger = Logger.getLogger(RequestProcess.class);
	
	public ArrayList<PartList> GetList(SelectablePurchasing purchase, DbContext ctx, MultiLevelBomHelper mlbh) {
		ArrayList<PartList> list = new ArrayList<PartList>();

//		Request request = ctx.load(Request.class, new IdImpl(purchase.id().toString()));
//		String requestId = request.getId().toString();
//		String requestIdno = request.getIdno().toString();

//		RowSelectionBuilder<Request,Row> rowSelectionBuilder = RowSelectionBuilder.create(Request.class, Request.Row.class);
//		rowSelectionBuilder.addForHead(Conditions.eq(Request.META.idno.getName(), purchase.getIdno()));
//		rowSelectionBuilder.add(Conditions.eq(Request.Row.META.product.getName() + "^" + Product.META.grpNo.getName(), "1"));
//		RowQuery<Request,Row> query = ctx.createQuery(rowSelectionBuilder.build());
		
//		Iterable<Row> rows = request.table().getRows();
		
		BufferedReader bufferedReader = null;
		try {
			bufferedReader = new BufferedReader(new FileReader(mlbh.getTransferFile()));

			String line = null;

			while ((line = bufferedReader.readLine()) != null) {
				String productId = line.substring(0, line.indexOf("#") - 1);
				Product product = ctx.load(Product.class, new IdImpl(productId));
				list.addAll(mlbh.getBOMList(product, purchase.getIdno()));
			}

		} catch (FileNotFoundException e) {
			logger.error(e.getMessage());
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		
//		for (Row row : rows) {
//
//			SelectablePart p = row.getProduct();
//			if (p != null) {
//				if (p instanceof Product) {
//					list.addAll(mlbh.getBOMList(p, purchase.getId().toString(), purchase.getIdno()));
//				}
//			}
//
//		}

		return list;
	}

	public ArrayList<PartListUser> GetListUser(SelectablePurchasing purchase, DbContext ctx, MultiLevelBomHelper mlbh) {
		ArrayList<PartListUser> list = new ArrayList<PartListUser>();

//		Request request = ctx.load(Request.class, new IdImpl(purchase.id().toString()));
//		String requestId = request.getId().toString();
//		String requestIdno = request.getIdno().toString();

//		RowSelectionBuilder<Request,Row> rowSelectionBuilder = RowSelectionBuilder.create(Request.class, Request.Row.class);
//		rowSelectionBuilder.addForHead(Conditions.eq(Request.META.idno.getName(), purchase.getIdno()));
//		rowSelectionBuilder.add(Conditions.eq(Request.Row.META.product.getName() + "^" + Product.META.grpNo.getName(), "1"));
//		RowQuery<Request,Row> query = ctx.createQuery(rowSelectionBuilder.build());
		
//		Iterable<Row> rows = request.table().getRows();
		
		BufferedReader bufferedReader = null;
		try {
			bufferedReader = new BufferedReader(new FileReader(mlbh.getTransferFile()));

			String line = null;

			while ((line = bufferedReader.readLine()) != null) {
				String productId = line.substring(0, line.indexOf("#") - 1);
				Product product = ctx.load(Product.class, new IdImpl(productId));
				list.addAll(mlbh.getBOMListUser(product, purchase.getIdno()));
			}

		} catch (FileNotFoundException e) {
			logger.error(e.getMessage());
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		
//		for (Row row : rows) {
//
//			SelectablePart p = row.getProduct();
//			if (p != null) {
//				if (p instanceof Product) {
//					list.addAll(mlbh.getBOMList(p, purchase.getId().toString(), purchase.getIdno()));
//				}
//			}
//
//		}

		return list;
	}
	
	public void WriteIndexValue(SelectablePurchasing purchase, DbContext ctx, SQLConnectionHandler sqlcon,
			boolean useSQL) {
		Request request = ctx.load(Request.class, new IdImpl(purchase.id().toString()));

		PartListUserIndexValues pluiv = new PartListUserIndexValues();
		pluiv.setAbasId(request.getId().toString());
		pluiv.setBelegNr(request.getIdno());
		pluiv.setDocumentType("Anfrage");
		pluiv.setCuSuNo(request.getVendor().getIdno());
		SelectableVendor sv = request.getVendor();
		pluiv.setCuSuName((sv instanceof Vendor) ? ((Vendor) sv).getAddr() : ((VendorContact) sv).getAddr());

		if (useSQL) {
			sqlcon.ExecuteSQLstatement(pluiv.CreateSQLStatement());
		}
	}

}
