cardStatusId = parameters.cardStatusId

cardStatusIdLong = cardStatusId.toLong();
 statusName = org.ofbiz.msaccomanagement.MSaccoManagementServices.getCardStatusId(statusName);

card = delegator.findByAnd("CardApplication",  [cardStatusId : cardStatusIdLong], null, false);

context.card = card;