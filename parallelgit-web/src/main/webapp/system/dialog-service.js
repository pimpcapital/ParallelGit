app.service('DialogService', function($uibModal) {

  this.confirm = function(title, message) {
    return $uibModal.open({
      templateUrl: 'system/confirm-dialog-modal.html',
      size: 'sm',
      resolve: {
        title: function() {
          return title;
        },
        message: function() {
          return message;
        }
      },
      controller : 'ConfirmDialogController'
    });
  };

  this.prompt = function(title, fields) {
    return $uibModal.open({
      templateUrl: 'system/prompt-dialog-modal.html',
      size: 'sm',
      resolve: {
        title: function() {
          return title;
        },
        fields: function() {
          return fields;
        }
      },
      controller : 'PromptDialogController'
    });
  }
});