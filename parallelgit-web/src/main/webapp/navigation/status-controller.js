app.controller('StatusController', function($rootScope, $scope, $filter, Status, Dialog, Notification, Connection) {

  $scope.branches = null;
  $scope.head = null;

  $scope.checkout = function(branch) {
    Status.checkout(branch);
  };

  $scope.refreshBranches = function(open) {
    if(open)
      Status.fetchBranches();
  };

  $scope.createBranch = function() {
    Dialog.prompt('New branch', {name: {label: 'Enter a new branch name', value: ''}}).then(function(fields) {
      var name = fields.name.value;
      var start = Status.head.commit.hash;
      Connection.send('create-branch', {name: name, start: start}).then(function(branch) {
        Notification.info('Branch "' + name + '" created');
        $rootScope.$broadcast('branch-created', branch);
      });
    });
  };

  $scope.deleteBranch = function() {
    Dialog.select('Delete branch', [
      {displayName: 'Name', name: 'ref', cellFilter: 'ref:true'},
      {displayName: 'Head Commit', name: 'commit.message'},
      {displayName: 'Last Update', field: 'commit.committer.timestamp', cellFilter: 'date'},
      {displayName: 'Committer', field: 'commit.committer.name'}
    ], $scope.branches).then(function(branch) {
      var name = $filter('ref')(branch.getRef(), true);
      Connection.send('delete-branch', {name: name}).then(function() {
        Notification.info('Branch "' + name + '" deleted');
        $rootScope.$broadcast('branch-created', branch);
      });
    });
  };

  $scope.$on('branches-refreshed', function(event, branches) {
    $scope.branches = branches;
  });
  $scope.$on('head-refreshed', function(event, head) {
    $scope.head = head;
  });


});