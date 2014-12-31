package restcomponent.model;

import java.util.List;

import org.ofbiz.accountholdertransactions.Transaction;

public class QueryAccount {
	private String telephoneNo;
	private String queryType;
	private List<Account> listBalances;
	private List<Transaction> listMinistatement;
	private List<Loan> listLoans;
	private Results results;
	public String getTelephoneNo() {
		return telephoneNo;
	}
	public void setTelephoneNo(String telephoneNo) {
		this.telephoneNo = telephoneNo;
	}
	public String getQueryType() {
		return queryType;
	}
	public void setQueryType(String queryType) {
		this.queryType = queryType;
	}
	public List<Account> getListBalances() {
		return listBalances;
	}
	public void setListBalances(List<Account> listBalances) {
		this.listBalances = listBalances;
	}
	public List<Transaction> getListMinistatement() {
		return listMinistatement;
	}
	public void setListMinistatement(List<Transaction> listMinistatement) {
		this.listMinistatement = listMinistatement;
	}
	public List<Loan> getListLoans() {
		return listLoans;
	}
	public void setListLoans(List<Loan> listLoans) {
		this.listLoans = listLoans;
	}
	public Results getResults() {
		return results;
	}
	public void setResults(Results results) {
		this.results = results;
	}
	

}
