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

  this.createFile = function(parent, name) {
    if(!parent.isDirectory())
      parent = parent.getParent();
    Connection.send('create-file', {directory: parent.getPath(), name: name}).then(function(attributes) {
      parent.addChild(attributes).then(function(file) {
        $rootScope.$broadcast('directory-created', file);
      });
    });
  };

  this.createDirectory = function(parent, name) {
    if(!parent.isDirectory())
      parent = parent.getParent();
    Connection.send('create-directory', {directory: parent.getPath(), name: name}).then(function(attributes) {
      parent.addChild(parent, attributes).then(function(dir) {
        $rootScope.$broadcast('directory-created', dir);
      });
    });
  };

  this.deleteFile = function(file) {
    var parent = file.getParent();
    Connection.send('delete-file', {path: file.path}).then(function() {
      parent.removeChild(file).then(function() {
        $rootScope.$broadcast('file-deleted', file)
      });
    });
  };

  this.copyFile = function(source, dir, name) {
    Connection.send('copy-file', {source: source.getPath(), directory: dir.getPath(), name: name}).then(function(attributes) {
      dir.addChild(attributes).then(function(file) {
        $rootScope.$broadcast('file-copied', [source, file])
      });
    });
  };

  this.moveFile = function(source, dir, name) {
    var parent = source.getParent();
    Connection.send('move-file', {source: source.getPath(), directory: dir.getPath(), name: name}).then(function(attributes) {
      parent.removeChild(source).then(function() {
        dir.addChild(attributes).then(function(file) {
          $rootScope.$broadcast('file-moved', [source, file])
        });
      });
    });
  };

  var me = this;

  $rootScope.$on('branch-checked-out', function() {
    me.reload();
  });

});