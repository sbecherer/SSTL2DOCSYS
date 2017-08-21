package de.abasgmbh.infosystem.sstl2docsys;

import java.util.ArrayList;

import de.abas.erp.common.type.IdImpl;
import de.abas.erp.db.DbContext;
import de.abas.erp.db.schema.part.Product;
import de.abas.erp.db.schema.part.SelectablePart;
import de.abas.erp.db.schema.purchasing.PurchaseOrder;
import de.abas.erp.db.schema.purchasing.PurchaseOrder.Row;
import de.abas.erp.db.schema.purchasing.SelectablePurchasing;
import de.abas.erp.db.schema.vendor.SelectableVendor;
import de.abas.erp.db.schema.vendor.Vendor;
import de.abas.erp.db.schema.vendor.VendorContact;

public class PurchaseOrderProcess {
	public ArrayList<PartList> GetList(SelectablePurchasing purchase, DbContext ctx, MultiLevelBomHelper mlbh) {
		ArrayList<PartList> list = new ArrayList<PartList>();

		PurchaseOrder purchseOrder = ctx.load(PurchaseOrder.class, new IdImpl(purchase.id().toString()));

		Iterable<Row> rows = purchseOrder.table().getRows();

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
		PurchaseOrder purchseOrder = ctx.load(PurchaseOrder.class, new IdImpl(purchase.id().toString()));

		PartListUserIndexValues pluiv = new PartListUserIndexValues();
		pluiv.setAbasId(purchseOrder.getId().toString());
		pluiv.setBelegNr(purchseOrder.getIdno());
		pluiv.setDocumentType("Bestellung");
		pluiv.setDocumentDate(purchseOrder.getDateFrom().toString());
		pluiv.setCuSuNo(purchseOrder.getVendor().getIdno());
		SelectableVendor sv = purchseOrder.getVendor();
		pluiv.setCuSuName((sv instanceof Vendor) ? ((Vendor) sv).getAddr() : ((VendorContact) sv).getAddr());

		sqlcon.ExecuteSQLstatement(pluiv.CreateSQLStatement());
	}

}
