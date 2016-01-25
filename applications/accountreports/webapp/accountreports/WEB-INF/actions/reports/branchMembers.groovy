import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilMisc;
 import org.ofbiz.entity.condition.*;
 import org.ofbiz.entity.util.*;
 import org.ofbiz.entity.*;
 import org.ofbiz.base.util.*;
 import javolution.util.FastList;
 import javolution.util.FastSet;
 import javolution.util.FastMap;
 import org.ofbiz.entity.transaction.TransactionUtil;
 import org.ofbiz.entity.util.EntityListIterator;
 import org.ofbiz.entity.GenericEntity;
 import org.ofbiz.entity.model.ModelField;
 import org.ofbiz.base.util.UtilValidate;
 import org.ofbiz.entity.model.ModelEntity;
 import org.ofbiz.entity.model.ModelReader;
 import  org.ofbiz.accounting.ledger.CrbReportServices

totalsBuilder = []
totalsList = [];

memberStatusListBuilder = []
memberStatusList = [];

summaryCondition = [];

summaryCondition.add(EntityCondition.makeCondition("isBranch", EntityOperator.EQUALS, "Y"));
summaryCondition.add(EntityCondition.makeCondition("partyId", EntityOperator.NOT_EQUAL, "Company"));


branches = delegator.findList('PartyGroup',  EntityCondition.makeCondition(summaryCondition, EntityOperator.AND), null,null,null,false)
memberStatuses = delegator.findList('MemberStatus', null, null,null,null,false)

branches.each { branch ->
    totalMembership = 0
    memberStatusList.add(UtilMisc.toMap("statusName", branch.groupName,))
    memberStatuses.each { status ->
        
        memberCondition = [];
        memberCondition.add(EntityCondition.makeCondition("branchId", EntityOperator.EQUALS, branch.partyId));
        memberCondition.add(EntityCondition.makeCondition("memberStatusId", EntityOperator.EQUALS, status.memberStatusId));
        members = delegator.findList('Member',  EntityCondition.makeCondition(memberCondition, EntityOperator.AND), null,null,null,false)
        branchStatusCount = 0;
        members.each { member ->
            branchStatusCount = branchStatusCount + 1;
        }
        
        memberStatusListBuilder = [
            statusName:status.name,
            count:branchStatusCount
        ] 
        memberStatusList.add(memberStatusListBuilder)
        totalMembership = totalMembership + branchStatusCount
    }

     totalsBuilder = [
            branchName : branch.groupName,
            branchTotal : totalMembership
        ]
    totalsList.add(totalsBuilder)
    context.memberStatusList = memberStatusList
    context.memberStatusList.add(UtilMisc.toMap("statusName", "Total Membership", "count",  totalMembership))
    context.memberStatusList.add(UtilMisc.toMap("statusName", "", "count",  ""))

}
context.totalsList = totalsList

    


  




