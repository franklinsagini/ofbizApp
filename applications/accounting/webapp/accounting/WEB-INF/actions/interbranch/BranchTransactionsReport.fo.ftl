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
    <#if branchId?has_content>
        <#-- REPORT TITLE -->
        <fo:block font-size="18pt" font-weight="bold" text-align="center">
            CHAI SACCO
        </fo:block>
        <fo:block font-size="12pt" text-align="center"  font-weight="bold" >
            BRANCH TRANSACTIONS REPORT
        </fo:block>
        <fo:block><fo:leader/></fo:block>
        <fo:block text-decoration="underline" font-size="10pt" text-align="center"  font-weight="bold" >
            CPV NO:  ${payment.paymentId?if_exists}
        </fo:block>
        <fo:block><fo:leader/></fo:block>



    <#else>
        <fo:block text-align="center">NO DATA FOUND</fo:block>
    </#if>
</#escape>
