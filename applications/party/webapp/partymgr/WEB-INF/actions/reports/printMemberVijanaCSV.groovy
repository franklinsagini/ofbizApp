import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionBuilder;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.entity.util.EntityFindOptions;
import java.text.SimpleDateFormat; 

stationId = "10069"
stationLong = stationId.toLong();

listOutMembers = []

expr = EntityCondition.makeCondition([EntityCondition.makeCondition("stationId", EntityOperator.EQUALS, stationLong)]);

memberList = delegator.findList("Member",expr,null,null, null, false);
memberList.eachWithIndex{ memberVijana, index ->
     partyId = memberVijana.partyId
     firstname = memberVijana.firstName
     lastname = memberVijana.lastName
     memberNumber= memberVijana.memberNumber
     name = firstname+"  "+lastname
     
     partyIdToString = partyId.toString();
    exprr = EntityCondition.makeCondition([EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, partyIdToString)]);
    noOfMembers = delegator.findList("VijanaTuinvest",exprr, null, null, null, false);
    int num =  noOfMembers.size();
    numToString = ""+num
    println("NuMBER OF MEMBERS"+name +""+numToString)
    
     exprrr = EntityCondition.makeCondition([EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, partyIdToString),
                                             EntityCondition.makeCondition("idSaccoMember", EntityOperator.EQUALS, "Y")]);
    noOfMembersChai = delegator.findList("VijanaTuinvest",exprrr, null, null, null, false);
    int chaiMembers = noOfMembersChai.size();
    chaiMembersToString = ""+chaiMembers
    println("NuMBER OF CHAI  MEMBERS"+name+""+noOfMembers)
    
          listOutMembers.add([ memberNumber: memberNumber, groupName: name,noOfMembers: numToString, noOfChaiMembers: chaiMembersToString])
    }

context.listOutMembers = listOutMembers;
