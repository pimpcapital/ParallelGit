app.config(function($routeProvider) {
  $routeProvider
    .when('/', {
      templateUrl: 'partials/home/main.html',
      controller: 'HomeCtrl'
    })
    .when('/news', {
      templateUrl: 'partials/news/main.html',
      name: 'News',
      icon: 'fa-newspaper-o'
    })
    .when('/get-started', {
      templateUrl: 'partials/get-started/main.html',
      controller: 'GetStartedCtrl',
      name: 'Get started',
      icon: 'fa-caret-square-o-right'
    })
    .when('/examples', {
      templateUrl: 'partials/examples/main.html',
      controller: 'ExamplesCtrl',
      name: 'Examples',
      icon: 'fa-cubes'
    })
    .when('/source', {
      templateUrl: 'partials/source/main.html',
      name: 'Source',
      icon: 'fa-code'
    })
    .when('/community', {
      templateUrl: 'partials/community/main.html',
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