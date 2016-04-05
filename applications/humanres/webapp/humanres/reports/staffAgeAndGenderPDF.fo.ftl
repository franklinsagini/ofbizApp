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

 <#if employee_listing?has_content>
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
                <fo:table-column column-width="90pt"/>
                <fo:table-column column-width="140pt"/>
                <fo:table-column column-width="100pt"/>
                <fo:table-column column-width="50pt"/>
                <fo:table-column column-width="60pt"/>  
                <fo:table-column column-width="70pt"/>
                
                
                <fo:table-header>
                    <fo:table-row font-weight="bold">
                        <fo:table-cell padding="2pt" background-color="#FB4924" border="1pt solid" border-width=".1mm">
                            <fo:block></fo:block>
                        </fo:table-cell>
                          <fo:table-cell padding="2pt" background-color="#FB4924" border="1pt solid" border-width=".1mm">
                            <fo:block>Payroll No</fo:block>
                        </fo:table-cell>
                        
                        <fo:table-cell padding="2pt" background-color="#FB4924" border="1pt solid" border-width=".1mm">
                            <fo:block>Name</fo:block>
                        </fo:table-cell>
                       
                        <fo:table-cell padding="2pt" background-color="#FB4924" border="1pt solid" border-width=".1mm">
                            <fo:block>Date Of Birth</fo:block>
                        </fo:table-cell>

                        <fo:table-cell padding="2pt" background-color="#FB4924" border="1pt solid" border-width=".1mm">
                            <fo:block>Year Of Birth</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#FB4924" border="1pt solid" border-width=".1mm">
                            <fo:block>Present Age</fo:block>
                        </fo:table-cell>  
                        <fo:table-cell padding="2pt" background-color="#FB4924" border="1pt solid" border-width=".1mm">
                            <fo:block>Gender</fo:block>
                        </fo:table-cell>

                    </fo:table-row>
                </fo:table-header>
                <fo:table-body>
                <#assign count=0>
                    <#list employee_listing as employee>
         
                        <fo:table-row>
                        <#assign count = count + 1>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block >${count}</fo:block>
                            </fo:table-cell>
                            
                             <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>${employee.employeeNumber?if_exists}</fo:block>
                            </fo:table-cell>
                            
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>${employee.name?if_exists}</fo:block>
                            </fo:table-cell>
                            
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>${employee.birthDate?if_exists}</fo:block>
                            </fo:table-cell>
                            
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>${employee.yearOfBirth?if_exists}</fo:block>
                            </fo:table-cell>
                            
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>${employee.presentYears?if_exists}</fo:block>
                            </fo:table-cell> 
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>${employee.gender?if_exists}</fo:block>
                            </fo:table-cell>
                            
                        </fo:table-row>
                    </#list>
                </fo:table-body>
            </fo:table>
        </fo:block>

    <#else>
        <fo:block text-align="center"> </fo:block>
    </#if>
    
 
  <#if employee_dob?has_content>
        <#-- REPORT TITLE -->
        <fo:block font-size="18pt" font-weight="bold" text-align="center">
            CHAI SACCO
        </fo:block>
        <fo:block font-size="12pt" text-align="center"  font-weight="bold" >
            STAFF DATE OF BIRTH LISTING REPORT
        </fo:block>
        <fo:block><fo:leader/></fo:block>
        <#-- Employee Details -->

        <fo:block space-after.optimum="10pt" font-size="10pt">
            <fo:table table-layout="fixed" width="100%">
                
                <fo:table-column column-width="20pt"/>
                <fo:table-column column-width="140pt"/>
                <fo:table-column column-width="190pt"/>
                <fo:table-column column-width="150pt"/>
               
                <fo:table-header>
                    <fo:table-row font-weight="bold">
                        <fo:table-cell padding="2pt" background-color="#FB4924" border="1pt solid" border-width=".1mm">
                            <fo:block></fo:block>
                        </fo:table-cell>
                          <fo:table-cell padding="2pt" background-color="#FB4924" border="1pt solid" border-width=".1mm">
                            <fo:block>Payroll No</fo:block>
                        </fo:table-cell>
                        
                        <fo:table-cell padding="2pt" background-color="#FB4924" border="1pt solid" border-width=".1mm">
                            <fo:block>Name</fo:block>
                        </fo:table-cell>
                       
                        <fo:table-cell padding="2pt" background-color="#FB4924" border="1pt solid" border-width=".1mm">
                            <fo:block>Date Of Birth</fo:block>
                        </fo:table-cell>

                    </fo:table-row>
                </fo:table-header>
                <fo:table-body>
                <#assign count=0>
                    <#list employee_dob as employee>
         
                        <fo:table-row>
                        <#assign count = count + 1>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block >${count}</fo:block>
                            </fo:table-cell>
                            
                             <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>${employee.employeeNumber?if_exists}</fo:block>
                            </fo:table-cell>
                            
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>${employee.name?if_exists}</fo:block>
                            </fo:table-cell>
                            
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>${employee.birthDate?if_exists}</fo:block>
                            </fo:table-cell>

                        </fo:table-row>
                    </#list>
                </fo:table-body>
            </fo:table>
        </fo:block>

    <#else>
        <fo:block text-align="center"> </fo:block>
    </#if>





 <#if employee_age?has_content>
        <#-- REPORT TITLE -->
        <fo:block font-size="18pt" font-weight="bold" text-align="center">
            CHAI SACCO
        </fo:block>
        <fo:block font-size="12pt" text-align="center"  font-weight="bold" >
            STAFF AGE LISTING REPORT
        </fo:block>
        <fo:block><fo:leader/></fo:block>
        <#-- Employee Details -->

        <fo:block space-after.optimum="10pt" font-size="10pt">
            <fo:table table-layout="fixed" width="100%">
                
               <fo:table-column column-width="50pt"/>
                <fo:table-column column-width="160pt"/>
                <fo:table-column column-width="190pt"/>
                <fo:table-column column-width="150pt"/>  
               
                
                
                <fo:table-header>
                    <fo:table-row font-weight="bold">
                        <fo:table-cell padding="2pt" background-color="#FB4924" border="1pt solid" border-width=".1mm">
                            <fo:block></fo:block>
                        </fo:table-cell>
                          <fo:table-cell padding="2pt" background-color="#FB4924" border="1pt solid" border-width=".1mm">
                            <fo:block>Payroll No</fo:block>
                        </fo:table-cell>
                        
                        <fo:table-cell padding="2pt" background-color="#FB4924" border="1pt solid" border-width=".1mm">
                            <fo:block>Name</fo:block>
                        </fo:table-cell>
                       
                       
                        <fo:table-cell padding="2pt" background-color="#FB4924" border="1pt solid" border-width=".1mm">
                            <fo:block>Present Age</fo:block>
                        </fo:table-cell>  
                        

                    </fo:table-row>
                </fo:table-header>
                <fo:table-body>
                <#assign count=0>
                    <#list employee_age as employee>
         
                        <fo:table-row>
                        <#assign count = count + 1>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block >${count}</fo:block>
                            </fo:table-cell>
                            
                             <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>${employee.employeeNumber?if_exists}</fo:block>
                            </fo:table-cell>
                            
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>${employee.name?if_exists}</fo:block>
                            </fo:table-cell>
                            
                            
                            
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>${employee.presentYears?if_exists}</fo:block>
                            </fo:table-cell> 
                           
                            
                        </fo:table-row>
                    </#list>
                </fo:table-body>
            </fo:table>
        </fo:block>

    <#else>
        <fo:block text-align="center"> </fo:block>
    </#if>
    



<#if employee_gender?has_content>
        <#-- REPORT TITLE -->
        <fo:block font-size="18pt" font-weight="bold" text-align="center">
            CHAI SACCO
        </fo:block>
        <fo:block font-size="12pt" text-align="center"  font-weight="bold" >
            STAFF GENDER LISTING REPORT
        </fo:block>
        <fo:block><fo:leader/></fo:block>
        <#-- Employee Details -->

        <fo:block space-after.optimum="10pt" font-size="10pt">
            <fo:table table-layout="fixed" width="100%">
                
               <fo:table-column column-width="50pt"/>
                <fo:table-column column-width="160pt"/>
                <fo:table-column column-width="190pt"/>
                <fo:table-column column-width="150pt"/>
                
                
                <fo:table-header>
                    <fo:table-row font-weight="bold">
                        <fo:table-cell padding="2pt" background-color="#FB4924" border="1pt solid" border-width=".1mm">
                            <fo:block></fo:block>
                        </fo:table-cell>
                          <fo:table-cell padding="2pt" background-color="#FB4924" border="1pt solid" border-width=".1mm">
                            <fo:block>Payroll No</fo:block>
                        </fo:table-cell>
                        
                        <fo:table-cell padding="2pt" background-color="#FB4924" border="1pt solid" border-width=".1mm">
                            <fo:block>Name</fo:block>
                        </fo:table-cell>
                         
                        <fo:table-cell padding="2pt" background-color="#FB4924" border="1pt solid" border-width=".1mm">
                            <fo:block>Gender</fo:block>
                        </fo:table-cell>

                    </fo:table-row>
                </fo:table-header>
                <fo:table-body>
                <#assign count=0>
                    <#list employee_gender as employee>
         
                        <fo:table-row>
                        <#assign count = count + 1>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block >${count}</fo:block>
                            </fo:table-cell>
                            
                             <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>${employee.employeeNumber?if_exists}</fo:block>
                            </fo:table-cell>
                            
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>${employee.name?if_exists}</fo:block>
                            </fo:table-cell>
                            
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>${employee.gender?if_exists}</fo:block>
                            </fo:table-cell>
                            
                        </fo:table-row>
                    </#list>
                </fo:table-body>
            </fo:table>
        </fo:block>

    <#else>
        <fo:block text-align="center"> </fo:block>
    </#if>
    




<#if employee_comparison?has_content>
        <#-- REPORT TITLE -->
        <fo:block font-size="18pt" font-weight="bold" text-align="center">
            CHAI SACCO
        </fo:block>
        <fo:block font-size="12pt" text-align="center"  font-weight="bold" >
            STAFF AGE COMPARISON  REPORT
        </fo:block>
        <fo:block><fo:leader/></fo:block>
        <#-- Employee Details -->

        <fo:block space-after.optimum="10pt" font-size="10pt">
            <fo:table table-layout="fixed" width="100%">
                
                <fo:table-column column-width="20pt"/>
                <fo:table-column column-width="60pt"/>
                <fo:table-column column-width="140pt"/>
                <fo:table-column column-width="100pt"/>
                <fo:table-column column-width="50pt"/>
                <fo:table-column column-width="60pt"/>  
                <fo:table-column column-width="50pt"/>
                <fo:table-column column-width="50pt"/>
                
                
                <fo:table-header>
                    <fo:table-row font-weight="bold">
                        <fo:table-cell padding="2pt" background-color="#FB4924" border="1pt solid" border-width=".1mm">
                            <fo:block></fo:block>
                        </fo:table-cell>
                          <fo:table-cell padding="2pt" background-color="#FB4924" border="1pt solid" border-width=".1mm">
                            <fo:block>Payroll No</fo:block>
                        </fo:table-cell>
                        
                        <fo:table-cell padding="2pt" background-color="#FB4924" border="1pt solid" border-width=".1mm">
                            <fo:block>Name</fo:block>
                        </fo:table-cell>
                       
                        <fo:table-cell padding="2pt" background-color="#FB4924" border="1pt solid" border-width=".1mm">
                            <fo:block>Date Of Birth</fo:block>
                        </fo:table-cell>

                        <fo:table-cell padding="2pt" background-color="#FB4924" border="1pt solid" border-width=".1mm">
                            <fo:block>Year Of Birth</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#FB4924" border="1pt solid" border-width=".1mm">
                            <fo:block>Present Age</fo:block>
                        </fo:table-cell>  
                        
                        <fo:table-cell padding="2pt" background-color="#FB4924" border="1pt solid" border-width=".1mm">
                            <fo:block>Age in 5 Years</fo:block>
                        </fo:table-cell>
                        
                         <fo:table-cell padding="2pt" background-color="#FB4924" border="1pt solid" border-width=".1mm">
                            <fo:block>Age in 10 Years</fo:block>
                        </fo:table-cell>

                    </fo:table-row>
                </fo:table-header>
                <fo:table-body>
                <#assign count=0>
                    <#list employee_comparison as employee>
         
                        <fo:table-row>
                        <#assign count = count + 1>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block >${count}</fo:block>
                            </fo:table-cell>
                            
                             <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>${employee.employeeNumber?if_exists}</fo:block>
                            </fo:table-cell>
                            
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>${employee.name?if_exists}</fo:block>
                            </fo:table-cell>
                            
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>${employee.birthDate?if_exists}</fo:block>
                            </fo:table-cell>
                            
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>${employee.yearOfBirth?if_exists}</fo:block>
                            </fo:table-cell>
                            
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>${employee.presentYears?if_exists}</fo:block>
                            </fo:table-cell> 
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>${employee.ageInFiveYears?if_exists}</fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>${employee.ageInTenYears?if_exists}</fo:block>
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

