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
        <fo:block font-size="10pt" text-align="left" font-weight="bold">
            BANK NAME: ${bankAccount.finAccountName?if_exists}
        </fo:block>
        <fo:block font-size="10pt" text-align="left" font-weight="bold">
            BANK RECONCILIATION AS AT: ${reconciliation.reconciledDate?if_exists}
        </fo:block>
        <fo:block><fo:leader/></fo:block>

    <#-- REPORT BODY -->
        <fo:block font-size="10pt" text-align="left"  font-weight="bold" >
            <fo:table table-layout="fixed">
                <fo:table-column column-number="1" column-width="proportional-column-width(50)"/>
                <fo:table-column column-number="2" column-width="proportional-column-width(50)"/>
                <fo:table-body>
                    <fo:table-row text-align="center" height="0.5cm" text-decoration="underline">
                        <fo:table-cell>
                            <fo:block>
                                CASH BOOK - Amounts in KES
                            </fo:block>
                        </fo:table-cell>
                        <fo:table-cell>
                            <fo:block>
                                BANK STATEMENT - Amounts in KES
                            </fo:block>
                        </fo:table-cell>
                    </fo:table-row>
                    <fo:table-row height="1.0in">
                        <fo:table-cell>
                            <fo:list-block provisional-distance-between-starts="2.4in">
                                <fo:list-item height="0.5cm">
                                    <fo:list-item-label>
                                        <fo:block font-weight="bold">Closing Balance Cash Book</fo:block>
                                    </fo:list-item-label>
                                    <fo:list-item-body start-indent="body-start()">
                                        <fo:block>24,193,668.91</fo:block>
                                    </fo:list-item-body>
                                </fo:list-item>
                                <fo:list-item height="0.5cm">
                                    <fo:list-item-label>
                                        <fo:block font-weight="bold">Less Unidentified Debits</fo:block>
                                    </fo:list-item-label>
                                    <fo:list-item-body start-indent="body-start()">
                                        <fo:block>1,493,372.76</fo:block>
                                    </fo:list-item-body>
                                </fo:list-item>
                                <fo:list-item height="1.5cm">
                                    <fo:list-item-label>
                                        <fo:block font-weight="bold">Add Ureceipted Bankings</fo:block>
                                    </fo:list-item-label>
                                    <fo:list-item-body start-indent="body-start()">
                                        <fo:block text-decoration="underline">11,033,027.55</fo:block>
                                    </fo:list-item-body>
                                </fo:list-item>
                                <fo:list-item>
                                    <fo:list-item-label>
                                        <fo:block font-weight="bold"></fo:block>
                                    </fo:list-item-label>
                                    <fo:list-item-body start-indent="body-start()">
                                        <fo:block><fo:leader/></fo:block>
                                    </fo:list-item-body>
                                </fo:list-item>
                                <fo:list-item>
                                    <fo:list-item-label>
                                        <fo:block font-weight="bold">Adjusted Cash Book Balance</fo:block>
                                    </fo:list-item-label>
                                    <fo:list-item-body start-indent="body-start()">
                                        <fo:block>14,654,014.12</fo:block>
                                    </fo:list-item-body>
                                </fo:list-item>
                            </fo:list-block>
                        </fo:table-cell>
                        <fo:table-cell>
                            <fo:list-block provisional-distance-between-starts="2.4in">
                                <fo:list-item>
                                    <fo:list-item-label>
                                        <fo:block font-weight="bold">Closing Balance Bank Statement</fo:block>
                                    </fo:list-item-label>
                                    <fo:list-item-body start-indent="body-start()">
                                        <fo:block>24,193,668.91</fo:block>
                                    </fo:list-item-body>
                                </fo:list-item>
                                <fo:list-item>
                                    <fo:list-item-label>
                                        <fo:block font-weight="bold">Add Uncredited Bankings</fo:block>
                                    </fo:list-item-label>
                                    <fo:list-item-body start-indent="body-start()">
                                        <fo:block>1,493,372.76</fo:block>
                                    </fo:list-item-body>
                                </fo:list-item>
                                <fo:list-item>
                                    <fo:list-item-label>
                                        <fo:block font-weight="bold">Less Unpresented Cheques</fo:block>
                                    </fo:list-item-label>
                                    <fo:list-item-body start-indent="body-start()">
                                        <fo:block text-decoration="underline">11,033,027.55</fo:block>
                                    </fo:list-item-body>
                                </fo:list-item>
                                <fo:list-item>
                                    <fo:list-item-label>
                                        <fo:block font-weight="bold"></fo:block>
                                    </fo:list-item-label>
                                    <fo:list-item-body start-indent="body-start()">
                                        <fo:block><fo:leader/></fo:block>
                                    </fo:list-item-body>
                                </fo:list-item>
                                <fo:list-item>
                                    <fo:list-item-label>
                                        <fo:block font-weight="bold">Adjusted Bank Statement Balance</fo:block>
                                    </fo:list-item-label>
                                    <fo:list-item-body start-indent="body-start()">
                                        <fo:block>14,654,014.12</fo:block>
                                    </fo:list-item-body>
                                </fo:list-item>
                            </fo:list-block>
                        </fo:table-cell>
                    </fo:table-row>
                </fo:table-body>
            </fo:table>
        </fo:block>


    <#else>
        <fo:block text-align="center">No BANKs Found With that ID</fo:block>
    </#if>

</#escape>

