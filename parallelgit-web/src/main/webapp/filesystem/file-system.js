app.service('FileSystem', function($q, $rootScope, File, Connection) {

  this._root = new File(null, {name: ''});
  var me = this;

  me.getRoot = function() {
    return me._root;
  };

  me.reload = function() {
    me._root.loadAttributes().then(function() {
      me._root.loadChildren(true).then(function() {
        $rootScope.$broadcast('filesystem-reloaded');
      })
    });
  };

  me.findFile = function(path, current) {
    var deferred = $q.defer();
    if(!angular.isArray(path))
      path = me._splitPath(path);
    if(current == null)
      current = me._root;
    if(path.length == 0)
      deferred.resolve(current);
    else {
      current.getChild(path.shift()).then(function(child) {
        if(child != null) {
          me.findFile(path, child).then(function(file) {
            deferred.resolve(file);
          });
        } else {
          deferred.resolve(null);
        }
      });
    }
    return deferred.promise;
  };

  me.createFile = function(parent, name) {
    if(!parent.isDirectory())
      parent = parent.getParent();
    Connection.send('create-file', {directory: parent.getPath(), name: name}).then(function(attributes) {
      parent.addChild(attributes).then(function(file) {
        $rootScope.$broadcast('directory-created', file);
      });
    });
  };

  me.createDirectory = function(parent, name) {
    if(!parent.isDirectory())
      parent = parent.getParent();
    Connection.send('create-directory', {directory: parent.getPath(), name: name}).then(function(attributes) {
      parent.addChild(parent, attributes).then(function(dir) {
        $rootScope.$broadcast('directory-created', dir);
      });
    });
  };

  me.deleteFile = function(file) {
    var parent = file.getParent();
    Connection.send('delete-file', {path: file.path}).then(function() {
      parent.removeChild(file).then(function() {
        $rootScope.$broadcast('file-deleted', file)
      });
    });
  };

  me.copyFile = function(source, dir, name) {
    Connection.send('copy-file', {source: source.getPath(), directory: dir.getPath(), name: name}).then(function(attributes) {
      dir.addChild(attributes).then(function(file) {
        $rootScope.$broadcast('file-copied', [source, file])
      });
    });
  };

  me.moveFile = function(source, dir, name) {
    var parent = source.getParent();
    Connection.send('move-file', {source: source.getPath(), directory: dir.getPath(), name: name}).then(function(attributes) {
      parent.removeChild(source).then(function() {
        dir.addChild(attributes).then(function(file) {
          $rootScope.$broadcast('file-moved', [source, file])
        });
      });
    });
  };

  me._splitPath = function(path) {
    var ret = [];
    var raw = path.split('/');
    for(var i = 0; i < raw.length; i++) {
      var name = raw[i];
      if(name.length > 0)
        ret.push(name);
    }
    return ret;
  };


  $rootScope.$on('head-refreshed', function() {
    me.reload();
  });

});