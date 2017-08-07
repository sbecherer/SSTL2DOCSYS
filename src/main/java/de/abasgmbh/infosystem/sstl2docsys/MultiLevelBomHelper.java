package de.abasgmbh.infosystem.sstl2docsys;

import java.util.ArrayList;
import java.util.List;

import de.abas.erp.db.DbContext;
import de.abas.erp.db.infosystem.standard.st.MultiLevelBOM;
import de.abas.erp.db.infosystem.standard.st.MultiLevelBOM.Row;
import de.abas.erp.db.schema.part.SelectablePart;

public class MultiLevelBomHelper {
	public ArrayList<PARTLISTUSER> getBOMList(DbContext ctx, SelectablePart part) {
		ArrayList<PARTLISTUSER> list = new ArrayList<PARTLISTUSER>();
		
		MultiLevelBOM multiLevelBom = ctx.openInfosystem(MultiLevelBOM.class);
		multiLevelBom.setArtikel(part);
		multiLevelBom.setBstartbut(true);
		
		for (Row row : multiLevelBom.table().getRows()) {
			PARTLISTUSER plu = new PARTLISTUSER();
			plu.setAbasId(part.getId().toString());
			plu.setArtikelNr(part.getIdno());
			plu.setUnterArtikelNr(row.getElem().getIdno());
			list.add(plu);
		}
		
		return list;
	}
}
