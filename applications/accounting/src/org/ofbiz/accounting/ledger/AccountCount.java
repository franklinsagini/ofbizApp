package org.ofbiz.accounting.ledger;

import java.math.BigDecimal;

public class AccountCount {

	private Long count;
	private BigDecimal total;
	public Long getCount() {
		return count;
	}
	public void setCount(Long count) {
		this.count = count;
	}
	public BigDecimal getTotal() {
		return total;
	}
	public void setTotal(BigDecimal total) {
		this.total = total;
	}
}
