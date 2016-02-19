app.factory('FileTab', function() {

  var aceModeList = ace.require("ace/ext/modelist");

  function FileTab(file) {
    this.file = file;
    this.data = null;
    this.aceOptions = {
      theme: 'merbivore_soft',
      mode: aceModeList.getModeForPath(file.getName()).name,
      onChange: function() {
      }
    };
  }

  return FileTab;

});