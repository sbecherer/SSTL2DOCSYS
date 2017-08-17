package de.abasgmbh.infosystem.sstl2docsys;

public class PartList {
	private String abasId;
	private String belegNr;
	private String artikelNr;
	private String unterArtikelNr;

	public String getAbasId() {
		return abasId;
	}

	public void setAbasId(String abasId) {
		this.abasId = abasId;
	}

	public String getBelegNr() {
		return belegNr;
	}

	public void setBelegNr(String belegNr) {
		this.belegNr = belegNr;
	}

	public String getArtikelNr() {
		return artikelNr;
	}

	public void setArtikelNr(String artikelNr) {
		this.artikelNr = artikelNr;
	}

	public String getUnterArtikelNr() {
		return unterArtikelNr;
	}

	public void setUnterArtikelNr(String unterArtikelNr) {
		this.unterArtikelNr = unterArtikelNr;
	}

	public String CreateSQLStatement() {
		String statement = "INSERT INTO [dbo].[TBL_SUPPLIERWEB_PARTLIST] ([AbasID], [BelegNr], [ArtikelNr], [UnterArtikelNr]) "
				+ "VALUES ('" + this.getAbasId() + "', '" + this.getBelegNr() + "', '" + this.getArtikelNr() + "', '"
				+ this.getUnterArtikelNr() + "')";

		return statement;
	}
}
