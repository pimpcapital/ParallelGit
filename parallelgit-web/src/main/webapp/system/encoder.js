app.service('Encoder', function() {
  this.encode = function(message) {
    return JSON.stringify(message);
  };
  this.decode = function(json) {
    return JSON.parse(json);
  };
});