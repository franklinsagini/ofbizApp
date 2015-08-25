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
    <#if memberStatementList?has_content>
        <#-- REPORT TITLE -->
        <!-- fo:block font-size="18pt" font-weight="bold" text-align="center">
            CHAI SACCO
        </fo:block -->
        <fo:block font-size="12pt" text-align="center"  font-weight="bold" >
            ${member.firstName?if_exists} ${member.middleName?if_exists} ${member.lastName?if_exists}
        </fo:block>
        
        <fo:list-block provisional-distance-between-starts="2in" font-size="10pt" margin-left="0.2in">
            <fo:list-item>
                <fo:list-item-label>
                    <fo:block font-weight="bold">Member Number</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block> ${member.memberNumber}</fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label>
                    <fo:block font-weight="bold">Payroll Number</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>${member.payrollNumber}</fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <!-- fo:list-item>
                <fo:list-item-label>
                    <fo:block font-weight="bold">Member Name</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>${member.firstName?if_exists} ${member.middleName?if_exists} ${member.lastName?if_exists}</fo:block>
                </fo:list-item-body>
            </fo:list-item -->
            
            <fo:list-item>
                <fo:list-item-label>
                    <fo:block font-weight="bold">Station Name</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                    	       <#assign stationId = member.stationId />
                              <#assign stationIdString = stationId.toString() />
                              <#assign station = delegator.findOne("Station", Static["org.ofbiz.base.util.UtilMisc"].toMap("stationId", stationIdString), true)/>
                                <#-- if (expectation.totalAmount??)>
								    Kshs.  ${expectation.totalAmount?string(",##0.0000")} 
								    ${station.stationNumber} 
								</#if -->
								${station.name}
 
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            
        </fo:list-block>
        <fo:block><fo:leader/></fo:block>
        <#-- fo:block margin-left="0.4in" text-decoration="underline" font-size="10pt" text-align="left"  font-weight="bold" >
            Member Statement
        </fo:block -->

		 <#list memberStatementList as statement>
		 <#assign totalAmount = statement.itemTotal >
		 
        <fo:list-block provisional-distance-between-starts="2in" font-size="10pt" margin-left="0.2in">
            <fo:list-item>
                <fo:list-item-label>
                    <fo:block font-weight="bold">${statement.name}</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block></fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label>
                    <fo:block font-weight="bold">${statement.code}</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block></fo:block>
                </fo:list-item-body>
            </fo:list-item>
            
            
           <fo:list-item>
                <fo:list-item-label>
                    <fo:block font-weight="bold">
                    <#if statement.loanNo??>
                    ${statement.loanNo}
                    </#if>
                    
                    </fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block></fo:block>
                </fo:list-item-body>
            </fo:list-item>
            
            <fo:list-item>
                <fo:list-item-label>
                    <fo:block font-weight="bold">
                    <#if statement.loanStatus??>
                    ${statement.loanStatus}
                    </#if>
                    
                    </fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block></fo:block>
                </fo:list-item-body>
            </fo:list-item>
            
        </fo:list-block>
        
        <fo:block><fo:leader/></fo:block>
        <#-- Loan Details -->
        <fo:block space-after.optimum="10pt" font-size="9pt">
            <fo:table table-layout="fixed" width="100%">
                <fo:table-column column-width="60pt"/>
                <fo:table-column column-width="160pt"/>
                <fo:table-column column-width="100pt"/>
                <fo:table-column column-width="100pt"/>
                <fo:table-column column-width="100pt"/>
                <#-- fo:table-column column-width="75pt"/>
                < fo:table-column column-width="60pt"/>
                <fo:table-column column-width="60pt"/>
                <fo:table-column column-width="60pt"/>
                <fo:table-column column-width="60pt"/>
                <fo:table-column column-width="60pt"/ -->
                <fo:table-header>
                    <fo:table-row font-weight="bold">
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block>Date</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block>Description</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm"  text-align="right">
                            <fo:block>Debit</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm"  text-align="right">
                            <fo:block>Credit</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm"  text-align="right">
                            <fo:block>Balance</fo:block>
                        </fo:table-cell>
                         <#-- fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block>Balance</fo:block>
                        </fo:table-cell>
                        < fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
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
                    <#list statement.listOfTransactions as transaction>
                    
                    	
                    	<#if (transaction.increaseDecrease = 'I') && (transaction.transactionAmount??)  >
                                <#if !(transaction.isLoanTransaction??)>
                                	<#assign totalAmount = totalAmount + transaction.transactionAmount >
                                </#if>
                                
                               <#if (transaction.isLoanTransaction??) && (transaction.isLoanTransaction == true)>
                                	<#assign totalAmount = totalAmount - transaction.transactionAmount >
                                </#if>	
                                		
						</#if>
						
						<#if (transaction.increaseDecrease = 'D') && (transaction.transactionAmount??)  >
                                
                                <#if !(transaction.isLoanTransaction??)>
                                	 <#assign totalAmount = totalAmount - transaction.transactionAmount >
                                </#if>	
                                
                                 <#if (transaction.isLoanTransaction??) && (transaction.isLoanTransaction == true)>
                                	 <#assign totalAmount = totalAmount + transaction.transactionAmount >
                                </#if>
                                		
						</#if>
                        <fo:table-row>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>
                                <#if transaction.transactionDate?? >
                                ${transaction.transactionDate?date}
                                </#if>
                                
                                </fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>
                                
                                <#if transaction.repaymentMode?? >
                                	
                                	${transaction.transactionDescription?if_exists} (${transaction.repaymentMode?if_exists})
                                
                                <#else>
                                	${transaction.transactionDescription?if_exists}
                                </#if>
                                
                                
                                </fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm" text-align="right">
                                <fo:block >
                                	<#if (transaction.increaseDecrease = 'D') && (transaction.transactionAmount??)  >
                                	
                                		
                                		   ${transaction.transactionAmount?string(",##0.00")}
								  
								</#if>
                                </fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm" text-align="right">
                                <fo:block>
                                	
                                	<#if (transaction.increaseDecrease = 'I') && (transaction.transactionAmount??)  >
                                	
                                		
                                		 ${transaction.transactionAmount?string(",##0.00")}
								  
								</#if>
								
                                </fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm" text-align="right">
                                <fo:block>
                              
								    ${totalAmount?string(",##0.00")} 
								
                               </fo:block>
                            </fo:table-cell>
                            
                            <#-- fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>
                              
                                <#if (expectation.totalAmount??)>
								    Kshs.  ${expectation.totalAmount?string(",##0.00")} 
								</#if>
								
                               </fo:block>
                            </fo:table-cell -->
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
                    
                    <#if statement.availableBalace??>
                                            <fo:table-row>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>
                                
                                </fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>
                                
                                
                                </fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm" text-align="right">
                                <fo:block >
                                 </fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm" text-align="right">
                                <fo:block font-size="12pt" font-weight="italics" font-style="italic" color="green">
                                	Available Balance
                                </fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm" text-align="right">
                                <fo:block  font-size="12pt" font-weight="italics" font-style="italic" color="green">
                                	${statement.availableBalace?string(",##0.00")} 
                               </fo:block>
                            </fo:table-cell>
                        </fo:table-row>
                    </#if>
                </fo:table-body>
            </fo:table>
        </fo:block>
        
        
        </#list>

    <#else>
        <fo:block text-align="center">NO DATA FOUND</fo:block>
    </#if>
</#escape>