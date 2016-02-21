app.controller('StatusController', function($rootScope, $scope, $q, $cookies, FileSystem, ConnectionService) {

  $scope.branches = null;
  $scope.status = null;

  function fetchBranches() {
    var deferred = $q.defer();
    ConnectionService.send('list-branches').then(function(branches) {
      $scope.branches = branches;
      deferred.resolve(branches);
    });
    return deferred.promise;
  }

  function fetchStatus() {
    var deferred = $q.defer();
    ConnectionService.send('get-status').then(function(status) {
      $scope.status = status;
      deferred.resolve(status);
    });
    return deferred.promise;
  }

  function checkout(branch) {
    var deferred = $q.defer();
    ConnectionService.send('checkout', {branch: branch}).then(function(status) {
      $scope.status = status;
      deferred.resolve(status);
    });
    return deferred.promise;
  }

  function checkoutDefaultBranch(branches) {
    var head = $cookies.get('head');
    if(branches.indexOf(head) < 0) {
      if(branches.indexOf('master') < 0 && branches.length > 0) {
        head = branches[0];
      } else
      head = 'master';
    }
    return checkout(head);
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