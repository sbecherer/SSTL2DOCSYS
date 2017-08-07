package de.abasgmbh.infosystem.sstl2docsys;

import java.util.ArrayList;

import de.abas.erp.api.gui.TextBox;
import de.abas.erp.axi.event.EventException;
import de.abas.erp.axi.screen.ScreenControl;
import de.abas.erp.axi2.EventHandlerRunner;
import de.abas.erp.axi2.annotation.ButtonEventHandler;
import de.abas.erp.axi2.annotation.EventHandler;
import de.abas.erp.axi2.annotation.FieldEventHandler;
import de.abas.erp.axi2.event.ButtonEvent;
import de.abas.erp.axi2.event.FieldEvent;
import de.abas.erp.axi2.type.ButtonEventType;
import de.abas.erp.axi2.type.FieldEventType;
import de.abas.erp.db.DbContext;
import de.abas.erp.db.Query;
import de.abas.erp.db.infosystem.custom.owis.SSTL2Docsys;
import de.abas.erp.db.schema.part.Product;
import de.abas.erp.db.selection.SelectionBuilder;
import de.abas.erp.jfop.rt.api.annotation.RunFopWith;

@EventHandler(head = SSTL2Docsys.class, row = SSTL2Docsys.Row.class)

@RunFopWith(EventHandlerRunner.class)

public class SSTL2DocsysEventHandler {

	@ButtonEventHandler(field="start", type = ButtonEventType.AFTER)
	public void startAfter(ButtonEvent event, ScreenControl screenControl, DbContext ctx, SSTL2Docsys head) throws EventException {
		// TODO Auto-generated method stub
		ctx.out().println("StartButton");
		//new TextBox(ctx, "Info", "Start").show();

		//Infosystem laden
		MultiLevelBomHelper mbh = new MultiLevelBomHelper();
		
		
		if (head.getYartikel() != null){
			mbh.getBOMList(ctx, head.getYartikel());
		} else {
			// alle Artikel laden
			SelectionBuilder<Product> selectionBuilder = SelectionBuilder.create(Product.class);
	        Query<Product> query = ctx.createQuery(selectionBuilder.build());

	        for (Product product : query) {
	        	ArrayList<PARTLISTUSER> list = mbh.getBOMList(ctx, head.getYartikel());
	        	ctx.out().println("Fuer Artikel: " + head.getYartikel().getIdno() + " wurden " + list.size() + " gefunden");
			}
		}
		
	}
	
	@ButtonEventHandler(field="ysqltestconn", type = ButtonEventType.AFTER)
	public void ysqltestconnAfter(ButtonEvent event, ScreenControl screenControl, DbContext ctx, SSTL2Docsys head) throws EventException {
		// TODO Auto-generated method stub
		new TextBox(ctx, "Info", "Connection Test").show();
		SQLConnectionHandler sch = new SQLConnectionHandler(ctx, head.getYsqlhost(), head.getYsqlport(), head.getYsqluser(), head.getYsqlpass());
		
		if (sch.getConnection() == null) {
			new TextBox(ctx, "Fehler!", "Verbindung zum SQL Server konnte nicht hergestellt werden!").show();
		} else
		{
			new TextBox(ctx, "Info", "Connection successful").show();
		}
	}

}
