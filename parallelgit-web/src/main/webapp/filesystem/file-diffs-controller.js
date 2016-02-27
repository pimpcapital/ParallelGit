app.controller('FileDiffsController', function($scope, $timeout, src, dest) {

  $timeout(function() {
    $scope.options = {
      lineWrapping : true,
      lineNumbers: true,
      origLeft: 'left',
      origRight: 'right'
    };

  });



});