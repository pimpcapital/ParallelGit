app.filter('ref', function() {
  return function(ref, short) {
    if(ref == null)
      return '';
    if(ref.indexOf('refs/heads/') == 0)
      return (short ? '' : 'Branch: ') + ref.substring('refs/heads/'.length);
    return ref;
  }
});