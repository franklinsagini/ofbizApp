 
 status = parameters.cardStatusId
 statusLong = status.toLong();
 
statusName = org.ofbiz.msaccomanagement.MSaccoManagementServices.getCardStatusName(statusLong);

println("############STATUS NAME########"+statusName); 
 
 msacco = delegator.findByAnd("MSaccoApplication", [cardStatusId : statusLong ], null, false);
 
 context.msacco = msacco
 context.statusName = statusName