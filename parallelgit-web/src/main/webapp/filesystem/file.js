app.factory('File', function($rootScope, $q, $timeout, Connection) {

  function File(parent, attributes) {
    this.getParent = function() {
      return parent;
    };
    this.path = resolvePath(parent, attributes.name);
    this.depth = parent != null ? parent.depth + 1 : 0;
    this.name = attributes.name;
    this.hash = attributes.hash;
    this.type = attributes.type;
    this.state = attributes.state;
  }

  File.prototype.getPath = function() {
    return this.path;
  };

  File.prototype.getDepth = function() {
    return this.depth;
  };

  File.prototype.getName = function() {
    return this.name;
  };

  File.prototype.getHash = function() {
    return this.hash;
  };

  File.prototype.unsetHash = function() {
    this.hash = null;
  };

  File.prototype.isDirectory = function() {
    return this.type == 'DIRECTORY';
  };

  File.prototype.loadAttributes = function() {
    var deferred = $q.defer();
    var file = this;
    Connection.send('get-file-attributes', {path: file.getPath()}).then(function(attributes) {
      file.hash = attributes.hash;
      file.type = attributes.type;
      file.state = attributes.state;
      var parent = file.getParent();
      if(parent != null)
        parent.loadAttributes();
      deferred.resolve(attributes);
    });
    return deferred.promise;
  };

  File.prototype.loadData = function() {
    var deferred = $q.defer();
    var file = this;
    Connection.send('read-file', {path: file.getPath()}).then(function(data) {
      file.data = data;
      deferred.resolve(data);
    });
    return deferred.promise;
  };

  File.prototype.saveData = function() {
    var file = this;
    file.cancelScheduledSave();
    Connection.send('write-file', {path: file.getPath(), data: file.data}).then(function() {
      file.loadAttributes();
      $rootScope.$broadcast('file-modified', file);
    });
  };

  File.prototype.cancelScheduledSave = function() {
    var file = this;
    $timeout.cancel(file.nextSave);
    file.nextSave = null;
  };

  File.prototype.scheduleSave = function() {
    var file = this;
    file.cancelScheduledSave();
    file.unsetHash();
    file.nextSave = $timeout(function() {
      file.saveData();
    }, 1000);
  };

  File.prototype.unloadData = function() {
    var file = this;
    if(file.nextSave != null)
      file.saveData();
    delete file.data;
  };

  File.prototype.acquireData = function() {
    var file = this;
    if(!file.access) {
      file.access = 1;
      file.loadData();
    } else
      file.access++;
  };

  File.prototype.releaseData = function() {
    var file = this;
    file.access--;
    if(file.access == 0) {
      file.unloadData();
      delete file.access;
    }
  };


  File.prototype.loadChildren = function(refresh) {
    var deferred = $q.defer();
    var dir = this;
    var children = dir.children;
    if(children == null || refresh) {
      children = dir.children = [];
      Connection.send('list-files', {path: this.path}).then(function(files) {
        angular.forEach(files, function(attributes) {
          var node = new File(dir, attributes);
          children.push(node);
        });
        deferred.resolve(children);
      });
    } else{
      deferred.resolve(children);
    }
    return deferred.promise;
  };

  File.prototype.getChild = function(name) {
    var deferred = $q.defer();
    var dir = this;
    dir.loadChildren(false).then(function(children) {
      var ret = null;
      for(var i = 0; i < children.length; i++) {
        var child = children[i];
        if(child.getName() == name) {
          ret = child;
          break;
        }
      }
      deferred.resolve(ret);
    });
    return deferred.promise;
  };

  File.prototype.addChild = function(attributes) {
    var deferred = $q.defer();
    var dir = this;
    dir.loadChildren(false).then(function(children) {
      var file = new File(dir, attributes);
      children.push(file);
      dir.loadAttributes();
      deferred.resolve(file);
    });
    return deferred.promise;
  };

  File.prototype.removeChild = function(file) {
    var deferred = $q.defer();
    var dir = this;
    dir.loadChildren(false).then(function(children) {
      children.splice(children.indexOf(file), 1);
      dir.loadAttributes();
      deferred.resolve(file);
    });
    return deferred.promise;
  };

  function resolvePath(parent, name) {
    if(parent != null) {
      var ret = parent.getPath();
      if(ret.charAt(ret.length - 1) != '/')
        ret += '/';
      return ret + name;
    }
    else
      return '/';
  }

  return File;

});