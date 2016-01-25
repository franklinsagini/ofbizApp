		import org.ofbiz.base.util.UtilMisc;
		import org.ofbiz.entity.Delegator;
		import org.ofbiz.entity.util.EntityUtil;
		import org.ofbiz.entity.condition.EntityCondition;
		import org.ofbiz.entity.condition.EntityConditionBuilder;
		import org.ofbiz.entity.condition.EntityConditionList;
		import org.ofbiz.entity.condition.EntityExpr;
		import org.ofbiz.entity.condition.EntityOperator;
		import org.ofbiz.base.util.UtilDateTime;
		import org.ofbiz.entity.util.EntityFindOptions;
		import java.text.SimpleDateFormat;
		
		import javolution.util.FastList;
		
		action = request.getParameter("action");
		
		
		transactionType = parameters.transaction
		startDate = parameters.startDate
		endDate = parameters.endDate
		asAtDate = parameters.asAtDate
		treasuryId = parameters.treasuryId
		
		action = request.getParameter("action");
		
		print " -------- Start Date"
		println startDate
		
		print " -------- End Date"
		println endDate
		
		//java.sql.Date sqlEndDate = null;
		//java.sql.Date sqlStartDate = null;
		java.sql.Date sqlAsAtDate = null;
		
		
		//if ((startDate?.trim())){
		//	dateStartDate = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(startDate);
		//	sqlStartDate = new java.sql.Date(dateStartDate.getTime());
		//}
		//(endDate != null) ||
		//if ((endDate?.trim())){
		//	dateEndDate = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(endDate);
		//	sqlEndDate = new java.sql.Date(dateEndDate.getTime());
		//}
		
		if ((asAtDate?.trim())){
			dateAsAtDate = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(asAtDate);
			sqlAsAtDate = new java.sql.Date(dateAsAtDate.getTime());
		}
		
		//startDateTimestamp = new Timestamp(sqlStartDate.getTime());
		//endDateTimestamp = new Timestamp(sqlEndDate.getTime());
		
		asAtDateTimestamp = new Timestamp(sqlAsAtDate.getTime());
		
		
		print "formatted Date"
		
		
		transactionals = [];
		
		  
		    exprBldr = new org.ofbiz.entity.condition.EntityConditionBuilder();
		
		//    if (!(sqlEndDate)  && !(dateAsAtDate)  && !(sqlAsAtDate)){
		     
		//        expr = exprBldr.AND() {
		//			EQUALS(transactionType : transact.transactionType)
		//		 }
		//     }
		//    
		    
		  //if ((sqlStartDate) && (sqlEndDate) && (treasuryId)){
			//	expr = exprBldr.AND() {
					 // GREATER_THAN_EQUAL_TO(createdStamp: startDateTimestamp)
					 // LESS_THAN_EQUAL_TO(createdStamp: endDateTimestamp)
				//	  EQUALS(transactionType : transactionType)
				//	  EQUALS(treasuryId: treasuryId)
			//	   }
			//   }
		     
		     	
			if ((treasuryId) && (sqlAsAtDate) ){
				expr = exprBldr.AND() {
					LESS_THAN_EQUAL_TO(createdStamp: asAtDateTimestamp)
					EQUALS(transactionType : transactionType)
					EQUALS(treasuryId: treasuryId)
				}
			}
	
			if (sqlAsAtDate && !(treasuryId)){
				expr = exprBldr.AND() {
					LESS_THAN_EQUAL_TO(createdStamp: asAtDateTimestamp)
					EQUALS(transactionType : transactionType)
				}
			}
			
			def totalAmount = BigDecimal.ZERO;
			def totalAmountStr = "";
			
			  transactedBy = delegator.findOne("Treasury", [treasuryId : treasuryId], false);
		 	
		     transactionList = delegator.findList("AccountTransaction", expr, null, null, null, false);
		 
		          transactionList.eachWithIndex {transactionItem, index ->
		 
					   accountNo = org.ofbiz.humanres.Leave.getAccountNo(transactionItem.memberAccountId);
					   accountName = org.ofbiz.humanres.Leave.getAccountName(transactionItem.memberAccountId);
					   mobileNumber =  org.ofbiz.humanres.Leave.getMobileNo(transactionItem.partyId);
					   transactionAmount = transactionItem.getBigDecimal("transactionAmount");
					   memberNumber =org.ofbiz.humanres.Leave.getMemberNumber(transactionItem.partyId);
					   createdStamp = transactionItem.getString("createdStamp"); 
			           chequeNo = transactionItem.getString("chequeNo");
			           slipNumber = transactionItem.getString("slipNumber");
		             
			            reference = transactionItem.getString("reference");
			            accountTransactionId = transactionItem.getString("accountTransactionId");
			            receiptNo = transactionItem.getString("receiptNo");
			            createdBy = transactionItem.createdBy
			             
			            println("########CreatedBy##########"+createdBy)
			            	
		            
				 transactionals.add([createdBy:createdBy, accountTransactionId : accountTransactionId, receiptNo : receiptNo,  reference : reference,slipNumber:slipNumber, chequeNo :chequeNo ,accountNo :accountNo, accountName :accountName, mobileNumber : mobileNumber, transactionAmount : transactionAmount, memberNumber : memberNumber,
				 createdStamp : createdStamp ]);
				 
				 totalAmount = totalAmount + transactionAmount.toBigDecimal();
				 totalAmountStr = totalAmount.toString();
			}
		 
		 
		    context.transactionals = transactionals;
		    context.treasuryIdName = transactedBy.name
		    context.totalAmountStr = totalAmountStr
