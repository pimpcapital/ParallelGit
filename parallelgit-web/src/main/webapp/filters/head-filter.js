app.filter('head', function($filter) {
  return function(head) {
    if(head == null)
      return '';
    if(head.ref != null)
      return 'Branch: ' + head.branch;
    return $filter('abbreviation')(head.commit.hash);
  }
});