app.factory('File', function($q, ConnectionService) {

  function File(parent, attributes) {
    this.name = attributes.name;
    this.parent = parent;
    this.path = resolvePath(parent, attributes.name);
    this.hash = attributes.hash;
    this.type = attributes.type;
    this.state = attributes.state;
  }

  File.prototype.getName = function() {
    return this.name;
  };

  File.prototype.getPath = function() {
    return this.path;
  };

  File.prototype.getParent = function() {
    return this.parent;
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
    ConnectionService.send('get-file-attributes', {path: file.getPath()}).then(function(attributes) {
      file.hash = attributes.hash;
      file.type = attributes.type;
      file.state = attributes.state;
      deferred.resolve(attributes);
    });
    return deferred.promise;
  };

  File.prototype.loadChildren = function() {
    var deferred = $q.defer();
    var dir = this;
    ConnectionService.send('list-files', {path: this.path}).then(function(files) {
      var children = [];
      angular.forEach(files, function(attributes) {
        var node = new File(dir, attributes);
        children.push(node);
      });
      sortFiles(children);
      dir.children = children;
      deferred.resolve(children);
    });
    return deferred.promise;
  };

  File.prototype.removeChild = function(file) {
    this.children.splice(this.children.indexOf(file), 1);
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

  function sortFiles(files) {
    files.sort(function(a, b) {
      if(a.isDirectory() && !b.isDirectory())
        return -1;
      if(!a.isDirectory() && b.isDirectory())
        return 1;
      return a.getName() - b.getName();
    });
  }

  return File;

});