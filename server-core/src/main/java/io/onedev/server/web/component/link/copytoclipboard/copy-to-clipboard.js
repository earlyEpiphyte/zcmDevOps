onedev.server.copyToClipboard = {
	onDomReady: function(buttonId, text) {
		var $button = $("#" + buttonId);
		var clipboard = new Clipboard("#"+buttonId, {
			text: function(trigger) {
				return text;
			}
		});
		$button.attr("title", "复制到粘贴板");
		$button.addClass("copy-to-clipboard");
	}
};