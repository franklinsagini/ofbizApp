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
	
	<#--REPORT TITLE-->
	    <fo:block font-size="18pt" font-weight="bold" text-align="center">
            CHAI SACCO
        </fo:block>
        <fo:block font-size="12pt" text-align="center"  font-weight="bold" >
            EMPLOYEE TERMINATION REPORT
        </fo:block>
        <fo:block><fo:leader/></fo:block>
        <#-- Employee Termination Details -->
	
         <fo:block space-after.optimum="10pt" font-size="10pt">
	       <fo:table table-layout="fixed" width="100%">

                <fo:table-column column-width="100pt"/>
                <fo:table-column column-width="120pt"/>
                <fo:table-column column-width="80pt"/>
                <fo:table-column column-width="120pt"/>
                 <fo:table-column column-width="120pt"/>
                
                 <fo:table-header>
                    <fo:table-row font-weight="bold">
                       <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block>Payroll No.</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block>Name</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block>ID Number:</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block>Branch:</fo:block>
                        </fo:table-cell>
                         <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block>Date Of Appointment</fo:block>
                        </fo:table-cell>
                   </fo:table-row>
                </fo:table-header>  
                                 <fo:table-body>
                <#assign count=0>
                    <#list personDetail as perDetail>
                    <#list branchDetail as branch>
                    
                        <fo:table-row>
                        <#assign count = count + 1>
                            
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block> ${perDetail.employeeNumber?if_exists} </fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>  ${perDetail.firstName?if_exists}  ${perDetail.lastName?if_exists} </fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>  ${perDetail.nationalIDNumber?if_exists}  </fo:block>
                            </fo:table-cell>
                             <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                               <#if branchDetail? has_content>
                                <fo:block> ${branch.groupName? if_exists}  </fo:block>
                               </#if>
                            </fo:table-cell>
                             <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block> ${perDetail.appointmentdate?if_exists}  </fo:block>
                            </fo:table-cell>
                            
                        </fo:table-row>
                     </#list>
                    </#list>
                </fo:table-body>
              </fo:table>
	      </fo:block>
	
	
	
	
	<fo:block space-after.optimum="10pt" font-size="10pt">
	       <fo:table table-layout="fixed" width="100%">

                <fo:table-column column-width="100pt"/>
                <fo:table-column column-width="100pt"/>
                <fo:table-column column-width="90pt"/>
                <fo:table-column column-width="90pt"/>
                <fo:table-column column-width="90pt"/>
                <fo:table-column column-width="90pt"/>
                
                
                 <fo:table-header>
                    <fo:table-row font-weight="bold">
                       <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block>Monthly Salary</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block>Leave Allowance</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block>Golden Handshake</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block>Transport Allowance</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block>Service Pay</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block>Total</fo:block>
                        </fo:table-cell>
                   </fo:table-row>
                </fo:table-header>  
                                 <fo:table-body>
                <#assign count=0>
                    <#list separationDetail as details>
                        <fo:table-row>
                        <#assign count = count + 1>
                            
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>${details.salary? if_exists}  </fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>${details.leaveAllowance? if_exists}  </fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block> ${details.goldenHandShake? if_exists} </fo:block>
                            </fo:table-cell>
                             <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>${details.transportAllowance? if_exists} </fo:block>
                            </fo:table-cell>
                              <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>${details.servicePay? if_exists} </fo:block>
                            </fo:table-cell>
                              <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>${details.total? if_exists} </fo:block>
                            </fo:table-cell>
                            
                        </fo:table-row>
                    </#list>
                </fo:table-body>
              </fo:table>
	      </fo:block>
	
	 <fo:block space-after.optimum="10pt" font-size="10pt">
	       <fo:table table-layout="fixed" width="100%">

                 <fo:table-column column-width="100pt"/>
                 <fo:table-column column-width="120pt"/>
                 <fo:table-column column-width="80pt"/>
                 <fo:table-column column-width="120pt"/>
                 <fo:table-column column-width="120pt"/>
                
                 <fo:table-header>
                    <fo:table-row font-weight="bold">
                       <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block>PAYE.</fo:block>
                        </fo:table-cell>
                         <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block>Employee Loans</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block>Lost/ Uncleared Dues</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block>Notice Period:</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block>Day Left Chai Sacco Activities</fo:block>
                        </fo:table-cell>
                        

                   </fo:table-row>
                </fo:table-header>  
                                 <fo:table-body>
                <#assign count=0>
                    <#list  separationDetail as details>
                        <fo:table-row>
                        <#assign count = count + 1>
                            
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>${ details.PAYE? if_exists } </fo:block>
                            </fo:table-cell>
                             <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>${ details.staffLoans? if_exists } </fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>${details.lostItemAmount? if_exists}  </fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block> ${details.noticePeriod ? if_exists}   days </fo:block>
                            </fo:table-cell>
                             <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>${details.dayLeftSaccoActivities? if_exists} </fo:block>
                            </fo:table-cell>
                            
                        </fo:table-row>
                    </#list>
                </fo:table-body>
              </fo:table>
	      </fo:block>
	 <fo:block font-size="12pt"  text-align="left" >
                      
           <fo:block> </fo:block>
        </fo:block>
	 <fo:block space-after.optimum="10pt" font-size="10pt">
	       <fo:table table-layout="fixed" width="100%">

                <fo:table-column column-width="100pt"/>
       
                 <fo:table-header>
                    <fo:table-row font-weight="bold">
                       <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block>Amount Remaining </fo:block>
                        </fo:table-cell>

                   </fo:table-row>
                </fo:table-header>  
                                 <fo:table-body>
                <#assign count=0>
                    <#list   separationDetail as details >
                        <fo:table-row>
                        <#assign count = count + 1>
                            
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>${details.amountPayableToChai ? if_exists} </fo:block>
                            </fo:table-cell>
                            
                        </fo:table-row>
                    </#list>
                </fo:table-body>
              </fo:table>
	      </fo:block>
	
	

   
</#escape>