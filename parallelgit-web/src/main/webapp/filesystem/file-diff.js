app.service('FileDiff', function($uibModal, Connection) {

  var me = this;

  me.diff = function(title, message) {
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