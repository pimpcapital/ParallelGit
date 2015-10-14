app.config(function($routeProvider) {
  $routeProvider
    .when('/', {
      templateUrl: 'partials/home.html'
    })
    .when('/news', {
      templateUrl: 'partials/news.html',
      name: 'News',
      icon: 'fa-newspaper-o'
    })
    .when('/get-started', {
      templateUrl: 'partials/get-started.html',
      controller: 'GetStartedCtrl',
      name: 'Get started',
      icon: 'fa-caret-square-o-right'
    })
    .when('/examples', {
      templateUrl: 'partials/examples.html',
      controller: 'ExamplesCtrl',
      name: 'Examples',
      icon: 'fa-cubes'
    })
    .when('/source', {
      templateUrl: 'partials/source.html',
      name: 'Source',
      icon: 'fa-code'
    })
    .when('/community', {
      templateUrl: 'partials/community.html',
      name: 'Community',
      icon: 'fa-comments'
    })
    .otherwise({
      redirectTo: '/'
    });
});

app.controller('NavigationCtrl', function($scope, $route, $location) {
  $scope.pages = [];
  angular.forEach($route.routes, function(route, path) {
    if(route.name != null) {
      $scope.pages.push(angular.extend({
        active: function() {
          return path == $location.path();
        }
      }, route))
    }
  });


});