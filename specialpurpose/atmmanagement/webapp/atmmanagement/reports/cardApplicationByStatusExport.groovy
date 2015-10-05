
cardStatusId = parameters.cardStatusId

cardStatusIdLong = cardStatusId.toLong();

action = request.getParameter("action");

cardApplications = [];
 card = delegator.findByAnd("CardApplication",  [cardStatusId : cardStatusIdLong], null, false);
 
 card.eachWithIndex { cardItem, index ->
 
 formNo = cardItem.getString("formNumber");
 cardNo = cardItem.getString("cardNumber");
 idNo = cardItem.getString("idNumber");
 fname = cardItem.getString("firstName");
 lname = cardItem.getString("lastName");
 
 cardStatusId = cardItem.getString("cardStatusId");
 cardStatus = delegator.findOne("CardStatus", [cardStatusId : cardStatusId.toLong()], false);
 status = cardStatus.getString("name");
 
 accId = cardItem.getString("memberAccountId");
 acc = delegator.findOne("MemberAccount", [memberAccountId : accId.toLong()], false);
 accNo = acc.getString("accountNo");
 
 cardApplications.add([fname :fname, lname :lname, IdNo : idNo, accNo : accNo, formNumber : formNo, cardNo : cardNo, cardStatus : status]);
 }
 
 
context.cardApplications = cardApplications;
context.status = status;
