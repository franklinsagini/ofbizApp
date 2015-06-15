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
    <#if membersList?has_content>
        <#-- REPORT TITLE -->
        <fo:block font-size="18pt" font-weight="bold" text-align="center">
            CHAI SACCO
        </fo:block>
        <fo:block font-size="12pt" text-align="center"  font-weight="bold" >
            Member Registration By Date
        </fo:block>
        <fo:block><fo:leader/></fo:block>
        <fo:block margin-left="0.4in" text-decoration="underline" font-size="10pt" text-align="left"  font-weight="bold" >
             Member Registration By Date
        </fo:block>


        <#-- fo:list-block provisional-distance-between-starts="2in" font-size="10pt" margin-left="0.2in">
            <fo:list-item>
                <fo:list-item-label>
                    <fo:block font-weight="bold">Product:</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>${product.name}</fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label>
                    <fo:block font-weight="bold">Max Loan Amount:</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>${product.maximumAmt?if_exists}</fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label>
                    <fo:block font-weight="bold">Max Repayment Period::</fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>${product.maxRepaymentPeriod?if_exists} Months</fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label>
                    <fo:block font-weight="bold">Rate: </fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>${product.interestRatePM?if_exists} % P.A</fo:block>
                </fo:list-item-body>
            </fo:list-item>
            <fo:list-item>
                <fo:list-item-label>
                    <fo:block font-weight="bold">Deduction Type: </fo:block>
                </fo:list-item-label>
                <fo:list-item-body start-indent="body-start()">
                    <fo:block>${product.deductionType?if_exists}</fo:block>
                </fo:list-item-body>
            </fo:list-item>
        </fo:list-block>
        <fo:block><fo:leader/></fo:block -->
        <#-- Loan Details -->
        <fo:block space-after.optimum="10pt" font-size="9pt">
            <fo:table table-layout="fixed" width="100%">
            	<fo:table-column column-width="30pt"/>
                <fo:table-column column-width="120pt"/>
                <fo:table-column column-width="50pt"/>
                <fo:table-column column-width="45pt"/>
                <fo:table-column column-width="50pt"/>
                 <fo:table-column column-width="70pt"/>
                <fo:table-column column-width="60pt"/>
                
                <fo:table-column column-width="40pt"/>
                <fo:table-column column-width="60pt"/>
                <fo:table-column column-width="60pt"/>
                <#-- fo:table-column column-width="60pt"/>
                <fo:table-column column-width="60pt"/>
                <fo:table-column column-width="60pt"/>
                <fo:table-column column-width="60pt"/ -->
                <fo:table-header>
                    <fo:table-row font-weight="bold">
                    	 <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block></fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block>Names</fo:block>
                        </fo:table-cell>
                        
                         <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block>Status</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block>ID Number</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block>Payroll Number</fo:block>
                        </fo:table-cell>
                        
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block>Mobile Number</fo:block>
                        </fo:table-cell>
                        
                       <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block>Join Date</fo:block>
                        </fo:table-cell>
                        
                                                
                         <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block>Member Number</fo:block>
                        </fo:table-cell>
                        
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block>Station</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block>Branch</fo:block>
                        </fo:table-cell>

                        <#-- fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
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
                <#assign count = 0>
                <fo:table-body>
                    <#list membersList as member>
                        <fo:table-row>
                        	<#assign count = count + 1>
                        	<fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>${count}</fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>${member.firstName?if_exists} ${member.middleName?if_exists} ${member.lastName?if_exists}</fo:block>
                            </fo:table-cell>
                            
                            <#assign status = delegator.findOne("MemberStatus", Static["org.ofbiz.base.util.UtilMisc"].toMap("memberStatusId", member.memberStatusId), true)/>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>${status.name?if_exists}</fo:block>
                            </fo:table-cell>
                            
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>${member.idNumber?if_exists}</fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>${member.payrollNumber?if_exists}</fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>${member.mobileNumber?if_exists}</fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>${member.joinDate?date}</fo:block>
                            </fo:table-cell>
                            
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>${member.memberNumber?if_exists}</fo:block>
                            </fo:table-cell>
                            <#assign stationId = member.stationId />

                             <#assign station = delegator.findOne("Station", Static["org.ofbiz.base.util.UtilMisc"].toMap("stationId", stationId.toString()), true)/>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>
                                <#if station?has_content>
                                ${station.stationNumber?if_exists} - ${station.name?if_exists}
                                </#if>
                                </fo:block>
                            </fo:table-cell>
                            
                             <#assign branch = delegator.findOne("PartyGroup", Static["org.ofbiz.base.util.UtilMisc"].toMap("partyId", member.branchId), true)/>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>
                                <#if branch?has_content>
                                ${branch.groupName?if_exists}
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

    <#else>
        <fo:block text-align="center">NO Members FOUND</fo:block>
    </#if>
</#escape>