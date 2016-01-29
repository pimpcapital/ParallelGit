app.filter('head', function() {
  return function(head) {
    if(head == null)
      return '';
    if(head.type == 'BRANCH')
      return 'Branch: ' + head.name;
    return head.name;
  }
});