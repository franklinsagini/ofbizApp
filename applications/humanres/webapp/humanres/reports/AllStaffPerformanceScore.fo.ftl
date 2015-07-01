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
    <#if allStaffUnderReview?has_content>

    <#-- REPORT TITLE -->
    <fo:block font-size="18pt" font-weight="bold" text-align="center">
        CHAI SACCO
    </fo:block>
    <fo:block font-size="12pt" text-align="center"  font-weight="bold" >
        STAFF PERFORMANCE SCORES REPORT FOR THE YEAR ${year}
    </fo:block>
    <fo:block><fo:leader/></fo:block>
    <#-- Employee Details -->
<#if scorelist?has_content>
    <#-- REPORT BODY -->
    <fo:block space-after.optimum="10pt" font-size="10pt">
        <fo:table table-layout="fixed" width="100%">
            <fo:table-column column-width="15pt"/>
            <fo:table-column column-width="100pt"/>
            <fo:table-column column-width="85pt"/>
            <fo:table-column column-width="85pt"/>
            <fo:table-column column-width="40pt"/>
            <fo:table-column column-width="40pt"/>
            <fo:table-column column-width="40pt"/>
            <fo:table-column column-width="40pt"/>
            <fo:table-column column-width="40pt"/>
            <fo:table-column column-width="40pt"/>
            <fo:table-column column-width="50pt"/>
            <fo:table-header>
                <fo:table-row font-weight="bold">
                    <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                        <fo:block></fo:block>
                    </fo:table-cell>
                    <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                        <fo:block>Staff Name</fo:block>
                    </fo:table-cell>
                    <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                        <fo:block>Branch</fo:block>
                    </fo:table-cell>
                    <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                        <fo:block>Department</fo:block>
                    </fo:table-cell>
                    <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                        <fo:block>First Quarter Score</fo:block>
                    </fo:table-cell>
                     <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                        <fo:block>Second Quarter Score</fo:block>
                    </fo:table-cell>
                     <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                        <fo:block>Third Quarter Score</fo:block>
                    </fo:table-cell>
                     <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                        <fo:block>Fourth Quarter Score</fo:block>
                    </fo:table-cell>
                    <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                        <fo:block>Total Year Score</fo:block>
                    </fo:table-cell>
                     <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                        <fo:block>Bonus (% of bonus of basic salary)</fo:block>
                    </fo:table-cell>
                    <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                        <fo:block>Salary Increment (% of basic salary)</fo:block>
                    </fo:table-cell>
                </fo:table-row>
            </fo:table-header>
            <fo:table-body>
            <#assign count=0>
                  <#list scorelist as score>
                     <fo:table-row>
                       <#assign count = count + 1>
                      <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <fo:block>${count}</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <fo:block>${score.name?if_exists}</fo:block>
                        </fo:table-cell>
                         <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <fo:block>${score.branch?if_exists}</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <fo:block>${score.department?if_exists}</fo:block>
                        </fo:table-cell>
                         <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <fo:block>${score.Q1?if_exists}</fo:block>
                        </fo:table-cell>
                         <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <fo:block>${score.Q2?if_exists}</fo:block>
                        </fo:table-cell>
                         <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <fo:block>${score.Q3?if_exists}</fo:block>
                        </fo:table-cell>
                         <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <fo:block>${score.Q4?if_exists}</fo:block>
                        </fo:table-cell>
                         <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <fo:block>${score.Total?if_exists}</fo:block>
                        </fo:table-cell>
                         <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <fo:block>${score.bonus?if_exists}</fo:block>
                        </fo:table-cell>
                         <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <fo:block>${score.increment?if_exists}</fo:block>
                        </fo:table-cell>
                     </fo:table-row>
                  </#list>
            </fo:table-body>
        </fo:table>
    </fo:block>
    <#else>
     <fo:block space-after.optimum="10pt" >
        <fo:block text-align="center" font-size="14pt">No Scores Were found</fo:block>
    </fo:block>
  </#if>
    <#else>
        <fo:block text-align="center">Nothing to show</fo:block>
    </#if>
</#escape>

