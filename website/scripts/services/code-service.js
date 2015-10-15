app.service('CodeService', function($http, $q) {

  var METHOD_PATTERN = /public void (\w+)\(\)/;
  var cache = {};
  var methods = {};

  function loadMethod(url, method) {
    var deferred = $q.defer();
    if(methods[url] != null && methods[url][method] != null) {
      deferred.resolve(methods[url][method]);
    } else {
      loadCode(url).then(function(lines) {
        var methodList = parseLines(lines);
        var methodsForUrl = methods[url];
        if(methodsForUrl == null) {
          methodsForUrl = {};
          methods[url] = methodsForUrl;
        }
        angular.forEach(methodList, function(method) {
          method.raw = method.lines.join('\n');
          methodsForUrl[method.name] = method;
        });
        deferred.resolve(methods[url][method]);
      });
    }
    return deferred.promise;
  }

  function loadCode(url) {
    var deferred = $q.defer();
    if(cache[url] == null) {
      $http.get(url)
        .success(function(source) {
          cache[url] = source.split('\n');
          deferred.resolve(cache[url]);
        });
    } else
      deferred.resolve(cache[url]);
    return deferred.promise;
  }

  function parseLines(lines) {
    var ret = [];
    var method, openBrackets;
    angular.forEach(lines, function(line, num) {
      if(method == null) {
        var trimmed = line.trim();
        if(trimmed == '@Before' || trimmed == '@Test') {
          method = {
            start: num,
            lines: [line]
          };
          openBrackets = 0;
        }
      } else if(method != null) {
        method.lines.push(line);
        if(method.name == null) {
          method.name = METHOD_PATTERN.exec(line)[1];
        }
        for(var i = 0; i < line.length; i++) {
          var c = line.charAt(i);
          if(c == '{') {
            openBrackets++;
          } else if(c == '}') {
            openBrackets--;
          }
        }
        if(openBrackets == 0) {
          method.end = num;
          ret.push(method);
          method = null;
        }
      }
    });
    return ret;
  }

  return {
    loadSourceCode: function(url, method) {
      return loadMethod(url, method);
    }
  }
});