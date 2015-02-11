paymentId = parameters.get("paymentId");

payment = delegator.findOne("Payment", [paymentId : paymentId], false);
context.payment = payment;
