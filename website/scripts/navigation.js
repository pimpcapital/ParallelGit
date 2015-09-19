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
    .when('/features', {
      templateUrl: 'partials/features.html',
      name: 'Features',
      icon: 'fa-star-half-o'
    })
    .when('/download', {
      templateUrl: 'partials/download.html',
      name: 'Download',
      icon: 'fa-cloud-download'
    })
    .when('/source', {
      templateUrl: 'partials/source.html',
      name: 'Source',
      icon: 'fa-code'
    })
    .when('/get-started', {
      templateUrl: 'partials/get-started.html',
      name: 'Get started',
      icon: 'fa-cubes'
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
  var current = $location.path();
  $scope.pages = [];
  angular.forEach($route.routes, function(route, path) {
    if(route.name != null) {
      $scope.pages.push(angular.extend({
        active: path == current
      }, route))
    }
  });
});