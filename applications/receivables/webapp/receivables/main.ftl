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
<div class="screenlet">
  <div class="screenlet-title-bar">
    <ul>
      <li class="h3">Remittance Importation</li>
      <li class="disabled">${delegator.getDelegatorName()}</li>
    </ul>
    <br class="clear"/>
  </div>
  <div class="screenlet-body">
    <#if !userLogin?has_content>
      <div>${uiLabelMap.WebtoolsForSomethingInteresting}.</div>
      <br />
      <div>${uiLabelMap.WebtoolsNoteAntRunInstall}</div>
      <br />
      <div><a href="<@ofbizUrl>checkLogin</@ofbizUrl>">${uiLabelMap.CommonLogin}</a></div>
    </#if>
    <#if userLogin?has_content>
      <ul class="webToolList">

        
      
        <#if security.hasPermission("DATAFILE_MAINT", session)>
          <li><h3>Import File</h3></li>
          <li><h3><a href="<@ofbizUrl>viewdatafile</@ofbizUrl>">Import Received Remittance</a></h3></li>
        </#if>
        	<li></li>
        	<li></li>
        	<li></li>
        <#if security.hasPermission("ENTITY_MAINT", session)>

          <li><h3><a href="<@ofbizUrl>EntityImport</@ofbizUrl>">Import Generated File (XML)</a></h3></li>
        </#if>
       </ul>
    </#if>
  </div>
</div>
