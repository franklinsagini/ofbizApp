import org.ofbiz.base.util.UtilMisc;

import org.ofbiz.entity.Delegator;

partyId = parameters.partyId

//Get Station Number
member = null;
globalEmployerCode = null;

if ((partyId != null) && (!partyId.equals(""))){
	partyId = partyId.toLong();
	member = delegator.findOne("Member", [partyId : partyId], false);
	//globalEmployerCode = station.employerCode
	//Get Employers with defaulters
	if (member.salutationId != null)
		salutation = delegator.findOne("Salutation", [salutationId : member.salutationId], false);
	
	if (member.genderId != null)
		gender = delegator.findOne("Gender", [genderId : member.genderId], false);
	
	if (member.maritalStatusId != null)
		maritalStatus = delegator.findOne("MaritalStatus", [maritalStatusId : member.maritalStatusId], false);
	
		
		
		
		if (member.memberClassId != null)
		memberClass = delegator.findOne("MemberClass", [memberClassId : member.memberClassId], false);

		if (member.memberTypeId != null)
		memberType = delegator.findOne("MemberType", [memberTypeId : member.memberTypeId],false);

		
		
} 


context.member = member;
context.salutation = salutation;
context.gender = gender;
context.maritalStatus = maritalStatus;

context.memberClass = memberClass;
context.memberType = memberType;

