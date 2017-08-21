package de.abasgmbh.infosystem.sstl2docsys;

import de.abas.eks.jfop.FOPException;
import de.abas.erp.common.type.IdImpl;
import de.abas.erp.db.DbContext;
import de.abas.erp.db.schema.purchasing.Request;
import de.abas.erp.db.schema.purchasing.Request.Row;

public class LoadRequestIterateOverRows {

	public int runFop(DbContext dbContext, String[] arg1) throws FOPException {
		// DbContext dbContext = fopSessionContext.getDbContext();

		Request request = dbContext.load(Request.class, new IdImpl("(5199283,4,0)"));

		dbContext.out().println(request.getIdno() + " - " + request.getDateFrom());
		Iterable<Row> rows = request.table().getRows();
		for (Row row : rows) {
			dbContext.out().println("-- " + row.getProduct().getIdno());
		}
		return 0;
	}
}
