app.controller('GetStartedCtrl', function($scope) {

  $scope.modules = [
    {
      name: 'Stable',
      version: '0.9.3',
      artifacts: [
        {
          name: 'ParallelGit Utils',
          groupId: 'com.beijunyi',
          artifactId: 'parallelgit-utils',
          version: '0.9.3'
        }, {
          name: 'ParallelGit FileSystem',
          groupId: 'com.beijunyi',
          artifactId: 'parallelgit-filesystem',
          version: '0.9.3'
        }, {
          name: 'ParallelGit Runtime',
          groupId: 'com.beijunyi',
          artifactId: 'parallelgit-commands',
          version: '0.9.3'
        }
      ]
    }
  ];

});