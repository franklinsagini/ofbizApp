import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.accounting.ledger.SasraReportsService;
import org.ofbiz.accounting.ledger.AccountCount;

statementofDepositReturnsList = [];
depositReturnsList = [];
statementofDepositReturnsTotalList = [];
totalAccounts = 0
totalAmount = BigDecimal.ZERO
/*
depositReturnsList = delegator.findList("StatementDepositReturns", null, UtilMisc.toSet("rangesId", "depositTypeId", "noOfAccounts", "amount"), UtilMisc.toList("rangesId"), null, false);

if (depositReturnsList) {
    depositReturnsList.each { depositReturn ->
        range = delegator.findOne("DepositReturnsRanges", UtilMisc.toMap("rangesId", depositReturn.rangesId), true);
        depositType = delegator.findOne("DepositType", UtilMisc.toMap("depositTypeId", depositReturn.depositTypeId), true);
        //count = SasraReportsService.getAccountTotalsCount(depositReturn.depositTypeId, new BigDecimal(range.minRange), new BigDecimal(range.maxRange))
        //total = SasraReportsService.getAccountTotalsTotal(depositReturn.depositTypeId, new BigDecimal(range.minRange), new BigDecimal(range.maxRange))
        statementofDepositReturnsList.add(["minRange":range.minRange, "maxRange":range.maxRange, "depositType":depositType.name, noOfAccounts:depositReturn.noOfAccounts, amount:depositReturn.amount]);
        totalAccounts = totalAccounts + depositReturn.noOfAccounts
        totalAmount = totalAmount + depositReturn.amount
    }
}
*/

depositReturnsList = delegator.findList("StatementDepositReturns", null, UtilMisc.toSet("rangesId", "depositTypeId", "noOfAccounts", "amount"), UtilMisc.toList("rangesId"), null, false);

if (depositReturnsList) {
    depositReturnsList.each { depositReturn ->
        range = delegator.findOne("DepositReturnsRanges", UtilMisc.toMap("rangesId", depositReturn.rangesId), true);
        depositType = delegator.findOne("DepositType", UtilMisc.toMap("depositTypeId", depositReturn.depositTypeId), true);
        statementofDepositReturnsList.add(["minRange":range.minRange, "maxRange":range.maxRange, "depositType":depositType.name, noOfAccounts:depositReturn.noOfAccounts, amount:depositReturn.amount]);
        totalAccounts = totalAccounts + depositReturn.noOfAccounts.toInteger()
        totalAmount = totalAmount + depositReturn.amount
    }
}



context.statementofDepositReturnsList = statementofDepositReturnsList;




statementofDepositReturnsTotalList.add("totalName":"Total Accounts", "balance":totalAccounts);
statementofDepositReturnsTotalList.add("totalName":"Total Amount in KES", "balance":totalAmount);

context.statementofDepositReturnsTotalList = statementofDepositReturnsTotalList;
