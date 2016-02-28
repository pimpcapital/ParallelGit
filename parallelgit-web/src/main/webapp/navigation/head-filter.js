app.filter('head', function() {
  return function(head) {
    if(head == null)
      return '';
    if(head.branch != null)
      return 'Branch: ' + head.branch;
    return head.commit;
  }
});