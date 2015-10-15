app.controller('ExamplesCtrl', function($scope, CodeService) {

  $scope.examples = [{
    name: 'Create Repository',
    examples: [{
      name: 'Create Bare Repository',
      module: 'utils',
      testClass: 'CreateRepositoryTest',
      method: 'createBareRepository'
    }]
  }];

  function makeUrl(module, testClass) {
    return 'https://raw.githubusercontent.com/beijunyi/ParallelGit/master/parallelgit-' + module + '/src/test/java/examples/' + testClass + '.java';
  }

  function prepareExamples() {
    angular.forEach($scope.examples, function(category) {
      angular.forEach(category.examples, function(example) {
        example.url = makeUrl(example.module, example.testClass);
      })
    });
  }

  prepareExamples();

});