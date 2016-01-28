app.controller('BranchController', function($scope, $cookies, WorkspaceService) {

  $scope.$on('branches', function(event, msg) {
    $scope.branches = msg.data;
    if($scope.head == null)
      checkoutDefaultBranch();
  });

  $scope.$on('head', function(event, msg) {
    $scope.head = msg.data;
  });

  $scope.$on('ready', function() {
    requestBranches();
  });

  function requestBranches() {
    $scope.branches = null;
    WorkspaceService.request('branches');
  }

  function checkout(branch) {
    WorkspaceService.update('checkout', null, branch);
  }

  function checkoutDefaultBranch() {
    var head = $cookies.get('head');
    if($scope.branches.indexOf(head) < 0) {
      if($scope.branches.length > 0)
        head = $scope.branches[0];
      else
        head = 'master';
    }
    checkout(head);
  }

  $scope.checkout = function(branch) {
    checkout(branch);
  }

});