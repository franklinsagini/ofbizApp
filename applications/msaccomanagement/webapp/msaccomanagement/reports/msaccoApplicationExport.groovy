

action = request.getParameter("action");

msaccoApplications = [];
 msacco = delegator.find("MsaccoApplicationView", null, null, null, ['memberAccountId'], null);
 while ((msaccoItem = msacco.next()) != null) {
 no = msaccoItem.getString("mobilePhoneNumber");
 idNo = msaccoItem.getString("idNumber");
 party = msaccoItem.getString("partyId");
 LongParty = party.toLong();
 name = delegator.findOne("Member", [partyId : LongParty], false);
 fname = name.getString("firstName");
 lname = name.getString("lastName");
 
 accId = msaccoItem.getString("memberAccountId");
 LongaccId = accId.toLong();
 acc = delegator.findOne("MemberAccount", [memberAccountId : LongaccId], false);
 accNo = acc.getString("accountNo");
 }
 
 
msaccoApplications.add([fname :fname, lname :lname, phone : no, IdNo : idNo, accNo : accNo]);
            
context.msaccoApplications = msaccoApplications;
