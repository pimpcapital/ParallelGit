app.service('FileSystem', function($rootScope, File, Connection) {

  var root = new File(null, {name: ''});

  this.getRoot = function() {
    return root;
  };

  this.reload = function() {
    root.loadAttributes().then(function() {
      root.loadChildren(true).then(function() {
        $rootScope.$broadcast('filesystem-reloaded');
      })
    });
  };

  this.createFile = function(dir, name) {
    if(!dir.isDirectory())
      dir = dir.getParent();
    dir.loadChildren().then(function() {
      Connection.send('create-file', {directory: dir.getPath(), name: name}).then(function(attributes) {
        var file = addChild(dir, attributes);
        $rootScope.$broadcast('directory-created', file);
      });
    });
  };

  this.createDirectory = function(dir, name) {
    if(!dir.isDirectory())
      dir = dir.getParent();
    dir.loadChildren().then(function() {
      Connection.send('create-directory', {directory: dir.getPath(), name: name}).then(function(attributes) {
        var file = addChild(dir, attributes);
        $rootScope.$broadcast('directory-created', file);
      });
    });
  };

  this.deleteFile = function(file) {
    Connection.send('delete-file', {path: file.path}).then(function() {
      removeChild(file);
      $rootScope.$broadcast('file-deleted', file)
    });
  };

  this.copyFile = function(source, dir, name) {
    dir.loadChildren().then(function() {
      Connection.send('copy-file', {source: source.getPath(), directory: dir.getPath(), name: name}).then(function(attributes) {
        var file = addChild(dir, attributes);
        $rootScope.$broadcast('file-copied', [source, file])
      });
    });
  };

  this.moveFile = function(source, dir, name) {
    dir.loadChildren().then(function() {
      Connection.send('move-file', {source: source.getPath(), directory: dir.getPath(), name: name}).then(function(attributes) {
        removeChild(source);
        var file = addChild(dir, attributes);
        $rootScope.$broadcast('file-moved', [source, file])
      });
    });
  };

  var me = this;
  $rootScope.$on('file-modified', function(event, file) {
    file.loadAttributes();
    var parent = file.getParent();
    propagateChanges(parent);
  });

  $rootScope.$on('branch-checked-out', function() {
    me.reload();
  });

  function propagateChanges(file) {
    var current = file;
    while(current != null) {
      current.loadAttributes();
      current = current.getParent();
    }
  }

  function removeChild(file) {
    var parent = file.getParent();
    parent.removeChild(file);
    propagateChanges(parent);
  }

  function addChild(dir, attributes) {
    var file = dir.addChild(attributes);
    propagateChanges(dir);
    return file;
  }

});