import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionBuilder;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.entity.util.EntityFindOptions;
import java.text.SimpleDateFormat;
import javolution.util.FastList;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;


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
	
	memberList = delegator.findByAnd("Member",  [stationId : stationId.toLong()], null, false);
	
} else{
	memberList = delegator.findByAnd("Member",  null, null, false);

}

if ((partyId != null) && (!partyId.equals(""))){
	
	memberList = delegator.findByAnd("Member",  [partyId : partyId.toLong()], null, false);
	
}


class Member{
	def memberNumber
	def payrollNumber
	def firstName
	def middleName
	def lastName
	
	def listNominee = []
}

class Nominee {
	def firstName
	def middleName
	def lastName
	def percentage
	def name
}

/***
class MemberExpecation{
	def payrollNumber
	def memberNumber
	def mobileNumber
	def memberNames;
	def status
	def termsOfService
	
	def listOfExpectReceive = []
}


class ExpectReceive{
	def payrollNumber
	def name
	def status
	def termsOfService
	def description
	def balance
	
	def expected
	def received
	def variance
} **/

def listMembers = [];

//def member

def memList = []
def nomineeList = []

//Buid a variance list

//context.stationName = station.name;
//context.stationNumber = station.stationNumber;
//context.month = month
countNo = 0;
memberList.eachWithIndex { memberItem, index ->
	
	countNo = countNo + 1;
	println " No is "+countNo;
	println " First Name ==  "+memberItem.firstName;
	member = new Member();
	
	member.memberNumber = memberItem.memberNumber;
	member.payrollNumber = memberItem.payrollNumber;
	member.firstName = memberItem.firstName;
	member.middleName = memberItem.middleName;
	member.lastName = memberItem.lastName;

	
	//Get Defaulted Loans for the Employer and add them to the list
	//employerCode = stationEmployerItem.employerCode;
	memberPartyId = memberItem.partyId;
	memberPartyId = memberPartyId.toLong();
	
	expr = exprBldr.AND() {
			GREATER_THAN_EQUAL_TO(createdStamp: startDateTimestamp)
			   LESS_THAN_EQUAL_TO(createdStamp: endDateTimestamp)
			               EQUALS(partyId : memberPartyId)
			
		}
   EntityFindOptions findOptions = new EntityFindOptions();
	
	
	memberNomineeList = delegator.findList("MemberNomineeMapped",  expr, null, ["createdStamp ASC"], findOptions, false);
		
	memberNomineeList.eachWithIndex { memberNomineeItem, nomineeIndex ->

		memberNominee = new Nominee();
		
		memberNominee.firstName = memberNomineeItem.firstName;
		memberNominee.middleName = memberNomineeItem.middleName;
		memberNominee.lastName = memberNomineeItem.lastName;
		memberNominee.percentage = memberNomineeItem.percentage;
		//memberNominee.name = memberNomineeItem.name;

		println " Nominee First Name ==  "+memberNominee.firstName;
		//println " Nominee Relationship Name ==  "+memberNominee.name;
		member.listNominee.add(memberNominee);
		
		nomineListBuilder = [
		firstName :memberNominee.firstName,
		middleName:memberNominee.firstName,
		lastName:memberNominee.lastName,
		percentage:memberNominee.percentage
		]
		
	}
	memList.add(nomineListBuilder);
	//memList.add([memPayrollNo :memberItem.payrollNumber,memFirtsName :member.firstName, memlastName : member.lastName,
	 //            memMiddleName :member.middleName, memMemberNo : member.memberNumber])
	   
	//memList.add([firstName:memberNominee.firstName,middleName:memberNominee.firstName,lastName:memberNominee.lastName,percentage:memberNominee.percentage])
	memList.add(UtilMisc.toMap("firstName", "Samoei"))
	 
	//listMembers.add(memList);
	//listMembers << member;
}
context.memList = memList
context.listMembers = listMembers;