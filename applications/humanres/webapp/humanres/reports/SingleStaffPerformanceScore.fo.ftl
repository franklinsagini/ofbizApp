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
    <#if ReviewGroup?has_content>

    <#-- REPORT TITLE -->
    <fo:block font-size="18pt" font-weight="bold" text-align="center">
        CHAI SACCO
    </fo:block>
    <fo:block font-size="12pt" text-align="center"  font-weight="bold" text-decoration="underline">
        ${fNameUpperCased} ${sNameUpperCased}'s HARMONIZED PERFORMANCE REPORT FOR THE YEAR ${year}
    </fo:block>
    <fo:block><fo:leader/></fo:block>

    <fo:block font-size="12pt" text-align="center"  font-weight="bold" text-decoration="underline">
        Quantitative Goals Scores
    </fo:block>
    <#-- Employee Details -->
<#if Goal_QuantitativeScorelist?has_content>
    <#-- REPORT BODY -->
    <fo:block space-after.optimum="10pt" font-size="10pt">
        <fo:table table-layout="fixed" width="100%">
            <fo:table-column column-width="30pt"/>
            <fo:table-column column-width="150pt"/>
            <fo:table-column column-width="150pt"/>
            
            <fo:table-column column-width="40pt"/>
            <fo:table-column column-width="40pt"/>
            <fo:table-column column-width="40pt"/>
            <fo:table-column column-width="40pt"/>
            <fo:table-column column-width="40pt"/>
            <fo:table-column column-width="40pt"/>
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
                  <#list Goal_QuantitativeScorelist as score>
                     <fo:table-row>
                       <#assign count = count + 1>
                      <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <fo:block>${count}</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <fo:block>${score.perspective?if_exists}</fo:block>
                        </fo:table-cell>
                         <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <fo:block>${score.Objective?if_exists}</fo:block>
                        </fo:table-cell>
                       
                         <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <fo:block text-align="right">${score.MaxPossibleScore?if_exists} %</fo:block>
                        </fo:table-cell>
                         <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <fo:block text-align="right">${score.Q1?if_exists}</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <fo:block text-align="right">${score.Q2?if_exists}</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <fo:block text-align="right">${score.Q3?if_exists}</fo:block>
                        </fo:table-cell>
                         <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <fo:block text-align="right">${score.Q4?if_exists}</fo:block>
                        </fo:table-cell>
                         <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <fo:block text-align="right">${score.Total?if_exists}</fo:block>
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
                           <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm" font-size="10pt">
                                <fo:block text-align="right">Total Score
                                </fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm" font-size="10pt">
                                <fo:block text-align="right">${QNT_Totalall4qstotalMaxScore}
                                </fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm" font-size="10pt">
                                <fo:block text-align="right">${QNT_Totalq1}
                                </fo:block>
                            </fo:table-cell>
                             <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm" font-size="10pt">
                                <fo:block text-align="right">${QNT_Totalq2}
                                </fo:block>
                            </fo:table-cell>
                             <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm" font-size="10pt">
                                <fo:block text-align="right">${QNT_Totalq3}
                                </fo:block>
                            </fo:table-cell>
                             <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm" font-size="10pt">
                                <fo:block text-align="right">${QNT_Totalq4}
                                </fo:block>
                            </fo:table-cell>
                             <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm" font-size="10pt">
                                <fo:block text-align="right">${QNT_Totalall4qstotalScore}
                                </fo:block>
                            </fo:table-cell>
                            
                        </fo:table-row>






            </fo:table-body>
        </fo:table>
    </fo:block>


  <fo:block font-size="12pt" text-align="center"  font-weight="bold" text-decoration="underline">
        Qualitative Goals Scores
    </fo:block>


    <#if Goal_QualitativeScorelist?has_content>
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
                  <#list Goal_QualitativeScorelist as score2>
                     <fo:table-row>
                       <#assign count = count + 1>
                      <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <fo:block>${count}</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <fo:block>${score2.IndicatorDescription2?if_exists}</fo:block>
                        </fo:table-cell>
                         <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <fo:block text-align="right">${score2.MaxPossibleScore2?if_exists} %</fo:block>
                        </fo:table-cell>
                         <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <fo:block text-align="right">${score2.Q1?if_exists}</fo:block>
                        </fo:table-cell>
                         <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <fo:block text-align="right">${score2.Q2?if_exists}</fo:block>
                        </fo:table-cell>
                         <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <fo:block text-align="right">${score2.Q3?if_exists}</fo:block>
                        </fo:table-cell>
                         <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <fo:block text-align="right">${score2.Q4?if_exists}</fo:block>
                        </fo:table-cell>
                         <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                            <fo:block text-align="right">${score2.Total?if_exists}</fo:block>
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
                                <fo:block text-align="right">${QTT_Totalall4qstotalMaxScore}
                                </fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm" font-size="10pt">
                                <fo:block text-align="right">${QTT_Totalq1}
                                </fo:block>
                            </fo:table-cell>
                             <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm" font-size="10pt">
                                <fo:block text-align="right">${QTT_Totalq2}
                                </fo:block>
                            </fo:table-cell>
                             <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm" font-size="10pt">
                                <fo:block text-align="right">${QTT_Totalq3}
                                </fo:block>
                            </fo:table-cell>
                             <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm" font-size="10pt">
                                <fo:block text-align="right">${QTT_Totalq4}
                                </fo:block>
                            </fo:table-cell>
                             <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm" font-size="10pt">
                                <fo:block text-align="right">${QTT_Totalall4qstotalScore}
                                </fo:block>
                            </fo:table-cell>
                            
                        </fo:table-row>
            </fo:table-body>
        </fo:table>
    </fo:block>

<fo:block font-size="12pt" text-align="right"  font-weight="bold" text-decoration="underline">
        Total Staff Performance Score: ${all4qstotalScore}
    </fo:block>

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
    <#else>
        <fo:block text-align="center">Nothing to show</fo:block>
    </#if>
</#escape>

