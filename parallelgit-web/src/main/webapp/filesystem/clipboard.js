app.service('Clipboard', function($rootScope, FileSystem, Dialog) {

  this._source = null;
  this._deleteOnPaste = null;

  this.cut = function(source) {
    this._source = source;
    this._deleteOnPaste = true;
  };

  this.copy = function(source) {
    this._source = source;
    this._deleteOnPaste = false;
  };

  this.paste = function(dest) {
    if(this._source != null) {
      var source = this._source;
      var isMove = this._deleteOnPaste;
      var dir = dest.isDirectory() ? dest : dest.getParent();
      if(isMove) {
        clear();
        Dialog.prompt('Move file', {name: {label: 'Enter a new name', value: source.getName()}}).then(function(fields) {
          FileSystem.moveFile(source, dir, fields.name.value);
        });
      } else {
        Dialog.prompt('Copy file', {name: {label: 'Enter a new name', value: source.getName()}}).then(function(fields) {
          FileSystem.copyFile(source, dir, fields.name.value);
        });
      }
    }
  };

  $rootScope.$on('reloaded-filesystem', function() {
    clear();
  });

  var me = this;
  function clear() {
    me._source = null;
    me._deleteOnPaste = null;
  }

});