import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import java.text.SimpleDateFormat;

destinationTreasury = parameters.destinationTreasury
transferDate = parameters.transferDate
//transferDate = transferDate.to
//transferDate.

//transferDate = Date.parse("E MMM dd H:m:s z yyyy", transferDate)
//transferDate = Date.parse("yyyy-M-d", transferDate)

SimpleDateFormat formatter = new SimpleDateFormat("yyyy-M-d");
java.sql.Date sqlDate = new java.sql.Date(formatter.parse(transferDate).getTime());
//def sqlDate = transferDate.toS;

println " Got the destination "+destinationTreasury
println " Got the transfer date "+transferDate

//Get the transfers

class TransferDetail{
	def treasuryFrom
	def treasuryTo
	def isIn
	def amountIn
	def amountOut
	def comment
	def timeTransferred
	def transferredBy
	def runningBalance
}

def mergedTransferDetailList = [];
def mergedSortedDetailsMapList = [];

transfersInDetailList = delegator.findByAnd("TreasuryTransfer",  [destinationTreasury : destinationTreasury, transferDate : sqlDate], null, false);
transfersInDetailList.eachWithIndex { transferItemIn, indexIn ->
	
	transfer = new TransferDetail();
	
	
	transfer.treasuryFrom = transferItemIn.sourceTreasury
	transfer.treasuryTo = transferItemIn.destinationTreasury
	transfer.isIn = true
	transfer.amountIn = transferItemIn.transactionAmount
	transfer.amountOut = BigDecimal.ZERO
	transfer.comment = "Incoming"
	transfer.timeTransferred = transferItemIn.createdStamp
	transfer.transferredBy = transferItemIn.createdBy
	
	mergedTransferDetailList.add(transfer)
}


transfersOutDetailList = delegator.findByAnd("TreasuryTransfer",  [sourceTreasury : destinationTreasury, transferDate : sqlDate], null, false);
transfersOutDetailList.eachWithIndex { transferItemIn, indexIn ->
	
	transfer = new TransferDetail();
	
	
	transfer.treasuryFrom = transferItemIn.sourceTreasury
	transfer.treasuryTo = transferItemIn.destinationTreasury
	transfer.isIn = false
	transfer.amountIn = BigDecimal.ZERO
	transfer.amountOut = transferItemIn.transactionAmount
	transfer.comment = "Incoming"
	transfer.timeTransferred = transferItemIn.createdStamp
	transfer.transferredBy = transferItemIn.createdBy
	
	mergedTransferDetailList.add(transfer)
}

mergedTransferDetailList.sort{it.timeTransferred}
runningBalance = BigDecimal.ZERO
runningBalance  = runningBalance.toBigDecimal()
//Build up a map list
mergedTransferDetailList.eachWithIndex { transferItemMerged, indexMerged ->
	
	//Build a map
	
//	def treasuryFrom
//	def treasuryTo
//	def isIn
//	def amountIn
//	def amountOut
//	def comment
//	def timeTransferred
//	def transferredBy
//	def runningBalance
	runningBalance  = runningBalance.toBigDecimal()
	if (transferItemMerged.isIn == true){
		runningBalance = runningBalance.add(transferItemMerged.amountIn)
		comment = "In"
	} else{
	
	runningBalance = runningBalance.subtract(transferItemMerged.amountOut)
	comment = "Out"
	}
	
	transferDetailTotal = [treasuryFrom:transferItemMerged.treasuryFrom, treasuryTo:transferItemMerged.treasuryTo, isIn:transferItemMerged.isIn, amountIn:transferItemMerged.amountIn, amountOut:transferItemMerged.amountOut, comment:comment, timeTransferred:transferItemMerged.timeTransferred, transferredBy:transferItemMerged.transferredBy, runningBalance:runningBalance ]
	
	mergedSortedDetailsMapList.add(transferDetailTotal);
	
}

context.mergedSortedDetailsMapList = mergedSortedDetailsMapList
