app.directive('sourceLoader', function() {
  return {
    restrict: 'E',
    scope: {
      module: '@',
      example: '@',
      method: '@'
    },
    template: '<div hljs language="java" source="source"></div>',
    controller: function($scope, CodeService) {
      $scope.source = 'Loading...';
      CodeService.loadSourceCode($scope.module, $scope.example, $scope.method).then(function(method) {
        $scope.source = method.raw;
      });
    }
  }
});