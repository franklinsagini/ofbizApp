package org.ofbiz.purchases.order;

import java.math.BigDecimal;
import java.util.List;

import org.apache.log4j.Logger;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;


public class OrderWorker  {
	
	public static String module = OrderWorker.class.getName();
	public static Logger log = Logger.getLogger(OrderWorker.class);
	
	
	public static BigDecimal getOrder(Delegator delegator, String orderId){
		
		log.info("orderId Passed ################################ " + orderId);
		
		if(delegator == null){
			throw new IllegalArgumentException("Null delegator is not allowed in this method");
		}
		GenericValue order = null;
		try {
			order = delegator.findOne("Order", UtilMisc.toMap("orderId", orderId), false);
			log.info("Order Has been Fetched Guyz ################################ " + order);
		} catch (GenericEntityException e) {
			Debug.logError(e, "Cannot Find Order", module);
			log.info("Bad News Jim Cannot Find Order ################################ " + order);
		}
		if (order == null){
			throw new IllegalArgumentException("The Passed order id "+orderId+" does not match an existing invoice");
		}
		BigDecimal orderTotal = getOrderTotal(order);
		log.info("Final Returned Order Total ################################ " + orderTotal);
		return orderTotal;
	}
	public static BigDecimal getOrderTotal(GenericValue order){
		BigDecimal orderTotal = BigDecimal.ZERO;
		List<GenericValue>orderLines = null;
		
		try {
			orderLines =  order.getRelated("OrderLine", null, null, false);
			log.info("Call for a celebration people Order Lines Found ################################ " + orderLines);
		} catch (GenericEntityException e) {
			Debug.logError("Trouble Getting OrderLines",module);
			log.info("Bad News Folks, We Still Need to learn more code, Orderlines Not Found ################################ " + orderLines);
		}
		if (orderLines != null){
			for (GenericValue orderLine : orderLines){
				log.info("Order Line To Loop ################################ " + orderLine);
				orderTotal = orderTotal.add(getOrderLineAmount(orderLine));
			}
		}
		log.info("Order Total ################################ " + orderTotal);
		return orderTotal;
	}
	private static BigDecimal getOrderLineAmount(GenericValue orderLine) {
		BigDecimal quantity = orderLine.getBigDecimal("quantity");
		log.info("Quantity ################################ " + quantity);
		if (quantity == null){
			quantity = BigDecimal.ONE;
		}
		BigDecimal unitPrice = orderLine.getBigDecimal("unitPrice");
		log.info("Quantity ################################ " + unitPrice);
		if(unitPrice == null){
			unitPrice = BigDecimal.ZERO;
		}
		BigDecimal lineAmount = quantity.multiply(unitPrice);
		log.info("Line Amount ################################ " + lineAmount);
		return lineAmount;
	}

}
