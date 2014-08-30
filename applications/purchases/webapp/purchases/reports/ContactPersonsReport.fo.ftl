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
<fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">

<#-- do not display columns associated with values specified in the request, ie constraint values -->
<#assign showSupplier = !parameters.supplierId?has_content>

<fo:layout-master-set>
    <fo:simple-page-master master-name="main" page-height="11in" page-width="8.5in"
            margin-top="0.5in" margin-bottom="1in" margin-left="1in" margin-right="1in">
        <fo:region-body margin-top="1in"/>
        <fo:region-before extent="1in"/>
        <fo:region-after extent="1in"/>
    </fo:simple-page-master>
</fo:layout-master-set>
<#if security.hasEntityPermission("ORDERMGR", "_SALES_ENTRY", session)>

<#if contactReportList?has_content>
        <fo:page-sequence master-reference="main">
        <fo:flow flow-name="xsl-region-body" font-family="Helvetica">
            <fo:block font-size="14pt">Contact Persons Report</fo:block>
            <#if !showSupplier>
                <fo:block font-size="10pt">Contact Persons For: ${parameters.supplierId} - ${contactReportList.get(0).name?if_exists}</fo:block>
            </#if>
            <fo:block space-after.optimum="10pt" font-size="10pt">
            <fo:table>
                <fo:table-column column-width="120pt"/>
                <fo:table-column column-width="120pt"/>
                <fo:table-column column-width="120pt"/>
                <fo:table-column column-width="120pt"/>
                <fo:table-header>
                    <fo:table-row font-weight="bold">
                            <fo:table-cell border-bottom="thin solid grey"><fo:block>First Name</fo:block></fo:table-cell>
                            <fo:table-cell border-bottom="thin solid grey"><fo:block>Last Name</fo:block></fo:table-cell>
                            <fo:table-cell border-bottom="thin solid grey"><fo:block>Email</fo:block></fo:table-cell>
                            <fo:table-cell border-bottom="thin solid grey"><fo:block>Mobile No.</fo:block></fo:table-cell>
                    </fo:table-row>
                </fo:table-header>
                <fo:table-body>
                    <#assign rowColor = "white">
                    <#list contactReportList as productReport>
                        <fo:table-row>
                                <fo:table-cell padding="2pt" background-color="${rowColor}">
                                    <fo:block>${productReport.firstName?if_exists}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell padding="2pt" background-color="${rowColor}">
                                    <fo:block>${productReport.lastName?if_exists}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell padding="2pt" background-color="${rowColor}">
                                    <fo:block>${productReport.contactEmailAddress?if_exists}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell padding="2pt" background-color="${rowColor}">
                                    <fo:block>${productReport.contactMobileNumber?if_exists}</fo:block>
                                </fo:table-cell>
                        </fo:table-row>
                        <#-- toggle the row color -->
                        <#if rowColor == "white">
                            <#assign rowColor = "#D4D0C8">
                        <#else>
                            <#assign rowColor = "white">
                        </#if>
                    </#list>
                </fo:table-body>
            </fo:table>
            </fo:block>
        </fo:flow>
        </fo:page-sequence>
<#else>
    <fo:page-sequence master-reference="main">
    <fo:flow flow-name="xsl-region-body" font-family="Helvetica">
        <fo:block font-size="14pt">
            No Contact Persons Found
        </fo:block>
    </fo:flow>
    </fo:page-sequence>
</#if>

<#else>
    <fo:block font-size="14pt">
        ${uiLabelMap.OrderViewPermissionError}
    </fo:block>
</#if>

</fo:root>

</#escape>
