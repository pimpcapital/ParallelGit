app.controller('TabDataController', function($rootScope, $scope) {

  $scope.file = null;

  $scope.inject = function(file) {
    $scope.file = file;
    file.acquireData();
  };

  $scope.$watch('file.data', function(newValue, oldValue) {
    if(oldValue != null && newValue != oldValue)
      $scope.file.scheduleSave();
  });

  $scope.$on('$destroy', function() {
    $scope.file.releaseData();
  });

});