app.controller('HomeCtrl', function($scope, $uibModal) {

  $scope.details = function (item) {
    $uibModal.open({
      templateUrl: 'partials/home/details-' + item + '.html',
      controller: function($scope, $modalInstance) {
        $scope.ok = function() {
          $modalInstance.dismiss();
        }
      }
    });
  };


});