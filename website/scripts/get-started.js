app.controller('GetStartedCtrl', function($scope) {

  $scope.modules = [
    {
      name: 'Stable',
      version: '1.0.0',
      artifacts: [
        {
          name: 'ParallelGit Utils',
          groupId: 'com.beijunyi',
          artifactId: 'parallelgit-utils',
          version: '1.0.0'
        }, {
          name: 'ParallelGit FileSystem',
          groupId: 'com.beijunyi',
          artifactId: 'parallelgit-filesystem',
          version: '1.0.0'
        }, {
          name: 'ParallelGit Commands',
          groupId: 'com.beijunyi',
          artifactId: 'parallelgit-commands',
          version: '1.0.0'
        }
      ]
    }
  ];

});