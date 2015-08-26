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
    <#if earningsTotalsReport?has_content>
	        <#-- REPORT TITLE -->
        <fo:block font-size="12pt" font-weight="bold" text-align="center">
            CHAI SACCO SOCIETY LTD
        </fo:block>
        <fo:block font-size="12pt" text-align="center"  font-weight="bold" >
            REPORT : Company Totals By Analysis
        </fo:block>
         <fo:block font-size="12pt" text-align="center"  font-weight="bold" >
            Employer Code : 01 - All Staff
        </fo:block>
         <fo:block font-size="12pt" text-align="center"  font-weight="bold" >
            Period : ${period.name?if_exists}
        </fo:block>
        
        <fo:block><fo:leader/></fo:block>
        <#-- Payroll Codes Details -->
        
        <#function zebra index>
		  <#if (index % 2) == 0>
		    <#return "white" />
		  <#else>
		    <#return "#C0D9AF" />
		  </#if>
		</#function>
		<fo:block font-size="10pt" font-weight="bold" text-align="left">
            Earnings
        </fo:block>
        <fo:block space-after.optimum="10pt" font-size="10pt">
            <fo:table table-layout="fixed" width="100%">
            	<fo:table-column column-width="30pt"/>
                <fo:table-column column-width="150pt"/>
                <fo:table-column column-width="200pt"/>
                <fo:table-column column-width="200pt"/>
                <fo:table-header>
                    <fo:table-row font-weight="bold">
                    	<fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block></fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block>Code</fo:block>
                        </fo:table-cell>
                        
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block>Name</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block>Total</fo:block>
                        </fo:table-cell>
                    </fo:table-row>
                </fo:table-header>
                <fo:table-body>
                	<#assign count = 0>
                	<#assign TotalAmount = 0>
                    <#list earningsTotalsReport as earnings>
                    	<#assign TotalAmount = TotalAmount + earnings.amount>
                    	<#assign count = count + 1>
                        <fo:table-row>
                        	<fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm" background-color="${zebra(earnings_index)}">
                                <fo:block>${count}</fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm" background-color="${zebra(earnings_index)}">
                                <fo:block>${earnings.payrollElementId?if_exists}</fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm" background-color="${zebra(earnings_index)}">
                                <fo:block>${earnings.name?if_exists}</fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm" text-align="right" background-color="${zebra(earnings_index)}">
                                <fo:block>${earnings.amount?string(",##0.00")}</fo:block>
                            </fo:table-cell>
                        </fo:table-row>
                    </#list>
                    <fo:table-row>
                    		<fo:table-cell padding="2pt"  border="1pt solid"  border-width=".1mm" >
                                <fo:block>  </fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt"  border="1pt solid"  border-width=".1mm" >
                                <fo:block>  </fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm" text-align="left" font-weight="bold">
                                <fo:block>Grand Total</fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm" text-align="right" font-weight="bold">
                                <fo:block>${TotalAmount?string(",##0.00")}</fo:block>
                            </fo:table-cell>
                        </fo:table-row>
                </fo:table-body>
            </fo:table>
        </fo:block>
        
        <#-- PAYE -->
        <fo:block font-size="10pt" font-weight="bold" text-align="left">
            P.A.Y.E
        </fo:block>
        <fo:block space-after.optimum="10pt" font-size="10pt">
            <fo:table table-layout="fixed" width="100%">
            	<fo:table-column column-width="30pt"/>
                <fo:table-column column-width="150pt"/>
                <fo:table-column column-width="200pt"/>
                <fo:table-column column-width="200pt"/>
                <fo:table-header>
                    <fo:table-row font-weight="bold">
                    	<fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block></fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block>Code</fo:block>
                        </fo:table-cell>
                        
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block>Name</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block>Total</fo:block>
                        </fo:table-cell>
                    </fo:table-row>
                </fo:table-header>
                <fo:table-body>
                	<#assign count = 0>
                	<#assign TotalAmount = 0>
                    <#list payeTotalsReport as earnings>
                    	<#assign TotalAmount = TotalAmount + earnings.amount>
                    	<#assign count = count + 1>
                        <fo:table-row>
                        	<fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm" background-color="${zebra(earnings_index)}">
                                <fo:block>${count}</fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm" background-color="${zebra(earnings_index)}">
                                <fo:block>${earnings.payrollElementId?if_exists}</fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm" background-color="${zebra(earnings_index)}">
                                <fo:block>${earnings.name?if_exists}</fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm" text-align="right" background-color="${zebra(earnings_index)}">
                                <fo:block>${earnings.amount?string(",##0.00")}</fo:block>
                            </fo:table-cell>
                        </fo:table-row>
                    </#list>
                    <fo:table-row>
                    		<fo:table-cell padding="2pt"  border="1pt solid"  border-width=".1mm" >
                                <fo:block>  </fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt"  border="1pt solid"  border-width=".1mm" >
                                <fo:block>  </fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm" text-align="left" font-weight="bold">
                                <fo:block></fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm" text-align="right" font-weight="bold">
                                <fo:block></fo:block>
                            </fo:table-cell>
                        </fo:table-row>
                </fo:table-body>
            </fo:table>
        </fo:block>
        
        <#-- Deductions -->
        <fo:block font-size="10pt" font-weight="bold" text-align="left">
            DEDUCTIONS
        </fo:block>
        <fo:block space-after.optimum="10pt" font-size="10pt">
            <fo:table table-layout="fixed" width="100%">
            	<fo:table-column column-width="30pt"/>
                <fo:table-column column-width="150pt"/>
                <fo:table-column column-width="200pt"/>
                <fo:table-column column-width="200pt"/>
                <fo:table-header>
                    <fo:table-row font-weight="bold">
                    	<fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block></fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block>Code</fo:block>
                        </fo:table-cell>
                        
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block>Name</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block>Total</fo:block>
                        </fo:table-cell>
                    </fo:table-row>
                </fo:table-header>
                <fo:table-body>
                	<#assign count = 0>
                	<#assign deductionsTotalAmount = 0>
                    <#list deductionsTotalsReport as earnings>
                    	<#assign deductionsTotalAmount = deductionsTotalAmount + earnings.amount>
                    	<#assign count = count + 1>
                        <fo:table-row>
                        	<fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm" background-color="${zebra(earnings_index)}">
                                <fo:block>${count}</fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm" background-color="${zebra(earnings_index)}">
                                <fo:block>${earnings.payrollElementId?if_exists}</fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm" background-color="${zebra(earnings_index)}">
                                <fo:block>${earnings.name?if_exists}</fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm" text-align="right" background-color="${zebra(earnings_index)}">
                                <fo:block>${earnings.amount?string(",##0.00")}</fo:block>
                            </fo:table-cell>
                        </fo:table-row>
                    </#list>
                    <fo:table-row>
                    		<fo:table-cell padding="2pt"  border="1pt solid"  border-width=".1mm" >
                                <fo:block>  </fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt"  border="1pt solid"  border-width=".1mm" >
                                <fo:block>  </fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm" text-align="left" font-weight="bold">
                                <fo:block>Grand Total</fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm" text-align="right" font-weight="bold">
                                <fo:block>${deductionsTotalAmount?string(",##0.00")}</fo:block>
                            </fo:table-cell>
                        </fo:table-row>
                </fo:table-body>
            </fo:table>
        </fo:block>
        
        
        <#-- Summary -->
        <fo:block font-size="10pt" font-weight="bold" text-align="left">
            SUMMARY
        </fo:block>
        <fo:block space-after.optimum="10pt" font-size="10pt">
            <fo:table table-layout="fixed" width="100%">
            	<fo:table-column column-width="30pt"/>
                <fo:table-column column-width="150pt"/>
                <fo:table-column column-width="200pt"/>
                <fo:table-column column-width="200pt"/>
                
                <fo:table-body>
                	
                        <fo:table-row>
                        	<fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm" >
                                <fo:block></fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm" >
                                <fo:block></fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm" >
                                <fo:block>GROSS PAY</fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm" text-align="right" font-weight="bold" >
                                <fo:block>${grossTotal?string(",##0.00")}</fo:block>
                            </fo:table-cell>
                        </fo:table-row>
                        
                         <fo:table-row>
                        	<fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm" >
                                <fo:block></fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm" >
                                <fo:block></fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm" >
                                <fo:block>PAYE</fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm" text-align="right" font-weight="bold" >
                                <fo:block>${payeTotal?string(",##0.00")}</fo:block>
                            </fo:table-cell>
                        </fo:table-row>
                        
                         <fo:table-row>
                        	<fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm" >
                                <fo:block></fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm" >
                                <fo:block></fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm" >
                                <fo:block>TOTAL DEDUCTIONS</fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm" text-align="right" font-weight="bold" >
                                <fo:block>${deductionsTotalAmount?string(",##0.00")}</fo:block>
                            </fo:table-cell>
                        </fo:table-row>
                   <#assign combinedDeduction = payeTotal + deductionsTotalAmount>
                   <#assign netPayTotal = grossTotal - combinedDeduction >
                    <fo:table-row>
                    		<fo:table-cell padding="2pt"  border="1pt solid"  border-width=".1mm" >
                                <fo:block>  </fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt"  border="1pt solid"  border-width=".1mm" >
                                <fo:block>  </fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm" text-align="left" >
                                <fo:block>NET PAY</fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm" text-align="right" font-weight="bold">
                                <fo:block>${netPayTotal?string(",##0.00")}</fo:block>
                            </fo:table-cell>
                        </fo:table-row>
                </fo:table-body>
            </fo:table>
        </fo:block>

    <#else>
        <fo:block text-align="center">NO DATA FOUND</fo:block>
    </#if>
</#escape>

