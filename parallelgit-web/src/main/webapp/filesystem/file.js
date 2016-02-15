app.factory('File', function() {

  function File(parent, attributes) {
    this.name = attributes.name;
    this.path = (parent != null ? parent : '') + '/' + attributes.name;
    this.hash = attributes.hash;
    this.type = attributes.type;
    this.state = attributes.state;
  }

  File.prototype.getPath = function() {
    return this.path;
  };

  File.prototype.isRoot = function() {
    return '/' == this.getPath();
  };

  File.prototype.getParent = function() {
    if(this.isRoot())
      return null;
    var path = this.getPath();
    var parentEnd = path.lastIndexOf('/');
    return path.substring(0., parentEnd);
  };

  File.prototype.isDirectory = function() {
    return this.type == 'DIRECTORY';
  };

  File.prototype.updateAttributes = function(attributes) {
    var changed = this.hash != attributes.hash || this.type != attributes.type;
    this.hash = attributes.hash;
    this.type = attributes.type;
    this.state = attributes.state;
    return changed;
  };

  return File;

});