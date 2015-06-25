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

    <#-- REPORT TITLE -->
    <fo:block font-size="18pt" font-weight="bold" text-align="center">CHAI SACCO</fo:block>
    <fo:block font-size="16pt" text-align="center"  font-weight="bold" >INTER BRANCH OWINGS REPORT</fo:block>

    <fo:block><fo:leader/></fo:block>
    <fo:block><fo:leader/></fo:block>

    <#if owingBranchId?has_content>
        <#assign owingBranchName = delegator.findOne("PartyGroup", {"partyId" : owingBranchId}, false)/>
    </#if>
    <#if ownedBranchId?has_content>
        <#assign owedBranchName = delegator.findOne("PartyGroup", {"partyId" : ownedBranchId}, false)/>
    </#if>

    <fo:block text-decoration="underline" font-size="12pt" text-align="center"  font-weight="bold" >
        WHAT ${owingBranchName.groupName?upper_case} OWES ${owedBranchName.groupName?upper_case}
    </fo:block>

    <fo:block><fo:leader/></fo:block>

    <fo:block text-decoration="underline" font-size="12pt"  margin-left="0.3in" font-weight="bold" >
        IN SUMMARY:
    </fo:block>


<fo:table table-layout="fixed" width="100%" font-size="11pt" margin-left="0.2in">
    <fo:table-column column-number="1" column-width="proportional-column-width(100)"/>
    <fo:table-body>
        <fo:table-row>
            <fo:table-cell>
                <fo:list-block provisional-distance-between-starts="2.0in">
                    <fo:list-item>
                        <fo:list-item-label font-weight="bold">
                            <fo:block>Report Date:</fo:block>
                        </fo:list-item-label>
                        <fo:list-item-body start-indent="body-start()" text-decoration="underline">
                            <fo:block>${.now?date}</fo:block>
                        </fo:list-item-body>
                    </fo:list-item>
                    <fo:list-item>
                        <fo:list-item-label font-weight="bold">
                            <fo:block>Report Time:</fo:block>
                        </fo:list-item-label>
                        <fo:list-item-body start-indent="body-start()" text-decoration="underline">
                            <fo:block>${.now?time}</fo:block>
                        </fo:list-item-body>
                    </fo:list-item>
                    <fo:list-item>
                        <fo:list-item-label font-weight="bold">
                            <fo:block>Number of Transactions:</fo:block>
                        </fo:list-item-label>
                        <fo:list-item-body start-indent="body-start()" text-decoration="underline">
                            <fo:block>${count}</fo:block>
                        </fo:list-item-body>
                    </fo:list-item>
                    <fo:list-item>
                        <fo:list-item-label font-weight="bold">
                            <fo:block>Total Amount (KES):</fo:block>
                        </fo:list-item-label>
                        <fo:list-item-body start-indent="body-start()" text-decoration="underline">
                            <fo:block>${total}</fo:block>
                        </fo:list-item-body>
                    </fo:list-item>
                </fo:list-block>
            </fo:table-cell>
        </fo:table-row>
    </fo:table-body>
</fo:table>
<fo:block><fo:leader/></fo:block>
    <fo:block text-decoration="underline" font-size="12pt" text-align="center" margin-left="0.3in" font-weight="bold" >
        Transactions List
    </fo:block>
    <#if ownedBranchId?has_content>
        <fo:block space-after.optimum="10pt" font-size="9pt"  margin-right="0.4in">
            <fo:table table-layout="fixed" width="100%" font-size="9pt" margin-left="0.2in" >
                <fo:table-column column-number="1" column-width="proportional-column-width(15)"/>
                <fo:table-column column-number="2" column-width="proportional-column-width(16)"/>
                <fo:table-column column-number="3" column-width="proportional-column-width(15)"/>
                <fo:table-column column-number="4" column-width="proportional-column-width(25)"/>
                <fo:table-column column-number="5" column-width="proportional-column-width(10)"/>
                <fo:table-header>
                    <fo:table-row font-weight="bold">
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block>Trans ID</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block>Trans Date</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block>Trans Type</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block>GL Account</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                            <fo:block>Amount</fo:block>
                        </fo:table-cell>
                    </fo:table-row>
                </fo:table-header>
                <fo:table-body>
                    <#list transactions as trans >
                        <fo:table-row>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>${trans.acctgTransId}</fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>${trans.createdTxStamp?date}</fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <#if trans.debitCreditFlag?if_exists == "D">
                                    <fo:block>Debit</fo:block>
                                <#else>
                                    <fo:block>Credit</fo:block>
                                </#if>
                            </fo:table-cell>
                                <#if trans.glAccountId?has_content>
                                    <#assign glAccount = delegator.findOne("GlAccount", {"glAccountId" : trans.glAccountId}, false)/>
                                </#if>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>${glAccount.accountName}</fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                <fo:block>${trans.amount}</fo:block>
                            </fo:table-cell>
                        </fo:table-row>
                    </#list>
                </fo:table-body>
            </fo:table>
        </fo:block>
    <#else>
        <fo:block><fo:leader/></fo:block>
        <fo:block><fo:leader/></fo:block>
        <fo:block text-align="center" font-size="10pt" font-weight="bold">
            NO DATA FOUND
        </fo:block>
    </#if>
</#escape>
