app.service('ClipboardService', function($rootScope) {

  var buffer = null;
  var deleteOnPaste = null;

  this.cut = function(file) {
    buffer = file;
    deleteOnPaste = true;
  };

  this.copy = function(file) {
    buffer = file;
    deleteOnPaste = false;
  };

  this.paste = function(dir) {

  };

  $rootScope.$on('reloaded-filesystem', function() {
    buffer = null;
    deleteOnPaste = null
  });

});