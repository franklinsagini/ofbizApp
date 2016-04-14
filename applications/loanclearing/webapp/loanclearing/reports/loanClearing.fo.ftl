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
 <#if clearingList?has_content>
    <#if clearingList?has_content>
    <#-- REPORT TITLE -->
    <#-- fo:block font-size="18pt" font-weight="bold" text-align="center">
        CHAI SACCO
    </fo:block>
    <fo:block font-size="12pt" text-align="center" text-decoration="underline" font-weight="bold" >
        ${title}
    </fo:block>
    <fo:block font-size="12pt" text-align="center" text-decoration="underline" font-weight="bold" >
       P.O BOX 278 - 00200 NAIROBI TEL: 020 - 214406/10
    </fo:block -->
    
    <fo:block><fo:leader/></fo:block>
     <fo:block><fo:leader/></fo:block>
      <fo:block><fo:leader/></fo:block>
    
 <fo:block font-size="12pt" text-align="center"  font-weight="bold" >
      LOAN CLEARANCE BY CASH
    </fo:block>
     <fo:block border="thin solid black"> </fo:block>
         <fo:block font-size="9pt" text-align="left">
                <fo:leader/>       
        </fo:block>

    <fo:block font-size="12pt" text-align="center" font-weight="bold" space-after="0.04in" text-decoration="underline" color="#FFFFFF">
     <#--  Branch : ${branch.groupName?if_exists} -->
     T
    </fo:block>

   
    <fo:block font-size="12pt" text-align="left" margin-left="0%" margin-bottom="0.2in" font-weight="bold">
      <#-- Date: ${accountTransactionParent.createdStamp?date}  Ref:  ${accountTransactionParent.accountTransactionParentId?if_exists} -->
      PART 1 				MEMBER PERSONAL DETAILS
    </fo:block>

	
     <fo:block space-after.optimum="10pt" font-size="9pt">
            <fo:table table-layout="fixed" width="100%">
            	<fo:table-column column-width="60pt"/>
                <fo:table-column column-width="70pt"/>
                <fo:table-column column-width="70pt"/>
                <fo:table-column column-width="60pt"/>
                <fo:table-column column-width="60pt"/>
                <fo:table-column column-width="80pt"/>
                <fo:table-column column-width="70pt"/>
                <fo:table-column column-width="90pt"/>
                <#-- fo:table-column column-width="60pt"/>
                <fo:table-column column-width="60pt"/>
                <fo:table-column column-width="60pt"/>
                <fo:table-column column-width="60pt"/>
                <fo:table-column column-width="60pt"/ -->
                <fo:table-header>
                
                
                                <fo:table-row>
                    <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block  font-weight="bold">
                                	Member No
                                </fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>
                                ${member.memberNumber}
                                </fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block text-align="left" font-weight="bold">
                                Payroll No
                                </fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block text-align="left">
                                	${member.payrollNumber}
                                </fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block text-align="left" font-weight="bold">
                                	Deposits A/C No.
                                </fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block text-align="left">
                                	<#assign memberId = member.partyId>
                                	<#assign depositAccNo= Static["org.ofbiz.loanclearing.LoanClearingServices"].getMemberDepositsAccountNumber(memberId) />
                                	${depositAccNo}
                                </fo:block>
                            </fo:table-cell>
                    <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block text-align="left" font-weight="bold">
                                Deposits A/C Name
                                </fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>
                                	
                                	<#assign depositAccName= Static["org.ofbiz.loanclearing.LoanClearingServices"].getMemberDepositsAccountName(memberId) />
                                	${depositAccName}
                                </fo:block>
                            </fo:table-cell>
                    </fo:table-row>
                
                <fo:table-row>
                    <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block  font-weight="bold">
                                	Branch
                                </fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>
                                	${branch.groupName}
                                </fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block text-align="left" font-weight="bold">
                                Station
                                </fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block text-align="left">
                              <#assign stationId = member.stationId />
                              <#assign station = delegator.findOne("Station", Static["org.ofbiz.base.util.UtilMisc"].toMap("stationId", stationId.toString()), true)/>
                                <#if station?has_content>
                                ${station.stationNumber?if_exists} - ${station.name?if_exists}
                                </#if>
                                </fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block text-align="left" font-weight="bold">
                                	Savings A/C Number
                                </fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block text-align="left" font-weight="bold">
                                    <#assign memberId = member.partyId>
                                	<#assign savingsAccNo= Static["org.ofbiz.loanclearing.LoanClearingServices"].getFosaSavingsAccountNumber(memberId) />
                                	${savingsAccNo}
                               
                                </fo:block>
                            </fo:table-cell>
                    <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block text-align="left" font-weight="bold">
                                 Savings A/C Name
                                </fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>
                                    <#assign memberId = member.partyId>
                                	<#assign savingsAccName= Static["org.ofbiz.loanclearing.LoanClearingServices"].getFosaSavingsAccountName(memberId) />
                                	${savingsAccName}
                                
                                </fo:block>
                            </fo:table-cell>
                    </fo:table-row>
                
                <fo:table-row>
                    <fo:table-cell padding="0pt" border="0pt solid" border-width="0mm">
                                <fo:block>
                                </fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="0pt solid" border-width="0mm">
                                <fo:block>
                                </fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="0pt solid" border-width="0mm" color="#FFFFFF">
                                <fo:block text-align="left" font-weight="bold" >
                                GRAND TOTAL
                                </fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="0pt solid" border-width="0mm">
                                <fo:block text-align="right" font-weight="bold">
                                </fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="0pt solid" border-width="0mm">
                                <fo:block text-align="right" font-weight="bold">
                                </fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="0pt solid" border-width="0mm">
                                <fo:block text-align="right" font-weight="bold">
                                </fo:block>
                            </fo:table-cell>
                    <fo:table-cell padding="2pt" border="0pt solid" border-width="0mm">
                                <fo:block text-align="right" font-weight="bold">
                                </fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="0pt solid" border-width="0mm">
                                <fo:block>
                                </fo:block>
                            </fo:table-cell>
                    </fo:table-row>
                    
                    <fo:table-row font-weight="bold">
                    	<fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block>Loan No</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block>Loan Type</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block>Original Amount</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block>Loan Balance</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block>Accrued Interest</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block>Accrued Insurance</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block>Total Amount</fo:block>
                        </fo:table-cell>
                         <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block>% Cleared</fo:block>
                        </fo:table-cell>
                        <#-- fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block>Insurance Due</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block>Insurance Paid</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block>Principal Due</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block>Principal Paid</fo:block>
                        </fo:table-cell -->
                    </fo:table-row>
                </fo:table-header>
                <fo:table-body>
                	<#-- assign count=0>
                	<#assign totalLoans=0>
                	<#assign totalDisbursed=0 -->
                	
                	<#assign totalLoanBalance = 0 >
                	<#assign totalAccruedInterest = 0 >
                	<#assign totalAccruedInsurance = 0 >
                	<#assign totalTotalAmount = 0 >
                	
                    <#list clearingList as clearItem>
                        <fo:table-row>
                        	<fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>${clearItem.loanNo?if_exists}</fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>${clearItem.loanType?if_exists}</fo:block>
                            </fo:table-cell>
                            
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm" >
                                <fo:block text-align="right" >
                              <#-- assign loanPartyId = loan.partyId />  
                              <#assign member = delegator.findOne("Member", Static["org.ofbiz.base.util.UtilMisc"].toMap("partyId", loanPartyId), true)/>
                                ${member.firstName?if_exists} ${member.middleName?if_exists} ${member.lastName?if_exists} -->
                               ${clearItem.origAmount?string(",##0.0000")}
                                </fo:block>
                            </fo:table-cell>
                            
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block text-align="right" >
                                <#--${loan.disbursementDate?date} -->
                                <#assign totalLoanBalance = totalLoanBalance + clearItem.loanBalance>
                                 ${clearItem.loanBalance?string(",##0.0000")}
                                </fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block text-align="right" > 
                                <#-- assign loanBalance = loan.loanAmt - Static["org.ofbiz.loans.LoanServices"].getLoansRepaidByLoanApplicationId(loan.loanApplicationId)>
                                <#assign totalLoans = totalLoans+loanBalance -->
                                <#-- ${expectation.remitanceDescription?if_exists} --> 
                                <#assign totalAccruedInterest = totalAccruedInterest + clearItem.accruedInterest>
                                ${clearItem.accruedInterest?string(",##0.0000")}
                                </fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block text-align="right" >
                                	<#-- assign loanProductId = loan.loanProductId />
                                	<#assign loanProduct = delegator.findOne("LoanProduct", Static["org.ofbiz.base.util.UtilMisc"].toMap("loanProductId", loanProductId), true)/ -->
                                	<#-- if (expectation.isReceived = 'Y') && (expectation.amount??)  >
                                	
                                		
                                		 Kshs.  ${expectation.amount?string(",##0.0000")}
								  ${loanProduct.code} 
								</#if -->
								<#assign totalAccruedInsurance = totalAccruedInsurance + clearItem.accruedInsurance>
								${clearItem.accruedInsurance?string(",##0.0000")}
								
                                </fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block text-align="right" >
                              <#assign totalTotalAmount  = totalTotalAmount  + clearItem.totalAmount>
                                ${clearItem.totalAmount?string(",##0.0000")}
                               </fo:block>
                            </fo:table-cell>
                            
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>
                              ${clearItem.percentageCleared}
                               </fo:block>
                            </fo:table-cell>
                            <#-- fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>${loanRepayment.totalInsuranceDue?if_exists}</fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>${loanRepayment.insuranceAmount?if_exists}</fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>${loanRepayment.totalPrincipalDue?if_exists}</fo:block>
                            </fo:table-cell>
                             <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>${loanRepayment.principalAmount?if_exists}</fo:block>
                            </fo:table-cell -->
                        </fo:table-row>
                        
                    </#list>
                    <fo:table-row>
                    <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>
                                </fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>
                                </fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block text-align="left" font-weight="bold">
                                GRAND TOTAL
                                </fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block text-align="right" font-weight="bold">
                                ${totalLoanBalance?string(",##0.0000")}
                                </fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block text-align="right" font-weight="bold">
                               ${totalAccruedInterest?string(",##0.0000")}
                                </fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block text-align="right" font-weight="bold">
                                 ${totalAccruedInsurance?string(",##0.0000")}
                                </fo:block>
                            </fo:table-cell>
                    <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block text-align="right" font-weight="bold">
                                ${totalTotalAmount?string(",##0.0000")}
                                </fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>
                                </fo:block>
                            </fo:table-cell>
                    </fo:table-row>
                </fo:table-body>
            </fo:table>
        </fo:block>
    
    <fo:list-block provisional-distance-between-starts="2.0in" font-size="10pt" text-align="left" margin-left="20%" margin-bottom="0.2in">
	        <fo:list-item>
	            <fo:list-item-label>
	                <fo:block font-weight="bold"></fo:block>
	            </fo:list-item-label>
	            <fo:list-item-body start-indent="body-start()">
	                <fo:block></fo:block>
	            </fo:list-item-body>
	        </fo:list-item>
	    </fo:list-block>

    
     <fo:block font-size="12pt" text-align="left" margin-left="0%" margin-bottom="0.2in">
     
     
     <#assign depositBalance= Static["org.ofbiz.loanclearing.LoanClearingServices"].getMemberDepositsAccountBalance(memberId) />
     
     	MEMBER DEPOSITS -------- Kshs. ${depositBalance?string(",##0.0000")}  ----   AS AT  -----${currentDate} -----------
    </fo:block>
    
    
    
    <fo:block space-after.optimum="10pt" font-size="9pt">
            <fo:table table-layout="fixed" width="100%">
            	<fo:table-column column-width="130pt"/>
                <fo:table-column column-width="130pt"/>
                <fo:table-column column-width="140pt"/>
                <fo:table-column column-width="150pt"/>
                <#-- fo:table-column column-width="60pt"/>
                <fo:table-column column-width="60pt"/>
                <fo:table-column column-width="60pt"/>
                <fo:table-column column-width="60pt"/>
                <fo:table-column column-width="60pt"/ -->
                    <fo:table-body>
                    
                      <fo:table-row>
                    <fo:table-cell padding="2pt" border="0pt solid" border-width="0mm">
                                <fo:block  font-weight="bold">
                                	Prepared By
                                </fo:block>
                            </fo:table-cell>
                           
                            <fo:table-cell padding="2pt" border="0pt solid" border-width="0mm">
                                <fo:block text-align="left" font-weight="bold">
                                DESIGNATION
                                </fo:block>
                            </fo:table-cell>
                           
                            <fo:table-cell padding="2pt" border="0pt solid" border-width="0mm">
                                <fo:block text-align="left" font-weight="bold">
                                	SIGNATURE
                                </fo:block>
                            </fo:table-cell>
                            
                    <fo:table-cell padding="2pt" border="0pt solid" border-width="0mm">
                                <fo:block text-align="left" font-weight="bold">
                                DATE
                                </fo:block>
                            </fo:table-cell>
                            
                    </fo:table-row>
                    
                    <fo:table-row>
                    <fo:table-cell padding="2pt" border="0pt solid" border-width="0mm">
                                <fo:block  font-weight="bold">
                                	
                                </fo:block>
                            </fo:table-cell>
                           
                            <fo:table-cell padding="2pt" border="0pt solid" border-width="0mm">
                                <fo:block text-align="left" font-weight="bold">
                                
                                </fo:block>
                            </fo:table-cell>
                           
                            <fo:table-cell padding="2pt" border="0pt solid" border-width="0mm">
                                <fo:block text-align="left" font-weight="bold">
                                	
                                </fo:block>
                            </fo:table-cell>
                            
                    <fo:table-cell padding="2pt" border="0pt solid" border-width="0mm">
                                <fo:block text-align="left" font-weight="bold" color="#FFFFFF">
                                test
                                </fo:block>
                            </fo:table-cell>
                            
                    </fo:table-row>
                    
                    

                    </fo:table-body>
                    
                    </fo:table>
       </fo:block>
       
  
  
   <fo:block font-size="12pt"  text-align="left" margin-left="0%" margin-bottom="0.2in" font-weight="bold">
      PART 2 ASSISTANT LOANS OFFICER/ LOANS OFFICER   
    </fo:block>
    <fo:block font-size="9pt" text-align="left" margin-left="0%"  margin-bottom="0.2in">
     	Authorized to clear the outstanding loan(s) as requested , the member will qualify for Kshs. 
    </fo:block>
    <fo:block font-size="9pt" text-align="left" margin-left="0%"  margin-bottom="0.2in">
     	------------------------------------------------------------------------------------------------------------------------------------------------------------------------- 
    </fo:block>
    
    <fo:block font-size="9pt" text-align="left" margin-left="0%" margin-bottom="0.2in">
		Signature -------------------------------------------- Date -------------------------------------------------------------------------------------------------------    
    </fo:block>
       
       
     <fo:block font-size="12pt"  text-align="left" margin-left="0%" margin-bottom="0.2in" font-weight="bold">
      PART 3  BRANCH MANAGER / CREDIT MANAGER
    </fo:block>
    <fo:block font-size="9pt" text-align="left" margin-left="0%"  margin-bottom="0.2in">
     	Authorized to clear the outstanding loan(s) as requested , the member will qualify for Kshs. ------------------------------------------------
    </fo:block>
    <fo:block font-size="9pt" text-align="left" margin-left="0%"  margin-bottom="0.2in">
     	Main/School Fees/ Emergency / Product Loan Payable in  ------------------------------------------ Months
    </fo:block>
    
    <fo:block font-size="9pt" text-align="left" margin-left="0%" margin-bottom="0.2in">
		Signature -------------------------------------------- Date -------------------------------------------------------------------------------------------------------    
    </fo:block>
    
    <#-- fo:block font-size="16pt"  text-align="left" margin-left="0%" margin-bottom="0.2in" font-weight="bold">
      PART 4  LOANS MANAGER
    </fo:block>
    
    <fo:block font-size="12pt" text-align="left" margin-left="0%" margin-bottom="0.2in">
      The request has been rejected or granted due to the following reasons. 
    </fo:block>
    
     <fo:block font-size="12pt" text-align="left" margin-left="0%" margin-bottom="0.2in">
      -------------------------------------------------------------------------------------------------------
    </fo:block>
        <fo:block font-size="12pt" text-align="left" margin-left="0%" margin-bottom="0.2in">
		Signature -------------------------------------------- Date -------------------------------------     
    </fo:block>
    
    
    <fo:block font-size="16pt"  text-align="left" margin-left="0%" margin-bottom="0.2in" font-weight="bold">
      PART 5  General MANAGER
    </fo:block>
        <fo:block font-size="12pt" text-align="left" margin-left="0%" margin-bottom="0.2in">
		To Clear Kshs.  -------------------------------------------- is Here by granted/Rejected     
    </fo:block -->
    
    By  CASH    ---------------------- MEDICARE  --------------------------OTHERS
    </#if>
	 </#if>
</#escape>