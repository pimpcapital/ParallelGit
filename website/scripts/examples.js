app.controller('ExamplesCtrl', function($scope, $location, $timeout, $anchorScroll) {

  $scope.modules = [
    {
      name: 'ParallelGit Utils',
      id: 'utils',
      categories: [{
        name: 'Create Repository',
        testClass: 'CreateRepositoryTest',
        examples: [{
          name: 'Create Bare Repository',
          method: 'createBareRepository'
        }, {
          name: 'Create Non-Bare Repository',
          method: 'createNonBareRepository'
        }]
      }, {
        name: 'Open Repository',
        testClass: 'OpenRepositoryTest',
        examples: [{
          name: 'Auto Detect and Open Bare Repository',
          method: 'autoDetectAndOpenBareRepository'
        }, {
          name: 'Auto Detect and Open Non-Bare Repository',
          method: 'autoDetectAndOpenNonBareRepository'
        }]
      }, {
        name: 'Change Default Settings',
        testClass: 'ChangeRepositorySettingsTest',
        examples: [{
          name: 'Change Default Committer',
          method: 'changeDefaultCommitter'
        }]
      }, {
        name: 'Create Branch',
        testClass: 'CreateBranchTest',
        examples: [{
          name: 'Create Branch from Another Branch',
          method: 'createBranchFromAnotherBranch'
        }, {
          name: 'Create Branch from Arbitrary Commit',
          method: 'createBranchFromArbitraryCommit'
        }]
      }, {
        name: 'Read File',
        testClass: 'ReadFileTest',
        examples: [{
          name: 'Read Bytes',
          method: 'readBytes'
        }, {
          name: 'Open InputStream',
          method: 'openInputStream'
        }]
      }, {
        name: 'Commit Changes',
        testClass: 'CommitChangesTest',
        examples: [{
          name: 'Commit Cached Changes',
          method: 'commitCachedChanges'
        }]
      }, {
        name: 'Cache Manipulation',
        testClass: 'CacheManipulationTest',
        examples: [{
          name: 'Insert File',
          method: 'insertFile'
        }, {
          name: 'Delete File',
          method: 'deleteFile'
        }, {
          name: 'Update File',
          method: 'updateFile'
        }]
      }, {
        name: 'Tag Commit',
        testClass: 'TagCommitTest',
        examples: [{
          name: 'Tag Branch Head Commit',
          method: 'tagBranchHeadCommit'
        }, {
          name: 'Tag Arbitrary Commit',
          method: 'tagArbitraryCommit'
        }]
      }]
    }, {
      name: 'ParallelGit FileSystem',
      id: 'filesystem',
      categories: [{
        name: 'Copy File',
        testClass: 'CopyFileTest',
        examples: [{
          name: 'Copy File within Same File System',
          method: 'copyFileWithinSameFileSystem'
        }, {
          name: 'Copy File to Different File System',
          method: 'copyFileToDifferentFileSystem'
        }, {
          name: 'Copy Directory within Same File System',
          method: 'copyDirectoryWithinSameFileSystem'
        }, {
          name: 'Copy Directory to Another File System',
          method: 'copyDirectoryToAnotherFileSystem'
        }]
      }]
    }
  ];

  $scope.gotoAnchor = function(name) {
    if($location.hash() != name)
      $location.hash(name);
    else
      $anchorScroll();
  };

  function makeUrl(module, testClass) {
    return 'https://raw.githubusercontent.com/beijunyi/ParallelGit/master/parallelgit-' + module + '/src/test/java/examples/' + testClass + '.java';
  }

  function prepareExamples() {
    angular.forEach($scope.modules, function(module) {
      angular.forEach(module.categories, function(category) {
        category.anchor = module.id + '-' + category.testClass;
        category.url = makeUrl(module.id, category.testClass);
      });
    });
  }

  prepareExamples();

});