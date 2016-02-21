app.service('FileSystem', function($rootScope, File, ConnectionService) {

  var root = new File(null, {name: ''});

  this.getRoot = function() {
    return root;
  };

  this.reload = function() {
    root.loadAttributes().then(function() {
      root.loadChildren().then(function() {
        $rootScope.$broadcast('filesystem-reloaded');
      })
    });
  };

  this.createFile = function(dir, name) {
    if(!dir.isDirectory())
      dir = dir.getParent();
    ConnectionService.send('create-file', {directory: dir.getPath(), name: name}).then(function(attributes) {
      var file = dir.addChild(attributes);
      propagateChanges(dir);
      $rootScope.$broadcast('file-created', file);
    });
  };

  this.deleteFile = function(file) {
    ConnectionService.send('delete-file', {path: file.path}).then(function() {
      var parent = file.getParent();
      parent.removeChild(file);
      propagateChanges(parent);
      $rootScope.$broadcast('file-deleted', file)
    });
  };

  $rootScope.$on('file-modified', function(event, file) {
    file.loadAttributes();
    var parent = file.getParent();
    propagateChanges(parent);
  });

  function propagateChanges(file) {
    var current = file;
    while(current != null) {
      current.loadAttributes();
      current = current.getParent();
    }
  }

});