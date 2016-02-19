app.factory('File', function() {

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

  File.prototype.removeChild = function(file) {
    this.children.splice(this.children.indexOf(file), 1);
  };

  File.prototype.getData = function() {
    return this.data;
  };

  File.prototype.isLoaded = function() {
    return this.data != null;
  };

  File.prototype.setData = function(data) {
    this.data = data;
  };

  File.prototype.isActive = function() {
    return this.active;
  };

  File.prototype.setActive = function(active) {
    this.active = active;
  };

  File.prototype.unload = function() {
    delete this.data;
    delete this.active;
  };

  return File;

});