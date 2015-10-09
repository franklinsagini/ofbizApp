
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionBuilder;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.entity.util.EntityFindOptions;
import java.text.SimpleDateFormat; 

startDate = parameters.dateFrom
endDate = parameters.dateB
activityId = parameters.activityId


//dateStartDate = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(startDate);
//dateEndDate = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(endDate);

 //sqlStartDate = new java.sql.Timestamp(dateStartDate.getTime());
 //sqlEndDate = new java.sql.Timestamp(dateEndDate.getTime());


filesInCirculationlist= [];

def actName = null ;

exprBldr = new org.ofbiz.entity.condition.EntityConditionBuilder()

	expr = exprBldr.AND() {
			NOT_EQUAL(currentPossesser: "REGISTRY")
			EQUALS(status : "ISSUED")
		}
		
EntityFindOptions findOptions = new EntityFindOptions();
findOptions.setMaxRows(100);
filesInCirculation = delegator.findList("RegistryFiles", expr, null, null, findOptions, false)
	
  filesInCirculation.eachWithIndex { filesInCirculationItem, index ->
 party = filesInCirculationItem.partyId;
 partylong = party.toLong();
 member = delegator.findOne("Member", [partyId : partylong], false);
 
reason = filesInCirculationItem.Reason;
 reasonToString =  reason.toString();
 
 
actName = org.ofbiz.registry.FileServices.getactivityName(reasonToString);
 
 println("#######REASON ID  #######"+reason);
 println("#######REASON NAME JAVA   #######"+actName);


//registryActivityName = delegator.findOne("RegistryFileActivity",[activityId : reason ],false);
 //activityName = registryActivityName.activity;
 // println("#######REASON NAME  #######"+activityName);
 
//activityName = filesInCirculationItem.Reason;
 
 payRollNo  = filesInCirculationItem.payrollNumber;
 memberNumber = filesInCirculationItem.memberNumber;
 fileOwner = "${member.firstName} ${member.lastName}";
 currentPossesser = filesInCirculationItem.currentPossesser;
 filewith = delegator.findOne("Person", [partyId : currentPossesser], false);
 filewithName ="${filewith.firstName} ${filewith.lastName}";
  println("#######NAME  #######"+filewithName);
 timein  = filesInCirculationItem.issueDate;
 dateStringin = timein.format("yyyy-MMM-dd HH:mm:ss a")
 time = dateStringin;


//activityName: activityName , 


 filesInCirculationlist.add([payRollNo : payRollNo, activityName: actName ,  fileOwner :fileOwner, memberNumber :memberNumber, filewith : filewithName, time : time]);
 }
context.filesInCirculationlist = filesInCirculationlist;

