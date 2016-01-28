app.service('DecodeService', function() {
  this.decode = function(json) {
    return JSON.parse(json);
  }
});