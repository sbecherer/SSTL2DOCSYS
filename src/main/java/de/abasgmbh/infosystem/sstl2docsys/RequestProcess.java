package de.abasgmbh.infosystem.sstl2docsys;

import java.util.ArrayList;

import de.abas.erp.common.type.IdImpl;
import de.abas.erp.db.DbContext;
import de.abas.erp.db.schema.part.Product;
import de.abas.erp.db.schema.part.SelectablePart;
import de.abas.erp.db.schema.purchasing.Request;
import de.abas.erp.db.schema.purchasing.Request.Row;
import de.abas.erp.db.schema.purchasing.SelectablePurchasing;
import de.abas.erp.db.schema.vendor.SelectableVendor;
import de.abas.erp.db.schema.vendor.Vendor;
import de.abas.erp.db.schema.vendor.VendorContact;

public class RequestProcess {
	public ArrayList<PartList> GetList(SelectablePurchasing purchase, DbContext ctx, MultiLevelBomHelper mlbh) {
		ArrayList<PartList> list = new ArrayList<PartList>();

		Request request = ctx.load(Request.class, new IdImpl(purchase.id().toString()));

		Iterable<Row> rows = request.table().getRows();

		for (Row row : rows) {

			SelectablePart p = row.getProduct();
			if (p != null) {
				if (p instanceof Product) {
					list.addAll(mlbh.getBOMList(p, purchase));
				}
			}

		}

		return list;
	}

	public void WriteIndexValue(SelectablePurchasing purchase, DbContext ctx, SQLConnectionHandler sqlcon) {
		Request request = ctx.load(Request.class, new IdImpl(purchase.id().toString()));

		PartListUserIndexValues pluiv = new PartListUserIndexValues();
		pluiv.setAbasId(request.getId().toString());
		pluiv.setBelegNr(request.getIdno());
		pluiv.setDocumentType("Anfrage");
		pluiv.setDocumentDate(request.getDateFrom().toString());
		pluiv.setCuSuNo(request.getVendor().getIdno());
		SelectableVendor sv = request.getVendor();
		pluiv.setCuSuName((sv instanceof Vendor) ? ((Vendor) sv).getAddr() : ((VendorContact) sv).getAddr());

		sqlcon.ExecuteSQLstatement(pluiv.CreateSQLStatement());
	}

}
