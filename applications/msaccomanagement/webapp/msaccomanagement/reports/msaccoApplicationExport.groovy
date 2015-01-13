

action = request.getParameter("action");

msaccoApplications = [];
 msacco = delegator.findList("MSaccoApplication", null, null, null,null, false);
 
 msacco.eachWithIndex { msaccoItem, index ->
	no = msaccoItem.mobilePhoneNumber;

 
 party = msaccoItem.partyId;
 name = delegator.findOne("Member", [partyId : party], false);
 fname = name.getString("firstName");
 lname = name.getString("lastName");
 idNo = name.getString("idNumber");
 
 accId = msaccoItem.memberAccountId;
 acc = delegator.findOne("MemberAccount", [memberAccountId : accId], false);
 accNo = acc.getString("accountNo");

 msaccoApplications.add([fname :fname, lname :lname, phone : no, IdNo : idNo, accNo : accNo]);
 }
 
 

            
context.msaccoApplications = msaccoApplications;
