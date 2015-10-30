app = angular.module('pg-site', ['hljs', 'ngRoute', 'ui.bootstrap']);

app.run(function($anchorScroll) {
  $anchorScroll.yOffset = 200;
});