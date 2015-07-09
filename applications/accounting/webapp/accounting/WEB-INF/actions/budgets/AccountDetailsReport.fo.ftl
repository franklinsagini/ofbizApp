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
    <#if budget?has_content>
        <#-- REPORT TITLE -->
        <fo:block font-size="18pt" font-weight="bold" text-align="center">
            CHAI SACCO
        </fo:block>
        <fo:block font-size="12pt" text-align="center"  font-weight="bold" >
            ${budget.comments?upper_case}
        </fo:block>
        <fo:block><fo:leader/></fo:block>
        <fo:block text-decoration="underline" font-size="11pt" text-align="center"  font-weight="bold" >
            BUDGET JUSTIFICATIONS REPORT
        </fo:block>

        <fo:block><fo:leader/></fo:block>
        <fo:block><fo:leader/></fo:block>

        <#list budgetItems as item >
            <#if item.glAccountId?has_content>
                <#assign account = delegator.findOne("GlAccount", {"glAccountId" : item.glAccountId}, false)/>
            </#if>
            <fo:block text-decoration="underline" font-size="11pt"  font-weight="bold" >
                ${account.accountCode?if_exists} - ${account.accountName?upper_case}
            </fo:block>
            <fo:block space-after.optimum="10pt" font-size="11pt"  margin-right="0.4in">
                <fo:table table-layout="fixed" width="100%" font-size="9pt" margin-left="0.2in" >
                    <fo:table-column column-number="1" column-width="proportional-column-width(60)"/>
                    <fo:table-column column-number="2" column-width="proportional-column-width(40)"/>
                    <fo:table-header>
                        <fo:table-row font-weight="bold">
                            <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                                <fo:block>Description/Justification</fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                                <fo:block>Budget Amount</fo:block>
                            </fo:table-cell>
                        </fo:table-row>
                    </fo:table-header>
                    <fo:table-body>
                    (Static["org.ofbiz.accounting.invoice.InvoiceWorker"].getInvoiceItemTotal(invoiceItem))
                     <#assign allBgtItms = (Static["org.ofbiz.accounting.budget.BudgetWorker"].getJustificationsForAccount(delegator, budget.budgetId, item.glAccountId))/>
                        <#list allBgtItms as bdgtItm>
                            <fo:table-row>
                                <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                    <fo:block>${bdgtItm.justification?if_exists}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                                    <fo:block>${bdgtItm.amount?if_exists}</fo:block>
                                </fo:table-cell>
                            </fo:table-row>
                        </#list>
                    </fo:table-body>
                </fo:table>
            </fo:block>
        </#list>

    <#else>
        <fo:block text-align="center">NO DATA FOUND</fo:block>
    </#if>
</#escape>
