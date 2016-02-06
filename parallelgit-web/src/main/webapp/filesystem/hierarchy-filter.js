app.filter('hierarchy', function() {
  return function(location) {
    var dirs = location.split('/');
    var depth = 0;
    var ret = '';
    while(dirs.length > 0) {
      var dir = dirs.shift();
      if(dir != '') {
        if(ret != '') {
          ret += '\n ';
          for(var i = 1; i < depth; i++)
            ret += '   ';
          ret += '└─';
        }
        ret += dir;
        depth++;
      }
    }
    return ret;
  }
});