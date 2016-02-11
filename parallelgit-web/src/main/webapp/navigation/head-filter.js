app.filter('head', function() {
  return function(status) {
    if(status == null)
      return '';
    if(status.attached)
      return 'Branch: ' + status.head;
    return status.head;
  }
});