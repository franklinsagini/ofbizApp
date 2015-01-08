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
    <#if memAccount?has_content>
      <#if memmm.stationId?has_content>
        <#assign stationId = memmm.stationId>
            <#assign station = delegator.findOne("Station", {"stationId" : stationId.toString()}, false)/>
        </#if>

    <#-- REPORT TITLE -->
    <fo:block font-size="18pt" font-weight="bold" text-align="center">
        CHAI SACCO
    </fo:block>
    <fo:block font-size="12pt" text-align="center"  font-weight="bold" >
        MEMBER ACCOUNTS DETAILS REPORT
          
    </fo:block>
   
    <fo:block><fo:leader/></fo:block>
    <fo:block font-size="12pt" text-align="center" font-weight="bold" space-after="0.04in" text-decoration="underline">
        Member Name: ${memmm.firstName} ${memmm.lastName}
    </fo:block>
     <fo:block font-size="12pt" text-align="center" font-weight="bold" space-after="0.04in" text-decoration="underline">
        Member Number: ${memmm.memberNumber}
    </fo:block>
    <fo:block font-size="12pt" text-align="center" font-weight="bold" text-decoration="underline" margin-bottom="0.2in">
        Member Station:${station.stationNumber?if_exists}- ${station.name?if_exists}
    </fo:block>


    <#-- REPORT BODY -->
    <fo:block space-after.optimum="10pt" font-size="10pt">
        <fo:table table-layout="fixed" width="100%">
            <fo:table-column column-width="200pt"/>
            <fo:table-column column-width="200pt"/>
            <fo:table-column column-width="200pt"/>
            <fo:table-header>
                <fo:table-row font-weight="bold">
                    <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                        <fo:block text-align="left">Account Type</fo:block>
                    </fo:table-cell>
                     <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                        <fo:block text-align="left">Account Number</fo:block>
                    </fo:table-cell>
                    <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                        <fo:block text-align="left">Account Balance</fo:block>
                    </fo:table-cell>
                  
                </fo:table-row>
            </fo:table-header>
            <fo:table-body>

                  <#list memAccount as activity>
                     <#if activity.memberAccountId?has_content>
                       <#assign memberAccId = activity.memberAccountId?number/>
                      <#assign acc = delegator.findOne("MemberAccount", {"memberAccountId" : memberAccId?long}, false)/>
                    </#if>
                     <#if acc.accountProductId?has_content>
                      <#assign accproduct = delegator.findOne("AccountProduct", {"accountProductId" : acc.accountProductId}, false)/>
                    </#if>

                     <fo:table-row>
                         <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <#if acc?has_content>
                                <fo:block>${accproduct.name?if_exists}</fo:block>
                            <#else>
                                <fo:block>Not Defined</fo:block>
                            </#if>
                        </fo:table-cell>
                        
                         <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <#if acc?has_content>
                                <fo:block>${acc.accountNo?if_exists}</fo:block>
                            <#else>
                                <fo:block>Not Defined</fo:block>
                            </#if>
                        </fo:table-cell>
                       
                        <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <#if activity?has_content>
                                <fo:block>${activity.savingsOpeningBalance?if_exists}</fo:block>
                            <#else>
                                <fo:block>Not Defined</fo:block>
                            </#if>
                        </fo:table-cell>
                        
                     </fo:table-row>
                  </#list>

            </fo:table-body>
        </fo:table>
    </fo:block>
</#if>

</#escape>

