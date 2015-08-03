import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilMisc;

if (!glAccountId) {
  return;
}

//thruDate =  parameters.thruDate
if (!thruDate) {
  thruDate = UtilDateTime.nowTimestamp();
}


if (!fromDate) {
  fromDate = thruDate - 1
}


println "############################################## "+thruDate
println "############################################## "+fromDate
println "############################################## "+glAccountId

finalTransList = []
accountTransList = []
finalTransListBuilder = []
currentacctgTransId = 0


summaryCondition = [];
summaryCondition.add(EntityCondition.makeCondition("createdTxStamp", EntityOperator.GREATER_THAN_EQUAL_TO, fromDate));
summaryCondition.add(EntityCondition.makeCondition("createdTxStamp", EntityOperator.LESS_THAN, thruDate));
summaryCondition.add(EntityCondition.makeCondition("glAccountId", EntityOperator.EQUALS, glAccountId));
acctgTransEntry = delegator.findList('AcctgTransEntry', EntityCondition.makeCondition(summaryCondition, EntityOperator.AND), null, null, null, false)

acctgTransEntry.each { obj ->
  println "#################################### TRYING TO ITERATE OVER AcctgTransEntry: "
  transCond = []
  currentacctgTransId = obj.acctgTransId
  if (currentacctgTransId != obj.acctgTransId) {
      transCond.add(EntityCondition.makeCondition("acctgTransId", EntityOperator.EQUALS, obj.acctgTransId));

  accountTransactionSublist = delegator.findList('AccountTransaction', EntityCondition.makeCondition(transCond, EntityOperator.AND), null, null, null, false)
  accountTransactionSublist.each { singleTransaction ->
    println "########################### ADDING  singleTransaction "+ singleTransaction.accountTransactionId
    accountTransList.add(singleTransaction)
  }
  }


  println "#################################### accountTransaction: "+ accountTransactionSublist
}

accountTransList.each { objTrans ->
  println "########################## OBJ: "+objTrans

 // member = delegator.findOne("Member", UtilMisc.toMap("partyId", objTrans.partyId), true);
  member = delegator.findOne("Member", [partyId : objTrans.partyId.toLong()], false);
  memberName = member.firstName + " " + member.middleName + " " + member.lastName
  println "#################################### memberName: "+memberName
  if (objTrans.transactionType != "Commission on MSACCO Withdrawal" || objTrans.transactionType != "Excise Duty") {

  }
  finalTransListBuilder = [
    createdStamp:objTrans.createdStamp,
    memberName:memberName,
    memberPhone:member.mobileNumber,
    transactionType:objTrans.transactionType,
    transactionAmount:objTrans.transactionAmount,
  ]

  finalTransList.add(finalTransListBuilder);
}


context.finalTransList = finalTransList
