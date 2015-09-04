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

 <#if staff?has_content>
        <#-- REPORT TITLE -->
        <fo:block font-size="18pt" font-weight="bold" text-align="center">
            CHAI SACCO
        </fo:block>
        <fo:block font-size="12pt" text-align="center"  font-weight="bold" >
           ${staffTypeUpper} STAFF LISTING REPORT
        </fo:block>
        <fo:block><fo:leader/></fo:block>
        <#-- Employee Details -->

        <fo:block space-after.optimum="10pt" font-size="10pt">
            <fo:table table-layout="fixed" width="100%">
            
                <fo:table-column column-width="20pt"/>
                <fo:table-column column-width="60pt"/>
                <fo:table-column column-width="110pt"/>
                <fo:table-column column-width="90pt"/>
                <fo:table-column column-width="60pt"/>
                <fo:table-column column-width="100pt"/>
                <fo:table-column column-width="50pt"/>
               <#-- <fo:table-column column-width="60pt"/>  -->
                <fo:table-column column-width="70pt"/>
                
                <fo:table-header>
                   
                    <fo:table-row font-weight="bold">
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block></fo:block>
                        </fo:table-cell>
                          <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block>Payroll No</fo:block>
                        </fo:table-cell>
                        
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block>Name</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block>Branch</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block>Department</fo:block>
                        </fo:table-cell>

                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block>Designation</fo:block>
                        </fo:table-cell>

                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block>Grade</fo:block>
                        </fo:table-cell>
                      <#--  <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block>Type Of Staff</fo:block>
                        </fo:table-cell>  -->
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block>Employment Date</fo:block>
                        </fo:table-cell>

                    </fo:table-row>
                   
                 </fo:table-header>
                <fo:table-body>
                <#assign count=0>
                    <#list staff as employee>
                            
                        <fo:table-row>
                        <#assign count = count + 1>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>${count}</fo:block>
                            </fo:table-cell>
                            
                             <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>${employee.employeeNumber?if_exists}</fo:block>
                            </fo:table-cell>
                            
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>${employee.firstName?if_exists}  ${employee.lastName?if_exists}</fo:block>
                            </fo:table-cell>
                            
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <#if employee.branchId?has_content>
                                 <#assign branch = delegator.findOne("PartyGroup", {"partyId",employee.branchId },false) /> 
                                <fo:block>${branch.groupName?if_exists}</fo:block>
                                <#else>
                                 <fo:block>Not Defined</fo:block>
                                </#if>
                            </fo:table-cell>
                            
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                               <#if employee.departmentId?has_content> 
                                   <#assign department = delegator.findOne("department", {"departmentId",employee.departmentId },false) /> 
                                <fo:block> ${department.departmentName?if_exists}</fo:block>
                               <#else>
                               <fo:block> </fo:block>
                                </#if>
                            </fo:table-cell>
                            
                             <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                               <#if employee.emplPositionTypeId?has_content> 
                                   <#assign position = delegator.findOne("EmplPositionType", {"emplPositionTypeId",employee.emplPositionTypeId },false) /> 
                                <fo:block> ${position.emplPositionType?if_exists}</fo:block>
                               <#else>
                               <fo:block> </fo:block>
                                </#if>
                            </fo:table-cell>
                            
                            
                             <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                               <#if employee.payGradeId?has_content>
                               <#assign payGrade = delegator.findOne("PayGrade", {"payGradeId",employee.payGradeId },false) /> 
                                <fo:block>${payGrade.payGradeName?if_exists}</fo:block>
                               <#else>
                               <fo:block></fo:block>
                                </#if>
                            </fo:table-cell>
                            
                           <#-- <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>${employee.employmentTerms?if_exists}</fo:block>
                            </fo:table-cell> -->
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>${employee.appointmentdate?if_exists}</fo:block>
                            </fo:table-cell>
                            
                        </fo:table-row>
                    </#list>
                </fo:table-body>
            </fo:table>
        </fo:block>

    <#else>
        <fo:block text-align="center"> </fo:block>
    </#if>

        <#if employeeList?has_content>
        <#-- REPORT TITLE -->
        <fo:block font-size="18pt" font-weight="bold" text-align="center">
            CHAI SACCO
        </fo:block>
        <fo:block font-size="12pt" text-align="center"  font-weight="bold" >
            STAFF LISTING REPORT
        </fo:block>
        <fo:block><fo:leader/></fo:block>
        <#-- Employee Details -->

        <fo:block space-after.optimum="10pt" font-size="10pt">
            <fo:table table-layout="fixed" width="100%">
                
                <fo:table-column column-width="20pt"/>
                <fo:table-column column-width="60pt"/>
                <fo:table-column column-width="110pt"/>
                <fo:table-column column-width="90pt"/>
                <fo:table-column column-width="60pt"/>
                <fo:table-column column-width="100pt"/>
                <fo:table-column column-width="50pt"/>
               <#-- <fo:table-column column-width="60pt"/>  -->
                <fo:table-column column-width="70pt"/>
                
                
                <fo:table-header>
                    <fo:table-row font-weight="bold">
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block></fo:block>
                        </fo:table-cell>
                          <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block>Payroll No</fo:block>
                        </fo:table-cell>
                        
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block>Name</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block>Branch</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block>Department</fo:block>
                        </fo:table-cell>

                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block>Designation</fo:block>
                        </fo:table-cell>

                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block>Grade</fo:block>
                        </fo:table-cell>
                      <#--  <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block>Type Of Staff</fo:block>
                        </fo:table-cell>  -->
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block>Employment Date</fo:block>
                        </fo:table-cell>

                    </fo:table-row>
                </fo:table-header>
                <fo:table-body>
                <#assign count=0>
                    <#list employeeList as employee>
         
                        <fo:table-row>
                        <#assign count = count + 1>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>${count}</fo:block>
                            </fo:table-cell>
                            
                             <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>${employee.employeeNumber?if_exists}</fo:block>
                            </fo:table-cell>
                            
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>${employee.firstName?if_exists}  ${employee.lastName?if_exists}</fo:block>
                            </fo:table-cell>
                            
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <#if employee.branchId?has_content>
                                 <#assign branch = delegator.findOne("PartyGroup", {"partyId",employee.branchId },false) /> 
                                <fo:block>${branch.groupName?if_exists}</fo:block>
                                <#else>
                                 <fo:block>Not Defined</fo:block>
                                </#if>
                            </fo:table-cell>
                            
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                               <#if employee.departmentId?has_content> 
                                   <#assign department = delegator.findOne("department", {"departmentId",employee.departmentId },false) /> 
                                <fo:block> ${department.departmentName?if_exists}</fo:block>
                               <#else>
                               <fo:block> </fo:block>
                                </#if>
                            </fo:table-cell>
                            
                             <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                               <#if employee.emplPositionTypeId?has_content> 
                                   <#assign position = delegator.findOne("EmplPositionType", {"emplPositionTypeId",employee.emplPositionTypeId },false) /> 
                                <fo:block> ${position.emplPositionType?if_exists}</fo:block>
                               <#else>
                               <fo:block> </fo:block>
                                </#if>
                            </fo:table-cell>
                            
                            
                             <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                               <#if employee.payGradeId?has_content>
                               <#assign payGrade = delegator.findOne("PayGrade", {"payGradeId",employee.payGradeId },false) /> 
                                <fo:block>${payGrade.payGradeName?if_exists}</fo:block>
                               <#else>
                               <fo:block></fo:block>
                                </#if>
                            </fo:table-cell>
                            
                           <#-- <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>${employee.employmentTerms?if_exists}</fo:block>
                            </fo:table-cell> -->
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>${employee.appointmentdate?if_exists}</fo:block>
                            </fo:table-cell>
                            
                        </fo:table-row>
                    </#list>
                </fo:table-body>
            </fo:table>
        </fo:block>

    <#else>
        <fo:block text-align="center"> </fo:block>
    </#if>


   
</#escape>

