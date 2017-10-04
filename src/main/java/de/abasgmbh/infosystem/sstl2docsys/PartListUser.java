package de.abasgmbh.infosystem.sstl2docsys;

public class PartListUser {
	private String abasId;
	private String userLogin;
	private String belegNr;
	private String artikelNr;
	private String unterArtikelNr;

	public String getAbasId() {
		return abasId;
	}

	public void setAbasId(String abasId) {
		this.abasId = abasId;
	}

	public String getUserLogin() {
		return userLogin;
	}

	public void setUserLogin(String userLogin) {
		this.userLogin = userLogin;
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
		String statement = "INSERT INTO [dbo].[TBL_SUPPLIERWEB_PARTLIST_USER] ([AbasID], [UserLogin], [BelegNr], [ArtikelNr], [UnterArtikelNr]) "
				+ "VALUES ('" + this.getAbasId() + "', '" + this.getUserLogin() + "', '" + this.getBelegNr() + "', '"
				+ this.getArtikelNr() + "', '" + this.getUnterArtikelNr() + "')";

		return statement;
	}
}
