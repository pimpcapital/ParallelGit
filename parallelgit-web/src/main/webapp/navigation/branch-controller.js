app.controller('BranchController', function($scope, $cookies, WorkspaceService) {

  $scope.branches = null;
  $scope.head = null;

  function requestHead() {
    WorkspaceService.request('head');
  }

  function requestBranches() {
    WorkspaceService.request('branches');
  }

  function checkout(branch) {
    WorkspaceService.request('checkout', null, branch);
  }

  function checkoutDefaultBranch() {
    var head = $cookies.get('head');
    if($scope.branches.indexOf(head) < 0) {
      if($scope.branches.indexOf('master') < 0 && $scope.branches.length > 0) {
        head = $scope.branches[0];
      } else
      head = 'master';
    }
    checkout(head);
  }

  $scope.checkout = function(branch) {
    checkout(branch);
  };

  $scope.$on('ready', function() {
    requestBranches();
  });
  $scope.$on('disconnect', function() {
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