app.controller('StatusController', function($rootScope, $scope, Status, Dialog, Notification, Connection) {

  $scope.branches = null;
  $scope.head = null;

  $scope.checkout = function(branch) {
    Status.checkout(branch);
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
      {title: 'Name', field: 'name', f: 'hashAbbreviation'},
      {title: 'Message', field: 'message'},
      {title: 'Time', field: 'committer', f: 'personDate'},
      {title: 'Committer', field: 'committer', f: 'personName'}
    ])
  };

  $scope.$on('branches-refreshed', function(event, branches) {
    $scope.branches = branches;
  });
  $scope.$on('head-refreshed', function(event, head) {
    $scope.head = head;
  });


});