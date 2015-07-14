cardStatusId = parameters.cardStatusId

cardStatusIdLong = cardStatusId.toLong();
card = delegator.findByAnd("CardApplication",  [cardStatusId : cardStatusIdLong], null, false);

context.card = card;