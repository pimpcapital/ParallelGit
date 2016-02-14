app.filter('head', function() {
  return function(status) {
    if(status == null)
      return '';
    if(status.head.type == 'BRANCH')
      return 'Branch: ' + status.head.name;
    return status.head.name;
  }
});