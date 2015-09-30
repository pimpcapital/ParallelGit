app = angular.module('pg-site', ['hljs', 'ngRoute', 'ui.bootstrap']);

app.config(function (hljsServiceProvider) {
  hljsServiceProvider.setOptions({
    tabReplace: '  '
  });
});