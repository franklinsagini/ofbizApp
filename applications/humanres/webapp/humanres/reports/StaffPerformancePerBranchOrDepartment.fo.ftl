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
    <#if ReviewGroup1?has_content>

    <#-- REPORT TITLE -->
    <fo:block font-size="18pt" font-weight="bold" text-align="center">
        CHAI SACCO
    </fo:block>
    
    <#-- BANG !!! -->
<#list listIndividualGoalScore as individual >

	<#assign partyId = individual.partyId />
	<#assign person = delegator.findOne("Person", Static["org.ofbiz.base.util.UtilMisc"].toMap("partyId", partyId), true)/>
    <fo:block font-size="12pt" text-align="center"  font-weight="bold" text-decoration="underline">
        ${person.firstName} ${person.lastName}'s PERFORMANCE REPORT FOR THE YEAR ${year}
    </fo:block>
    <fo:block><fo:leader/></fo:block>

    <fo:block font-size="12pt" text-align="center"  font-weight="bold" text-decoration="underline">
        Quantitative Goals Scores
    </fo:block>
    <#-- Employee Details -->
<#if individual.listQuantitativeGoalScore?has_content>
    <#-- REPORT BODY -->
    <fo:block space-after.optimum="10pt" font-size="10pt">
        <fo:table table-layout="fixed" width="100%">
            <fo:table-column column-width="20pt"/>
            <fo:table-column column-width="85pt"/>
            <fo:table-column column-width="80pt"/>
            <fo:table-column column-width="80pt"/>
            <fo:table-column column-width="80pt"/>
            <fo:table-column column-width="38pt"/>
            <fo:table-column column-width="38pt"/>
            <fo:table-column column-width="38pt"/>
            <fo:table-column column-width="38pt"/>
            <fo:table-column column-width="38pt"/>
            <fo:table-column column-width="38pt"/>
            <fo:table-header>
                <fo:table-row font-weight="bold">
                    <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                        <fo:block></fo:block>
                    </fo:table-cell>
                    <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                        <fo:block>Goal Perspective</fo:block>
                    </fo:table-cell>
                    <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                        <fo:block>Strategic Objective</fo:block>
                    </fo:table-cell>
                    <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                        <fo:block>Action Plan</fo:block>
                    </fo:table-cell>
                    <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                        <fo:block>Indicator</fo:block>
                    </fo:table-cell>
                     <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                        <fo:block>Max Score</fo:block>
                    </fo:table-cell>
                     <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                        <fo:block>Quarter One</fo:block>
                    </fo:table-cell>
                     <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                        <fo:block>Quarter two</fo:block>
                    </fo:table-cell>
                    <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                        <fo:block>Quarter three</fo:block>
                    </fo:table-cell>
                    <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                        <fo:block>Quarter four</fo:block>
                    </fo:table-cell>
                    <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                        <fo:block>Total Score</fo:block>
                    </fo:table-cell>
                </fo:table-row>
            </fo:table-header>
            <fo:table-body>
            <#assign count=0>
                  <#list individual.listQuantitativeGoalScore as score>
                     <fo:table-row>
                       <#assign count = count + 1>
                      <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <fo:block>${count}</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <fo:block>${score.name?if_exists}</fo:block>
                        </fo:table-cell>
                         <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <fo:block>${score.objectiveName?if_exists}</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <fo:block>${score.actionPlanName?if_exists}</fo:block>
                        </fo:table-cell>
                         <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <fo:block>${score.indicatorName?if_exists}</fo:block>
                        </fo:table-cell>
                         <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <fo:block text-align="right">${score.maxScore?if_exists} %</fo:block>
                        </fo:table-cell>
                         <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <fo:block text-align="right">${score.quarterOne?if_exists}</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <fo:block text-align="right">${score.quarterTwo?if_exists}</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <fo:block text-align="right">${score.quarterThree?if_exists}</fo:block>
                        </fo:table-cell>
                         <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <fo:block text-align="right">${score.quarterFour?if_exists}</fo:block>
                        </fo:table-cell>
                         <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <fo:block text-align="right">${score.total?if_exists}</fo:block>
                        </fo:table-cell>
                     </fo:table-row>
                  </#list>




                       <fo:table-row column-height="30mm" font-weight="bold">
                             <fo:table-cell padding="2pt" border="1pt solid" border-width="0mm" font-size="10pt">
                                <fo:block text-align="right">
                                </fo:block>
                            </fo:table-cell>
                             <fo:table-cell padding="2pt" border="1pt solid" border-width="0mm" font-size="10pt">
                                <fo:block text-align="right">
                                </fo:block>
                            </fo:table-cell>
                             <fo:table-cell padding="2pt" border="1pt solid" border-width="0mm" font-size="10pt">
                                <fo:block text-align="right">
                                </fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width="0mm" font-size="12pt">
                                <fo:block text-align="left" >
                                </fo:block>
                            </fo:table-cell>
                           <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm" font-size="10pt">
                                <fo:block text-align="right">Total Score
                                </fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm" font-size="10pt">
                                <fo:block text-align="right">${individual.quantitativeMaxScore}
                                </fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm" font-size="10pt">
                                <fo:block text-align="right">${individual.quantitativeQuarterOne}
                                </fo:block>
                            </fo:table-cell>
                             <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm" font-size="10pt">
                                <fo:block text-align="right">${individual.quantitativeQuarterTwo}
                                </fo:block>
                            </fo:table-cell>
                             <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm" font-size="10pt">
                                <fo:block text-align="right">${individual.quantitativeQuarterThree}
                                </fo:block>
                            </fo:table-cell>
                             <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm" font-size="10pt">
                                <fo:block text-align="right">${individual.quantitativeQuarterFour}
                                </fo:block>
                            </fo:table-cell>
                             <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm" font-size="10pt">
                                <fo:block text-align="right">${individual.quantitativeTotal}
                                </fo:block>
                            </fo:table-cell>
                            
                        </fo:table-row>






            </fo:table-body>
        </fo:table>
    </fo:block>


  <fo:block font-size="12pt" text-align="center"  font-weight="bold" text-decoration="underline">
        Qualitative Goals Scores
    </fo:block>


    <#if individual.listQualitativeGoalScore?has_content>
    <#-- REPORT BODY -->
    <fo:block space-after.optimum="10pt" font-size="10pt">
        <fo:table table-layout="fixed" width="100%">
            <fo:table-column column-width="22pt"/>
            <fo:table-column column-width="250pt"/>
            <fo:table-column column-width="50pt"/>
            <fo:table-column column-width="50pt"/>
            <fo:table-column column-width="50pt"/>
            <fo:table-column column-width="50pt"/>
            <fo:table-column column-width="50pt"/>
            <fo:table-column column-width="50pt"/>
            <fo:table-header>
                <fo:table-row font-weight="bold">
                    <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                        <fo:block></fo:block>
                    </fo:table-cell>
                    <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                        <fo:block>Goal Indicator</fo:block>
                    </fo:table-cell>
                     <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                        <fo:block text-align="right">Max Score</fo:block>
                    </fo:table-cell>
                     <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                        <fo:block text-align="right">Quarter One</fo:block>
                    </fo:table-cell>
                     <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                        <fo:block text-align="right">Quarter two</fo:block>
                    </fo:table-cell>
                    <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                        <fo:block text-align="right">Quarter three</fo:block>
                    </fo:table-cell>
                    <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                        <fo:block text-align="right">Quarter four</fo:block>
                    </fo:table-cell>
                    <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                        <fo:block text-align="right">Total Score</fo:block>
                    </fo:table-cell>
                </fo:table-row>
            </fo:table-header>
            <fo:table-body>
            <#assign count=0>
                  <#list individual.listQualitativeGoalScore as score2>
                     <fo:table-row>
                       <#assign count = count + 1>
                      <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <fo:block>${count}</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <fo:block>${score2.name?if_exists}</fo:block>
                        </fo:table-cell>
                         <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <fo:block text-align="right">${score2.maxScore?if_exists} %</fo:block>
                        </fo:table-cell>
                         <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <fo:block text-align="right">${score2.quarterOne?if_exists}</fo:block>
                        </fo:table-cell>
                         <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <fo:block text-align="right">${score2.quarterTwo?if_exists}</fo:block>
                        </fo:table-cell>
                         <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <fo:block text-align="right">${score2.quarterThree?if_exists}</fo:block>
                        </fo:table-cell>
                         <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <fo:block text-align="right">${score2.quarterFour?if_exists}</fo:block>
                        </fo:table-cell>
                         <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <fo:block text-align="right">${score2.total?if_exists}</fo:block>
                        </fo:table-cell>
                     </fo:table-row>
                  </#list>

                  <fo:table-row column-height="30mm" font-weight="bold">
                            <fo:table-cell padding="2pt" border="1pt solid" border-width="0mm" font-size="12pt">
                                <fo:block text-align="left" >
                                </fo:block>
                            </fo:table-cell>
                           <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm" font-size="10pt">
                                <fo:block text-align="right">Total Score
                                </fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm" font-size="10pt">
                                <fo:block text-align="right">${individual.qualitativeMaxScore}
                                </fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm" font-size="10pt">
                                <fo:block text-align="right">${individual.qualitativeQuarterOne}
                                </fo:block>
                            </fo:table-cell>
                             <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm" font-size="10pt">
                                <fo:block text-align="right">${individual.qualitativeQuarterTwo}
                                </fo:block>
                            </fo:table-cell>
                             <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm" font-size="10pt">
                                <fo:block text-align="right">${individual.qualitativeQuarterThree}
                                </fo:block>
                            </fo:table-cell>
                             <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm" font-size="10pt">
                                <fo:block text-align="right">${individual.qualitativeQuarterFour}
                                </fo:block>
                            </fo:table-cell>
                             <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm" font-size="10pt">
                                <fo:block text-align="right">${individual.qualitativeTotal}
                                </fo:block>
                            </fo:table-cell>
                            
                        </fo:table-row>
            </fo:table-body>
        </fo:table>
    </fo:block>

<fo:block font-size="12pt" text-align="right"  font-weight="bold" text-decoration="underline">
        Total Staff Performance Score: ${individual.all4qstotalScore}
    </fo:block>
    <fo:block font-size="12pt" text-align="right"  font-weight="bold" text-decoration="underline">
    </fo:block>
<fo:block font-size="12pt" text-align="right"  font-weight="bold" text-decoration="underline">
        ==================================================================================================================================================
    </fo:block>
    <fo:block font-size="12pt" text-align="right"  font-weight="bold" text-decoration="underline">
        
    </fo:block>

<#-- END !!! Wont work -->

    <#else>
     <fo:block space-after.optimum="10pt" >
        <fo:block text-align="center" font-size="14pt">No Scores Were found</fo:block>
    </fo:block>

  </#if>
    <#else>
     <fo:block space-after.optimum="10pt" >
        <fo:block text-align="center" font-size="14pt">No Scores Were found</fo:block>
    </fo:block>
  </#if>
</#list>
    <#else>
        <fo:block text-align="center">Nothing to show</fo:block>
    </#if>
</#escape>

