cardStatusId = parameters.cardStatusId
   
cardStatusIdLong = cardStatusId.toLong();
msacco = delegator.findByAnd("MSaccoApplication",  [cardStatusId : cardStatusIdLong], null, false);
   
context.msacco = msacco;