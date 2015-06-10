import org.ofbiz.base.util.UtilMisc;

import org.ofbiz.entity.Delegator;

stationId = parameters.stationId
partyId = parameters.stationId

//Get Station Number
member = null;
globalEmployerCode = null;

if ((stationId != null) && (!stationId.equals(""))){
	//member = delegator.findOne("Member", [stationId : stationId], false);
	//globalEmployerCode = station.employerCode
	
	//Get Employers with defaulters
	memberList = delegator.findByAnd("Member",  [stationId : stationId.toLong()], null, false);
	
} else{
	memberList = delegator.findByAnd("Member",  null, null, false);

}
//Station Number
//stationNumber = station.stationNumber

//employerCode = station.employerCode
//def defaultLoanStatusId = org.ofbiz.accountholdertransactions.LoanUtilities.getLoanStatusId('DEFAULTED');
//defaultLoanStatusId = defaultLoanStatusId.toLong();
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
	memberNomineeList = delegator.findByAnd("MemberNomineeMapped",  [partyId : memberPartyId], null, false);
		
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
	}
	 
	listMembers << member;
	
	
}

context.listMembers = listMembers;