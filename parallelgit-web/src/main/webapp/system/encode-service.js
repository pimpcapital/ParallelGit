app.service('EncodeService', function() {
  this.encode = function(message) {
    return JSON.stringify(message);
  }
});