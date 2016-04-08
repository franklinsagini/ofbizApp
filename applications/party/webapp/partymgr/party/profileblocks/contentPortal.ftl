
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
