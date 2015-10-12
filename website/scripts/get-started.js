app.controller('GetStartedCtrl', function($scope) {

  $scope.modules = [
    {
      name: 'Stable',
      version: '0.9.3',
      artifacts: [
        {
          name: 'ParallelGit Utils',
          groupId: 'com.beijunyi.parallelgit',
          artifactId: 'parallelgit-utils',
          version: '0.9.3'
        }, {
          name: 'ParallelGit FileSystem',
          groupId: 'com.beijunyi.parallelgit',
          artifactId: 'parallelgit-filesystem',
          version: '0.9.3'
        }, {
          name: 'ParallelGit Runtime',
          groupId: 'com.beijunyi.parallelgit',
          artifactId: 'parallelgit-runtime',
          version: '0.9.3'
        }
      ]
    }
  ];

});