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
    <#if earningsList?has_content>
        <#-- REPORT TITLE -->
        <fo:block font-size="18pt" margin-left="20mm" font-weight="bold" text-align="left">
            CHAI SACCO
        </fo:block>
        <fo:block font-size="12pt" text-align="left"  font-weight="bold" >
            Employee Payslip for the period  ${period.name} 
        </fo:block>
        <fo:block><fo:leader/></fo:block>
        <#-- fo:block margin-left="0.4in" text-decoration="underline" font-size="10pt" text-align="left"  font-weight="bold" >
            Member Statement
        </fo:block -->

		 <#-- list memberStatementList as statement>
		 <#assign totalAmount = statement.itemTotal -->
		 
        <fo:list-block provisional-distance-between-starts="2in" font-size="10pt" margin-left="0.2in">
            <fo:list-item>
                <fo:list-item-label>
                    <fo:block font-weight="bold">Name</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>${person.firstName?if_exists} ${person.lastName?if_exists} </fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label>
                    <fo:block font-weight="bold">Payroll No</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>${person.employeeNumber?if_exists}</fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label>
                    <fo:block font-weight="bold">Bank</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>${paypoint.bankName?if_exists}</fo:block>
                </fo:list-item-body>
            </fo:list-item>
             <fo:list-item>
                <fo:list-item-label>
                    <fo:block font-weight="bold">Bank Branch</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>${paypoint.branchName?if_exists}</fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label>
                    <fo:block font-weight="bold">Account Number</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>${paypoint.accountNumber?if_exists}</fo:block>
                </fo:list-item-body>
            </fo:list-item>
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
                <fo:table-column column-width="70pt"/>
                <fo:table-column column-width="70pt"/>
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
                            <fo:block>Name</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block>Amount</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block>Balance</fo:block>
                        </fo:table-cell>
                    </fo:table-row>
                </fo:table-header>
                <fo:table-body>
                		<fo:table-row>
                            
                            <fo:table-cell padding="2pt" border-width=".1mm" font-size="16pt" font-weight="bold">
                                <fo:block>Earnings</fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border-width=".1mm">
                                <fo:block></fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border-width=".1mm">
                                <fo:block>
								
                                </fo:block>
                            </fo:table-cell>
                            
                        </fo:table-row>
                    <#list earningsList as element>
                    
                               <fo:table-row>
                            
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            	<#assign payrollElement = delegator.findOne("PayrollElement", Static["org.ofbiz.base.util.UtilMisc"].toMap("payrollElementId", element.payrollElementId), true)/>
                                <#if element.isbold=='Y'>
                                <fo:block font-weight = "bold">${payrollElement.name?if_exists}                                
                                </fo:block>
                                <#else>
                                 <fo:block>${payrollElement.name?if_exists}</fo:block>
                                </#if>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block text-align="right">${element.amount?string(",##0.00")}</fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block text-align="right">
								<#if element.balance?has_content >
									${element.balance?string(",##0.00")}
								</#if>
								
                                </fo:block>
                            </fo:table-cell>
                            
                        </fo:table-row>
                    </#list>
                    
                     <fo:table-row column-height="30mm">
                            
                            <fo:table-cell padding="2pt" >
                                <fo:block></fo:block>
                            </fo:table-cell>
                            <fo:table-cell >
                                <fo:block></fo:block>
                            </fo:table-cell>
                            <fo:table-cell >
                                <fo:block>
								
                                </fo:block>
                            </fo:table-cell>
                            
                        </fo:table-row>
                    
                    <#-- CALCULATED -->
                    
                     <#-- list calculatedList as element>
                    
                               <fo:table-row>
                            
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            	<#assign payrollElement = delegator.findOne("PayrollElement", Static["org.ofbiz.base.util.UtilMisc"].toMap("payrollElementId", element.payrollElementId), true)/>
                                <fo:block>${payrollElement.name?if_exists}</fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>${element.amount?string(",##0.00")}</fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>
								<#if element.balance?has_content >
								${element.balance?string(",##0.00")}
								</#if>
								
                                </fo:block>
                            </fo:table-cell>
                            
                        </fo:table-row>
                    </#list -->
                </fo:table-body>
            </fo:table>
            
            
            <#-- Table 2 -->
            <fo:table table-layout="fixed" width="100%">
                <fo:table-column column-width="100pt"/>
                <fo:table-column column-width="70pt"/>
                <fo:table-column column-width="70pt"/>
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
                        <fo:table-cell padding="2pt" background-color="#FFFFFF" border-width=".1mm">
                            <fo:block></fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#FFFFFF" border-width=".1mm">
                            <fo:block></fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#FFFFFF" color="#FFFFFF"  border-width=".1mm">
                            <fo:block>NAME</fo:block>
                        </fo:table-cell>
                    </fo:table-row>
                </fo:table-header>
                <fo:table-body>
      
                    <#-- CALCULATED -->
                    
                     <#list calculatedList as element>
                     		<#if element.payrollElementId != "PAYE">
                     		<#if element.payrollElementId != "NHIF">
                     		<#if element.payrollElementId != "NSSF">
                     		<#if element.payrollElementId != "NSSFVOL">
                     		<#if element.payrollElementId != "PENSION">
                     		<#if element.payrollElementId != "TOTDEDUCTIONS">
                     		<#if element.payrollElementId != "NETPAY">
                               <fo:table-row>
                            
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            	<#assign payrollElement = delegator.findOne("PayrollElement", Static["org.ofbiz.base.util.UtilMisc"].toMap("payrollElementId", element.payrollElementId), true)/>
                                <#if element.isbold=='Y'>
                                <fo:block font-weight = "bold">${payrollElement.name?if_exists}                                
                                </fo:block>
                                <#else>
                                 <fo:block>${payrollElement.name?if_exists}</fo:block>
                                </#if>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block text-align="right">${element.amount?string(",##0.00")}</fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block text-align="right">
								<#if element.balance?has_content >
								${element.balance?string(",##0.00")}
								</#if>
								
                                </fo:block>
                            </fo:table-cell>
                            
                        </fo:table-row>
                         </#if>
                        </#if>
                        </#if>
                        </#if>
                        </#if>
                        </#if>
                        </#if>
                    </#list>
                </fo:table-body>
            </fo:table>
            
             <#-- Deductions Table-->
         <fo:table table-layout="fixed" width="100%">
                <fo:table-column column-width="100pt"/>
                <fo:table-column column-width="70pt"/>
                <fo:table-column column-width="70pt"/>
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
                        <fo:table-cell padding="2pt" border-width=".1mm" font-size="16pt" font-weight="bold">
                            <fo:block>Deductions</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" border-width=".1mm">
                            <fo:block></fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" border-width=".1mm">
                            <fo:block></fo:block>
                        </fo:table-cell>
                    </fo:table-row>
                </fo:table-header>
                <fo:table-body>
                	
                	<#list payeList as element>
                               <fo:table-row>
                            
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            	<#assign payrollElement = delegator.findOne("PayrollElement", Static["org.ofbiz.base.util.UtilMisc"].toMap("payrollElementId", element.payrollElementId), true)/>
                                <#if element.isbold=='Y'>
                                <fo:block font-weight = "bold">${payrollElement.name?if_exists}                                
                                </fo:block>
                                <#else>
                                 <fo:block>${payrollElement.name?if_exists}</fo:block>
                                </#if>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block text-align="right">${element.amount?string(",##0.00")}</fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block text-align="right">
								<#if element.balance?has_content >
								${element.balance?string(",##0.00")}
								</#if>
								
                                </fo:block>
                            </fo:table-cell>
                            
                        </fo:table-row>
                    </#list>
                    <#list nhifList as element>
                               <fo:table-row>
                            
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            	<#assign payrollElement = delegator.findOne("PayrollElement", Static["org.ofbiz.base.util.UtilMisc"].toMap("payrollElementId", element.payrollElementId), true)/>
                                <#if element.isbold=='Y'>
                                <fo:block font-weight = "bold">${payrollElement.name?if_exists}                                
                                </fo:block>
                                <#else>
                                 <fo:block>${payrollElement.name?if_exists}</fo:block>
                                </#if>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block text-align="right">${element.amount?string(",##0.00")}</fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block text-align="right">
								<#if element.balance?has_content >
								${element.balance?string(",##0.00")}
								</#if>
								
                                </fo:block>
                            </fo:table-cell>
                            
                        </fo:table-row>
                    </#list>
                    <#list nssfList as element>
                               <fo:table-row>
                            
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            	<#assign payrollElement = delegator.findOne("PayrollElement", Static["org.ofbiz.base.util.UtilMisc"].toMap("payrollElementId", element.payrollElementId), true)/>
                                <#if element.isbold=='Y'>
                                <fo:block font-weight = "bold">${payrollElement.name?if_exists}                                
                                </fo:block>
                                <#else>
                                 <fo:block>${payrollElement.name?if_exists}</fo:block>
                                </#if>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block text-align="right">${element.amount?string(",##0.00")}</fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block text-align="right">
								<#if element.balance?has_content >
								${element.balance?string(",##0.00")}
								</#if>
								
                                </fo:block>
                            </fo:table-cell>
                            
                        </fo:table-row>
                    </#list>
                    <#list nssfVolList as element>
                               <fo:table-row>
                            
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            	<#assign payrollElement = delegator.findOne("PayrollElement", Static["org.ofbiz.base.util.UtilMisc"].toMap("payrollElementId", element.payrollElementId), true)/>
                                <#if element.isbold=='Y'>
                                <fo:block font-weight = "bold">${payrollElement.name?if_exists}                                
                                </fo:block>
                                <#else>
                                 <fo:block>${payrollElement.name?if_exists}</fo:block>
                                </#if>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block text-align="right">${element.amount?string(",##0.00")}</fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block text-align="right">
								<#if element.balance?has_content >
								${element.balance?string(",##0.00")}
								</#if>
								
                                </fo:block>
                            </fo:table-cell>
                            
                        </fo:table-row>
                    </#list>
                    <#list pensionList as element>
                               <fo:table-row>
                            
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            	<#assign payrollElement = delegator.findOne("PayrollElement", Static["org.ofbiz.base.util.UtilMisc"].toMap("payrollElementId", element.payrollElementId), true)/>
                                <#if element.isbold=='Y'>
                                <fo:block font-weight = "bold">${payrollElement.name?if_exists}                                
                                </fo:block>
                                <#else>
                                 <fo:block>${payrollElement.name?if_exists}</fo:block>
                                </#if>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block text-align="right">${element.amount?string(",##0.00")}</fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block text-align="right">
								<#if element.balance?has_content >
								${element.balance?string(",##0.00")}
								</#if>
								
                                </fo:block>
                            </fo:table-cell>
                            
                        </fo:table-row>
                    </#list>
      
                     <#list deductionsList as element>
                               <fo:table-row>
                            
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            	<#assign payrollElement = delegator.findOne("PayrollElement", Static["org.ofbiz.base.util.UtilMisc"].toMap("payrollElementId", element.payrollElementId), true)/>
                                <#if element.isbold=='Y'>
                                <fo:block font-weight = "bold">${payrollElement.name?if_exists}                                
                                </fo:block>
                                <#else>
                                 <fo:block>${payrollElement.name?if_exists}</fo:block>
                                </#if>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block text-align="right">${element.amount?string(",##0.00")}</fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block text-align="right">
								<#if element.balance?has_content >
								${element.balance?string(",##0.00")}
								</#if>
								
                                </fo:block>
                            </fo:table-cell>
                            
                        </fo:table-row>
                    </#list>
                    <#list totDeductionsList as element>
                               <fo:table-row>
                            
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            	<#assign payrollElement = delegator.findOne("PayrollElement", Static["org.ofbiz.base.util.UtilMisc"].toMap("payrollElementId", element.payrollElementId), true)/>
                                <#if element.isbold=='Y'>
                                <fo:block font-weight = "bold">${payrollElement.name?if_exists}                                
                                </fo:block>
                                <#else>
                                 <fo:block>${payrollElement.name?if_exists}</fo:block>
                                </#if>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block text-align="right">${element.amount?string(",##0.00")}</fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block text-align="right">
								<#if element.balance?has_content >
								${element.balance?string(",##0.00")}
								</#if>
								
                                </fo:block>
                            </fo:table-cell>
                            
                        </fo:table-row>
                    </#list>
                </fo:table-body>
            </fo:table>
            
            <#-- NET PAY-->
            <fo:table table-layout="fixed" width="100%">
                <fo:table-column column-width="100pt"/>
                <fo:table-column column-width="70pt"/>
                <fo:table-column column-width="70pt"/>
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
                        <fo:table-cell padding="2pt" border-width=".1mm" color="#FFFFFF"  font-size="16pt" font-weight="bold">
                            <fo:block>Deductions</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" border-width=".1mm">
                            <fo:block></fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" border-width=".1mm">
                            <fo:block></fo:block>
                        </fo:table-cell>
                    </fo:table-row>
                </fo:table-header>
                <fo:table-body>
      
                     <#list netPayList as element>
                               <fo:table-row>
                            
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            	<#assign payrollElement = delegator.findOne("PayrollElement", Static["org.ofbiz.base.util.UtilMisc"].toMap("payrollElementId", element.payrollElementId), true)/>
                                
                                <#if element.isbold=='Y'>
                                <fo:block font-weight = "bold">${payrollElement.name?if_exists}                                
                                </fo:block>
                                <#else>
                                 <fo:block>${payrollElement.name?if_exists}</fo:block>
                                </#if>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block text-align="right">${element.amount?string(",##0.00")}</fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block text-align="right">
								<#if element.balance?has_content >
								${element.balance?string(",##0.00")}
								</#if>
								
                                </fo:block>
                            </fo:table-cell>
                            
                        </fo:table-row>
                    </#list>
                </fo:table-body>
            </fo:table>
            
            <#-- Payslip Message -->
            <fo:table table-layout="fixed" width="100%">
                <fo:table-column column-width="200pt"/>
                <fo:table-column column-width="80pt"/>
                <fo:table-column column-width="80pt"/>
                <fo:table-column column-width="100pt"/>
                <fo:table-column column-width="100pt"/>
                <fo:table-header>
                    <fo:table-row font-weight="bold">
                        <fo:table-cell padding="2pt" border-width=".1mm" color="#FFFFFF"  font-size="16pt" font-weight="bold">
                            <fo:block>Payslip Message</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" border-width=".1mm">
                            <fo:block></fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" border-width=".1mm">
                            <fo:block></fo:block>
                        </fo:table-cell>
                    </fo:table-row>
                </fo:table-header>
                <fo:table-body>
      
                     <#list payslipMsgList as element>
                               <fo:table-row>

                            <fo:table-cell   font-size="10pt" font-weight="bold">
                                <fo:block> ${element.description?string}</fo:block>
                            </fo:table-cell>
                           
                            
                        </fo:table-row>
                    </#list>
                </fo:table-body>
            </fo:table>
        </fo:block>
        
        
        <#-- /#list -->
        
        
     
        

    <#else>
        <fo:block text-align="center">NO DATA FOUND</fo:block>
    </#if>
</#escape>