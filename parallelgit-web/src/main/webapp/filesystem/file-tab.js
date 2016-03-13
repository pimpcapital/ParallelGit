app.factory('FileTab', function() {

  var aceModeList = ace.require("ace/ext/modelist");

  function FileTab(file) {
    this.file = file;
    this.data = null;
    this.active = false;
    this.aceOptions = {
      theme: 'merbivore_soft',
      mode: aceModeList.getModeForPath(file.getName()).name
    };
  }

  FileTab.prototype.getFile = function() {
    return this.file;
  };

  FileTab.prototype.getPath = function() {
    return this.getFile().getPath();
  };

  FileTab.prototype.setActive =  function(active) {
    return this.active = active;
  };

  return FileTab;

});