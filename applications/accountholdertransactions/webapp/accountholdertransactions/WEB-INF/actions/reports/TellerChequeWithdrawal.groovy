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
	amountToCheck = parameters.amountToCheckGreaterThan
	
	action = request.getParameter("action");
	
	print " -------- Start Date"
	println startDate
	
	print " -------- End Date"
	println endDate
	
	java.sql.Date sqlEndDate = null;
	java.sql.Date sqlStartDate = null;
	transactionalsBankers = [];
	transactionalsNormal = [];
	
	def amtToCheckToBigDec = BigDecimal.ZERO;
	def bankersId = 10000;
	def normalsId = 10020;
	
	bnkLong = bankersId.toLong();
	nrmLong = normalsId.toLong();
	
	
	
	//dateStartDate = Date.parse("yyyy-MM-dd hh:mm:ss", startDate).format("dd/MM/yyyy")
	
	if ((startDate?.trim())){
		dateStartDate = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(startDate);
		sqlStartDate = new java.sql.Date(dateStartDate.getTime());
		startDateTimestamp = new Timestamp(sqlStartDate.getTime());
		
		   context.startDate = startDate
	}
	//(endDate != null) ||
	if ((endDate?.trim())){
		dateEndDate = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(endDate);
		sqlEndDate = new java.sql.Date(dateEndDate.getTime());
		endDateTimestamp = new Timestamp(sqlEndDate.getTime());
		
		   context.endDate = endDate
	}
	
	if(amountToCheck){
	    amtToCheckToBigDec = amountToCheck.toBigDecimal();
	}
	
	print "formatted Date"
	
	 context.transactionType = "CHEQUE WITHDRAWAL REGISTER"
	
	    exprBldrB = new org.ofbiz.entity.condition.EntityConditionBuilder();
	    
	     exprBldrN= new org.ofbiz.entity.condition.EntityConditionBuilder();
	    
	    transactionTypeBankerChek = "BANKERSWITHDRAWAL"
	    
	    if (!(sqlEndDate)){
	     
	        exprBldrBankersChecks = exprBldrB.AND() {
				EQUALS(transactionType : transactionTypeBankerChek)
				EQUALS(chequeTypeId : bnkLong)
				
			 }
	     }
	    
	    	if ((sqlEndDate) && (sqlStartDate) && !(amountToCheck)){
			exprBldrBankersChecks = exprBldrB.AND() {
				  GREATER_THAN_EQUAL_TO(createdStamp: startDateTimestamp)
				  LESS_THAN_EQUAL_TO(createdStamp: endDateTimestamp)
				  EQUALS(transactionType : transactionTypeBankerChek)
				  EQUALS(chequeTypeId : bnkLong)
			   }
		   }
	     
	     	
		if ((amountToCheck) && (sqlStartDate) && (sqlEndDate) ){
			exprBldrBankersChecks = exprBldrB.AND() {
			    GREATER_THAN_EQUAL_TO(createdStamp: startDateTimestamp)
				LESS_THAN_EQUAL_TO(createdStamp: endDateTimestamp)
				EQUALS(transactionType : transactionTypeBankerChek)
				GREATER_THAN_EQUAL_TO(transactionAmount: amtToCheckToBigDec)
				EQUALS(chequeTypeId : bnkLong)
			}
		}
		
		////////////////Normal Checks
		
		if (!(sqlEndDate)){
	     
	        exprBldrNormalChecks = exprBldrN.AND() {
				EQUALS(transactionType : transactionType)
				EQUALS(chequeTypeId : nrmLong)
			 }
	     }
	    
	    	if ((sqlEndDate) && (sqlStartDate) && !(amountToCheck)){
			exprBldrNormalChecks = exprBldrN.AND() {
				  GREATER_THAN_EQUAL_TO(createdStamp: startDateTimestamp)
				  LESS_THAN_EQUAL_TO(createdStamp: endDateTimestamp)
				  EQUALS(transactionType : transactionType)
				  EQUALS(chequeTypeId : nrmLong)
			   }
		   }
	     
	     	
		if ((amountToCheck) && (sqlStartDate) && (sqlEndDate) ){
			exprBldrNormalChecks = exprBldrN.AND() {
			    GREATER_THAN_EQUAL_TO(createdStamp: startDateTimestamp)
				LESS_THAN_EQUAL_TO(createdStamp: endDateTimestamp)
				EQUALS(transactionType : transactionType)
				GREATER_THAN_EQUAL_TO(transactionAmount: amtToCheckToBigDec)
				EQUALS(chequeTypeId : nrmLong)
			}
		}
	
	     transactionList = delegator.findList("AccountTransaction", exprBldrBankersChecks, null, null, null, false);
	 
	          transactionList.eachWithIndex {transactionItem, index ->
	 
				 accountNo = org.ofbiz.humanres.Leave.getAccountNo(transactionItem.memberAccountId);
				 accountName = org.ofbiz.humanres.Leave.getAccountName(transactionItem.memberAccountId);
				 mobileNumber =  org.ofbiz.humanres.Leave.getMobileNo(transactionItem.partyId);
				 transactionAmount = transactionItem.getString("transactionAmount");
				 memberNumber =org.ofbiz.humanres.Leave.getMemberNumber(transactionItem.partyId);
				 createdStamp = transactionItem.getString("createdStamp"); 
	             chequeNo = transactionItem.getString("chequeNo");
	             slipNumber = transactionItem.getString("slipNumber");
	             payee = transactionItem.getString("payee");
	             reference = transactionItem.getString("reference");
	             accountTransactionId = transactionItem.getString("accountTransactionId");
	             receiptNo = transactionItem.getString("receiptNo");
	              createdBy = transactionItem.createdBy
	               
	             
	            println("########INSIDE GR AccountNo"+accountNo)

	            
			 transactionalsBankers.add([payee : payee,createdBy:createdBy, accountTransactionId : accountTransactionId, receiptNo : receiptNo,  reference : reference,slipNumber:slipNumber, chequeNo :chequeNo ,accountNo :accountNo, accountName :accountName, mobileNumber : mobileNumber, transactionAmount : transactionAmount, memberNumber : memberNumber,
				 createdStamp : createdStamp ]);
			 
			 }
	 
	 
	    context.transactionalsBankers = transactionalsBankers;
	    context.transactionBankerChk = "BANKERS CHEQUE"
	    
	    
	     transactionListNormals = delegator.findList("AccountTransaction", exprBldrNormalChecks, null, null, null, false);
	 
	          transactionListNormals.eachWithIndex {transactionItemNormal, index ->
	 
				 accountNo = org.ofbiz.humanres.Leave.getAccountNo(transactionItemNormal.memberAccountId);
				 accountName = org.ofbiz.humanres.Leave.getAccountName(transactionItemNormal.memberAccountId);
				 mobileNumber =  org.ofbiz.humanres.Leave.getMobileNo(transactionItemNormal.partyId);
				 transactionAmount = transactionItemNormal.getString("transactionAmount");
				 memberNumber =org.ofbiz.humanres.Leave.getMemberNumber(transactionItemNormal.partyId);
				 createdStamp = transactionItemNormal.getString("createdStamp"); 
	             chequeNo = transactionItemNormal.getString("chequeNo");
	             slipNumber = transactionItemNormal.getString("slipNumber");
	             payee = transactionItemNormal.getString("payee");
	             reference = transactionItemNormal.getString("reference");
	             accountTransactionId = transactionItemNormal.getString("accountTransactionId");
	             receiptNo = transactionItemNormal.getString("receiptNo");
	             createdBy = transactionItemNormal.createdBy
	               
	            println("########INSIDE GR AccountNo"+accountNo)

	        transactionalsNormal.add([payee : payee, createdBy:createdBy, accountTransactionId : accountTransactionId, receiptNo : receiptNo,  reference : reference,slipNumber:slipNumber, chequeNo :chequeNo ,accountNo :accountNo, accountName :accountName, mobileNumber : mobileNumber, transactionAmount : transactionAmount, memberNumber : memberNumber,
				 createdStamp : createdStamp ]);
			 
			 }
	 
	 
	    context.transactionalsNormal = transactionalsNormal;
	    context.transactionNormalChk = "ORDINARY CHEQUE"
	    
	   
