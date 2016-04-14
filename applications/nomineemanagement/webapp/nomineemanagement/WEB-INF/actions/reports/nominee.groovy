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

memList = []
memListBuilder = [];
membersFoundList = [];

stationId = parameters.stationId
partyId = parameters.partyId

startDate = parameters.startDate
endDate = parameters.endDate

action = request.getParameter("action");

print " -------- Start Date"
println startDate

print " -------- End Date"
println endDate

java.sql.Date sqlEndDate = null;
java.sql.Date sqlStartDate = null;

//dateStartDate = Date.parse("yyyy-MM-dd hh:mm:ss", startDate).format("dd/MM/yyyy")

if ((startDate?.trim())){
	dateStartDate = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(startDate);
	sqlStartDate = new java.sql.Date(dateStartDate.getTime());
}
//(endDate != null) ||
if ((endDate?.trim())){
	dateEndDate = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(endDate);
	sqlEndDate = new java.sql.Date(dateEndDate.getTime());
}

print "formatted Date"


    startDateTimestamp = new Timestamp(sqlStartDate.getTime());
	endDateTimestamp = new Timestamp(sqlEndDate.getTime());

exprBldr = new org.ofbiz.entity.condition.EntityConditionBuilder()


//Get Station Number
member = null;
globalEmployerCode = null;

if ((stationId != null) && (!stationId.equals(""))){
	
	membersFoundList = delegator.findByAnd("Member",  [stationId : stationId.toLong()], null, false);
	
} else{
	membersFoundList = delegator.findByAnd("Member",  null, null, false);

}

if ((partyId != null) && (!partyId.equals(""))){
	
	membersFoundList = delegator.findByAnd("Member",  [partyId : partyId.toLong()], null, false);
	
}
membersFoundList.each { singleMember ->

	fullname = singleMember.firstName + "  " + singleMember.middleName + " " + singleMember.lastName + " Payroll Number: " + singleMember.payrollNumber
	
	memList.add(UtilMisc.toMap("firstName", "       MEMBERS DETAILS"))
	
	memListBuilder = [
		
		firstName : fullname
	
	]

	memList.add(memListBuilder)
	
	memList.add(UtilMisc.toMap("firstName", "       NOMINEES DETAILS"))
	
	memberPartyId = singleMember.partyId;
	memberPartyId = memberPartyId.toLong();
	
	expr = exprBldr.AND() {
			GREATER_THAN_EQUAL_TO(createdStamp: startDateTimestamp)
			   LESS_THAN_EQUAL_TO(createdStamp: endDateTimestamp)
			               EQUALS(partyId : memberPartyId)
			
		}
   EntityFindOptions findOptions = new EntityFindOptions();
	
	
	memberNomineeList = delegator.findList("MemberNomineeMapped",  expr, null, ["createdStamp ASC"], findOptions, false);
	
	memberNomineeList.each { singleNominee ->
		memListBuilder = [
			firstName : singleNominee.firstName,
			middleName : singleNominee.middleName,
		    lastName : singleNominee.lastName,
		    percentage : singleNominee.percentage,
		    relationship : singleNominee.name
	]
	
	memList.add(memListBuilder)
	
	}
	memList.add(UtilMisc.toMap("firstName", " "))
	
	
	
}

context.memList = memList

    


  




