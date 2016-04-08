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
  <div id="partyContent" class="screenlet">
    <div class="screenlet-title-bar">
      <ul>
        <li class="h3">Staff Documents</li>
      </ul>
      <br class="clear" />
    </div>
    <div class="screenlet-body">
       <br class="clear" />
          <ul>
            <li class="h1"><b><font color="red">Upload Document</b></font></li>
          </ul>
       <br class="clear" />
       
      <form id="uploadPartyContent" method="post" enctype="multipart/form-data" action="<@ofbizUrl>uploadPartyContentt</@ofbizUrl>">
        <input type="hidden" name="dataCategoryId" value="PERSONAL"/>
        <input type="hidden" name="contentTypeId" value="DOCUMENT"/>
        <input type="hidden" name="statusId" value="CTNT_PUBLISHED"/>
        <input type="hidden" name="partyId" value="public"/>
        <input type="file" name="uploadedFile" class="required error" size="25"/>
        <div>
         
           <br class="clear" />
           <br class="clear" />
         
        <select name="partyContentTypeId" class="required error">
          <#list partyContentTypes as partyContentType>
            <option value="${partyContentType.partyContentTypeId}">${partyContentType.get("description", locale)?default(partyContentType.partyContentTypeId)}</option>
          </#list>
        </select>
        </div>
          <br class="clear" />
          <br class="clear" />
        <select name="isPublic" hidden="true">
            <option value="Y">${uiLabelMap.CommonYes}</option>
        </select>
       
        <input type="submit" value="${uiLabelMap.CommonUpload}" />
      </form>
       
          <br class="clear" />
          <br class="clear" />
           <ul>
             <li class="h1"><b><font color="green">Uploaded Documents</b></font></li>
           </ul>
       
        <hr />
         ${screens.render("component://party/widget/partymgr/ProfileScreens.xml#ContentListPortal")}
        
      <div id='progress_bar'><div></div></div>
    </div>
  </div>
  <script type="text/javascript">
    jQuery("#uploadPartyContent").validate({
        submitHandler: function(form) {
            <#-- call upload scripts - functions defined in PartyProfileContent.js -->
            uploadPartyContent();
            getUploadProgressStatus();
            form.submit();
        }
    });
  </script>
