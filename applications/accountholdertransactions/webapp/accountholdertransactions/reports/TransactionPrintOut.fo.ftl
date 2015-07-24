<#--
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
-->
<#escape x as x?xml>
 <#if accountTransactionList?has_content>
    <#if accountTransactionList?has_content>
    <#-- REPORT TITLE -->
    <#-- fo:block font-size="18pt" font-weight="bold" text-align="center">
        CHAI SACCO
    </fo:block -->
    <fo:block font-size="12pt" text-align="center" text-decoration="underline" font-weight="bold" >
        Transaction Receipt
    </fo:block>
    <fo:block><fo:leader/></fo:block>
    
    <fo:block font-size="12pt" text-align="center" font-weight="bold" space-after="0.04in" text-decoration="underline">
      
       Branch : ${branch.groupName?if_exists}
    </fo:block>
    <fo:block font-size="12pt" text-align="left" margin-left="20%" margin-bottom="0.2in">
      Date: ${accountTransactionParent.createdStamp?date}  Ref:  ${referenceNo?if_exists}
    </fo:block>

		<fo:list-block provisional-distance-between-starts="2.0in" font-size="10pt" text-align="left" margin-left="20%" margin-bottom="0.2in">
        <fo:list-item>
            <fo:list-item-label>
                <fo:block font-weight="bold">Account Name:</fo:block>
            </fo:list-item-label>
            <fo:list-item-body start-indent="body-start()">
                <fo:block>${memberAccount.accountName}</fo:block>
            </fo:list-item-body>
        </fo:list-item>
        <fo:list-item>
            <fo:list-item-label>
                <fo:block font-weight="bold">Account No. :</fo:block>
            </fo:list-item-label>
            <fo:list-item-body start-indent="body-start()">
                <fo:block> ${memberAccount.accountNo}</fo:block>
            </fo:list-item-body>
        </fo:list-item>  
        
        <#if chequeNo?has_content>
        <fo:list-item>
            <fo:list-item-label>
                <fo:block font-weight="bold">Cheque No. :</fo:block>
            </fo:list-item-label>
            <fo:list-item-body start-indent="body-start()">
                <fo:block> ${chequeNo}</fo:block>
            </fo:list-item-body>
        </fo:list-item>  
        </#if>
    </fo:list-block>
    
    <#list accountTransactionList as transaction>
	    <fo:list-block provisional-distance-between-starts="2.0in" font-size="10pt" text-align="left" margin-left="20%" margin-bottom="0.2in">
	        <fo:list-item>
	            <fo:list-item-label >
	                <fo:block font-weight="bold">
	                <#if transaction.transactionType == "NORMAL CHEQUE WITHDRAWAL CHARGES">
	                	CHARGES
	                <#else>
	                	${transaction.transactionType?if_exists}
	                </#if>
	                </fo:block>
	            </fo:list-item-label>
	            <fo:list-item-body start-indent="body-start()">
	                <fo:block>Kshs. ${transaction.transactionAmount?string(",##0.00")}</fo:block>
	            </fo:list-item-body>
	        </fo:list-item>
	    </fo:list-block>
    </#list>
    
    <fo:list-block provisional-distance-between-starts="2.0in" font-size="10pt" text-align="left" margin-left="20%" margin-bottom="0.2in">
	        <fo:list-item>
	            <fo:list-item-label>
	                <fo:block font-weight="bold">Total</fo:block>
	            </fo:list-item-label>
	            <fo:list-item-body start-indent="body-start()">
	                <fo:block>Kshs.  ${totalAmount?string(",##0.00")}</fo:block>
	            </fo:list-item-body>
	        </fo:list-item>
	    </fo:list-block>
	    
	    
	    <#if transactionTypeWithdrawal?has_content>
	     <fo:list-block provisional-distance-between-starts="2.0in" font-size="10pt" text-align="left" margin-left="20%" margin-bottom="0.2in">
	        <fo:list-item>
	            <fo:list-item-label>
	                <fo:block font-weight="bold">Balance</fo:block>
	            </fo:list-item-label>
	            <fo:list-item-body start-indent="body-start()">
	                <fo:block>Kshs.  ${balanceAmount?string(",##0.00")}</fo:block>
	            </fo:list-item-body>
	        </fo:list-item>
	    </fo:list-block>
	    </#if>

    
     <fo:block font-size="12pt" text-align="left" margin-left="20%" margin-bottom="0.2in">
      We acknowledge receipt of the above amount.
    </fo:block>
    
     <fo:block font-size="12pt"  text-align="left" margin-left="20%" margin-bottom="0.2in">
      SIGNATURE --------------------------------------------------------------------------
    </fo:block>
    <fo:block font-size="12pt" text-align="left" margin-left="20%"  margin-bottom="0.2in">
      Name ----------------${member.firstName} ${member.middleName} ${member.lastName}-----
    </fo:block>
    
    <fo:block font-size="12pt" text-align="left" margin-left="20%" margin-bottom="0.2in">
      ID No. ------------------------${member.idNumber}------------------------------------
    </fo:block>
    
    
    <fo:block font-size="12pt" text-align="left" margin-left="20%" margin-bottom="0.2in">
      Served By. ------------------------${createdBy}------------------------------------
    </fo:block>
    </#if>
	 </#if>
</#escape>