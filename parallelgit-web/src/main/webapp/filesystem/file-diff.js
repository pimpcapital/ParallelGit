app.service('FileDiff', function($uibModal, Connection, DiffView) {

  var me = this;

  me.diff = function(srcRev, srcFile, destRev, destFile) {

    return $uibModal.open({
      templateUrl: 'filesystem/file-diff-modal.html',
      size: 'xlg',
      animation: false,
      resolve: {
        title: function() {
          return title;
        },
        message: function() {
          return message;
        }
      },
      controller : 'FileDiffController'
    }).result;
  };

});