import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.accounting.ledger.SasraReportsService;
import org.ofbiz.accounting.ledger.AccountCount;


classificationId = "1"
AccountCount accountCount = SasraReportsService.getAccountTotals(classificationId, 1, 50000.00)
count = SasraReportsService.getAccountTotalsCount(classificationId, new BigDecimal(1), new BigDecimal(50000))
total = SasraReportsService.getAccountTotalsTotal(classificationId, new BigDecimal(1), new BigDecimal(50000))

System.out.println("THIS IS ITTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT COUNT "+count)

System.out.println("THIS IS ITTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT TOTAL "+total)

//classificationId = "2"
//getAccountTotalsTotal(String classificationId, BigDecimal bdLower, BigDecimal bdUpper)
//Use those two methods

//Long count = 50L;
//BigDecimal total = new BigDecimal("300000");

//accountCount.setCount(count)
//accountCount.setTotal(total)

//count = accountCount.getCount()
//total = accountCount.getTotal()



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
        statementofDepositReturnsList.add(["minRange":range.minRange, "maxRange":range.maxRange, "depositType":depositType.name, noOfAccounts:depositReturn.noOfAccounts, amount:depositReturn.amount]);
        totalAccounts = totalAccounts + (depositReturn.noOfAccounts).toInteger()
        totalAmount = totalAmount + depositReturn.amount
    }
}
statementofDepositReturnsTotalList.add("totalName":"Total Accounts", "balance":totalAccounts);
statementofDepositReturnsTotalList.add("totalName":"Total Amount in KES", "balance":totalAmount);
context.statementofDepositReturnsList = statementofDepositReturnsList;
context.statementofDepositReturnsTotalList = statementofDepositReturnsTotalList;
