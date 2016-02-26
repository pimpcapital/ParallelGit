app.controller('FileDiffController', function($scope, $timeout) {


  $timeout(function() {
    $scope.options = {
      lineWrapping : true,
      lineNumbers: true,
      theme: 'merbivore-soft',
      mode: 'xml'
    };

    $scope.data = 'test test';
  });



});