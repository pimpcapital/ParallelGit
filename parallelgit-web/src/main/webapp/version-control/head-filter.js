app.filter('head', function($filter) {
  return function(head) {
    if(head == null)
      return '';
    var ref = head.getRef();
    if(ref!= null)
      return $filter('ref')(ref);
    return ref;
  }
});