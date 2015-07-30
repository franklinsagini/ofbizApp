import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.base.util.UtilDateTime;
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



summaryCondition = [];
//summaryCondition.add(EntityCondition.makeCondition("createdTxStamp", EntityOperator.GREATER_THAN_EQUAL_TO, fromDate));
summaryCondition.add(EntityCondition.makeCondition("createdTxStamp", EntityOperator.LESS_THAN, thruDate));
summaryCondition.add(EntityCondition.makeCondition("glAccountId", EntityOperator.EQUALS, glAccountId));
acctgTransEntry = delegator.findList('AcctgTransEntry', EntityCondition.makeCondition(summaryCondition, EntityOperator.AND), null, null, null, false)

acctgTransEntry.each { obj ->
  println "#################################### TRYING TO ITERATE OVER AcctgTransEntry: "
  transCond = []
  transCond.add(EntityCondition.makeCondition("acctgTransId", EntityOperator.EQUALS, obj.acctgTransId));
  accountTransaction = delegator.findList('AccountTransaction', EntityCondition.makeCondition(transCond, EntityOperator.AND), null, null, null, false)
  accountTransList.add(accountTransaction)
  println "#################################### acctgTransId: "+obj.acctgTransId
}

accountTransList.each { obj ->
  finalTransListBuilder = []
  memberCond = []
  memberCond.add(EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, obj.partyId));
  member = delegator.findList('Member', EntityCondition.makeCondition(memberCond, EntityOperator.AND), null, null, null, false)
  memberName = member.firstName + " " + member.middleName + " " + member.lastName
  println "#################################### memberName: "+memberName
  finalTransListBuilder = [
    createdStamp:obj.createdStamp,
    memberName:memberName,
    memberPhone:member.mobileNumber,
    memberPhone:member.mobileNumber,
    transactionType:obj.transactionType,
    transactionAmount:obj.transactionAmount,
  ]

  finalTransList.add(finalTransListBuilder);
}

finalTransList.each { obj ->
  println obj
}
context.finalTransList = finalTransList
