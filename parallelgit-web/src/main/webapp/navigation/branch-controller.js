app.controller('BranchController', function($q, $scope, $cookies, ConnectionService, WorkspaceService) {

  $scope.branches = null;
  $scope.status = null;

  function requestHead() {
    WorkspaceService.request('head');
  }

  function setupBranches() {
    var deferred = $q.defer();
    ConnectionService.send('list-branches').then(function(branches) {
      $scope.branches = branches;
      deferred.resolve(branches);
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
    setupBranches()
      .then(checkoutDefaultBranch);
  });
  $scope.$on('lockdown', function() {
    $scope.branches = null;
    $scope.head = null;
  });

  $scope.$on('branches', function(event, msg) {
    $scope.branches = msg.data;
    if($scope.head == null)
      checkoutDefaultBranch();
  });

  $scope.$on('head', function(event, msg) {
    $scope.head = msg.data;
  });

  $scope.$on('checkout', function() {
    requestHead();
  });

});