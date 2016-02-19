app.factory('File', function(ConnectionService) {

  function File(parent, attributes) {
    this.name = attributes.name;
    this.parent = parent;
    this.path = (parent != null ? parent.getPath() : '') + '/' + attributes.name;
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

  function updateFileAttributes(file) {
    getFileAttributes(file.path).then(function(attributes) {
      file.updateAttributes(attributes);
    })
  }

  File.prototype.updateAttributes = function(attributes) {
    var changed = this.hash != attributes.hash || this.type != attributes.type;
    this.hash = attributes.hash;
    this.type = attributes.type;
    this.state = attributes.state;
    return changed;
  };

  File.prototype.loadChildren = function() {
    var dir = this;
    ConnectionService.send('list-files', {path: this.path}).then(function(files) {
      angular.forEach(files, function(file) {
        var node = new File(dir, file);
        dir.children.push(node);
      });
      sortChildren();
    })
  };

  File.prototype.removeChild = function(file) {
    this.children.splice(this.children.indexOf(file), 1);
  };

  function sortChildren() {
    this.children.sort(function(a, b) {
      if(a.isDirectory() && !b.isDirectory())
        return -1;
      if(!a.isDirectory() && b.isDirectory())
        return 1;
      return a.name - b.name;
    });
  }

  return File;

});