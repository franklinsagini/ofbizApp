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
    <#if mergedTransactionsList?has_content>
        <#-- REPORT TITLE -->
        <!-- fo:block font-size="18pt" font-weight="bold" text-align="center">
            CHAI SACCO
        </fo:block -->
        <fo:block font-size="12pt" text-align="center"  font-weight="bold" >
        </fo:block>
        
        <#--fo:list-block provisional-distance-between-starts="2in" font-size="10pt" margin-left="0.2in">
            <fo:list-item>
                <fo:list-item-label>
                    <fo:block font-weight="bold">Member Number</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block> </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label>
                    <fo:block font-weight="bold">Payroll Number</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block></fo:block>
                </fo:list-item-body>
            </fo:list-item>
            < fo:list-item>
                <fo:list-item-label>
                    <fo:block font-weight="bold">Member Name</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>${member.firstName?if_exists} ${member.middleName?if_exists} ${member.lastName?if_exists}</fo:block>
                </fo:list-item-body>
            </fo:list-item >
            
            <fo:list-item>
                <fo:list-item-label>
                    <fo:block font-weight="bold">Station Name</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>
                    	       
                    </fo:block>
                </fo:list-item-body>
            </fo:list-item >
            
        </fo:list-block -->
        <fo:block><fo:leader/></fo:block>

		<#function zebra index>
		  <#if (index % 2) == 0>
		    <#return "white" />
		  <#else>
		    <#return "#C0D9AF" />
		  </#if>
		</#function>

		
		 <#-- assign totalAmount = statement.itemTotal -->
		 
        <fo:list-block provisional-distance-between-starts="2in" font-size="10pt" margin-left="0.2in">
            <fo:list-item>
                <fo:list-item-label>
                    <fo:block font-weight="bold">CHAI CO-OPERATIVE SAVINGS AND CREDIT SOCIETY LIMITED</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block></fo:block>
                </fo:list-item-body>
            </fo:list-item>
            
            <fo:list-item>
                <fo:list-item-label>
                    <fo:block font-weight="bold">P.O. BOX 278 - 00200 CITY SQUARE, NAIROBI KENYA. KTDA PLAZA - MOI AVENUE</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block></fo:block>
                </fo:list-item-body>
            </fo:list-item>
            
            
            <fo:list-item>
                <fo:list-item-label>
                    <fo:block font-weight="bold">TEL: 254 (0) 20 214406, FAX 254 (0) 20 214410. E-MAIL: info@chai-sacco.co.ke</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block></fo:block>
                </fo:list-item-body>
            </fo:list-item>
            
             <fo:list-item>
                <fo:list-item-label>
                    <fo:block font-weight="bold">TELLER RECONCILIATION (${tellerName}) AS AT ${transferDate?string["dd/MM/yyyy"]}</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block></fo:block>
                </fo:list-item-body>
            </fo:list-item>
            
            
            
           <#-- fo:list-item>
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
            </fo:list-item -->
            <#-- fo:list-item>
                <fo:list-item-label>
                    <fo:block font-weight="bold">Payroll Number:</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>${member.payrollNumber?if_exists}</fo:block>
                </fo:list-item-body>
            </fo:list-item -->
            
        </fo:list-block>
        <fo:block><fo:leader/></fo:block>
        <#-- Loan Details -->
        <fo:block space-after.optimum="10pt" font-size="9pt">
            <fo:table table-layout="fixed" width="100%">
                <fo:table-column column-width="100pt"/>
                <fo:table-column column-width="100pt"/>
                <fo:table-column column-width="150pt"/>
                <fo:table-column column-width="60pt"/>
                <fo:table-column column-width="60pt"/>
                <fo:table-column column-width="100pt"/>
                
                <#-- fo:table-column column-width="50pt"/>
                
                <fo:table-column column-width="45pt"/>
                <fo:table-column column-width="80pt"/>
                <fo:table-column column-width="45pt"/>
                <fo:table-column column-width="20pt"/>
                
                <fo:table-column column-width="50pt"/>
                <fo:table-column column-width="50pt"/ -->

                <#-- fo:table-column column-width="75pt"/>
                < fo:table-column column-width="60pt"/>
                <fo:table-column column-width="60pt"/>
                <fo:table-column column-width="60pt"/>
                <fo:table-column column-width="60pt"/>
                <fo:table-column column-width="60pt"/ -->
                <fo:table-header>
                    <fo:table-row font-weight="bold">
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm" text-align="left">
                            <fo:block>Transaction Date</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm" text-align="left">
                            <fo:block>Account No</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm"  text-align="left">
                            <fo:block>Description</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm"  text-align="left">
                            <fo:block>Debit(KShs.)</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm"  text-align="left">
                            <fo:block>Credit (KShs)</fo:block>
                        </fo:table-cell>

                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm"  text-align="left">
                            <fo:block>Running Balance (KShs.)</fo:block>
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

                <#assign runningBalance=0>
                <#assign transactionsCount=0>
                <#assign totalDebits=0>
                <#assign totalCredits=0>
                
                     <#list mergedTransactionsList as transactionItem>
                    	<#assign transactionsCount = transactionsCount + 1>
                    	<#if transactionItem.increaseDecrease == 'I' >
                                <#assign runningBalance=runningBalance+transactionItem.transactionAmount>
                                
                                <#assign totalDebits=totalDebits+transactionItem.transactionAmount>
                                
                         </#if>
                      	<#if transactionItem.increaseDecrease == 'D' >
                                <#assign runningBalance=runningBalance - transactionItem.transactionAmount>
                                <#assign totalCredits=totalCredits+transactionItem.transactionAmount>
                         </#if>
                    	
       	                
                        <fo:table-row>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm" background-color="${zebra(transactionItem_index)}" text-align="left">
                                <fo:block>
                                <#if transactionItem.transactionDateTime?? >
                                	${transactionItem.transactionDateTime?string["dd/MM/yyyy HH:mm:ss"]}
                                </#if>
                                </fo:block>
                            </fo:table-cell>
                            
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm" background-color="${zebra(transactionItem_index)}" text-align="left">
                                <fo:block>${transactionItem.memberAccountNo?if_exists}</fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm" text-align="left" background-color="${zebra(transactionItem_index)}">
                                <fo:block >
                                	${transactionItem.description?if_exists}
                                </fo:block>
                            </fo:table-cell>
                            
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm" text-align="right" background-color="${zebra(transactionItem_index)}">
                                <fo:block>
                                 <#-- (transaction.increaseDecrease = 'D') &&  -->
                                <#if (transactionItem.increaseDecrease = 'I')  >
                                		   ${transactionItem.transactionAmount?string(",##0.00")}
								</#if>
								
								
                                </fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm" text-align="right" background-color="${zebra(transactionItem_index)}">
                                <fo:block>
                              
								   <#if (transactionItem.increaseDecrease = 'D')  >
                                		   ${transactionItem.transactionAmount?string(",##0.00")}
								</#if>
								
                               </fo:block>
                            </fo:table-cell>
                            
                            <#-- fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>
                              
                                <#if (expectation.totalAmount??)>
								    Kshs.  ${expectation.totalAmount?string(",##0.00")} 
								</#if>
								
                               </fo:block>
                            </fo:table-cell -->
                            
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm"  text-align="right"  background-color="${zebra(transactionItem_index)}">
                                <fo:block>
                              
                                		   ${runningBalance?string(",##0.00")}
								
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
                </fo:table-body>
            </fo:table>
        </fo:block>
        
        <fo:block space-after.optimum="10pt" font-size="9pt">
            <fo:table table-layout="fixed" width="100%">
				<fo:table-column column-width="100pt"/>
                <fo:table-column column-width="100pt"/>
                <fo:table-column column-width="150pt"/>
                <fo:table-column column-width="60pt"/>
                <fo:table-column column-width="60pt"/>
                <fo:table-column column-width="100pt"/>
                <#-- fo:table-column column-width="75pt"/>
                < fo:table-column column-width="60pt"/>
                <fo:table-column column-width="60pt"/>
                <fo:table-column column-width="60pt"/>
                <fo:table-column column-width="60pt"/>
                <fo:table-column column-width="60pt"/ -->
                <fo:table-header>
                    <fo:table-row font-weight="bold">
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm" text-align="left">
                            <fo:block>Sub Total</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm" text-align="left">
                            <fo:block>${transactionsCount}</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm"  text-align="right">
                            <fo:block></fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm"  text-align="right">
                            <fo:block>${totalDebits?string(",##0.00")}</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm"  text-align="right">
                            <fo:block>${totalCredits?string(",##0.00")}</fo:block>
                        </fo:table-cell>

                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm"  text-align="left">
                            <fo:block></fo:block>
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
                
                <fo:table-row>
                	    <fo:table-cell padding="2pt" background-color="white" border="0pt solid" border-width="0" text-align="left">
                            <fo:block></fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt"  background-color="white" border="0pt solid" border-width="0"  text-align="left">
                            <fo:block></fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt"  background-color="white" border="0pt solid" border-width="0"   text-align="right">
                            <fo:block></fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt"  background-color="white" border="0pt solid" border-width="0"   text-align="right">
                            <fo:block></fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt"  background-color="white" border="0pt solid" border-width="0"   text-align="left">
                            <fo:block></fo:block>
                        </fo:table-cell>

                        <fo:table-cell padding="2pt"  background-color="white" border="0pt solid" border-width="0"   text-align="left">
                            <fo:block></fo:block>
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
                </fo:table-body>
            </fo:table>
        </fo:block>
        
        

    <#else>
        <fo:block text-align="center">NO DATA FOUND</fo:block>
    </#if>
</#escape>