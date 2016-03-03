app.filter('filterBy', function($filter) {
  return function() {
    var filterName = [].splice.call(arguments, 1, 1)[0];
    return filterName != null ? $filter(filterName).apply(null, arguments) : arguments[0];
  };
});