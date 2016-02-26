app.controller('FileDiffController', function($scope, $timeout) {


  $timeout(function() {
    $scope.options = {
      lineWrapping : true,
      lineNumbers: true,
      theme: 'merbivore-soft',
      orig: 'orig',
      origLeft: 'origLeft',
      origRight: 'origRight',
      mode: 'xml'
    };

    $scope.data = 'test test';
  });



});