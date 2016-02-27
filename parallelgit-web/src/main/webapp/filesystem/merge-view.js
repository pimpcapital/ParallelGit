app.service('MergeView', function($uibModal) {

  this.showDiffs = function(src, dest) {
    return $uibModal.open({
      templateUrl: 'filesystem/file-diffs-modal.html',
      size: 'xlg',
      backdrop: 'static',
      resolve: {
        src: function() {
          return src;
        },
        dest: function() {
          return dest;
        }
      },
      controller : 'FileDiffsController'
    }).result;
  };

  this.showConflicts = function(base, ours, theirs) {

  };

});