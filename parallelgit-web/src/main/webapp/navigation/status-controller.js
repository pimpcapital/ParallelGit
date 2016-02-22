app.controller('StatusController', function($rootScope, $scope, $q, $cookies, FileSystem, Connection) {

  $scope.branches = null;
  $scope.status = null;

  function fetchBranches() {
    var deferred = $q.defer();
    Connection.send('list-branches').then(function(branches) {
      $scope.branches = branches;
      deferred.resolve(branches);
    });
    return deferred.promise;
  }

  function fetchStatus() {
    var deferred = $q.defer();
    Connection.send('get-status').then(function(status) {
      $scope.status = status;
      deferred.resolve(status);
    });
    return deferred.promise;
  }

  function checkout(branch) {
    var deferred = $q.defer();
    Connection.send('checkout', {branch: branch}).then(function(status) {
      $scope.status = status;
      deferred.resolve(status);
      $rootScope.$broadcast('branch-checked-out', status);
    });
    return deferred.promise;
  }

  $scope.checkout = function(branch) {
    checkout(branch);
  };

  $scope.$on('ready', function() {
    fetchBranches();
    fetchStatus();
    FileSystem.reload();
  });
  $scope.$on('lockdown', function() {
    $scope.branches = null;
    $scope.head = null;
  });

});