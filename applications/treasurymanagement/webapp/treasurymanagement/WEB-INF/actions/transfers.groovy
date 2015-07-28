import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;

class TransferTotal{
	def destinationTreasury
	def treasury
	def name
	def transferDate
	def openingBalance
	def amountIn
	def amountOut
	
	def transactionsIn
	def transactionsOut
	
	def amountBalance
	
}

class TransferDetail{
	def treasuryFrom
	def treasuryTo
	
	def amountIn
	def amountOut
	def comment
	def timeTransferred
	def transferredBy	
}


//Build a transfer totals list
def mergedTransferTotalList = [];

transfersTotalList = delegator.findByAnd("TreasuryTransfersIncomingTotal",  null, null, false);
transfersTotalList.eachWithIndex { transferItem, index ->
	
	//Get total transferred out
	totalOut = BigDecimal.ZERO;
	transfersOutList = delegator.findByAnd("TreasuryTransfersOutgoingTotal",  [transferDate : transferItem.transferDate, sourceTreasury: transferItem.destinationTreasury], null, false);
	
	transfersOutList.eachWithIndex { transferOutItem, outIndex ->
		totalOut = totalOut.add(transferOutItem.transactionAmount);
	}
	
	
	//transferTotal = new TransferTotal();
	//transferTotal.destinationTreasury  = transferItem.destinationTreasury
	//transferTotal.treasury = transferItem.destinationTreasury
	//transferTotal.name = transferItem.name
	//transferTotal.transferDate = transferItem.transferDate
	//transferTotal.amountIn = transferItem.transactionAmount
	//transferTotal.amountOut = BigDecimal.ZERO;
	//transferTotal.amountBalance = transferItem.transactionAmount;
	openingBalance = BigDecimal.ZERO;
	openingBalance = org.ofbiz.treasurymanagement.TreasuryUtility.getOpeningBalance(transferItem.destinationTreasury, transferItem.transferDate);

	transactionsIn = BigDecimal.ZERO;
	transactionsIn = org.ofbiz.treasurymanagement.TreasuryUtility.getTransactionsIn(transferItem.destinationTreasury, transferItem.transferDate);
	transactionsOut = BigDecimal.ZERO;
	transactionsOut = org.ofbiz.treasurymanagement.TreasuryUtility.getTransactionsOut(transferItem.destinationTreasury, transferItem.transferDate);
	
	balanceAmount = BigDecimal.ZERO;
	
	balanceAmount = transferItem.transactionAmount.toBigDecimal();
	
	balanceAmount = balanceAmount.add(openingBalance).add(transactionsIn).subtract(totalOut);
	balanceAmount = balanceAmount.subtract(transactionsOut);
	
	
	

	transferTotal = [destinationTreasury:transferItem.destinationTreasury, treasury:transferItem.destinationTreasury, name:transferItem.name, transferDate:transferItem.transferDate, openingBalance:openingBalance,  amountIn:transferItem.transactionAmount, amountOut:totalOut, transactionsIn:transactionsIn, transactionsOut:transactionsOut, amountBalance:balanceAmount ]
	
	mergedTransferTotalList.add(transferTotal)
}

println "Items are ... "+ mergedTransferTotalList.size()

mergedTransferTotalList.sort{it.transferDate}
mergedTransferTotalList.reverse(true);


context.transfers = mergedTransferTotalList