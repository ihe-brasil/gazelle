/**
 * select active modal
 * 
 * @author Guillaume Thomazon
 */

var ActiveModal = new function() {
	this.activeModalPanel = null;
	this.setActiveModalPanel = function(a) {
		this.activeModalPanel = a;
	};
};