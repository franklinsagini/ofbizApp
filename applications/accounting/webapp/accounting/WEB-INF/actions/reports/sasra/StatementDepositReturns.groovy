import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.accounting.ledger.SasraReportsService;
import org.ofbiz.accounting.ledger.AccountCount;

statementofDepositReturnsList = [];
depositReturnsList = [];
statementofDepositReturnsTotalList = [];
totalAccounts = 0
totalAmount = BigDecimal.ZERO

depositReturnsList = delegator.findList("StatementDepositReturns", null, UtilMisc.toSet("rangesId", "depositTypeId", "noOfAccounts", "amount"), UtilMisc.toList("rangesId"), null, false);

if (depositReturnsList) {
    depositReturnsList.each { depositReturn ->
        range = delegator.findOne("DepositReturnsRanges", UtilMisc.toMap("rangesId", depositReturn.rangesId), true);
        depositType = delegator.findOne("DepositType", UtilMisc.toMap("depositTypeId", depositReturn.depositTypeId), true);
        System.out.println("DEPOSIT TYPEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE " + depositReturn.depositTypeId)
        System.out.println("DEPOSIT MINNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNN " + range.minRange)
        System.out.println("DEPOSIT MAXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX " + range.maxRange)
        count = SasraReportsService.getAccountTotalsCount(depositReturn.depositTypeId, new BigDecimal(range.minRange), new BigDecimal(range.maxRange))
        total = SasraReportsService.getAccountTotalsTotal(depositReturn.depositTypeId, new BigDecimal(range.minRange), new BigDecimal(range.maxRange))



        statementofDepositReturnsList.add(["minRange":range.minRange, "maxRange":range.maxRange, "depositType":depositType.name, noOfAccounts:count, amount:total]);
        totalAccounts = totalAccounts + count
        totalAmount = totalAmount + total
    }
}




context.statementofDepositReturnsList = statementofDepositReturnsList;




statementofDepositReturnsTotalList.add("totalName":"Total Accounts", "balance":totalAccounts);
statementofDepositReturnsTotalList.add("totalName":"Total Amount in KES", "balance":totalAmount);

context.statementofDepositReturnsTotalList = statementofDepositReturnsTotalList;
