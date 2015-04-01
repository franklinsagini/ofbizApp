//this code needs modifications yet its specific.

jQuery(document).ready( function() {
  jQuery("input[name*='fromDatenot']").bind('focusout', checkDate);
  jQuery("input[name*='thruDatenot']").bind('focusout', checkDate);
});

function checkDate() {
  var a = jQuery("input[name*='fromDatenot']");
  var b = jQuery("input[name*='thruDatenot']");

  if(a.val() !="" && b.val() !="") {
    if (a.val() >= b.val()) {
      showErrorAlertLoadUiLabel("", "", "CommonUiLabels", "CommonFromDateThruDateCheck")
    }
  }
}
