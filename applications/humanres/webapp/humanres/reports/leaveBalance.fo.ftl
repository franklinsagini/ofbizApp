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
    <#if employee?has_content>

    <#-- REPORT TITLE -->
    <fo:block font-size="18pt" font-weight="bold" text-align="center">
        CHAI SACCO
    </fo:block>
    <fo:block font-size="12pt" text-align="center"  font-weight="bold" >
        EMPLOYEE LEAVE BALANCE REPORT
    </fo:block>
    <fo:block><fo:leader/></fo:block>
    <#-- Employee Details -->
    <fo:block font-size="10pt" text-align="left" font-weight="bold" space-after="0.04in">
        Payroll Number: ${employee.employeeNumber}
    </fo:block>
    <fo:block font-size="10pt" text-align="left" font-weight="bold">
        Employee Name: ${employee.firstName} ${employee.lastName}
    </fo:block>
<
 <fo:block>
    <fo:list-block provisional-distance-between-starts="1in" text-align="center">
        <fo:list-item>
            <fo:list-item-label>
                <fo:block font-weight="bold">${uiLabelMap.CommonUsername}</fo:block>
            </fo:list-item-label>
            <fo:list-item-body start-indent="body-start()">
                <fo:block><#if userLogin?exists>${userLogin.userLoginId?if_exists}</#if></fo:block>
            </fo:list-item-body>
        </fo:list-item>
        <fo:list-item>
            <fo:list-item-label>
                <fo:block font-weight="bold">${uiLabelMap.CommonDate}</fo:block>
            </fo:list-item-label>
            <fo:list-item-body start-indent="body-start()">
                <fo:block>${nowTimestamp?if_exists}</fo:block>
            </fo:list-item-body>
        </fo:list-item>
    </fo:list-block>
 </fo:block>


    <#else>
        <fo:block text-align="center">No Employees Found With that ID</fo:block>
    </#if>
    <#if employee?has_content>

    </#if>
</#escape>

