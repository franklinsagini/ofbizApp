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

msaccoSettlementAc = "34000013"
atmSettlementAc = "34000010"


println "############################################## "+thruDate
println "############################################## "+fromDate
println "############################################## "+glAccountId
finalTransList = []

//MSACCO SETTLEMENT
if(glAccountId == msaccoSettlementAc) {
  accountTransList = []
  finalTransListBuilder = []
  currentacctgTransId = 0
  runningBalance = 0


summaryCondition = [];
summaryCondition.add(EntityCondition.makeCondition("createdTxStamp", EntityOperator.GREATER_THAN_EQUAL_TO, fromDate));
summaryCondition.add(EntityCondition.makeCondition("createdTxStamp", EntityOperator.LESS_THAN, thruDate));
summaryCondition.add(EntityCondition.makeCondition("glAccountId", EntityOperator.EQUALS, glAccountId));
summaryCondition.add(EntityCondition.makeCondition("glAccountTypeId", EntityOperator.EQUALS, "MEMBER_DEPOSIT"));
acctgTransEntry = delegator.findList('AcctgTransEntry', EntityCondition.makeCondition(summaryCondition, EntityOperator.AND), null, null, null, false)

acctgTransEntry.each { obj ->
  println "#################################### TRYING TO ITERATE OVER AcctgTransEntry: "
  transCond = []
  currentacctgTransId = obj.acctgTransId
  transCond.add(EntityCondition.makeCondition("acctgTransId", EntityOperator.EQUALS, obj.acctgTransId));

  accountTransactionSublist = delegator.findList('AccountTransaction', EntityCondition.makeCondition(transCond, EntityOperator.AND), null, null, null, false)
  accountTransactionSublist.each { singleTransaction ->
    println "########################### ADDING  singleTransaction "+ singleTransaction.accountTransactionId
    accountTransList.add(singleTransaction)
  }

  println "#################################### accountTransaction: "+ accountTransactionSublist
}

accountTransList.each { objTrans ->
  println "########################## OBJ: "+objTrans
  mobileNumber = null
  conditions = []
  conditions.add(EntityCondition.makeCondition("memberAccountId", EntityOperator.EQUALS, objTrans.memberAccountId));
  mSaccoApplication = delegator.findList('MSaccoApplication', EntityCondition.makeCondition(conditions, EntityOperator.AND), null, null, null, false)
  mSaccoApplication.each { singlemSaccoApplication ->
    mobileNumber = singlemSaccoApplication.mobilePhoneNumber
  }
 // member = delegator.findOne("Member", UtilMisc.toMap("partyId", objTrans.partyId), true);
  member = delegator.findOne("Member", [partyId : objTrans.partyId.toLong()], false);
  memberName = member.firstName + " " + member.middleName + " " + member.lastName

  println "#################################### memberName: "+memberName

  if (objTrans.transactionType == 'MSACCOWITHDRAWAL' || objTrans.transactionType == 'M-sacco Settlement Charge') {
    runningBalance = runningBalance + objTrans.transactionAmount
    finalTransListBuilder = [
      createdStamp:objTrans.createdStamp,
      memberName:memberName,
      reference:mobileNumber,
      transactionType:objTrans.transactionType,
      transactionAmount:objTrans.transactionAmount,
      runningBalance:runningBalance

    ]

    finalTransList.add(finalTransListBuilder);
  }

}

}


//ATM SETTLEMENT
if (glAccountId == atmSettlementAc) {
  accountTransList = []
  finalTransListBuilder = []
  runningBalance = 0

  summaryCondition = [];
  summaryCondition.add(EntityCondition.makeCondition("createdTxStamp", EntityOperator.GREATER_THAN_EQUAL_TO, fromDate));
  summaryCondition.add(EntityCondition.makeCondition("createdTxStamp", EntityOperator.LESS_THAN, thruDate));
  summaryCondition.add(EntityCondition.makeCondition("glAccountId", EntityOperator.EQUALS, glAccountId));
  summaryCondition.add(EntityCondition.makeCondition("glAccountTypeId", EntityOperator.EQUALS, "MEMBER_DEPOSIT"));
  acctgTransEntry = delegator.findList('AcctgTransEntry', EntityCondition.makeCondition(summaryCondition, EntityOperator.AND), null, null, null, false)

  acctgTransEntry.each { obj ->
    transCond = []
    transCond.add(EntityCondition.makeCondition("acctgTransId", EntityOperator.EQUALS, obj.acctgTransId));
    accountTransactionSublist = delegator.findList('AccountTransaction', EntityCondition.makeCondition(transCond, EntityOperator.AND), null, null, null, false)
    accountTransactionSublist.each { singleTransaction ->
      accountTransList.add(singleTransaction)
    }
  }

accountTransList.each { objTrans ->
  cardNumber = null
   def cardNumberSliced = null
  conditions = []
  conditions.add(EntityCondition.makeCondition("memberAccountId", EntityOperator.EQUALS, objTrans.memberAccountId));
  cardApplication = delegator.findList('CardApplication', EntityCondition.makeCondition(conditions, EntityOperator.AND), null, null, null, false)
  cardApplication.each { singlemcardApplication ->
    cardNumber = singlemcardApplication.cardNumber

    def cardNumberSliced = cardNumber[-11..-1]
  }
 // member = delegator.findOne("Member", UtilMisc.toMap("partyId", objTrans.partyId), true);
  member = delegator.findOne("Member", [partyId : objTrans.partyId.toLong()], false);
  memberName = member.firstName + " " + member.middleName + " " + member.lastName
  println "#################################### memberName: "+memberName

  if (objTrans.transactionType == 'ATMWITHDRAWAL' || objTrans.transactionType == 'ATM Clearing Account') {
    runningBalance = runningBalance + objTrans.transactionAmount
    finalTransListBuilder = [
      createdStamp:objTrans.createdStamp,
      memberName:memberName,
      reference:cardNumber,
      transactionType:objTrans.transactionType,
      transactionAmount:objTrans.transactionAmount,
      runningBalance:runningBalance
    ]

    finalTransList.add(finalTransListBuilder);
  }

}


}

context.finalTransList = finalTransList
