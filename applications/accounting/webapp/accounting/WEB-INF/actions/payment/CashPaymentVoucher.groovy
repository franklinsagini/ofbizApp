import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.base.util.UtilMisc;

paymentId = parameters.get("paymentId");

payment = delegator.findOne("Payment", [paymentId : paymentId], false);
context.payment = payment;


organizationPartyIdFrom = payment.get("partyIdFrom");
organizationPartyGroupFrom = delegator.findOne("PartyGroup", [partyId : organizationPartyIdFrom], false);
context.organizationPartyGroupFrom = organizationPartyGroupFrom.groupName;

if(!organizationPartyGroupFrom){
  organizationPartyIdToLong = organizationPartyIdTo.toLong()
  organizationPartyGroupFrom = delegator.findOne("Member", [partyId : organizationPartyIdToLong], false);
  if(organizationPartyGroupTo){
    context.organizationPartyGroupFrom = organizationPartyGroupFrom.firstName + " " + organizationPartyGroupFrom.lastName;
  }
}
if(!organizationPartyGroupFrom){
  organizationPartyGroupFrom = delegator.findOne("Person", [partyId : organizationPartyIdTo], false);
  context.organizationPartyGroupFrom = organizationPartyGroupFrom.firstName + " " + organizationPartyGroupFrom.lastName;
}



organizationPartyIdTo = payment.get("partyIdTo");
organizationPartyGroupTo = delegator.findOne("PartyGroup", [partyId : organizationPartyIdTo], false);
context.organizationPartyGroupTo = organizationPartyGroupTo;

if(!organizationPartyGroupTo){
  organizationPartyIdToLong = organizationPartyIdTo.toLong()
  organizationPartyGroupTo = delegator.findOne("Member", [partyId : organizationPartyIdToLong], false);
  if(organizationPartyGroupTo){
    context.organizationPartyGroupTo = organizationPartyGroupTo.firstName + " " + organizationPartyGroupTo.lastName;
  }
}
if(!organizationPartyGroupTo){
  organizationPartyGroupTo = delegator.findOne("Person", [partyId : organizationPartyIdTo], false);
  context.organizationPartyGroupTo = organizationPartyGroupTo.firstName + " " + organizationPartyGroupTo.lastName;
}


orderBy = UtilMisc.toList("acctgTransId", "acctgTransEntrySeqId");

acctgTransAndEntries = delegator.findList("AcctgTransAndEntries", EntityCondition.makeCondition("paymentId", EntityOperator.EQUALS, paymentId), null, orderBy, null, false);

context.acctgTransAndEntries = acctgTransAndEntries;

