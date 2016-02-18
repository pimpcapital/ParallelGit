app.service('DialogService', function($uibModal) {
  this.prompt = function(text, fields) {
    $uibModal.open({
      templateUrl: 'system/prompt-dialog-modal.html',
      size: 'sm',
      resolve: {
        text: function() {
          return text;
        },
        fields: function() {
          return fields;
        }
      },
      controller : 'PromptDialogController'
    });
  }
});