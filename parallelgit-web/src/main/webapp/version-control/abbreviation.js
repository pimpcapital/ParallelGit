app.filter('abbreviation', function() {
  return function(hash) {
    return hash.substring(0, 7);
  }
});