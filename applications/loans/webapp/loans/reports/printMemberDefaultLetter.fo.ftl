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
 <#if loanApplication?has_content>
    <#if loanApplication?has_content>
    <#-- REPORT TITLE -->
     <fo:block font-size="18pt" text-align="left">
        Factory Unit Manager
    </fo:block>
  
    <fo:block font-size="18pt"  text-align="left">
       <#assign stationId = member.stationId />
       <#assign stationIdString = stationId.toString() />
       <#assign station = delegator.findOne("Station", Static["org.ofbiz.base.util.UtilMisc"].toMap("stationId", stationIdString), true)/>
                                
		${station.name}
    </fo:block>
    <fo:block font-size="18pt"  text-align="left">
       ${station.boxAddress}
    </fo:block>
    <fo:block font-size="18pt" text-align="left">
       Dear Sir/Madam
    </fo:block>
    <fo:block font-size="12pt" text-align="center" text-decoration="underline" font-weight="bold" >
        RE: LOAN DEFAULTER ${member.firstName} ${member.middleName} ${member.lastName} P/N: ${member.payrollNumber} M/NO: ${member.memberNumber}
    </fo:block>
    <fo:block><fo:leader/></fo:block>
    <#-- Employee Details -->
    <fo:block font-size="12pt" text-align="center" font-weight="bold" space-after="0.04in" text-decoration="underline">
      
       Loan Type : ${loanProduct.name?if_exists}
    </fo:block>
    <fo:block font-size="12pt" text-align="center" margin-bottom="0.2in">
       The above named was granted ${loanProduct.name?if_exists} by the society on ${loanApplication.disbursementDate?date} amounting to
       Kshs. ${loanApplication.loanAmt?string(",##0.00")} Payable in ${loanApplication.repaymentPeriod} installments.
       <b/>
      
	   The loan repayment is now in default and the loan development account is analyzed below	: -  
    </fo:block>


    <fo:list-block provisional-distance-between-starts="2.0in" font-size="10pt" margin-left="35%" margin-bottom="0.2in">
        <fo:list-item>
            <fo:list-item-label>
                <fo:block font-weight="bold">Loan outstanding as at ${nowTimestamp?if_exists}</fo:block>
            </fo:list-item-label>
            <fo:list-item-body start-indent="body-start()">
                <fo:block><#-- Kshs ${minimumShareContribution?string(",##0.00")} --></fo:block>
            </fo:list-item-body>
        </fo:list-item>
        <fo:list-item>
            <fo:list-item-label>
                <fo:block font-weight="bold">Add Accrued Interest</fo:block>
            </fo:list-item-label>
            <fo:list-item-body start-indent="body-start()">
                <fo:block><#-- Kshs ${loanRepayment?string(",##0.00")} --> </fo:block>
            </fo:list-item-body>
        </fo:list-item>
        <fo:list-item>
            <fo:list-item-label>
                <fo:block font-weight="bold"> Add Accrued Insurance </fo:block>
            </fo:list-item-label>
            <fo:list-item-body start-indent="body-start()">
                <fo:block> - </fo:block>
            </fo:list-item-body>
        </fo:list-item>
        
        <fo:list-item>
            <fo:list-item-label>
                <fo:block font-weight="bold">Less Shares as at  ${nowTimestamp?if_exists}</fo:block>
            </fo:list-item-label>
            <fo:list-item-body start-indent="body-start()">
                <fo:block><#-- Kshs ${totalDeduction?string(",##0.00")} --> </fo:block>
            </fo:list-item-body>
        </fo:list-item>
        <fo:list-item>
            <fo:list-item-label>
                <fo:block font-weight="bold">Balance Due as at  ${nowTimestamp?if_exists}</fo:block>
            </fo:list-item-label>
            <fo:list-item-body start-indent="body-start()">
                <fo:block>Kshs ${totalDeduction?string(",##0.00")}</fo:block>
            </fo:list-item-body>
        </fo:list-item>
        
    </fo:list-block>
    
     <fo:block font-size="12pt" text-align="center" margin-bottom="0.2in">
      The above amount should now be recovered from the guarantors salaries as how below in the attached schedule
    </fo:block>
    
     <fo:block space-after.optimum="10pt" font-size="9pt">
            <fo:table table-layout="fixed" width="100%">
            	<fo:table-column column-width="20pt"/>
                <fo:table-column column-width="40pt"/>
                <fo:table-column column-width="60pt"/>
                <fo:table-column column-width="140pt"/>
                <fo:table-column column-width="70pt"/>
                <fo:table-column column-width="70pt"/>
                <fo:table-column column-width="70pt"/>
                
                
                <fo:table-header>
                    <fo:table-row font-weight="bold">
                    	<fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block></fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block>Member No</fo:block>
                        </fo:table-cell>

                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block>Payroll No</fo:block>
                        </fo:table-cell>


                        
                         <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block>Name</fo:block>
                        </fo:table-cell>


                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block>Station Name</fo:block>
                        </fo:table-cell>
                        
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block>Loan No</fo:block>
                        </fo:table-cell>
                        
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block>Amount</fo:block>
                        </fo:table-cell>
                        
                        
                    </fo:table-row>
                </fo:table-header>
                <fo:table-body>
                	<#assign count=0>
                	<#-- <#assign totalExpected=0>
                	<#assign totalReceived=0>
                	<#assign totalVariance=0>
                	assign totalDisbursed=0 -->
                	
                    <#list myGuarantorList as myGuarantorItem>
                        <fo:table-row>
                        	<#assign count = count + 1>
                        	
                        	<#assign memberId = myGuarantorItem.guarantorId />
                              <#assign member = delegator.findOne("Member", Static["org.ofbiz.base.util.UtilMisc"].toMap("partyId", memberId), true)/>
                                <#-- if (expectation.totalAmount??)>
								    Kshs.  ${expectation.totalAmount?string(",##0.0000")} 
								    ${station.stationNumber} 
								</#if -->
                        	<fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>${count}</fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>${member.memberNumber?if_exists}</fo:block>
                            </fo:table-cell>
                            
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>
                                  ${member.payrollNumber?if_exists}
                                </fo:block>
                            </fo:table-cell>
                            
                            <#-- fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>${myGuarantorItem.name?if_exists}</fo:block>
                            </fo:table-cell -->

                            
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block text-align="left">
								
							
								
								${member.firstName} ${member.middleName} ${member.lastName}
								
                               </fo:block>
                            </fo:table-cell>
                            
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block text-align="right">
                                	<#assign stationId = member.stationId />
                              <#assign stationIdString = stationId.toString() />
                              <#assign station = delegator.findOne("Station", Static["org.ofbiz.base.util.UtilMisc"].toMap("stationId", stationIdString), true)/>
                                <#-- if (expectation.totalAmount??)>
								    Kshs.  ${expectation.totalAmount?string(",##0.0000")} 
								    ${station.stationNumber} 
								</#if -->
								${station.name}
							<#--	${myGuarantorItem.loanAmt?string(",##0.00")} -->
                               </fo:block>
                            </fo:table-cell>
                            
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
								                           
                                <fo:block text-align="right"> 
                               <#-- <#assign loanBalance = myGuarantorItem.loanAmt - Static["org.ofbiz.loans.LoanServices"].getLoansRepaidByLoanApplicationId(myGuarantorItem.loanApplicationId)>
                                 assign totalLoans = totalLoans+loanBalance -->
                                <#-- ${expectation.remitanceDescription?if_exists}  
                                ${loanBalance?string(",##0.0000")} -->
                                ${loanApplication.loanNo?if_exists}
                                </fo:block>
                            </fo:table-cell>
                            
                                   <#-- assign totalExpected = totalExpected+expectedReceived.expected>
                            <#assign totalReceived = totalReceived+expectedReceived.received>
                            <#assign totalVariance = totalVariance+expectedReceived.variance -->
                           <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block text-align="right">
                                <#assign loanBalance = loanApplication.loanAmt - Static["org.ofbiz.loans.LoanServices"].getLoansRepaidByLoanApplicationId(loanApplication.loanApplicationId)>
                                <#-- assign totalLoans = totalLoans+loanBalance -->
                                <#-- ${expectation.remitanceDescription?if_exists}  -->
                                ${loanBalance?string(",##0.0000")}
                               </fo:block>
                            </fo:table-cell>
                            
                            
                        </fo:table-row>
                        
                    </#list>
                    
                </fo:table-body>
            </fo:table>
        </fo:block>
    
        <fo:block font-size="12pt" text-align="center" margin-bottom="0.2in">
      By copies of this letter, all the guarantors are informed accordingly!
    </fo:block>
    <fo:block font-size="12pt" text-align="left" margin-bottom="0.2in">
      Yours faithfully
    </fo:block>
    <fo:block font-size="12pt" text-align="left" margin-bottom="0.2in">
      General Manager
    </fo:block>
    </#if>
	 </#if>
</#escape>