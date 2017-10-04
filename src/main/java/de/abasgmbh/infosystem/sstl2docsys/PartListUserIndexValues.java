package de.abasgmbh.infosystem.sstl2docsys;

public class PartListUserIndexValues {
	private String abasId;
	private String belegNr;
	private String documentType;
	private String cuSuNo;
	private String cuSuName;

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

	public String getDocumentType() {
		return documentType;
	}

	public void setDocumentType(String documentType) {
		this.documentType = documentType;
	}

	public String getCuSuNo() {
		return cuSuNo;
	}

	public void setCuSuNo(String cuSuNo) {
		this.cuSuNo = cuSuNo;
	}

	public String getCuSuName() {
		return cuSuName;
	}

	public void setCuSuName(String cuSuName) {
		this.cuSuName = cuSuName;
	}

	public String CreateSQLStatement() {
		String statement = "INSERT INTO [dbo].[TBL_SUPPLIERWEB_PARTLIST_USER_INDEXVALUES] ([AbasID], [BelegNr], [DocumentType], [DocumentDate], [CuSuNo], [CuSuName]) "
				+ "VALUES ('" + this.getAbasId() + "', '" + this.getBelegNr() + "', '" + this.getDocumentType()
				+ "', FORMAT(getdate(), 'yyyyMMdd'),'" + this.getCuSuNo() + "', '" + this.getCuSuName() + "')";

		return statement;
	}
}
