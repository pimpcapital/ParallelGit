app.controller('ExamplesCtrl', function($scope, $location, $timeout, $anchorScroll) {

  $scope.examples = [{
    name: 'Create Repository',
    module: 'utils',
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
    module: 'utils',
    testClass: 'OpenRepositoryTest',
    examples: [{
      name: 'Auto Detect and Open Repository',
      method: 'autoDetectAndOpenRepository'
    }]
  }, {
    name: 'Change Settings',
    module: 'utils',
    testClass: 'ChangeRepositorySettingsTest',
    examples: [{
      name: 'Change Default Committer',
      method: 'changeDefaultCommitter'
    }]
  }, {
    name: 'Create Branch',
    module: 'utils',
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
    module: 'utils',
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
    module: 'utils',
    testClass: 'CommitChangesTest',
    examples: [{
      name: 'Commit Cached Changes',
      method: 'commitCachedChanges'
    }]
  }, {
    name: 'Cache Manipulation',
    module: 'utils',
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
    module: 'utils',
    testClass: 'TagCommitTest',
    examples: [{
      name: 'Tag Branch Head Commit',
      method: 'tagBranchHeadCommit'
    }, {
      name: 'Tag Arbitrary Commit',
      method: 'tagArbitraryCommit'
    }]
  }];

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
    angular.forEach($scope.examples, function(category) {
      category.anchor = category.name.replace(' ', '');
      category.url = makeUrl(category.module, category.testClass);
    });
  }

  prepareExamples();

});