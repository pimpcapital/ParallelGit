app.controller('FileDiffController', function($scope, $timeout) {


  $timeout(function() {
    $scope.options = {
      lineWrapping : true,
      lineNumbers: true,
      orig: 'orig',
      origLeft: 'A\nA\nA\n\nb\nB\nb\n',
      origRight: 'B\nb\nB\n\nC\nC\nC\n',
      mode: 'xml'
    };

    $scope.data = 'b\nb\nb\n';
  });



});