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
    <#if listDepositReturnItem?has_content>
        <#-- REPORT TITLE -->
        <fo:block font-size="18pt" font-weight="bold" text-align="center">
            CHAI SACCO
        </fo:block>
        <fo:block font-size="12pt" text-align="center"  font-weight="bold" >
            STATEMENT OF DEPOSIT RETURNS
        </fo:block>
        <fo:block><fo:leader/></fo:block>
        <fo:block margin-left="0.4in" text-decoration="underline" font-size="10pt" text-align="left"  font-weight="bold" >
            SASRA REPORT AS AT 13/02/2015
        </fo:block>
        
        
        
        <#-- Start Report Header -->
        <fo:block space-after.optimum="10pt" font-size="9pt">
            <fo:table table-layout="fixed" width="100%">
            	<#-- fo:table-column column-width="30pt"/ -->
                <fo:table-column column-width="170pt"/>
                <fo:table-column column-width="370pt"/>
                
                <fo:table-header>
                    <fo:table-row font-weight="bold">
                    	
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block></fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block></fo:block>
                        </fo:table-cell>
                       
                    </fo:table-row>
                </fo:table-header>
                <fo:table-body>
                
                		<fo:table-row>
                        	
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block></fo:block>
                            </fo:table-cell>
                            
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>
                                
                                </fo:block>
                            </fo:table-cell>
                            
                        </fo:table-row>
						
                	
                        <fo:table-row>
                        	
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>Name of Sacco Society</fo:block>
                            </fo:table-cell>
                            
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>
                                CHAI SACCO SOCIETY LTD
                                </fo:block>
                            </fo:table-cell>
                            
                        </fo:table-row>
						
						 <fo:table-row>
                        	
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>Financial Year</fo:block>
                            </fo:table-cell>
                            
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>
                                ${year?if_exists}
                                </fo:block>
                            </fo:table-cell>
                            
                        </fo:table-row>
						
						 <fo:table-row>
                        	
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>Start Date</fo:block>
                            </fo:table-cell>
                            
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>
                                ${sqlStartDate?if_exists}
                                </fo:block>
                            </fo:table-cell>
                            
                        </fo:table-row>
						
						 <fo:table-row>
                        	
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>End Date</fo:block>
                            </fo:table-cell>
                            
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>
                                ${sqlEndDate?if_exists}
                                </fo:block>
                            </fo:table-cell>
                            
                        </fo:table-row>
					
                </fo:table-body>
            </fo:table>
        </fo:block>
        
        <#-- End Report Header-->


        <#-- fo:list-block provisional-distance-between-starts="2in" font-size="10pt" margin-left="0.2in">
            <fo:list-item>
                <fo:list-item-label>
                    <fo:block font-weight="bold">Member:</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>${member.firstName} ${member.middleName} ${member.lastName}</fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label>
                    <fo:block font-weight="bold">ID Number:</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>${member.idNumber?if_exists}</fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label>
                    <fo:block font-weight="bold">Payroll Number:</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>${member.payrollNumber?if_exists}</fo:block>
                </fo:list-item-body>
            </fo:list-item>
            
        </fo:list-block -->
        <fo:block><fo:leader/></fo:block>
        <#-- Loan Details -->
        <fo:block space-after.optimum="10pt" font-size="9pt">
            <fo:table table-layout="fixed" width="100%">
            	<#-- fo:table-column column-width="30pt"/ -->
                <fo:table-column column-width="40pt"/>
                <fo:table-column column-width="150pt"/>
                <fo:table-column column-width="120pt"/>
                <fo:table-column column-width="80pt"/>
                <fo:table-column column-width="150pt"/>
                <#--fo:table-column column-width="90pt"/>
                <fo:table-column column-width="90pt"/>
                < fo:table-column column-width="60pt"/>
                <fo:table-column column-width="60pt"/>
                <fo:table-column column-width="60pt"/>
                <fo:table-column column-width="60pt"/>
                <fo:table-column column-width="60pt"/ -->
                <fo:table-header>
                    <fo:table-row font-weight="bold">
                    	<#-- fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block></fo:block>
                        </fo:table-cell -->
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block>No</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block>Range</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block>Type Of Deposit</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block>No Of Account</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block>Amount</fo:block>
                        </fo:table-cell>
                        <#--fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block>Amount Disbursed</fo:block>
                        </fo:table-cell>
                         <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block>Station</fo:block>
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
                	<#assign count=0>
                	<#assign totalLoans=0>
                	<#assign totalDisbursed=0>
                	
                    <#list listDepositReturnItem as depositReturnItem>
                    	<#assign listDepositType = depositReturnItem.listDepositType>
                        <fo:table-row>
                        	<#assign count = count + 1>
                        	<#-- fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>${count}</fo:block>
                            </fo:table-cell -->
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>${depositReturnItem.no?if_exists}</fo:block>
                            </fo:table-cell>
                            
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>
                                ${depositReturnItem.range?if_exists}
                                </fo:block>
                            </fo:table-cell>
                            
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block></fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
								                           
                                <fo:block> 
                                
                                </fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>
                                	
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
                        
                        <#-- Deposit Types Start Here -->
                        <#list listDepositType as depositType>
                        <fo:table-row>
                        	<#-- fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>${count}</fo:block>
                            </fo:table-cell -->
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block></fo:block>
                            </fo:table-cell>
                            
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>
                                
                                </fo:block>
                            </fo:table-cell>
                            
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>${depositType.name?if_exists}</fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
								                           
                                <fo:block> 
                                
                              
                                ${depositType.noOfAccount?if_exists}
                                </fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>
                                	
								Kshs.  ${depositType.amount?string(",##0.0000")}
								
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
                        <#-- Deposit TYpes End Here -->
                        
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
                                <fo:block>
                                	TOTAL 
                                </fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>
                                	10
                                </fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>
                                	134000.00
                                </fo:block>
                            </fo:table-cell>
                            
                    
                    </fo:table-row>
                </fo:table-body>
            </fo:table>
        </fo:block>

    <#else>
        <fo:block text-align="center">NO DATA FOUND</fo:block>
    </#if>
</#escape>