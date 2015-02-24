import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionBuilder;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.entity.util.EntityFindOptions;
import java.text.SimpleDateFormat; 

action = request.getParameter("action");
overStayedFileslist = [];
now = Calendar.getInstance().getTime().toString();
noww = new SimpleDateFormat("E MMM dd HH:mm:ss zzz yyyy", Locale.UK).parse(now);
today = new java.sql.Timestamp(noww.getTime());

exprBldr = new org.ofbiz.entity.condition.EntityConditionBuilder();

expr = exprBldr.AND() {
			LESS_THAN(issuedReturnDate: today)
			EQUALS(status: "ISSUED")
		}
		
		
EntityFindOptions findOptions = new EntityFindOptions();
findOptions.setMaxRows(100);
overStayedFiles = delegator.findList("RegistryFiles", expr, null, ["issuedReturnDate ASC"], findOptions, false);


 overStayedFiles.eachWithIndex { overStayedFilesItem, index ->
 party = overStayedFilesItem.partyId;
 partylong = party.toLong();
 member = delegator.findOne("Member", [partyId : partylong], false);
 currentPossesser = delegator.findOne("Person", [partyId : overStayedFilesItem.currentPossesser], false);
 act = delegator.findOne("RegistryFileActivity", [activityId : overStayedFilesItem.Reason], false);
 
 fileOwner = "${member.firstName}  ${member.lastName}";
 currentPossessor =  "${currentPossesser.firstName}  ${currentPossesser.lastName}";
 issuedFor = act.activity;
 duration = overStayedFilesItem.activityDuration;
 timeout = overStayedFilesItem.issueDate;
 dateStringout = timeout.format("yyyy-MMM-dd HH:mm:ss a")
 timeIssued = dateStringout;
  timein = overStayedFilesItem.issuedReturnDate;
 dateStringin = timein.format("yyyy-MMM-dd HH:mm:ss a")
 returnDate = dateStringin;
 
 
 
 
 
 overStayedFileslist.add([fileOwner :fileOwner, issuedFor : issuedFor, timeIssued :timeIssued, 
 duration :duration, returnDate : returnDate, currentPossessor :currentPossessor]);
 }
 
 
context.overStayedFileslist = overStayedFileslist;
