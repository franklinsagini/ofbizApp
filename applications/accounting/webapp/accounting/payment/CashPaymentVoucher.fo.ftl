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
    <#if payment?has_content>
        <#-- REPORT TITLE -->
        <fo:block font-size="18pt" font-weight="bold" text-align="center">
            CHAI SACCO
        </fo:block>
        <fo:block font-size="16pt" text-align="center"  font-weight="bold" >
            CASH PAYMENT VOUCHER
        </fo:block>
        <fo:block><fo:leader/></fo:block>
        <fo:block text-decoration="underline" font-size="14pt" text-align="center"  font-weight="bold" >
            CPV NO:  ${payment.paymentId?if_exists}
        </fo:block>
        <fo:block><fo:leader/></fo:block>

         <#if payment.paymentTypeId?has_content>
            <#assign paymentType = delegator.findOne("PaymentType", {"paymentTypeId" : payment.paymentTypeId}, false)/>
        </#if>
         <#if payment.paymentMethodId?has_content>
            <#assign paymentMethod = delegator.findOne("PaymentMethod", {"paymentMethodId" : payment.paymentMethodId}, false)/>
        </#if>
         <#if payment.statusId?has_content>
            <#assign statusItem = delegator.findOne("StatusItem", {"statusId" : payment.statusId}, false)/>
        </#if>
<#--        <#if activity.actionBy?has_content>
            <#assign actionBy = delegator.findOne("Person", {"partyId" : activity.actionBy}, false)/>
        </#if>
        <#if activity.currentPossesser?has_content>
            <#assign currentPossesser = delegator.findOne("Person", {"partyId" : activity.currentPossesser}, false)/>
        </#if>
         <#if activity.Reason?has_content>
            <#assign reason = delegator.findOne("RegistryFileActivity", {"activityId" : activity.Reason}, false)/>
        </#if> -->



        <fo:table table-layout="fixed" width="100%" font-size="14pt" >
            <fo:table-column column-number="1" column-width="proportional-column-width(50)"/>
            <fo:table-column column-number="2" column-width="proportional-column-width(50)"/>
            <fo:table-body>
                <fo:table-row>
                    <fo:table-cell>
                        <fo:list-block provisional-distance-between-starts="1.4in">
                            <fo:list-item>
                                <fo:list-item-label font-weight="bold">
                                    <fo:block>Payment To:</fo:block>
                                </fo:list-item-label>
                                <fo:list-item-body start-indent="body-start()" text-decoration="underline">
                                <#if organizationPartyGroupTo?exists>
                                    <fo:block>${organizationPartyGroupTo?if_exists}</fo:block>
                                <#else>
                                     <fo:block></fo:block>
                                </#if>
                                </fo:list-item-body>
                            </fo:list-item>
                            <fo:list-item>
                                <fo:list-item-label font-weight="bold">
                                    <fo:block>Payment Type:</fo:block>
                                </fo:list-item-label>
                                <fo:list-item-body start-indent="body-start()" text-decoration="underline">
                                    <fo:block>${paymentType.description?if_exists}</fo:block>
                                </fo:list-item-body>
                            </fo:list-item>
                            <fo:list-item>
                                <fo:list-item-label font-weight="bold">
                                    <fo:block>Payment Status:</fo:block>
                                </fo:list-item-label>
                                <fo:list-item-body start-indent="body-start()" text-decoration="underline">
                                    <fo:block>${statusItem.description?if_exists}</fo:block>
                                </fo:list-item-body>
                            </fo:list-item>
                            <fo:list-item>
                                <fo:list-item-label font-weight="bold">
                                    <fo:block>Total Amount</fo:block>
                                </fo:list-item-label>
                                <fo:list-item-body start-indent="body-start()" text-decoration="underline">
                                    <fo:block>${payment.amount?if_exists}</fo:block>
                                </fo:list-item-body>
                            </fo:list-item>
                        </fo:list-block>
                    </fo:table-cell>
                    <fo:table-cell>
                        <fo:list-block provisional-distance-between-starts="1.4in">
                            <fo:list-item>
                                <fo:list-item-label font-weight="bold">
                                    <fo:block>Payment From:</fo:block>
                                </fo:list-item-label>
                                <fo:list-item-body start-indent="body-start()" text-decoration="underline">
                                    <fo:block>${organizationPartyGroupFrom?if_exists}</fo:block>
                                </fo:list-item-body>
                            </fo:list-item>
                            <fo:list-item>
                                <fo:list-item-label font-weight="bold">
                                    <fo:block>Payment Mode:</fo:block>
                                </fo:list-item-label>
                                <fo:list-item-body start-indent="body-start()" text-decoration="underline">
                                    <fo:block>${paymentMethod.paymentMethodId?if_exists}</fo:block>
                                </fo:list-item-body>
                            </fo:list-item>
                            <fo:list-item>
                                <fo:list-item-label font-weight="bold">
                                    <fo:block>CPV Date:</fo:block>
                                </fo:list-item-label>
                                <fo:list-item-body start-indent="body-start()" text-decoration="underline">
                                    <fo:block>${payment.effectiveDate?if_exists}</fo:block>
                                </fo:list-item-body>
                            </fo:list-item>
                            <fo:list-item>
                                <fo:list-item-label font-weight="bold">
                                    <fo:block>EFT/CHQ/REF No: </fo:block>
                                </fo:list-item-label>
                                <fo:list-item-body start-indent="body-start()" text-decoration="underline">
                                    <fo:block>${payment.paymentRefNum?if_exists}</fo:block>
                                </fo:list-item-body>
                            </fo:list-item>
                        </fo:list-block>
                    </fo:table-cell>
                </fo:table-row>
            </fo:table-body>
        </fo:table>

        <fo:block><fo:leader/></fo:block>
        <fo:block font-size="9pt"  text-align="left" margin-left="0%" margin-top="0%" margin-bottom="0.0in">
        <fo:table table-layout="fixed" width="100%" font-size="13pt" >
            <fo:table-column column-number="1" column-width="proportional-column-width(100)"/>
            <fo:table-body>
                <fo:table-row>
                    <fo:table-cell>
                        <fo:list-block provisional-distance-between-starts="1.8in">
                            <fo:list-item>
                                <fo:list-item-label font-weight="bold">
                                    <fo:block>Payment Description: </fo:block>
                                </fo:list-item-label>
                                <fo:list-item-body start-indent="body-start()" text-decoration="underline">
                                    <fo:block>${payment.comments?if_exists}</fo:block>
                                </fo:list-item-body>
                            </fo:list-item>
                        </fo:list-block>
                    </fo:table-cell>
                </fo:table-row>
            </fo:table-body>
        </fo:table>
        </fo:block>
        <fo:block><fo:leader/></fo:block>
        <fo:block><fo:leader/></fo:block>
        <#if acctgTransAndEntries?has_content>
        <fo:block space-after.optimum="10pt" font-size="9pt"  margin-right="0.4in">
        <fo:table table-layout="fixed" width="100%" font-size="9pt" margin-left="0.2in" >
            <fo:table-column column-number="1" column-width="proportional-column-width(37)"/>
            <fo:table-column column-number="2" column-width="proportional-column-width(22)"/>
            <fo:table-column column-number="3" column-width="proportional-column-width(20)"/>
            <fo:table-column column-number="4" column-width="proportional-column-width(21)"/>
            <fo:table-header>
              <fo:table-row font-weight="bold">
                <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                  <fo:block>Account Name</fo:block>
                </fo:table-cell>
                <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                  <fo:block>Account Code</fo:block>
                </fo:table-cell>
                <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                  <fo:block>Debit Amount</fo:block>
                </fo:table-cell>
                <fo:table-cell padding="2pt" background-color="#D4D0C8" border="1pt solid" border-width=".1mm">
                  <fo:block>Credit Amount</fo:block>
                </fo:table-cell>
              </fo:table-row>
            </fo:table-header>
            <fo:table-body>
            <#list acctgTransAndEntries as acctgTransAndEntry>
                <fo:table-row>
                  <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                      <fo:block>${acctgTransAndEntry.accountName?if_exists}</fo:block>
                  </fo:table-cell>
                  <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                      <fo:block>${acctgTransAndEntry.accountCode?if_exists}</fo:block>
                  </fo:table-cell>
                  <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                      <#if acctgTransAndEntry.debitCreditFlag?if_exists == "D">
                          <fo:block>${acctgTransAndEntry.origAmount?if_exists}</fo:block>
                      <#else>
                          <fo:block></fo:block>
                      </#if>
                  </fo:table-cell>
                  <fo:table-cell padding="2pt" border="1pt solid" border-width=".1mm">
                      <#if acctgTransAndEntry.debitCreditFlag?if_exists == "C">
                          <fo:block>${acctgTransAndEntry.origAmount?if_exists}</fo:block>
                      <#else>
                           <fo:block></fo:block>
                      </#if>
                  </fo:table-cell>
                </fo:table-row>
                </#list>
            </fo:table-body>
        </fo:table>
        </fo:block>
        </#if>
        <fo:block font-size="12pt"  text-align="left"  margin-top="20%" margin-bottom="0.2in">
          PAYING OFFICER SIGNATURE ------------------------------------------------DATE--------------------------
        </fo:block>
        <fo:block><fo:leader/></fo:block>
        <fo:block><fo:leader/></fo:block>

        <fo:block font-size="12pt"  text-align="left"  margin-top="20%" margin-bottom="0.2in">
          PAYEE SIGNATURE ----------------------------------------------------------DATE----------------------------
        </fo:block>

    <#else>
        <fo:block text-align="center">NO DATA FOUND</fo:block>
    </#if>
</#escape>
