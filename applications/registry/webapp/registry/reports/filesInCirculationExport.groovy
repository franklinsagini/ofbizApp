
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionBuilder;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.entity.util.EntityFindOptions;
import java.text.SimpleDateFormat; 

filesInCirculationlist= [];

exprBldr = new org.ofbiz.entity.condition.EntityConditionBuilder()





	expr = exprBldr.AND() {
			NOT_EQUAL(currentPossesser: "REGISTRY")
		}
EntityFindOptions findOptions = new EntityFindOptions();
findOptions.setMaxRows(100);
filesInCirculation = delegator.findList("RegistryFiles", expr, null, null, findOptions, false)
	
  filesInCirculation.eachWithIndex { filesInCirculationItem, index ->
 party = filesInCirculationItem.partyId;
 partylong = party.toLong();
 member = delegator.findOne("Member", [partyId : partylong], false);
 
 memberNumber = filesInCirculationItem.memberNumber;
 fileOwner = "${member.firstName} ${member.lastName}";
 currentPossesser = filesInCirculationItem.currentPossesser;
 filewith = delegator.findOne("Person", [partyId : currentPossesser], false);
 filewithName ="${filewith.firstName} ${filewith.lastName}";
 
  timein  = filesInCirculationItem.issueDate;
 dateStringin = timein.format("yyyy-MMM-dd HH:mm:ss a")
 time = dateStringin;
 
 
 
 
 filesInCirculationlist.add([fileOwner :fileOwner, memberNumber :memberNumber, filewith : filewithName, time : time]);
 }
context.filesInCirculationlist = filesInCirculationlist;