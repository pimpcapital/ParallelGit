app.filter('filename', function() {
  return function(path) {
    if(path == null)
      return '';
    return path.substring(path.lastIndexOf('/') + 1);
  }
});