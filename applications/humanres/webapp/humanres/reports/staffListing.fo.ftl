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
                <fo:table-column column-width="50pt"/>
                <fo:table-column column-width="90pt"/>
                <fo:table-column column-width="90pt"/>
                <fo:table-column column-width="70pt"/>
                <fo:table-column column-width="90pt"/>
                <fo:table-column column-width="60pt"/>
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
                    <#list staff as employe>
                             <#if employe.partyId?has_content>
                                <#assign   branchPerson = delegator.findOne("Person", {"partyId": employe.partyId},false) />
                                <#assign brachSearch = branchPerson.branchId/>
                                <#assign deptSearch = branchPerson.departmentId/>
                                <#assign jopPositionSearch = branchPerson.emplPositionTypeId/>
                                <#assign payGradeSearch = branchPerson.payGradeId/>
                                <#assign religionSearch = branchPerson.religionId/>
                                
                             </#if>
                             <#if brachSearch?has_content>
                                <#assign  branchP = delegator.findOne("PartyGroup", {"partyId": brachSearch},false) />
                             </#if>
                            
                             <#if deptSearch?has_content>
                                <#assign  deptPerson = delegator.findOne("department", {"departmentId": deptSearch},false) />
                             </#if>
                             
                              <#if jopPositionSearch?has_content>
                                <#assign  jopPerson = delegator.findOne("EmplPositionType", {"emplPositionTypeId": jopPositionSearch},false) />
                             </#if>
                             
                              <#if payGradeSearch?has_content>
                                <#assign  gradePerson = delegator.findOne("PayGrade", {"payGradeId": payGradeSearch},false) />
                             </#if>
                             
                              <#if religionSearch?has_content>
                                <#assign  religionPerson = delegator.findOne("Religion", {"religionId": religionSearch},false) />
                             </#if>
                            
                        <fo:table-row>
                        <#assign count = count + 1>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>${count}</fo:block>
                            </fo:table-cell>
                             <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>${employe.employeeNumber?if_exists}</fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>${employe.firstName?if_exists}  ${employe.lastName?if_exists}</fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <#if branchP?has_content>
                                <fo:block>${branchP.groupName? if_exists}</fo:block>
                                <#else>
                                 <fo:block>Not Defined</fo:block>
                                </#if>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                               <#if deptPerson?has_content> 
                                <fo:block>${deptPerson.departmentName?if_exists}</fo:block>
                               <#else>
                               <fo:block></fo:block>
                                </#if>
                            </fo:table-cell>
                            
                             <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                               <#if jopPerson?has_content> 
                                <fo:block>${jopPerson.emplPositionType?if_exists}</fo:block>
                               <#else>
                               <fo:block></fo:block>
                                </#if>
                            </fo:table-cell>
                            
                             <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                               <#if gradePerson?has_content> 
                                <fo:block>${gradePerson.payGradeName?if_exists}</fo:block>
                               <#else>
                               <fo:block></fo:block>
                                </#if>
                            </fo:table-cell>
                            
                           <#-- <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>${employe.employmentTerms?if_exists}</fo:block>
                            </fo:table-cell> -->
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>${employe.appointmentdate?if_exists}</fo:block>
                            </fo:table-cell>
                            
                            
                        </fo:table-row>
                    </#list>
                </fo:table-body>
            </fo:table>
        </fo:block>

    <#else>
        <fo:block text-align="center">NO DATA FOUND</fo:block>
    </#if>




<#else>
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
                <fo:table-column column-width="50pt"/>
                <fo:table-column column-width="90pt"/>
                <fo:table-column column-width="90pt"/>
                <fo:table-column column-width="70pt"/>
                <fo:table-column column-width="90pt"/>
                <fo:table-column column-width="60pt"/>
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
                    <#if employee.partyId?has_content>
                                <#assign branchPersonR = delegator.findOne("Person", {"partyId": employee.partyId},false) />
                                <#assign brachSearchR = branchPersonR.branchId/>
                                <#assign deptSearchR = branchPersonR.departmentId/>
                                <#assign jopPositionSearchR = branchPersonR.emplPositionTypeId/>
                                <#assign payGradeSearchR = branchPersonR.payGradeId/>
                                <#assign religionSearchR = branchPersonR.religionId/>
                                
		                             <#if brachSearchR?has_content>
		                                 <#assign  branchPR = delegator.findOne("PartyGroup", {"partyId": brachSearchR},false) />
		                                 <#else>
		                              </#if>
		                            
		                             <#if deptSearchR?has_content>
		                                <#assign  deptPersonR = delegator.findOne("department", {"departmentId": deptSearchR},false) />
		                                <#else>
		                             </#if>
		                             
		                              <#if jopPositionSearchR?has_content>
		                                <#assign  jopPersonR = delegator.findOne("EmplPositionType", {"emplPositionTypeId": jopPositionSearchR},false) />
		                                <#else>
		                             </#if>
		                             
		                              <#if payGradeSearchR?has_content>
		                                <#assign  gradePersonR = delegator.findOne("PayGrade", {"payGradeId": payGradeSearchR},false) />
		                                <#else>
		                             </#if>
		                             
		                              <#if religionSearch?has_content>
		                                <#assign  religionPersonR = delegator.findOne("Religion", {"religionId": religionSearchR},false) />
		                                <#else>
		                             </#if>
                             </#if>
                             
                             
                            
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
                                <#if branchPR?has_content>
                                <fo:block>${branchPR.groupName? if_exists}</fo:block>
                                <#else>
                                 <fo:block>Not Defined</fo:block>
                                </#if>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                               <#if deptPersonR?has_content> 
                                <fo:block> ${deptPersonR.departmentName?if_exists}</fo:block>
                               <#else>
                               <fo:block> </fo:block>
                                </#if>
                            </fo:table-cell>
                            
                             <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                               <#if jopPersonR?has_content> 
                                <fo:block>${jopPersonR.emplPositionType?if_exists}</fo:block>
                               <#else>
                               <fo:block></fo:block>
                                </#if>
                            </fo:table-cell>
                            
                             <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                               <#if gradePersonR?has_content> 
                                <fo:block>${gradePersonR.payGradeName?if_exists}</fo:block>
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
        <fo:block text-align="center">NO DATA FOUND</fo:block>
    </#if>
</#if>




   
</#escape>

