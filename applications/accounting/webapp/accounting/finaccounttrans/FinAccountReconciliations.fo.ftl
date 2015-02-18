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
    <#if bankAccount?has_content>

        <#-- REPORT TITLE -->
        <fo:block font-size="18pt" font-weight="bold" text-align="center">
            CHAI SACCO
        </fo:block>
        <fo:block font-size="12pt" text-align="center"  font-weight="bold" >
            BANK RECONCILIATION REPORT
        </fo:block>
        <fo:block><fo:leader/></fo:block>
        <#-- BANK Details -->
        <fo:block font-size="10pt" text-align="center" font-weight="bold">
            BANK NAME: ${bankAccount.finAccountName?if_exists}
        </fo:block>
        <fo:block font-size="10pt" text-align="center" font-weight="bold">
            RECONCILIATION AS AT: ${reconciliation.reconciledDate?if_exists}
        </fo:block>
        <fo:block><fo:leader/></fo:block>

    <#-- REPORT BODY -->
        <fo:table table-layout="fixed" width="100%" font-size="9pt" margin-left="0%">
            <fo:table-column column-number="1" column-width="proportional-column-width(10)"/>
            <fo:table-column column-number="1" column-width="proportional-column-width(60)"/>
            <fo:table-column column-number="1" column-width="proportional-column-width(30)"/>
            <fo:table-header>
              <fo:table-row font-weight="bold">
                <fo:table-cell padding="1%" background-color="#D4D0C8" border="1pt solid" border-width=".5mm">
                  <fo:block font-size="10pt">Operator</fo:block>
                </fo:table-cell>
                <fo:table-cell padding="1%" background-color="#D4D0C8" border="1pt solid" border-width=".5mm">
                  <fo:block font-size="10pt">Reconciliation Item</fo:block>
                </fo:table-cell>
                <fo:table-cell padding="1%" background-color="#D4D0C8" border="1pt solid" border-width=".5mm">
                  <fo:block font-size="10pt">Amount in KES</fo:block>
                </fo:table-cell>
              </fo:table-row>
            </fo:table-header>
            <fo:table-body>
                <fo:table-row font-weight="bold">
                    <fo:table-cell border="1pt solid">
                        <fo:block></fo:block>
                    </fo:table-cell>
                    <fo:table-cell border="1pt solid" padding="1%">
                        <fo:block>Balance as per Cash Book</fo:block>
                    </fo:table-cell>
                    <fo:table-cell border="1pt solid" text-align="right" padding="1%">
                        <fo:block>${cashBook?if_exists}</fo:block>
                    </fo:table-cell>
                </fo:table-row>
                <fo:table-row>
                    <fo:table-cell border="1pt solid" padding="1%">
                        <fo:block>ADD</fo:block>
                    </fo:table-cell>
                    <fo:table-cell border="1pt solid" padding="1%">
                        <fo:block>Unreceipted Direct Deposits</fo:block>
                    </fo:table-cell>
                    <fo:table-cell border="1pt solid" text-align="right" padding="1%">
                        <fo:block>${cashBook?if_exists}</fo:block>
                    </fo:table-cell>
                </fo:table-row>
                <fo:table-row>
                    <fo:table-cell border="1pt solid">
                        <fo:block></fo:block>
                    </fo:table-cell>
                    <fo:table-cell border="1pt solid" padding="1%">
                        <fo:block>Unpresented Cheques</fo:block>
                    </fo:table-cell>
                    <fo:table-cell border="1pt solid" text-align="right" padding="1%">
                        <fo:block>${unreceiptedDirectDeposits?if_exists}</fo:block>
                    </fo:table-cell>
                </fo:table-row>
                <fo:table-row>
                    <fo:table-cell border="1pt solid" padding="1%">
                        <fo:block>LESS</fo:block>
                    </fo:table-cell>
                    <fo:table-cell border="1pt solid" padding="1%">
                        <fo:block>Uncredited Cheques</fo:block>
                    </fo:table-cell>
                    <fo:table-cell border="1pt solid" text-align="right" padding="1%">
                        <fo:block>${unpresentedCheques?if_exists}</fo:block>
                    </fo:table-cell>
                </fo:table-row>
                <fo:table-row>
                    <fo:table-cell border="1pt solid">
                        <fo:block></fo:block>
                    </fo:table-cell>
                    <fo:table-cell border="1pt solid" padding="1%">
                        <fo:block>Unidentified Debits</fo:block>
                    </fo:table-cell>
                    <fo:table-cell border="1pt solid" text-align="right" padding="1%">
                        <fo:block>${withdrawalNotInCashBook?if_exists}</fo:block>
                    </fo:table-cell>
                </fo:table-row>
                <fo:table-row font-weight="bold">
                    <fo:table-cell border="1pt solid">
                        <fo:block></fo:block>
                    </fo:table-cell>
                    <fo:table-cell border="1pt solid" padding="1%">
                        <fo:block font-weight="bold">Balance as per bank statement</fo:block>
                    </fo:table-cell>
                    <fo:table-cell border="1pt solid" text-align="right" padding="1%">
                        <fo:block>${bankStatement?if_exists}</fo:block>
                    </fo:table-cell>
                </fo:table-row>
            </fo:table-body>
        </fo:table>

    <#else>
        <fo:block text-align="center">No BANKs Found With that ID</fo:block>
    </#if>

</#escape>

