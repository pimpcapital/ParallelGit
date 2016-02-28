app.controller('StatusController', function($scope, Status) {

  $scope.branches = null;
  $scope.head = null;

  $scope.checkout = function(branch) {
    Status.checkout(branch);
  };

  $scope.$on('branches-refreshed', function(event, branches) {
    $scope.branches = branches;
  });
  $scope.$on('head-refreshed', function(event, head) {
    $scope.head = head;
  });


});