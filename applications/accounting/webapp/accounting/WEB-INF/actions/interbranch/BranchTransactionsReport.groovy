import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.base.util.UtilMisc;
import javolution.util.FastList;


branchId = parameters.get("organizationPartyId");
fundingTypeId = parameters.get("fundingTypeId");
entriesList = [];

List mainAndExprs = FastList.newInstance();
mainAndExprs.add(EntityCondition.makeCondition("glAccountId", EntityOperator.EQUALS, parameters.glAccountId));

if (fundingTypeId == "1") {
  //USE HQ ATM Settlement Account  gl_account_id = '206'

  //organization_party_id == branchId
  //party_id == memberId

  mainAndExprs.add(EntityCondition.makeCondition("glAccountId", EntityOperator.EQUALS, "206"));
  mainAndExprs.add(EntityCondition.makeCondition("organizationPartyId", EntityOperator.EQUALS, branchId));

entriesList = delegator.findList("AcctgTransAndEntries", EntityCondition.makeCondition(mainAndExprs, EntityOperator.AND), UtilMisc.toSet("transactionDate", "acctgTransId", "accountName","acctgTransTypeId", "debitCreditFlag", "amount"), UtilMisc.toList("acctgTransEntrySeqId"), null, false);

}
if (fundingTypeId == "2") {
  System.out.println("####################################### fundingTypeId: " + fundingTypeId)
}
if (fundingTypeId == "3") {
  System.out.println("####################################### fundingTypeId: " + fundingTypeId)
}

System.out.println("####################################### BranchId: " + branchId)

