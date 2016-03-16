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
 import java.text.SimpleDateFormat;
  import org.ofbiz.accounting.branchreports.BranchUtilServices


// --- date modification
java.sql.Date sqlEndDate = null;
java.sql.Date sqlStartDate = null;

if ((parameters.startDate?.trim())){
    dateStartDate = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(parameters.startDate);

    sqlStartDate = new java.sql.Date(dateStartDate.getTime());
}
//(endDate != null) ||
if ((parameters.endDate?.trim())){
    dateEndDate = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(parameters.endDate);
    sqlEndDate = new java.sql.Date(dateEndDate.getTime());
}

startDateTimestamp = new Timestamp(sqlStartDate.getTime());
endDateTimestamp = new Timestamp(sqlEndDate.getTime());

// ---End  date modification

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
        //memberCondition.add(EntityCondition.makeCondition("branchId", EntityOperator.EQUALS, branch.partyId));
        memberCondition.add(EntityCondition.makeCondition("memberStatusId", EntityOperator.EQUALS, status.memberStatusId));

        memberCondition.add(EntityCondition.makeCondition("createdStamp", EntityOperator.GREATER_THAN_EQUAL_TO, startDateTimestamp));
        memberCondition.add(EntityCondition.makeCondition("createdStamp", EntityOperator.LESS_THAN_EQUAL_TO, endDateTimestamp));

        openingMemberCondition = [];
        openingMemberCondition.add(EntityCondition.makeCondition("memberStatusId", EntityOperator.EQUALS, status.memberStatusId));
        openingMemberCondition.add(EntityCondition.makeCondition("createdStamp", EntityOperator.LESS_THAN, startDateTimestamp));

        closingMemberCondition = [];
        closingMemberCondition.add(EntityCondition.makeCondition("memberStatusId", EntityOperator.EQUALS, status.memberStatusId));
        closingMemberCondition.add(EntityCondition.makeCondition("createdStamp", EntityOperator.LESS_THAN_EQUAL_TO, endDateTimestamp));

        members = delegator.findList('MemberStatusLog',  EntityCondition.makeCondition(memberCondition, EntityOperator.AND), null,null,null,false)


        openingMembers = delegator.findList('MemberStatusLog',  EntityCondition.makeCondition(openingMemberCondition, EntityOperator.AND), null,null,null,false)

        closingMembers = delegator.findList('MemberStatusLog',  EntityCondition.makeCondition(closingMemberCondition, EntityOperator.AND), null,null,null,false)

        openingBranchStatusCount = 0;
        openingMembers.each { member ->
            branchId = BranchUtilServices.getMembersBranch(delegator, member.partyId)
            if (branchId == branch.partyId) {
                openingBranchStatusCount = openingBranchStatusCount + 1;
            }
        }

        closingBranchStatusCount = 0;
        closingMembers.each { member ->
            branchId = BranchUtilServices.getMembersBranch(delegator, member.partyId)
            if (branchId == branch.partyId) {
                closingBranchStatusCount = closingBranchStatusCount + 1;
            }
        }

        branchStatusCount = 0;
        members.each { member ->
            branchId = BranchUtilServices.getMembersBranch(delegator, member.partyId)
            if (branchId == branch.partyId) {
                branchStatusCount = branchStatusCount + 1;
            }

        }




        memberStatusListBuilder = [
            statusName:status.name,
            balanceBroughtForward:openingBranchStatusCount,
            count:branchStatusCount,
            closingBalance:closingBranchStatusCount
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