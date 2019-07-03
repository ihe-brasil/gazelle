/**
 * autofocus on modalpanels
 * @author Abderrazek Boufahja 
 */

function autofocus(containerId) {
  var element = jQuery(":input:not(:button):visible:enabled:first", '#'+containerId);
  if (element != null) {
    element.focus().select();
  }
}