app.filter('head', function($filter) {
  return function(head) {
    if(head == null)
      return '';
    if(head.branch != null)
      return 'Branch: ' + head.branch;
    return $filter('hashAbbreviation')(head.commit.hash);
  }
});