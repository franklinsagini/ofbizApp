 <script language="JavaScript" type="text/javascript">
        function setUploadUrl(newUrl) {
            var toExec = 'document.imageUploadForm.action="' + newUrl + '";';
            eval(toExec);
        };
    </script>
    <h3>Add Member ID Front</h3>
    <form method="post" enctype="multipart/form-data" action="<@ofbizUrl>memberAttachments?partyId=${partyId}&amp;upload_file_type=original&amp;idfronturl=Y</@ofbizUrl>" name="imageUploadForm">
        <table cellspacing="0" class="basic-table">
            <tr>
                <td width="20%" align="right" valign="top">
                    <input type="file" size="50" name="fname"/>
                </td>
                <td>&nbsp;</td>
                <td width="80%" colspan="4" valign="top">
                    <input type="submit" class="smallSubmit" value="Upload ID"/>
                </td>
            </tr>
        </table>
        <!-- span class="tooltip">${uiLabelMap.ProductOriginalImageMessage} : {ofbiz.home}/applications/product/config/ImageProperties.xml&quot;</span -->
    </form>