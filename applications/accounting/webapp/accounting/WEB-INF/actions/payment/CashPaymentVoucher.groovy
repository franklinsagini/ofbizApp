import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.base.util.UtilMisc;

paymentId = parameters.get("paymentId");

payment = delegator.findOne("Payment", [paymentId : paymentId], false);
context.payment = payment;


organizationPartyIdFrom = payment.get("partyIdFrom");
organizationPartyGroupFrom = delegator.findOne("PartyGroup", [partyId : organizationPartyIdFrom], false);
if (organizationPartyGroupFrom) {
  context.organizationPartyGroupFrom = organizationPartyGroupFrom.groupName;
}


if(!organizationPartyGroupFrom){
  organizationPartyGroupFrom = delegator.findOne("Person", [partyId : organizationPartyIdTo], false);
  context.organizationPartyGroupFrom = organizationPartyGroupFrom.firstName + " " + organizationPartyGroupFrom.lastName;
}

if(!organizationPartyGroupFrom){
  organizationPartyIdToLong = organizationPartyIdTo.toLong()
  organizationPartyGroupFrom = delegator.findOne("Member", [partyId : organizationPartyIdToLong], false);
  if(organizationPartyGroupFrom){
    context.organizationPartyGroupFrom = organizationPartyGroupFrom.firstName + " " + organizationPartyGroupFrom.lastName;
  }
}


organizationPartyIdTo = payment.get("partyIdTo");
organizationPartyGroupTo = delegator.findOne("PartyGroup", [partyId : organizationPartyIdTo], false);
if (organizationPartyGroupTo) {
  context.organizationPartyGroupTo = organizationPartyGroupTo.groupName;
}



if(!organizationPartyGroupTo){
  organizationPartyGroupTo = delegator.findOne("Person", [partyId : organizationPartyIdTo], false);
  context.organizationPartyGroupTo = organizationPartyGroupTo.firstName + " " + organizationPartyGroupTo.lastName;
}

if(!organizationPartyGroupTo){
  organizationPartyIdToLong = organizationPartyIdTo.toLong()
  organizationPartyGroupTo = delegator.findOne("Member", [partyId : organizationPartyIdToLong], false);
  if(organizationPartyGroupTo){
    context.organizationPartyGroupTo = organizationPartyGroupTo.firstName + " " + organizationPartyGroupTo.lastName;
  }
}

orderBy = UtilMisc.toList("acctgTransId", "acctgTransEntrySeqId");

acctgTransAndEntries = delegator.findList("AcctgTransAndEntries", EntityCondition.makeCondition("paymentId", EntityOperator.EQUALS, paymentId), null, orderBy, null, false);

context.acctgTransAndEntries = acctgTransAndEntries;

