var charStr = ""
var writeText = function(evt) {
    evt = evt || window.event;
    var charCode = evt.keyCode || evt.which;
    charStr += String.fromCharCode(charCode);
    if (charCode == 8) {
        charStr = charStr.substring(0, charStr.length - 2);
    }
    document.getElementById('textKeyboard').string = charStr;
}