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
    <#if listStationDefaulted?has_content>
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

		 <#list listStationDefaulted as stationDefaulted>
		 <#-- assign totalAmount = statement.itemTotal -->
		 
        <fo:list-block provisional-distance-between-starts="2in" font-size="10pt" margin-left="0.2in">
            <fo:list-item>
                <fo:list-item-label>
                    <fo:block font-weight="bold">Employer Name</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>${stationDefaulted.employerName}</fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label>
                    <fo:block font-weight="bold">Employer Code</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>${stationDefaulted.employerCode}</fo:block>
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
                <fo:table-column column-width="30pt"/>
                <fo:table-column column-width="50pt"/>
                <fo:table-column column-width="55"/>
                <fo:table-column column-width="50pt"/>
                <fo:table-column column-width="50pt"/>
                
                <fo:table-column column-width="50pt"/>
                <fo:table-column column-width="45pt"/>
                <fo:table-column column-width="80pt"/>
                <fo:table-column column-width="45pt"/>
                <fo:table-column column-width="20pt"/>
                
                <fo:table-column column-width="50pt"/>
                <fo:table-column column-width="50pt"/>

                <#-- fo:table-column column-width="75pt"/>
                < fo:table-column column-width="60pt"/>
                <fo:table-column column-width="60pt"/>
                <fo:table-column column-width="60pt"/>
                <fo:table-column column-width="60pt"/>
                <fo:table-column column-width="60pt"/ -->
                <fo:table-header>
                    <fo:table-row font-weight="bold">
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm" text-align="left">
                            <fo:block>Loan No</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm" text-align="left">
                            <fo:block>Loan Type</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm"  text-align="right">
                            <fo:block>Orig Amt</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm"  text-align="right">
                            <fo:block>Loan Bal</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm"  text-align="left">
                            <fo:block>Dis Date</fo:block>
                        </fo:table-cell>

                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm"  text-align="left">
                            <fo:block>Last Paid</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block>Payroll No</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm"  text-align="left">
                            <fo:block>Name</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm"  text-align="left">
                            <fo:block>Status</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm"  text-align="right">
                            <fo:block>Time Diff</fo:block>
                        </fo:table-cell>


                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm"  text-align="right">
                            <fo:block>Shares</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm"  text-align="left">
                            <fo:block>Terms Of Service</fo:block>
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
                <#assign loanCount=0>
                <#assign totalDisbursed=0>
                <#assign totalBalance=0>
                <#assign totalShares=0>
                
                    <#list stationDefaulted.listOfDefaultedLoans as defaultedItem>
                    
                    	<#assign loanCount=loanCount+1>
       	                
                        <fo:table-row>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm" background-color="${zebra(defaultedItem_index)}" text-align="left">
                                <fo:block>
                                <#if defaultedItem.loanNo?? >
                                
                                	${defaultedItem.loanNo}
                                
                                </#if>
                                <#-- ${defaultedItem.transactionDate?date} -->
                                </fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm" background-color="${zebra(defaultedItem_index)}" text-align="left">
                                <fo:block>${defaultedItem.loanType?if_exists}</fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm" text-align="right" background-color="${zebra(defaultedItem_index)}">
                                <fo:block >
                                
                                <#-- (transaction.increaseDecrease = 'D') &&  -->
                                	<#if (defaultedItem.loanAmt??)  >
                                	
                                		
                                		   ${defaultedItem.loanAmt?string(",##0.00")}
                                		   <#assign totalDisbursed= totalDisbursed + defaultedItem.loanAmt>
								  
								</#if>
                                </fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm" text-align="right" background-color="${zebra(defaultedItem_index)}">
                                <fo:block>
                                <#-- (transaction.increaseDecrease = 'I') && -->
                                	
                                	<#if  (defaultedItem.loanBalance??)  >
                                	
                                		
                                		 ${defaultedItem.loanBalance?string(",##0.00")}
                                		 <#assign totalBalance = totalBalance + defaultedItem.loanBalance>
								</#if>
								
                                </fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm" text-align="left" background-color="${zebra(defaultedItem_index)}">
                                <fo:block>
                              
								    ${defaultedItem.disbursementDate?string["dd/MM/yyyy"]}
								
                               </fo:block>
                            </fo:table-cell>
                            
                            <#-- fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>
                              
                                <#if (expectation.totalAmount??)>
								    Kshs.  ${expectation.totalAmount?string(",##0.00")} 
								</#if>
								
                               </fo:block>
                            </fo:table-cell -->
                            
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm" background-color="${zebra(defaultedItem_index)}">
                                <fo:block>
                              
                                <#if (defaultedItem.lastPaid??)>
								    ${defaultedItem.lastPaid?string["dd/MM/yyyy"]}
								</#if>
								
                               </fo:block>
                            </fo:table-cell>
                            
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm" background-color="${zebra(defaultedItem_index)}">
                                <fo:block>
                              
                                <#if (defaultedItem.payrollNo??)>
								   ${defaultedItem.payrollNo} 
								</#if>
								
                               </fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm" background-color="${zebra(defaultedItem_index)}">
                                <fo:block>
                              
                                <#if (defaultedItem.name??)>
								   ${defaultedItem.name} 
								</#if>
								
                               </fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm" background-color="${zebra(defaultedItem_index)}">
                                <fo:block>
                              
                               <#if (defaultedItem.memberStatus??)>
								   ${defaultedItem.memberStatus} 
								</#if>
								
                               </fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm" background-color="${zebra(defaultedItem_index)}">
                                <fo:block>
                              
                                <#if (defaultedItem.timeDifference??)>
								   ${defaultedItem.timeDifference} 
								</#if>
								
                               </fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm" background-color="${zebra(defaultedItem_index)}">
                                <fo:block>
                              
                                 <#if (defaultedItem.shareAmount??)>
								   ${defaultedItem.shareAmount?string(",##0.00")} 
								   
								   <#assign totalShares= totalShares + defaultedItem.shareAmount>
								</#if>
								
                               </fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm" background-color="${zebra(defaultedItem_index)}">
                                <fo:block>
                              
                                 <#if (defaultedItem.termsOfService??)>
								   ${defaultedItem.termsOfService} 
								</#if>
								
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
        
        
        </#list>
        
                <fo:block space-after.optimum="10pt" font-size="9pt">
            <fo:table table-layout="fixed" width="100%">
                <fo:table-column column-width="30pt"/>
                <fo:table-column column-width="50pt"/>
                <fo:table-column column-width="55pt"/>
                <fo:table-column column-width="50pt"/>
                <fo:table-column column-width="50pt"/>
                
                <fo:table-column column-width="50pt"/>
                <fo:table-column column-width="45pt"/>
                <fo:table-column column-width="80pt"/>
                <fo:table-column column-width="45pt"/>
                <fo:table-column column-width="20pt"/>
                
                <fo:table-column column-width="50pt"/>
                <fo:table-column column-width="50pt"/>

                <#-- fo:table-column column-width="75pt"/>
                < fo:table-column column-width="60pt"/>
                <fo:table-column column-width="60pt"/>
                <fo:table-column column-width="60pt"/>
                <fo:table-column column-width="60pt"/>
                <fo:table-column column-width="60pt"/ -->
                <fo:table-header>
                    <fo:table-row font-weight="bold">
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm" text-align="left">
                            <fo:block>Total</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm" text-align="left">
                            <fo:block>${loanCount}</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm"  text-align="right">
                            <fo:block>${totalDisbursed?string(",##0.00")}</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm"  text-align="right">
                            <fo:block>${totalBalance?string(",##0.00")}</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm"  text-align="left">
                            <fo:block></fo:block>
                        </fo:table-cell>

                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm"  text-align="left">
                            <fo:block></fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block></fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm"  text-align="left">
                            <fo:block></fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm"  text-align="left">
                            <fo:block></fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm"  text-align="right">
                            <fo:block></fo:block>
                        </fo:table-cell>


                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm"  text-align="right">
                            <fo:block>${totalShares?string(",##0.00")}</fo:block>
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
                        <fo:table-cell padding="2pt"  background-color="white" border="0pt solid" border-width="0" >
                            <fo:block></fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt"  background-color="white" border="0pt solid" border-width="0"   text-align="left">
                            <fo:block></fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt"  background-color="white" border="0pt solid" border-width="0"   text-align="left">
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